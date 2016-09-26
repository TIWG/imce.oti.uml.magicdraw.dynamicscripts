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

import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Constraint, Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.actions.SetPropertyAggregationToNone
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper
import org.omg.oti.magicdraw.uml.canonicalXMI.MagicDrawIDGenerator
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML
import org.omg.oti.uml.read.api._
import org.omg.oti.uml.validation._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.{None, Option, StringContext}
import scala.Predef.{ArrowAssoc, String, require}
import scalaz._
import Scalaz._
import scala.util.{Success, Try}

/**
* Validates all TypedElements in scope of the selected packages per MOF 2.5 well-formedness constraints
*
 * @see MOF 2.5, Section 12.4 EMOF Constraints
 *      [1] The type of Operation::raisedException is limited to be Class rather than Type.
 *      [22] A TypedElement cannot be typed by an Association.
 *      [23] A TypedElement other than a LiteralSpecification or an OpaqueExpression must have a Type.
 *      [28] A Property typed by a kind of DataType must have aggregation = none.
 *      [29] A Property owned by a DataType can only be typed by a DataType.
 *      [30] Each Association memberEnd Property must be typed by a Class.
  * @see MOF 2.5, Section 14.4 CMOF Constraints
 *      [2] The type of Operation::raisedException is limited to be Class rather than Type.
 *      [22] A TypedElement cannot be typed by an Association.
 *      [23] A TypedElement other than a LiteralSpecification or an OpaqueExpression must have a Type.
 *      [28] A Property typed by a kind of DataType must have aggregation = none.
 *      [29] A Property owned by a DataType can only be typed by a DataType.
 *      [30] Each Association memberEnd Property must be typed by a Class.
*/
object MOFTypedElementValidation {

  def doit
  ( p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    pkg: Profile, selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawAdapterForProfileCharacteristics(p),
    (oa: MagicDrawOTIProfileAdapter) => {

      val app = Application.getInstance()
      val guiLog = app.getGUILog
      guiLog.clearLog()

      implicit val umlOps = oa.umlOps
      import umlOps._

      val selectedPackages
      : Set[UMLPackage[MagicDrawUML]]
      = selection.toSet selectByKindOf { case p: Package => umlPackage(p) }

      doit(p, oa, selectedPackages)

    })

  def doit
  ( p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    top: Package, selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawAdapterForProfileCharacteristics(p),
    (oa: MagicDrawOTIProfileAdapter) => {

      val app = Application.getInstance()
      val guiLog = app.getGUILog
      guiLog.clearLog()

      implicit val umlOps = oa.umlOps
      import umlOps._

      val selectedPackages
      : Set[UMLPackage[MagicDrawUML]]
      = selection.toSet selectByKindOf { case p: Package => umlPackage(p) }

      doit(p, oa, selectedPackages)

    })

  def doit
  ( p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawAdapterForProfileCharacteristics(p),
    (oa: MagicDrawOTIProfileAdapter) => {

      val app = Application.getInstance()
      val guiLog = app.getGUILog
      guiLog.clearLog()

      implicit val umlOps = oa.umlOps
      import umlOps._

      val selectedPackages
      : Set[UMLPackage[MagicDrawUML]]
      = selection.toSet selectByKindOf { case pv: PackageView => umlPackage(getPackageOfView(pv).get) }

      doit(p, oa, selectedPackages)

    })

  def doit
  ( p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawAdapterForProfileCharacteristics(p),
    (oa: MagicDrawOTIProfileAdapter) => {

      val app = Application.getInstance()
      val guiLog = app.getGUILog
      guiLog.clearLog()

      implicit val umlOps = oa.umlOps
      import umlOps._

      val selectedPackages
      : Set[UMLPackage[MagicDrawUML]]
      = selection.toSet selectByKindOf { case pv: PackageView => umlPackage(getPackageOfView(pv).get) }

      doit(p, oa, selectedPackages)

    })

