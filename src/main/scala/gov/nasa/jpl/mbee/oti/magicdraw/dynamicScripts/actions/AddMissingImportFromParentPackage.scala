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
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.actions

import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import com.nomagic.magicdraw.annotation.Annotation
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.MagicDrawUMLUtil
import com.nomagic.magicdraw.core.Application

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