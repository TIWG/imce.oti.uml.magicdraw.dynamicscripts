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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.ProjectWindow
import com.nomagic.magicdraw.validation.{RuleViolationResult, ValidationRunData}
import com.nomagic.ui.ExtendedPanel
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.json.magicDrawValidation.{ElementAnnotation, RuleViolationDataResults, ValidationResults}
import org.omg.oti.magicdraw.uml.canonicalXMI.helper.MagicDrawOTIJsonElementHelperForProfileAdapter

import scala.collection.immutable.{Iterable, Vector}
import scala.{None, Option, Some}
import scala.Predef.String

case class ProjectValidationResultPanelInfo
( validatedProject: Project,
  validationID: String,
  validationRunData: ValidationRunData,
  results: Iterable[RuleViolationResult],
  projectWindow: ProjectWindow,
  uiPanel: ExtendedPanel ) {

  def toValidationResults
  (implicit ojeh: MagicDrawOTIJsonElementHelperForProfileAdapter)
  : ValidationResults
  = ValidationResults(
    name = projectWindow.getInfo.getName,
    elementResults = toRuleViolationDataResults)

  def toRuleViolationDataResults
  (implicit ojeh: MagicDrawOTIJsonElementHelperForProfileAdapter)
  : Iterable[RuleViolationDataResults]
  = {
    import ojeh._

    results.groupBy(_.getElement).flatMap {

      case (element: Element, violations: Iterable[RuleViolationResult]) =>
        Some(RuleViolationDataResults(
          element,
          for {
            v <- violations
            ann = v.getAnnotation
          } yield ElementAnnotation(
            constraint = v.getRule,
            severity = ann.getSeverity,
            kind = Option.apply(ann.getKind),
            text = Option.apply(ann.getText))))

      case _ =>
        None
    }
  }
}