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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{ElementImport, NamedElement, PackageImport, VisibilityKindEnum}
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read._

import scala.{Boolean,Unit}
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