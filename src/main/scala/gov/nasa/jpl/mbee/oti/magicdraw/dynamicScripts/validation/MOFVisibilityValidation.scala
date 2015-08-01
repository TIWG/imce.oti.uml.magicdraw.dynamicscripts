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

import java.awt.event.ActionEvent

import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.actions._
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml.read.api._
import org.omg.oti.uml.validation._

import scala.collection.JavaConversions._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Success, Try}

/**
* Validates all TypedElements in scope of the selected packages per MOF 2.5 well-formedness constraints
*
 * @see MOF 2.5, Section 12.4 EMOF Constraints
 *      [4] Core::Basic and EMOF does not support visibilities.
 *      All property visibilities must be explicitly set to public where applicable,
 *      that is for all NamedElements, ElementImports and PackageImports.
 *      Furthermore, no alias is allowed for any ElementImport.
 *
 * @see MOF 2.5, Section 14.4 CMOF Constraints
 *      [7] CMOF does not support visibilities.
 *      All property visibilities must be explicitly set to public where applicable,
 *      that is for all NamedElements, ElementImports, and PackageImports.
 *      Furthermore, no alias is allowed for any ElementImport.
*/
object MOFVisibilityValidation {

  def doit
  ( p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    pkg: Profile, selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection.toIterator selectByKindOf { case p: Package => umlPackage( p ) } toSet

    doit(p, selectedPackages)
  }

  def doit
  ( p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    top: Package, selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection.toIterator selectByKindOf { case p: Package => umlPackage( p ) } toSet

    doit(p, selectedPackages)
  }

  def doit
  ( p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    doit(
      p,
      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( pv.getPackage ) } )
  }

  def doit
  ( p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    doit(
      p,
      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( pv.getPackage ) } )
  }

  def doit
  ( p: Project,
    pkgs: Iterable[UMLPackage[MagicDrawUML]] )
  ( implicit _umlUtil: MagicDrawUMLUtil )
  : Try[Option[MagicDrawValidationDataResults]] = {

    import _umlUtil._

    val otiV = OTIMagicDrawValidation(p)

    val elementMessages = scala.collection.mutable.HashMap[
      Element,
      scala.collection.mutable.ArrayBuffer[OTIMagicDrawValidation.MDValidationInfo]]()

    for {
      v <- VisibilityValidationHelper.analyzePackageContents(pkgs)
      if VisibilityValidationStatus.ValidVisibilityStatus != v.status
      mdE = umlMagicDrawUMLElement(v.e).getMagicDrawElement
      vOptInfo <- v.status match {
        case VisibilityValidationStatus.ValidVisibilityStatus =>
          Success(None)
        case VisibilityValidationStatus.MissingPublicVisibilityStatus =>
          require(v.isRepairable)
          otiV.makeValidationInfo(
            otiV.MD_OTI_ValidationConstraint_MissingPublicVisibility,
            v.explanation,
            SetVisibilityToPublic() :: Nil)
        case VisibilityValidationStatus.InvalidNonPublicVisibilityStatus =>
          require(v.isRepairable)
          otiV.makeValidationInfo(
            otiV.MD_OTI_ValidationConstraint_InvalidNonPublicVisibility,
            v.explanation,
            SetVisibilityToPublic() :: Nil)
        case VisibilityValidationStatus.InvalidAliasedElementImportStatus =>
          require(!v.isRepairable)
          otiV.makeValidationInfo(
            otiV.MD_OTI_ValidationConstraint_InvalidAliasedElementImport,
            v.explanation,
            Nil)
      }
      vInfo <- vOptInfo
      validationInfo = elementMessages.getOrElseUpdate(
        mdE, scala.collection.mutable.ArrayBuffer[OTIMagicDrawValidation.MDValidationInfo]())
    } validationInfo += vInfo

    otiV.makeMDIllegalArgumentExceptionValidation(
      "EMOF [4] & CMOF [7] Visibility Validation",
      elementMessages.toMap)

  }
}