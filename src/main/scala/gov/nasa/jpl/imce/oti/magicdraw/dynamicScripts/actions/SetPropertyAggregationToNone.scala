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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{AggregationKindEnum, Property}
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read._

import scala.{Boolean,Unit}
/**
* @see MOF 2.5, Section 12.4 EMOF Constraints
* [28] A Property typed by a kind of DataType must have aggregation = none.
*
* @see XMI 2.5, Section 9.4.1 EMOF Package
* [28] A Property typed by a kind of DataType must have aggregation = none.
*/
case class SetPropertyAggregationToNone()
(implicit umlUtil: MagicDrawUMLUtil)
  extends MagicDrawValidationDataResults.ValidationAnnotationAction(
    "Set Property aggregation to none",
    "Set Property aggregation to none") {

  def canExecute(annotation: Annotation): Boolean =
    annotation.getTarget match {
      case p: Property =>
        p.getDatatype != null && p.getAggregation != AggregationKindEnum.NONE
      case _ =>
        false
    }

  def execute(annotation: Annotation): Unit =
    annotation.getTarget match {
      case p: Property =>
        if (p.getDatatype != null && p.getAggregation != AggregationKindEnum.NONE)
          p.setAggregation(AggregationKindEnum.NONE)
      case _ =>
        ()
    }

}