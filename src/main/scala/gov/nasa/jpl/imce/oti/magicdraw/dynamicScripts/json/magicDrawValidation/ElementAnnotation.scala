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

import scala.Predef.String
import scala.Option

/**
  * OTI UML Json data structure for a MagicDraw Annotation
  *
  * @see com.nomagic.magicdraw.annotation.Annotation
  * @see com.nomagic.magicdraw.validation.RuleViolationResult
  *
  * @param constraint OTI UML Json conversion of the RuleViolationResult.getRule
  * @param severity OTI UML Json conversion of Annotation.getSeverity
  * @param kind Annotation.getKind, if any
  * @param text Annotation.getText, if any
  */
case class ElementAnnotation
( constraint: OTIMOFElement.OTIUMLConstraint,
  severity: OTIMOFElement.OTIUMLEnumerationLiteral,
  kind: Option[String],
  text: Option[String])

object ElementAnnotation {

  implicit def reads
  : Reads[ElementAnnotation]
  = Json.reads[ElementAnnotation]

  implicit def writes
  : Writes[ElementAnnotation]
  = Json.writes[ElementAnnotation]

  implicit def formats
  : Format[ElementAnnotation]
  = Json.format[ElementAnnotation]

}