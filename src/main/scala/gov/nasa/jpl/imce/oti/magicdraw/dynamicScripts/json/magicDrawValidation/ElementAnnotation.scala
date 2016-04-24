/*
 *
 * License Terms
 *
 * Copyright (c) 2014-2016, California Institute of Technology ("Caltech").
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