  def doit
  ( p: Project,
    oa: MagicDrawOTIProfileAdapter,
    pkgs: Set[UMLPackage[MagicDrawUML]] )
  : Try[Option[MagicDrawValidationDataResults]]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(p),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {

      implicit val umlOps = oa.umlOps
      import umlOps._

      val otiV = OTIMagicDrawValidation(p)

      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      implicit val otiCharacteristicsProvider = oa.otiCharacteristicsProvider

      val elementMessages = scala.collection.mutable.HashMap[
        Element,
        scala.collection.mutable.ArrayBuffer[OTIMagicDrawValidation.MDValidationInfo]]()

      for {
        v <- TypedElementValidationHelper.analyzePackageContents(pkgs)
        if TypedElementValidationStatus.ValidTypedElementStatus != v.status
        mdTE = umlMagicDrawUMLTypedElement(v.typedElement).getMagicDrawTypedElement
        vOptInfo <- {
          val vStatus: TypedElementValidationStatus.TypedElementValidationStatus = v.status
          val vResult
          : Try[Option[(Constraint, String, List[MagicDrawValidationDataResults.ValidationAnnotationAction])]]
          = vStatus match {
            case TypedElementValidationStatus.ValidTypedElementStatus =>
              Success(None)
            case TypedElementValidationStatus.InvalidOperationRaisedExceptionNonClassTypeStatus =>
              otiV.makeValidationInfo(
                otiV.MD_OTI_ValidationConstraint_InvalidOperationRaisedExceptionNonClassType,
                v.explanation,
                Nil)
            case TypedElementValidationStatus.InvalidTypedElementWithAssociationTypeStatus =>
              otiV.makeValidationInfo(
                otiV.MD_OTI_ValidationConstraint_InvalidTypedElementWithAssociationType,
                v.explanation,
                Nil)
            case TypedElementValidationStatus.InvalidUntypedTypedElementStatus =>
              otiV.makeValidationInfo(
                otiV.MD_OTI_ValidationConstraint_InvalidUntypedTypedElement,
                v.explanation,
                Nil)
            case TypedElementValidationStatus.InvalidDataTypePropertyAggregationStatus =>
              require(v.isRepairable)
              otiV.makeValidationInfo(
                otiV.MD_OTI_ValidationConstraint_InvalidDataTypePropertyAggregation,
                v.explanation,
                SetPropertyAggregationToNone() :: Nil)
            case TypedElementValidationStatus.InvalidDataTypePropertyWithNonDataTypeTypeStatus =>
              otiV.makeValidationInfo(
                otiV.MD_OTI_ValidationConstraint_InvalidDataTypePropertyWithNonDataTypeType,
                v.explanation,
                Nil)
            case TypedElementValidationStatus.InvalidAssociationMemberEndPropertyNonClassTypeStatus =>
              otiV.makeValidationInfo(
                otiV.MD_OTI_ValidationConstraint_InvalidAssociationMemberEndPropertyNonClassType,
                v.explanation,
                Nil)
          }
          vResult
        }
        vInfo <- vOptInfo
        validationInfo = elementMessages.getOrElseUpdate(
          mdTE, scala.collection.mutable.ArrayBuffer[OTIMagicDrawValidation.MDValidationInfo]())
      } validationInfo += vInfo

      val elementValidationMessages: Map[Element, Iterable[OTIMagicDrawValidation.MDValidationInfo]] =
        (for {tuple <- elementMessages} yield tuple._1 -> tuple._2.to[Seq]).toMap

      val validation =
        otiV.makeMDIllegalArgumentExceptionValidation(
          "EMOF [1,22,23,28,29,30] & CMOF [2,22,23,28,29,30] TypedElement Validation",
          elementValidationMessages)

      otiV.toTryOptionMagicDrawValidationDataResults(p, s"*** OTI MOF Typed Element Validation ***", validation)

    })
}