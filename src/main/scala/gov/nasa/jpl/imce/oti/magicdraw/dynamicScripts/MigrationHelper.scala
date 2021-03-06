/*
 * Copyright 2016 California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * License Terms
 */

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent
import java.io.File
import java.lang.{IllegalArgumentException,System, Throwable}
import java.nio.file.Path

import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

import scala.language.postfixOps
import scala.util.{Failure,Success,Try}
import scala.{Boolean,Int,Option,None,Some,StringContext,Unit}
import scala.Predef.{require,String}

import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.uml.ConvertElementInfo
import com.nomagic.magicdraw.uml.Refactoring
import com.nomagic.task.ProgressStatus
import com.nomagic.task.RunnableWithProgress
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element

import org.eclipse.emf.common.util.URI
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil
import org.omg.oti.changeMigration.Metamodel
import org.omg.oti.changeMigration.Old2NewIDMapping

import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDGUILogHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML._
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes.MainToolbarMenuAction
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object MigrationHelper {

  def customMigrationConvertOnlyIncomingReferences(
    project: Project, ev: ActionEvent,
    script: MainToolbarMenuAction ): Try[Option[MagicDrawValidationDataResults]] = {

    chooseMigrationFile match {
      case Some( migrationFile ) =>
        require( migrationFile.exists && migrationFile.canRead )
        val migrationURI = URI.createFileURI( migrationFile.getAbsolutePath )
        migrate(project, migrationURI, convertOnlyIncomingReferences=true)
      case None =>
        Success( None )
    }
  }

  def customMigrationConvertAllReferences(
                                           project: Project, ev: ActionEvent,
                                           script: MainToolbarMenuAction ): Try[Option[MagicDrawValidationDataResults]] = {

    chooseMigrationFile match {
      case Some( migrationFile ) =>
        require( migrationFile.exists && migrationFile.canRead )
        val migrationURI = URI.createFileURI( migrationFile.getAbsolutePath )
        migrate(project, migrationURI, convertOnlyIncomingReferences=false)
      case None =>
        Success( None )
    }
  }

  def chooseMigrationFile: Option[File] = {
    val dir: Path = null // @todo update...

    val ff = new FileFilter() {

      def getDescription: String = "*.migration.xmi"

      def accept( f: File ): Boolean =
        f.isDirectory ||
          ( f.isFile && f.getName.endsWith( ".migration.xmi" ) )

    }

    val fc = new JFileChooser( MDUML.getApplicationInstallDir ) {

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

  def applyMigration
  ( project: Project,
    old2newIDmigration: Old2NewIDMapping,
    convertOnlyIncomingReferences: Boolean )
  : Try[Unit] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog

    val entries = old2newIDmigration.getEntries
    guiLog.log( s" Applying ${entries.size} old2new ID migration entries" )

    var error: Throwable = null

    val runnable = new RunnableWithProgress() {
      def run( progressStatus: ProgressStatus ): Unit = {

        val proxyManager = project.getProxyManager

        val migrationPairs = entries flatMap { entry =>
          project.getElementByID( entry.getOldID.get ) match {
            case oe: Element if proxyManager.isElementProxy( oe ) =>
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
              guiLog.addGUILogHyperlink(
                s" new=<A>${ne.getID}</A> <= old=${oe.getID}",
                ( ne.getID, ne ))
          }
        }
        if ( migrationPairs.isEmpty ) {
          error = new IllegalArgumentException( s"Migration metadata does not match anything. No proxy migration rules created." )
          return
        }

        progressStatus.setCurrent( 0 )
        progressStatus.setMax( migrationPairs.size.toLong )
        migrationPairs.foreach {
          case ( oe, ne ) =>
            val info = new ConvertElementInfo( oe.getClassType )
            info.setConvertOnlyIncomingReferences( convertOnlyIncomingReferences )
            Refactoring.Replacing.replace( oe, ne, info )
            progressStatus.increase()
        }
      }
    }

    MagicDrawProgressStatusRunner.runWithProgressStatus( runnable, s"Migrate XMI:IDs", true, 0 )

    if ( error != null )
      return Failure( error )

    Success( () )
  }

  def migrate(
    project: Project,
    migrationURI: URI,
    convertOnlyIncomingReferences: Boolean ): Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( project )

    val mdInstallDir = MDUML.getApplicationInstallDir
    require( mdInstallDir.exists && mdInstallDir.isDirectory )
    val otiDir = new File( mdInstallDir, "dynamicScripts/org.omg.oti.changeMigration/resources" )
    require( otiDir.exists && otiDir.isDirectory )
    val migrationMM = Metamodel( otiDir )

    migrationMM.loadOld2NewIDMappingResource( migrationURI ) match {
      case Failure( t ) => Failure( t )
      case Success( old2newIDmigration ) =>

        if ( project.promptUseLocalModuleWithWizard )

          applyMigration( project, old2newIDmigration, convertOnlyIncomingReferences ) match {
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