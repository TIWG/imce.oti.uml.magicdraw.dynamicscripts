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
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.MultiplicityElement
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import org.omg.oti.uml.MetaPropertyReference
import org.omg.oti.uml.read.api.{UMLMultiplicityElement, UMLValueSpecification}
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}

import scala.language.postfixOps

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