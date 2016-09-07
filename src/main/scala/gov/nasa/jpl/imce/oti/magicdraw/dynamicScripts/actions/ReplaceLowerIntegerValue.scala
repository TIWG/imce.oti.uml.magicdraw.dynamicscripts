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

import java.lang.Integer

import com.nomagic.magicdraw.annotation.Annotation
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{LiteralInteger, MultiplicityElement}
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil

import scala.Predef.{Integer2int,String}
import scala.{Boolean,StringContext,Unit}

/**
* @see MOF 2.5, Section 12.4 EMOF Constraints
* [32] The values of MultiplicityElement::lowerValue and upperValue must be
* of kind LiteralInteger and LiteralUnlimitedNatural respectively.
*/
case class ReplaceLowerIntegerValue(oldKind: String, newValue: Integer)
                                   (implicit umlUtil: MagicDrawUMLUtil)
  extends MagicDrawValidationDataResults.ValidationAnnotationAction(
    s"Replace old $oldKind lower value with a LiteralInteger value $newValue",
    s"Replace old $oldKind lower value with a LiteralInteger value $newValue") {

  def canExecute(annotation: Annotation): Boolean =
    annotation.getTarget match {
      case mult: MultiplicityElement =>
        mult.getLowerValue match {
          case l: LiteralInteger =>
            l.getValue != newValue
          case _ =>
            true
        }
      case _ =>
        false
    }

  def execute(annotation: Annotation): Unit =
    annotation.getTarget match {
      case mult: MultiplicityElement =>
        val f = Project.getProject(mult).getElementsFactory
        val mem = ModelElementsManager.getInstance
        if (null != mult.getLowerValue)
          mem.removeElement(mult.getLowerValue)
        val lower = f.createLiteralIntegerInstance()
        lower.setValue(newValue)
        mult.setLowerValue(lower)
      case _ =>
        ()
    }

}