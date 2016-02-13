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
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}
import org.omg.oti.uml.read.api.UMLPackage
import org.omg.oti.uml.validation.{DefaultValueValidationHelper, DefaultValueValidationStatus}

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Success,Try}
import scala.Predef.{ArrowAssoc}
import scala.{Option,None}
/**
* Validates all TypedElements in scope of the selected packages per MOF 2.5 well-formedness constraints
*
 * @see MOF 2.5, Section 12.4 EMOF Constraints
 *      [24] A TypedElement that is a kind of Parameter or Property typed by a Class cannot have a default value.
  *     [25] For a TypedElement that is a kind of Parameter or Property typed by an Enumeration, the defaultValue,
  *           if any, must be a kind of InstanceValue.
  *     [26] For a TypedElement that is a kind of Parameter or Property typed by a PrimitiveType, the defaultValue,
  *           if any, must be a kind of LiteralSpecification.
  *     [31] A multi-valued Property or Parameter cannot have a default value.
 * @see MOF 2.5, Section 14.4 CMOF Constraints
 *      [13] A multi-valued Property or Parameter cannot have a default value.
  *           The default value of a Property or Parameter typed by a PrimitiveType must be a kind of LiteralSpecification.
  *           The default value of a Property or Parameter typed by an Enumeration must be a kind of InstanceValue.
  *           A Property or Parameter typed by a Class cannot have a default value.
  *     [24] A TypedElement that is a kind of Parameter or Property typed by a Class cannot have a default value.
  *     [25] For a TypedElement that is a kind of Parameter or Property typed by an Enumeration,
  *           the defaultValue, if any, must be a kind of InstanceValue.
  *     [26] For a TypedElement that is a kind of Parameter or Property typed by an PrimitiveType,
  *           the defaultValue, if any, must be a kind of LiteralSpecification.
*/
object MOFDefaultValueValidation {

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.BrowserContextMenuAction,
   tree: Tree, node: Node,
   pkg: Profile, selection: java.util.Collection[Element])
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection
      .toIterable
      .selectByKindOf { case p: Package => umlPackage(p) }
      .to[Set]

    doit(p, selectedPackages)
  }

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.BrowserContextMenuAction,
   tree: Tree, node: Node,
   top: Package, selection: java.util.Collection[Element])
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection
        .toIterable
        .selectByKindOf { case p: Package => umlPackage(p) }
        .to[Set]

    doit(p, selectedPackages)
  }

  def doit
  (p: Project,
   ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Profile,
   selection: java.util.Collection[PresentationElement])
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    doit(
      p,
      selection.toSet selectByKindOf { case pv: PackageView => umlPackage(getPackageOfView(pv).get) })
  }

  def doit
  ( p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement])
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    doit(
      p,
      selection.toSet selectByKindOf { case pv: PackageView => umlPackage(getPackageOfView(pv).get) })
  }

  def doit
  (p: Project,
   pkgs: Iterable[UMLPackage[MagicDrawUML]])
  (implicit _umlUtil: MagicDrawUMLUtil)
  : Try[Option[MagicDrawValidationDataResults]] = {

    import _umlUtil._

    val otiV = OTIMagicDrawValidation(p)

    val elementMessages = scala.collection.mutable.HashMap[
      Element,
      scala.collection.mutable.ArrayBuffer[OTIMagicDrawValidation.MDValidationInfo]]()

    for {
      v <- DefaultValueValidationHelper.analyzePackageContents(pkgs)
      if DefaultValueValidationStatus.ValidDefaultValueStatus != v.status
      mdE = umlMagicDrawUMLElement(v.e).getMagicDrawElement
      vOptInfo <- v.status match {
        case DefaultValueValidationStatus.ValidDefaultValueStatus =>
          Success(None)
        case DefaultValueValidationStatus.InvalidDefaultValueForClassTypedParameterOrPropertyStatus =>
          otiV.makeValidationInfo(
            otiV.MD_OTI_ValidationConstraint_InvalidDefaultValueForClassTypedParameterOrProperty,
            v.explanation,
            Nil)
        case DefaultValueValidationStatus.InvalidDefaultValueForEnumerationTypedParameterOrPropertyStatus =>
          otiV.makeValidationInfo(
            otiV.MD_OTI_ValidationConstraint_InvalidDefaultValueForEnumerationTypedParameterOrProperty,
            v.explanation,
            Nil)
        case DefaultValueValidationStatus.InvalidDefaultValueForPrimitiveTypeTypedParameterOrPropertyStatus =>
          otiV.makeValidationInfo(
            otiV.MD_OTI_ValidationConstraint_InvalidDefaultValueForPrimitiveTypeTypedParameterOrProperty,
            v.explanation,
            Nil)
        case DefaultValueValidationStatus.InvalidDefaultValueForMultiValuedParameterOrPropertyStatus =>
          otiV.makeValidationInfo(
            otiV.MD_OTI_ValidationConstraint_InvalidDefaultValueForMultiValuedParameterOrProperty,
            v.explanation,
            Nil)
      }
      vInfo <- vOptInfo
      validationInfo = elementMessages.getOrElseUpdate(
        mdE, scala.collection.mutable.ArrayBuffer[OTIMagicDrawValidation.MDValidationInfo]())
    } validationInfo += vInfo

    val elementValidationMessages: Map[Element, Iterable[OTIMagicDrawValidation.MDValidationInfo]] =
      (for { tuple <- elementMessages } yield tuple._1 -> tuple._2.to[Seq]).toMap

    val validation =
      otiV.makeMDIllegalArgumentExceptionValidation(
        "EMOF [24,25,26,31] & CMOF [13,24,25,26] DefaultValue Validation",
        elementValidationMessages)
    otiV.toTryOptionMagicDrawValidationDataResults(p, "MOF Default Value Validation", validation)
  }
}