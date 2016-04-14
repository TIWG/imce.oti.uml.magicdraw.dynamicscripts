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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation

import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement,PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.actions._
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper
import org.omg.oti.magicdraw.uml.canonicalXMI.MagicDrawIDGenerator
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML
import org.omg.oti.uml.read.api.UMLPackage
import org.omg.oti.uml.validation._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Failure,Success,Try}
import scala.{Option,None,Some,StringContext}
import scala.Predef.ArrowAssoc

/**
* Validates all MultiplicityElements in scope of the selected packages per MOF 2.5 well-formedness constraints
*
* @see MOF 2.5, Section 12.4 EMOF Constraints
* [32] The values of MultiplicityElement::lowerValue and upperValue must be
* of kind LiteralInteger and LiteralUnlimitedNatural respectively.
* @see MOF 2.5, Section 14.4 CMOF Constraints
* [14] The values of MultiplicityElement::lowerValue and upperValue must
* be of kind LiteralInteger and LiteralUnlimitedNatural respectively.
*/
object MOFMultiplicityValidation {

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
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(p),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {

      implicit val umlOps = oa.umlOps
      import umlOps._

      val otiV = OTIMagicDrawValidation(p)

      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      implicit val otiCharacteristicsProvider = oa.otiCharacteristicsProvider

      val elementMessages = scala.collection.mutable.HashMap[
        Element,
        scala.collection.mutable.ArrayBuffer[OTIMagicDrawValidation.MDValidationInfo]]()

      for {
        v <- ConnectableMultiplicityValidationHelper.analyzePackageContents(pkgs)
        if MultiplicityValueValidationStatus.ValidValueStatus != v.status
        mdPoP = umlMagicDrawUMLMultiplicityElement(v.parameter_or_property).getMagicDrawMultiplicityElement
        vOptInfo <- (v.status, v.value, v.valueRepair) match {
          case (MultiplicityValueValidationStatus.ValidValueStatus, _, _) =>
            Success(None)
          case (MultiplicityValueValidationStatus.RedundantValueStatus, Some(vDelete), _) =>
            val mdVDelete = umlMagicDrawUMLElement(vDelete).getMagicDrawElement
            otiV.makeValidationInfo(
              otiV.MD_OTI_ValidationConstraint_RedundantValue,
              Some(s"Delete redundant ${v.role.propertyName} value for ${v.parameter_or_property.qualifiedName.get}"),
              DeleteRedundantValue(v.role) :: Nil)
          case (_, Some(vDelete), Some(vRepair)) =>
            val mdVDelete = umlMagicDrawUMLElement(vDelete).getMagicDrawElement
            if (MultiplicityElement_lowerValue == v.role)
              otiV.makeValidationInfo(
                otiV.MD_OTI_ValidationConstraint_InvalidValueAsInteger,
                Some(s"Replace lower value for ${v.parameter_or_property.qualifiedName.get} with $vRepair"),
                ReplaceLowerIntegerValue(vDelete.xmiType.head, vRepair) :: Nil)
            else
              otiV.makeValidationInfo(
                otiV.MD_OTI_ValidationConstraint_InvalidValueAsUnlimitedNatural,
                Some(s"Replace upper value for ${v.parameter_or_property.qualifiedName.get} with $vRepair"),
                ReplaceUpperUnlimitedNaturalValue(vDelete.xmiType.head, vRepair) :: Nil)
          case (MultiplicityValueValidationStatus.InvalidValueAsIntegerStatus, _, _) =>
            otiV.makeValidationInfo(
              otiV.MD_OTI_ValidationConstraint_InvalidValueAsInteger,
              v.explanation,
              Nil)
          case (MultiplicityValueValidationStatus.InvalidValueAsStringStatus, _, _) =>
            otiV.makeValidationInfo(
              otiV.MD_OTI_ValidationConstraint_InvalidValueAsString,
              v.explanation,
              Nil)
          case (MultiplicityValueValidationStatus.InvalidValueAsUnlimitedNaturalStatus, _, _) =>
            otiV.makeValidationInfo(
              otiV.MD_OTI_ValidationConstraint_InvalidValueAsUnlimitedNatural,
              v.explanation,
              Nil)
          case (MultiplicityValueValidationStatus.InvalidValueKindStatus, _, _) =>
            otiV.makeValidationInfo(
              otiV.MD_OTI_ValidationConstraint_InvalidValueKind,
              v.explanation,
              Nil)
          case _ =>
            Success(None)
        }
        vInfo <- vOptInfo
        validationInfo = elementMessages.getOrElseUpdate(
          mdPoP, scala.collection.mutable.ArrayBuffer[OTIMagicDrawValidation.MDValidationInfo]())
      } validationInfo += vInfo

      val elementValidationMessages: Map[Element, Iterable[OTIMagicDrawValidation.MDValidationInfo]] =
        (for {tuple <- elementMessages} yield tuple._1 -> tuple._2.to[Seq]).toMap

      val validation =
        otiV.makeMDIllegalArgumentExceptionValidation(
          "EMOF [32] & CMOF [14] Multiplicity Validation",
          elementValidationMessages)
      otiV.toTryOptionMagicDrawValidationDataResults(p, "MOF MultiplicityElement Validation", validation)

    })
}