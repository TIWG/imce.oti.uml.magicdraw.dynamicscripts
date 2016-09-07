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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.json.magicDrawValidation

import play.api.libs.json._

import org.omg.oti.json.uml.OTIMOFElement
import scala.collection.immutable.Iterable

/**
  * OTI UML Json data structure for MagicDraw RuleViolationResults for the same MagicDraw Element
  *
  * @see com.nomagic.magicdraw.validation.RuleViolationResult
  * @see com.nomagic.magicdraw.annotation.Annotation
  *
  * @param element OTI UML Json conversion of RuleViolationResult.getElement
  * @param annotations OTI UML Json conversion of all Annotations that annotate the element
  */
case class RuleViolationDataResults
( element: OTIMOFElement,
  annotations: Iterable[ElementAnnotation] )

object RuleViolationDataResults {

  implicit def reads
  : Reads[RuleViolationDataResults]
  = Json.reads[RuleViolationDataResults]

  implicit def writes
  : Writes[RuleViolationDataResults]
  = Json.writes[RuleViolationDataResults]

  implicit def formats
  : Format[RuleViolationDataResults]
  = Json.format[RuleViolationDataResults]

}