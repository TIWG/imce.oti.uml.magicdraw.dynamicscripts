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
import java.lang.System

import com.nomagic.magicdraw.actions.ActionsID
import com.nomagic.magicdraw.actions.ActionsProvider
import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML

import org.eclipse.emf.common.util.URI
import org.omg.oti.changeMigration.Metamodel

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes.MainToolbarMenuAction
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.magicdraw.enhanced.migration.LocalModuleMigrationInterceptor

import scala.util.{Failure, Success, Try}
import scala.{Option,None,Some,StringContext}
import scala.Predef.require

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object openLocalProjectSkipLocalModule {

  def doit( project: Project, ev: ActionEvent, script: MainToolbarMenuAction ): Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val ap = ActionsProvider.getInstance
    val mainToolbarActionMgr = ap.getMainToolbarActions
    val openProjectAction = mainToolbarActionMgr.getActionFor( ActionsID.OPEN_PROJECT )

    val mdInstallDir = MDUML.getApplicationInstallDir
    require( mdInstallDir.exists && mdInstallDir.isDirectory )
    val otiDir = new File( mdInstallDir, "dynamicScripts/org.omg.oti" )
    require( otiDir.exists && otiDir.isDirectory )
    val migrationMM = Metamodel( otiDir )

    val guiLog = a.getGUILog
    guiLog.clearLog()

    MigrationHelper.chooseMigrationFile match {
      case Some( migrationFile ) =>
        require( migrationFile.exists && migrationFile.canRead )
        val migrationURI = URI.createFileURI( migrationFile.getAbsolutePath )

        migrationMM.loadOld2NewIDMappingResource( migrationURI ) match {
          case Failure( t ) => Failure( t )
          case Success( old2newIDmigration ) =>
            val entries = old2newIDmigration.getEntries
            guiLog.log( s" Loaded ${entries.size} old2new ID migration entries" )

            require( old2newIDmigration.getModelIdentifier.isDefined )
            val oldModuleFile = new File( mdInstallDir, old2newIDmigration.getModelIdentifier.get )
            require( oldModuleFile.exists && oldModuleFile.canRead )

            LocalModuleMigrationInterceptor.clearForceSkipLocalModules
            LocalModuleMigrationInterceptor.addForceSkipLocalModule( URI.createFileURI( oldModuleFile.getAbsolutePath ) )

            try {
              openProjectAction.actionPerformed( ev )
            }
            finally {
              LocalModuleMigrationInterceptor.clearForceSkipLocalModules
            }
        }
      case None =>
        System.out.println( s"openLocalProjectSkipLocalModule: cancelled" )
    }
    Success( None )
  }
}