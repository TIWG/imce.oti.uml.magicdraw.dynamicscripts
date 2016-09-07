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
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.MultiplicityElement
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}
import org.omg.oti.uml.MetaPropertyReference
import org.omg.oti.uml.read.api.{UMLMultiplicityElement, UMLValueSpecification}

import scala.{Boolean,Option,Some,Unit}

/**
* @see MOF 2.5, Section 12.4 EMOF Constraints
* [32] The values of MultiplicityElement::lowerValue and upperValue must be
* of kind LiteralInteger and LiteralUnlimitedNatural respectively.
*
* @see XMI 2.5, Section 9.4.1 EMOF Package
* Properties whose values are the default values are not serialized except
* where the value is being used to specify the default itself:
* specifically if it is the value of the property Property::defaultValue in a metamodel.
*/
case class DeleteRedundantValue
(role: MetaPropertyReference[MagicDrawUML, UMLMultiplicityElement[MagicDrawUML], UMLValueSpecification[MagicDrawUML]])
(implicit umlUtil: MagicDrawUMLUtil)
  extends MagicDrawValidationDataResults.ValidationAnnotationAction(
    "Delete redundant default value (1)",
    "Delete redundant default value (1)") {

  def canExecute(annotation: Annotation): Boolean =
    annotation.getTarget match {
      case mult: MultiplicityElement =>
        if (umlUtil.MultiplicityElement_lowerValue == role)
          null != mult.getLowerValue && !mult.getLowerValue.isInvalid
        else
          null != mult.getUpperValue && !mult.getUpperValue.isInvalid
      case _ =>
        false
    }

  def execute(annotation: Annotation): Unit =
    annotation.getTarget match {
      case mult: MultiplicityElement =>
        val mem = ModelElementsManager.getInstance
        if (umlUtil.MultiplicityElement_lowerValue == role)
          Option.apply(mult.getLowerValue) match {
            case Some(v) if !v.isInvalid =>
              mem.removeElement(v)
            case _ =>
              ()
          }
        else
          Option.apply(mult.getUpperValue) match {
            case Some(v) if !v.isInvalid =>
              mem.removeElement(v)
            case _ =>
              ()
          }
      case _ =>
        ()
    }

}