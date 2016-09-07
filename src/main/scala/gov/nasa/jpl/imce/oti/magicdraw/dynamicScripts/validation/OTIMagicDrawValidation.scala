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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation

import java.lang.IllegalArgumentException

import com.nomagic.magicdraw.annotation.Annotation
import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.validation.{RuleViolationResult, ValidationRunData}
import com.nomagic.task.RunnableWithProgress
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Constraint, Element, EnumerationLiteral, Package => MDPackage}
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults.ValidationAnnotationAction
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.internal.MDValidationAPIHelper._
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.actions.ExportAsJUnitResult
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil
import org.omg.oti.uml.UMLError

import scala.Predef.{augmentString,require,ArrowAssoc,String}
import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Failure, Success, Try}
import scalaz.Scalaz._
import scalaz._
import scala.{Option,None,Some,StringContext,Tuple2,Tuple3,Unit}

object OTIMagicDrawValidation {

  type MDValidationInfo = (Constraint, String, List[ValidationAnnotationAction])
}

case class OTIMagicDrawValidation(project: Project)(implicit mdUtil: MagicDrawUMLUtil) {

  val mdOTIValidationSuite = {
    val s = project.lookupValidationSuite("*::MagicDrawOTIValidation")
    require(
      s.isDefined,
      "Failed to find the MagicDraw validation suite package '*::MagicDrawOTIValidation'")
    s.get
  }

  val MD_OTI_ValidationConstraint_NotOTISpecificationRoot =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::NotOTISpecificationRoot")

  val MD_OTI_ValidationConstraint_UnresolvedCrossReference =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::UnresolvedCrossReference")

  // DefaultValueValidationStatus

  val MD_OTI_ValidationConstraint_InvalidDefaultValueForClassTypedParameterOrProperty =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDefaultValueForClassTypedParameterOrProperty")

  val MD_OTI_ValidationConstraint_InvalidDefaultValueForEnumerationTypedParameterOrProperty =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDefaultValueForEnumerationTypedParameterOrProperty")

  val MD_OTI_ValidationConstraint_InvalidDefaultValueForPrimitiveTypeTypedParameterOrProperty =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDefaultValueForPrimitiveTypeTypedParameterOrProperty")

  val MD_OTI_ValidationConstraint_InvalidDefaultValueForMultiValuedParameterOrProperty =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDefaultValueForMultiValuedParameterOrProperty")

  // MultiplicityValueValidationStatus

  val MD_OTI_ValidationConstraint_RedundantValue =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::RedundantValue")

  val MD_OTI_ValidationConstraint_InvalidValueAsUnlimitedNatural =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidValueAsUnlimitedNatural")

  val MD_OTI_ValidationConstraint_InvalidValueAsInteger =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidValueAsInteger")

  val MD_OTI_ValidationConstraint_InvalidValueAsString =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidValueAsString")

  val MD_OTI_ValidationConstraint_InvalidValueKind =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidValueKind")

  // NamedElementValidationStatus

  val MD_OTI_ValidationConstraint_InvalidUnnamedNamedElement =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidUnnamedNamedElement")

  // TypedElementValidationStatus

  val MD_OTI_ValidationConstraint_InvalidOperationRaisedExceptionNonClassType =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidOperationRaisedExceptionNonClassType")

  val MD_OTI_ValidationConstraint_InvalidTypedElementWithAssociationType =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidTypedElementWithAssociationType")

  val MD_OTI_ValidationConstraint_InvalidUntypedTypedElement =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidUntypedTypedElement")

  val MD_OTI_ValidationConstraint_InvalidDataTypePropertyAggregation =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDataTypePropertyAggregation")

  val MD_OTI_ValidationConstraint_InvalidDataTypePropertyWithNonDataTypeType =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDataTypePropertyWithNonDataTypeType")

  val MD_OTI_ValidationConstraint_InvalidAssociationMemberEndPropertyNonClassType =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidAssociationMemberEndPropertyNonClassType")

  // VisibilityValidationStatus

  val MD_OTI_ValidationConstraint_MissingPublicVisibility =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::MissingPublicVisibility")

  val MD_OTI_ValidationConstraint_InvalidNonPublicVisibility =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidNonPublicVisibility")

  val MD_OTI_ValidationConstraint_InvalidAliasedElementImport =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidAliasedElementImport")


  val MD_OTI_ValidationConstraint_ =
    project.lookupValidationConstraint(
      mdOTIValidationSuite, "*::")

  /**
   * @param validationConstraint A MagicDraw <<validationRule>>-stereotyped Constraint
   * @param message A validation message, if none, defaults to the validationConstraint's raw message
   * @param actions MagicDraw DynamicScripts validation annotation actions
   */
  def makeValidationInfo
  (validationConstraint: Option[Constraint],
   message: Option[String],
   actions: List[ValidationAnnotationAction]): Try[Option[OTIMagicDrawValidation.MDValidationInfo]] =
    validationConstraint match {
      case None =>
        Failure(new IllegalArgumentException(
          "Must provide a MagicDraw <<validationRule>>-stereotyped Constraint"))
      case Some(c) =>
        message match {
          case None =>
            project.getRuleRawMessage(c) match {
              case None =>
                Failure(new IllegalArgumentException(
                  s"""Must provide a message since the <<validationRule>>-stereotyped Constraint
                     |'${c.getQualifiedName}' does not have one""".stripMargin))
              case Some(m) =>
                Success(Some(Tuple3(c, m, actions)))
            }
          case Some(m) =>
            Success(Some(Tuple3(c, m, actions)))
        }
    }

  /**
   * @param validationConstraint A MagicDraw <<validationRule>>-stereotyped Constraint
   * @param message A validation message, if none, defaults to the validationConstraint's raw message
   * @param actions MagicDraw DynamicScripts validation annotation actions
   */
  def constructValidationInfo
  (validationConstraint: Option[Constraint],
   message: Option[String],
   actions: List[ValidationAnnotationAction]): Try[OTIMagicDrawValidation.MDValidationInfo] =
    validationConstraint match {
      case None =>
        Failure(new IllegalArgumentException(
          "Must provide a MagicDraw <<validationRule>>-stereotyped Constraint"))
      case Some(c) =>
        message match {
          case None =>
            project.getRuleRawMessage(c) match {
              case None =>
                Failure(new IllegalArgumentException(
                  s"""Must provide a message since the <<validationRule>>-stereotyped Constraint
                     |'${c.getQualifiedName}' does not have one""".stripMargin))
              case Some(m) =>
                Success(Tuple3(c, m, actions))
            }
          case Some(m) =>
            Success(Tuple3(c, m, actions))
        }
    }

  /**
   * Populates & opens MagicDraw's validation results window
    *
    * @param validationMessage Used for the title of the validation result window & shown in the MagicDraw message window
   * @param elementMessages Maps MagicDraw elements to a collection of validation constraint/message/actions
   * @param vSuite MagicDraw DynamicScript Validation Suite Information
   */
  def makeMDIllegalArgumentExceptionValidation
  (validationMessage: String,
   elementMessages: Map[Element, Iterable[OTIMagicDrawValidation.MDValidationInfo]],
   vSuite: MDPackage = mdOTIValidationSuite)
  : NonEmptyList[java.lang.Throwable] \/ Option[MagicDrawValidationDataResults] = {
    val app = Application.getInstance()
    val guiLog = app.getGUILog
    if (elementMessages.isEmpty) {
      guiLog.log(s"OK -- no violations of $validationMessage")
      Option.empty[MagicDrawValidationDataResults].right[NonEmptyList[java.lang.Throwable]]
    }
    else {

      val elements = elementMessages.keySet
      val results = for {
        resultSet <- elementMessages.values
        result <- resultSet
      } yield result

      val (repairable, nonRepairable) = results partition { case (_, _, actions) => actions.nonEmpty }

      val nElements = elements.size
      val nResults = results.size
      val nFixable = repairable.size
      val nNonFixable = nonRepairable.size

      val summary =
        if (nNonFixable == 0)
          s"$nFixable fixable violations of $validationMessage"
        else if (nFixable == 0)
          s"$nNonFixable violations of $validationMessage (all require user repair)"
        else
          s"""$nResults violations of $validationMessage
              |($nFixable fixable / $nNonFixable require user repair)
           """.stripMargin

      guiLog.log(summary)

      val initialLevel: Option[EnumerationLiteral] = None
      val minimumLevel: Option[EnumerationLiteral] = (initialLevel /: results) {
        case (None, (c, _, _)) =>
          project.getRuleSeverityLevel(c)
        case (Some(severityLevel), (c, _, _)) =>
          project.getRuleSeverityLevel(c) match {
            case None =>
              Some(severityLevel)
            case Some(otherLevel) =>
              if (project.isValidationSeverityHigherOrEqual(severityLevel, otherLevel))
                Some(otherLevel)
              else
                Some(severityLevel)
          }
      }

      val minimumLevelOrError: NonEmptyList[java.lang.Throwable] \/ EnumerationLiteral =
        minimumLevel
        .fold[NonEmptyList[java.lang.Throwable] \/ EnumerationLiteral](
          NonEmptyList(
            UMLError.umlAdaptationError(
              """|At least one error message must involve
                 |a MagicDraw <<validationRule>>-stereotyped Constraint with a severity level""".stripMargin('|'))
          ).left
        ) { level =>
          level.right[NonEmptyList[java.lang.Throwable]]
        }

      val runDataOrError =
        minimumLevelOrError.map { minimumLevel =>
          new ValidationRunData(vSuite, false, elements, minimumLevel)
        }

      val exportAction = ExportAsJUnitResult()

      val runResults = elementMessages flatMap {
        case (element, results) =>
          results map {
            case (constraint, message, actions) =>
              val annotation = new Annotation(element, constraint, message, exportAction :: actions)
              actions foreach {
                case vaa: ValidationAnnotationAction =>
                  vaa.annotation = Some(annotation)
                case _ =>
                  ()
              }
              new RuleViolationResult(annotation, constraint)
          }
      }

      runDataOrError
      .map { runData =>
        MagicDrawValidationDataResults(
          summary,
          runData,
          runResults.toList,
          List[RunnableWithProgress]())
          .some
      }
    }

  }

  def toTryOptionMDValidationDataResults
  (p: Project, message: String, maybeErrors: NonEmptyList[java.lang.Throwable] \/ Unit)
  : Try[Option[MagicDrawValidationDataResults]] = {

    maybeErrors
      .fold[Try[Option[MagicDrawValidationDataResults]]](
      l = (nels: NonEmptyList[java.lang.Throwable]) =>
      {
        val errorMessages
        : Map[com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element, (String, List[com.nomagic.actions.NMAction])] =
          nels
            .toList
            .map { cause =>
              p.getModel -> Tuple2(cause.getMessage, List.empty[com.nomagic.actions.NMAction])
            }
            .toMap


        scala.util.Success(
          p.makeMDIllegalArgumentExceptionValidation(
            message,
            errorMessages,
            "*::MagicDrawOTIValidation",
            "*::UnresolvedCrossReference"
          )
            .validationDataResults
            .some
        )
      },
      r = (_: Unit) =>
        Success(None)
    )

  }

  def errorSet2TryOptionMDValidationDataResults
  (p: Project, message: String, maybeErrors: Set[java.lang.Throwable] \/ Unit)
  : Try[Option[MagicDrawValidationDataResults]] = {

    maybeErrors
      .fold[Try[Option[MagicDrawValidationDataResults]]](
      l = (nels: Set[java.lang.Throwable]) =>
      {
        val errorMessages
        : Map[com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element, (String, List[com.nomagic.actions.NMAction])] =
          nels
            .map { cause =>
              p.getModel -> Tuple2(cause.getMessage, List.empty[com.nomagic.actions.NMAction])
            }
            .toMap


        scala.util.Success(
          p.makeMDIllegalArgumentExceptionValidation(
            message,
            errorMessages,
            "*::MagicDrawOTIValidation",
            "*::UnresolvedCrossReference"
          )
            .validationDataResults
            .some
        )
      },
      r = (_: Unit) =>
        Success(None)
    )

  }

  def toTryOptionMDValidationDataResults
  (p: Project, message: String, maybeErrors: Option[NonEmptyList[java.lang.Throwable]])
  : Try[Option[MagicDrawValidationDataResults]] = {

    maybeErrors
      .fold[scala.util.Try[Option[MagicDrawValidationDataResults]]](
      scala.util.Success(Option.empty[MagicDrawValidationDataResults])
    ){ nels: NonEmptyList[java.lang.Throwable] =>
      val errors
      : Map[com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element, (String, List[com.nomagic.actions.NMAction])] =
        nels
          .toList
          .map { error =>
            p.getModel -> Tuple2(error.getMessage, List.empty[com.nomagic.actions.NMAction])
          }
          .toMap


      scala.util.Success(
        p.makeMDIllegalArgumentExceptionValidation(
          message,
          errors,
          "*::MagicDrawOTIValidation",
          "*::UnresolvedCrossReference"
        )
          .validationDataResults
          .some
      )
    }

  }

  def errorSet2TryOptionMDValidationDataResults
  (p: Project, message: String, maybeErrors: Option[Set[java.lang.Throwable]])
  : Try[Option[MagicDrawValidationDataResults]] = {

    maybeErrors
      .fold[scala.util.Try[Option[MagicDrawValidationDataResults]]](
      scala.util.Success(Option.empty[MagicDrawValidationDataResults])
    ){ nels: Set[java.lang.Throwable] =>
      val errors
      : Map[com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element, (String, List[com.nomagic.actions.NMAction])] =
        nels
          .map { error =>
            p.getModel -> Tuple2(error.getMessage, List.empty[com.nomagic.actions.NMAction])
          }
          .toMap


      scala.util.Success(
        p.makeMDIllegalArgumentExceptionValidation(
          message,
          errors,
          "*::MagicDrawOTIValidation",
          "*::UnresolvedCrossReference"
        )
          .validationDataResults
          .some
      )
    }

  }

  def toTryOptionMagicDrawValidationDataResults
  ( p: Project,
    message: String,
    results: NonEmptyList[java.lang.Throwable] \/ Option[MagicDrawValidationDataResults] )
  : Try[Option[MagicDrawValidationDataResults]] =
    results.fold[Try[Option[MagicDrawValidationDataResults]]](
    l = (nels: NonEmptyList[java.lang.Throwable]) =>
      toTryOptionMDValidationDataResults(p, message, nels.some),
    r = (mdValidationDataResults: Option[MagicDrawValidationDataResults]) =>
      Success(mdValidationDataResults)
    )
}