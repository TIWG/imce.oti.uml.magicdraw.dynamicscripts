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

import com.nomagic.magicdraw.annotation.Annotation
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{VisibilityKindEnum, NamedElement, ElementImport, PackageImport}
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read._

import scala.language.postfixOps

/**
* @see MOF 2.5, Section 12.4 EMOF Constraints
* [4] Core::Basic and EMOF does not support visibilities.
 *      All property visibilities must be explicitly set to public where applicable,
 *      that is for all NamedElements, ElementImports and PackageImports.
 *      Furthermore, no alias is allowed for any ElementImport.
*
* @see XMI 2.5, Section 9.4.1 EMOF Package
* [7] CMOF does not support visibilities.
 *      All property visibilities must be explicitly set to public where applicable,
 *      that is for all NamedElements, ElementImports, and PackageImports.
 *      Furthermore, no alias is allowed for any ElementImport.
*/
case class SetVisibilityToPublic()
(implicit umlUtil: MagicDrawUMLUtil)
  extends MagicDrawValidationDataResults.ValidationAnnotationAction(
    "Set visibility to public",
    "Set visibility to public") {

  def canExecute(annotation: Annotation): Boolean =
    annotation.getTarget match {
      case ne: NamedElement =>
        ne.getVisibility != VisibilityKindEnum.PUBLIC
      case ei: ElementImport =>
        ei.getVisibility != VisibilityKindEnum.PUBLIC
      case pi: PackageImport =>
        pi.getVisibility != VisibilityKindEnum.PUBLIC
      case _ =>
        false
    }

  def execute(annotation: Annotation): Unit =
    annotation.getTarget match {
      case ne: NamedElement =>
        if (ne.getVisibility != VisibilityKindEnum.PUBLIC)
          ne.setVisibility(VisibilityKindEnum.PUBLIC)
      case ei: ElementImport =>
        if (ei.getVisibility != VisibilityKindEnum.PUBLIC)
          ei.setVisibility(VisibilityKindEnum.PUBLIC)
      case pi: PackageImport =>
        if (pi.getVisibility != VisibilityKindEnum.PUBLIC)
          pi.setVisibility(VisibilityKindEnum.PUBLIC)
      case _ =>
        ()
    }

}