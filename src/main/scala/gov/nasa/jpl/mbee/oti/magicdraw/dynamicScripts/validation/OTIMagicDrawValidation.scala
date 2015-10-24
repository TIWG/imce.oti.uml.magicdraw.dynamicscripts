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
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.validation

import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.annotation.Annotation
import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.validation.{RuleViolationResult, ValidationRunData, ValidationSuiteHelper}
import com.nomagic.task.RunnableWithProgress
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Constraint, Element, EnumerationLiteral}
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults.ValidationAnnotationAction
import gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.actions.ExportAsJUnitResult

import org.omg.oti.uml.UMLError
import org.omg.oti.magicdraw.uml.read.{MagicDrawUMLUtil, MagicDrawUMLOps}

import scala.collection.JavaConversions._
import scala.language.{implicitConversions, postfixOps}
import scalaz._, Scalaz._
import scala.util.{Failure, Success, Try}

object OTIMagicDrawValidation {

  type MDValidationInfo = (Constraint, String, List[ValidationAnnotationAction])
}

case class OTIMagicDrawValidation(project: Project)(implicit mdUtil: MagicDrawUMLUtil) {

  import mdUtil._

  val mdOTIValidationSuite = {
    val s = MagicDrawValidationDataResults.lookupValidationSuite(project, "*::MagicDrawOTIValidation")
    require(
      s.isDefined,
      "Failed to find the MagicDraw validation suite package '*::MagicDrawOTIValidation'")
    s.get
  }

  val MD_OTI_ValidationConstraint_NotOTISpecificationRoot =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::NotOTISpecificationRoot")

  val MD_OTI_ValidationConstraint_UnresolvedCrossReference =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::UnresolvedCrossReference")

  // DefaultValueValidationStatus

  val MD_OTI_ValidationConstraint_InvalidDefaultValueForClassTypedParameterOrProperty =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDefaultValueForClassTypedParameterOrProperty")

  val MD_OTI_ValidationConstraint_InvalidDefaultValueForEnumerationTypedParameterOrProperty =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDefaultValueForEnumerationTypedParameterOrProperty")

  val MD_OTI_ValidationConstraint_InvalidDefaultValueForPrimitiveTypeTypedParameterOrProperty =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDefaultValueForPrimitiveTypeTypedParameterOrProperty")

  val MD_OTI_ValidationConstraint_InvalidDefaultValueForMultiValuedParameterOrProperty =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDefaultValueForMultiValuedParameterOrProperty")

  // MultiplicityValueValidationStatus

  val MD_OTI_ValidationConstraint_RedundantValue =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::RedundantValue")

  val MD_OTI_ValidationConstraint_InvalidValueAsUnlimitedNatural =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidValueAsUnlimitedNatural")

  val MD_OTI_ValidationConstraint_InvalidValueAsInteger =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidValueAsInteger")

  val MD_OTI_ValidationConstraint_InvalidValueAsString =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidValueAsString")

  val MD_OTI_ValidationConstraint_InvalidValueKind =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidValueKind")

  // NamedElementValidationStatus

  val MD_OTI_ValidationConstraint_InvalidUnnamedNamedElement =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidUnnamedNamedElement")

  // TypedElementValidationStatus

  val MD_OTI_ValidationConstraint_InvalidOperationRaisedExceptionNonClassType =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidOperationRaisedExceptionNonClassType")

  val MD_OTI_ValidationConstraint_InvalidTypedElementWithAssociationType =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidTypedElementWithAssociationType")

  val MD_OTI_ValidationConstraint_InvalidUntypedTypedElement =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidUntypedTypedElement")

  val MD_OTI_ValidationConstraint_InvalidDataTypePropertyAggregation =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDataTypePropertyAggregation")

  val MD_OTI_ValidationConstraint_InvalidDataTypePropertyWithNonDataTypeType =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidDataTypePropertyWithNonDataTypeType")

  val MD_OTI_ValidationConstraint_InvalidAssociationMemberEndPropertyNonClassType =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidAssociationMemberEndPropertyNonClassType")

  // VisibilityValidationStatus

  val MD_OTI_ValidationConstraint_MissingPublicVisibility =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::MissingPublicVisibility")

  val MD_OTI_ValidationConstraint_InvalidNonPublicVisibility =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidNonPublicVisibility")

  val MD_OTI_ValidationConstraint_InvalidAliasedElementImport =
    MagicDrawValidationDataResults.lookupValidationConstraint(
      mdOTIValidationSuite, "*::InvalidAliasedElementImport")


  val MD_OTI_ValidationConstraint_ =
    MagicDrawValidationDataResults.lookupValidationConstraint(
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
            Option.apply(mdOTIValidationSuite.vsh.getRuleRawMessage(c)) match {
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
            Option.apply(mdOTIValidationSuite.vsh.getRuleRawMessage(c)) match {
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
   * @param validationMessage Used for the title of the validation result window & shown in the MagicDraw message window
   * @param elementMessages Maps MagicDraw elements to a collection of validation constraint/message/actions
   * @param vSuite MagicDraw DynamicScript Validation Suite Information
   */
  def makeMDIllegalArgumentExceptionValidation
  (validationMessage: String,
   elementMessages: Map[Element, Iterable[OTIMagicDrawValidation.MDValidationInfo]],
   vSuite: MagicDrawValidationDataResults.ValidationSuiteInfo = mdOTIValidationSuite)
  : NonEmptyList[java.lang.Throwable] \/ Option[MagicDrawValidationDataResults] = {
    val app = Application.getInstance()
    val guiLog = app.getGUILog
    if (elementMessages.isEmpty) {
      guiLog.log(s"OK -- no violations of $validationMessage")
      Option.empty[MagicDrawValidationDataResults].right[NonEmptyList[java.lang.Throwable]]
    }
    else {

      val elements = elementMessages.keySet
      val results = elementMessages.values.flatten.toSeq
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
          Option.apply(vSuite.vsh.getRuleSeverityLevel(c))
        case (Some(severityLevel), (c, _, _)) =>
          Option.apply(vSuite.vsh.getRuleSeverityLevel(c)) match {
            case None =>
              Some(severityLevel)
            case Some(otherLevel) =>
              if (ValidationSuiteHelper.isSeverityHigherOrEqual(severityLevel, otherLevel))
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
          new ValidationRunData(vSuite.suite, false, elements, minimumLevel)
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
          MagicDrawValidationDataResults.makeMDIllegalArgumentExceptionValidation(
            p, message,
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
        MagicDrawValidationDataResults.makeMDIllegalArgumentExceptionValidation(
          p, message,
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