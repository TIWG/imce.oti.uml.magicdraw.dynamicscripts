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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.ProjectWindow
import com.nomagic.magicdraw.validation.{RuleViolationResult, ValidationRunData}
import com.nomagic.ui.ExtendedPanel
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.json.magicDrawValidation.{ElementAnnotation, RuleViolationDataResults, ValidationResults}
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._

import scala.collection.immutable.Iterable
import scala.{None, Option, Some}
import scala.Predef.String

case class ProjectValidationResultPanelInfo
( validatedProject: Project,
  validationID: String,
  validationRunData: ValidationRunData,
  results: Iterable[RuleViolationResult],
  projectWindow: ProjectWindow,
  uiPanel: ExtendedPanel ) {

  def toValidationResultsForProfileAdapter
  (implicit ojeh: MagicDrawOTIJsonElementHelperForProfileAdapter)
  : ValidationResults
  = ValidationResults(
    name = projectWindow.getInfo.getName,
    elementResults = toRuleViolationDataResultsProfileAdapter)

  def toRuleViolationDataResultsProfileAdapter
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
  def toValidationResultsForDataAdapter
  (implicit ojeh: MagicDrawOTIJsonElementHelperForDataAdapter)
  : ValidationResults
  = ValidationResults(
    name = projectWindow.getInfo.getName,
    elementResults = toRuleViolationDataResultsForDataAdapter)

  def toRuleViolationDataResultsForDataAdapter
  (implicit ojeh: MagicDrawOTIJsonElementHelperForDataAdapter)
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