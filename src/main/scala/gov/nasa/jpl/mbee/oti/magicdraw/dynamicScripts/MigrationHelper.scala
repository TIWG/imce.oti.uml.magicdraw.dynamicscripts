/*
 *
 * License Terms
 *
 * Copyright (c) 2014-2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * *   Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *   Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * *   Neither the name of Caltech nor its operating division, the Jet
 *    Propulsion Laboratory, nor the names of its contributors may be
 *    used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent
import java.io.File

import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

import scala.collection.JavaConversions.mapAsJavaMap
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.ApplicationEnvironment
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.core.modules.ModulesServiceInternal
import com.nomagic.magicdraw.core.project.ProjectsManager
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.uml.ConvertElementInfo
import com.nomagic.magicdraw.uml.Refactoring
import com.nomagic.magicdraw.uml.actions.SelectInContainmentTreeRunnable
import com.nomagic.task.ProgressStatus
import com.nomagic.task.RunnableWithProgress
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element

import org.eclipse.emf.common.util.URI
import org.omg.oti.magicdraw.MagicDrawUMLUtil
import org.omg.oti.changeMigration.Metamodel
import org.omg.oti.changeMigration.Old2NewIDMapping

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes.MainToolbarMenuAction
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object MigrationHelper {

  def customMigration(
    project: Project, ev: ActionEvent,
    script: MainToolbarMenuAction ): Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( project )

    val mdInstallDir = new File( ApplicationEnvironment.getInstallRoot )
    require( mdInstallDir.exists && mdInstallDir.isDirectory )
    val otiDir = new File( mdInstallDir, "dynamicScripts/org.omg.oti" )
    require( otiDir.exists && otiDir.isDirectory )
    val migrationMM = Metamodel( otiDir )

    chooseMigrationFile match {
      case Some( migrationFile ) =>
        require( migrationFile.exists && migrationFile.canRead )
        val migrationURI = URI.createFileURI( migrationFile.getAbsolutePath )

        migrationMM.loadOld2NewIDMappingResource( migrationURI ) match {
          case Failure( t ) =>
            Failure( t )
          case Success( old2newIDmigration ) =>

            applyMigration( project, old2newIDmigration, convertOnlyIncomingReferences=true ) match {
              case Failure( t ) => Failure( t )
              case Success( _ ) => Success( None )
            }
        }
      case None =>
        Success( None )
    }
  }

  def chooseMigrationFile: Option[File] = {
    val dir = ProjectsManager.getRecentFilePath match {
      case _@ ( null | "" ) =>
        System.getProperty( "user.dir" ) match {
          case _@ ( null | "" ) => "."
          case d                => d
        }
      case d => d
    }

    val ff = new FileFilter() {

      def getDescription: String = "*.migration.xmi"

      def accept( f: File ): Boolean =
        f.isDirectory() ||
          ( f.isFile() && f.getName.endsWith( ".migration.xmi" ) )

    }

    val mdInstallDir = new File( ApplicationEnvironment.getInstallRoot )
    val fc = new JFileChooser( mdInstallDir ) {

      override def getFileSelectionMode: Int = JFileChooser.FILES_ONLY

      override def getDialogTitle = "Select a *.migration.xmi file"
    }

    fc.setFileFilter( ff )
    fc.setFileHidingEnabled( true )
    fc.setAcceptAllFileFilterUsed( false )

    fc.showOpenDialog( Application.getInstance().getMainFrame ) match {
      case JFileChooser.APPROVE_OPTION =>
        val migrationFile = fc.getSelectedFile
        Some( migrationFile )
      case _ =>
        None
    }
  }

  def applyMigration( project: Project, old2newIDmigration: Old2NewIDMapping, convertOnlyIncomingReferences: Boolean = false ): Try[Unit] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog()

    val entries = old2newIDmigration.getEntries
    guiLog.log( s" Applying ${entries.size} old2new ID migration entries" )

    var error: Throwable = null

    val runnable = new RunnableWithProgress() {
      def run( progressStatus: ProgressStatus ): Unit = {

        val proxyManager = project.getProxyManager

        val migrationPairs = entries flatMap { entry =>
          project.getElementByID( entry.getOldID.get ) match {
            case oe: Element if ( proxyManager.isElementProxy( oe ) ) =>
              project.getElementByID( entry.getNewID.get ) match {
                case ne: Element => Some( ( oe, ne ) )
                case _           => None
              }
            case _ => None
          }
        } toList

        if ( migrationPairs.size > 2000 ) {
          migrationPairs foreach { case ( oe, ne ) => System.out.println( s" new=${ne.getID} => old=${oe.getID}" ) }
        }
        else {
          migrationPairs foreach {
            case ( oe, ne ) =>
              guiLog.addHyperlinkedText(
                s" new=<A>${ne.getID}</A> <= old=${oe.getID}",
                Map( ne.getID -> new SelectInContainmentTreeRunnable( ne ) ) )
          }
        }
        if ( migrationPairs.isEmpty ) {
          error = new IllegalArgumentException( s"Migration metadata does not match anything. No proxy migration rules created." )
          return
        }

        progressStatus.setCurrent( 0 )
        progressStatus.setMax( migrationPairs.size )
        migrationPairs.foreach {
          case ( oe, ne ) =>
            val info = new ConvertElementInfo( oe.getClassType )
            info.setConvertOnlyIncomingReferences( convertOnlyIncomingReferences )
            Refactoring.Replacing.replace( oe, ne, info )
            progressStatus.increase
        }
      }
    }

    MagicDrawProgressStatusRunner.runWithProgressStatus( runnable, s"Migrate XMI:IDs", true, 0 )

    if ( error != null )
      return Failure( error )

    Success( Unit )
  }

  def migrate(
    project: Project,
    migrationFile: File ): Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( project )

    val mdInstallDir = new File( ApplicationEnvironment.getInstallRoot )
    require( mdInstallDir.exists && mdInstallDir.isDirectory )
    val otiDir = new File( mdInstallDir, "dynamicScripts/org.omg.oti" )
    require( otiDir.exists && otiDir.isDirectory )
    val migrationMM = Metamodel( otiDir )

    require( migrationFile.exists && migrationFile.canRead )
    val migrationURI = URI.createFileURI( migrationFile.getAbsolutePath )

    migrationMM.loadOld2NewIDMappingResource( migrationURI ) match {
      case Failure( t ) => Failure( t )
      case Success( old2newIDmigration ) =>
        val iPrimaryProject = project.getPrimaryProject

        if ( ModulesServiceInternal.useLocalModuleWithWizard( iPrimaryProject ) )

          applyMigration( project, old2newIDmigration ) match {
            case Failure( t ) => Failure( t )
            case Success( _ ) => Success( None )
          }

        else {
          guiLog.log( "Cancelled" )
          Success( None )
        }
    }
  }
}