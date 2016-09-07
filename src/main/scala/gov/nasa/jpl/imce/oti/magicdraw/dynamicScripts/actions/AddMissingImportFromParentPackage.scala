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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.actions

import com.nomagic.magicdraw.annotation.Annotation
import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package

import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil

import scala.{Boolean,None,Some,StringContext,Unit}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
case class AddMissingImportFromParentPackage()( implicit umlUtil: MagicDrawUMLUtil )
  extends MagicDrawValidationDataResults.ValidationAnnotationAction(
    "Add missing import from parent package",
    "Add missing import from parent package" ) {

  def canExecute( annotation: Annotation ): Boolean =
    annotation.getTarget match {
      case mdPkg: Package =>
        import umlUtil._
        val pkg = umlPackage( mdPkg )
        pkg.nestingPackage match {
          case None         => false
          case Some( ppkg ) => !ppkg.importedPackages.contains( pkg )
        }
      case _ => false
    }

  def execute( annotation: Annotation ): Unit =
    annotation.getTarget match {
      case mdPkg: Package =>
        import umlUtil._
        val pkg = umlPackage( mdPkg )
        pkg.nestingPackage match {
          case Some( ppkg ) if !ppkg.importedPackages.contains( pkg ) =>

            val f = Project.getProject( mdPkg ).getElementsFactory
            val i = f.createPackageImportInstance
            i.setImportingNamespace( umlMagicDrawUMLPackage(ppkg).getMagicDrawPackage )
            i.setImportedPackage( mdPkg )

            val app = Application.getInstance()
            val guiLog = app.getGUILog
            guiLog.log( s"Added import from '${ppkg.name.get}' => '${mdPkg.getName}'" )
          case _ =>
            ()
        }
      case _ => ()
    }
}