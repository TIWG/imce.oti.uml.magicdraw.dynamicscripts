/*
 *
 * License Terms
 *
 * Copyright (c) 2014-2016, California Institute of Technology ("Caltech").
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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent
import java.io.File
import java.lang.System

import com.nomagic.magicdraw.core.utils.ChangeElementID
import com.nomagic.magicdraw.core.{Application, ApplicationEnvironment, Project}
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.uml.UUIDRegistry
import com.nomagic.magicdraw.uml.actions.SelectInContainmentTreeRunnable
import com.nomagic.task.{ProgressStatus, RunnableWithProgress}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDGUILogHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.eclipse.emf.common.util.URI
import org.omg.oti.changeMigration.Metamodel
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil

import scala.Predef.{ArrowAssoc, require}
import scala.collection.JavaConversions.{mapAsJavaMap, seqAsJavaList}
import scala.collection.immutable._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}
import scala.{Option,None,Some,StringContext,Unit}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object changePackageExtentIDs {

  def doit(
    project: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.MainToolbarMenuAction ): Try[Option[MagicDrawValidationDataResults]] =
    doit( project )

  def doit( project: Project ): Try[Option[MagicDrawValidationDataResults]] = {
    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( project )

    val mdInstallDir = MDUML.getApplicationInstallDir
    require( mdInstallDir.exists && mdInstallDir.isDirectory )

    val otiChangeMigrationDir = new File( mdInstallDir, "dynamicScripts/org.omg.oti.changeMigration/resources" )
    require( otiChangeMigrationDir.exists && otiChangeMigrationDir.isDirectory )
    val migrationMM = Metamodel( otiChangeMigrationDir )

    val dir = project.getProjectDirectory
    require( dir.isDefined && dir.get.exists && dir.get.isDirectory )
    val migrationF = new File( dir.get, project.getName+".migration.xmi" )
    require( migrationF.exists && migrationF.canRead )
    val migrationURI = URI.createFileURI( migrationF.getAbsolutePath )

    migrationMM.loadOld2NewIDMappingResource( migrationURI ) match {
      case Failure( t ) => Failure( t )
      case Success( old2newIDmigration ) =>
        val entries = old2newIDmigration.getEntries filter { entry =>
          val difference = for {
            newID <- entry.getNewID
            oldID <- entry.getOldID
          } yield newID != oldID
          difference.getOrElse(false)
        }
        guiLog.log( s" Loaded ${entries.size} old2new ID migration entries" )

        val resetElements =
          entries
          .flatMap { entry =>
            project.getElementByID( entry.getOldID.get ) match {
              case e: Element =>
                Some(e)
              case _ =>
                None
            }}
          .to[Set]
        val resetMap = entries
          .flatMap { entry =>
            val oldId = entry.getOldID.get
            val newId = entry.getNewID.get
            Option.apply(project.getElementByID(oldId)) match {
              case Some(e: Element) =>
                val uuid = UUIDRegistry.getUUID(e)
                if (oldId == newId)
                  None
                else
                  Seq(oldId -> newId, uuid -> uuid)
              case _ =>
                None
            }
          }
          .toMap

        val runnable = new RunnableWithProgress() {
          def run( progressStatus: ProgressStatus ): Unit = {
            project.changeElementIDs(resetElements, resetMap, progressStatus )
          }
        }

        MagicDrawProgressStatusRunner.runWithProgressStatus( runnable, s"Change XMI:IDs", true, 0 )

        if ( entries.size > 2000 ) {
          entries foreach { entry => System.out.println( s" new=${entry.getNewID.get} => old=${entry.getOldID.get}" ) }
        } else {
          entries foreach { entry =>
            val id = entry.getNewID.get
            project.getElementByID( id ) match {
              case e: Element =>
                guiLog.addGUILogHyperlink(
                  s" new=<A>$id</A> <= old=${entry.getOldID.get}",
                  (id, e))
              case _ =>
                ()
            }
          }
        }

        guiLog.log( s"Done! (${entries.size} old2new ID migrations)" )
        Success( None )
    }
  }
}