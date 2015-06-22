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
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent

import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import org.omg.oti.api._
import org.omg.oti.magicdraw.{MagicDrawUML, MagicDrawUMLUtil}
import org.omg.oti.validation._

import scala.collection.JavaConversions._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Success, Try}

/**
* @See MOF 2.5, Section 12.4 EMOF Constraints
* [32] The values of MultiplicityElement::lowerValue and upperValue must be
* of kind LiteralInteger and LiteralUnlimitedNatural respectively.
*/
object fixMultiplicities {

  def doit(
            p: Project, ev: ActionEvent,
            script: DynamicScriptsTypes.BrowserContextMenuAction,
            tree: Tree, node: Node,
            pkg: Profile, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection.toIterator selectByKindOf { case p: Package => umlPackage( p ) } toSet

    doit(p, selectedPackages)
  }

  def doit(
            p: Project, ev: ActionEvent,
            script: DynamicScriptsTypes.BrowserContextMenuAction,
            tree: Tree, node: Node,
            top: Package, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection.toIterator selectByKindOf { case p: Package => umlPackage( p ) } toSet

    doit(p, selectedPackages)
  }

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    doit(
      p,
      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( pv.getPackage ) } )
  }
  
  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    doit(
      p,
      selection.toSet selectByKindOf { case pv: PackageView => umlPackage( pv.getPackage ) } )
  }

  def doit(
    p: Project,
    pkgs: Iterable[UMLPackage[MagicDrawUML]] )
          ( implicit _umlUtil: MagicDrawUMLUtil ): Try[Option[MagicDrawValidationDataResults]] = {

    import _umlUtil._

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val mem = ModelElementsManager.getInstance

    val elementMessages: Iterable[(Element, ( String, List[NMAction] ))] = for {
      v <- ConnectableMultiplicityValidationHelper.
        analyzePackageContents(pkgs).toList.sortBy(_.parameter_or_property.qualifiedName.get)
      if MultiplicityValueValidationStatus.ValidValueStatus != v.status
      mdPoP = umlMagicDrawUMLMultiplicityElement(v.parameter_or_property).getMagicDrawMultiplicityElement
      elementMessage <- (v.status, v.isInvalid, v.value, v.valueRepair) match {
        case ( MultiplicityValueValidationStatus.ValidValueStatus, _, _, _ ) =>
          None
        case ( MultiplicityValueValidationStatus.RedundantValueStatus, _, Some(vDelete), _) =>
          val mdVDelete = umlMagicDrawUMLElement(vDelete).getMagicDrawElement
          Some(
            mdPoP ->
              Tuple2(
                s"Delete redundant ${v.role.propertyName} value for ${v.parameter_or_property.qualifiedName.get}",
                actions.DeleteRedundantValue(v.role) :: Nil) )
        case ( _, true, Some(vDelete), Some(vRepair)) =>
          val mdVDelete = umlMagicDrawUMLElement(vDelete).getMagicDrawElement
          if (MultiplicityElement_lowerValue == v.role)
            Some(
              mdPoP ->
                Tuple2(
                  s"Replace lower value for ${v.parameter_or_property.qualifiedName.get} with $vRepair",
                  actions.ReplaceLowerIntegerValue(vDelete.xmiType.head, vRepair) :: Nil))
          else
            Some(
              mdPoP ->
                Tuple2(
                  s"Replace upper value for ${v.parameter_or_property.qualifiedName.get} with $vRepair",
                  actions.ReplaceUpperUnlimitedNaturalValue(vDelete.xmiType.head, vRepair) :: Nil))
        case _ =>
          System.out.println(
            s"""Unrecognized ${v.role.propertyName} combination:
               |${v.parameter_or_property.qualifiedName.get} ${v.status}, ${v.isInvalid}, ${v.value}, ${v.valueRepair}
             """.stripMargin)
          None
      }
    } yield elementMessage


    if ( elementMessages.isEmpty ) {
      guiLog.log( s"OK -- no violations of EMOF [32] multiplicity range well-formedness" )
      Success( None )
    }
    else {

      val (repairable, nonRepairable) = elementMessages partition { case (_, (_, actions)) => actions.nonEmpty }

      val n = elementMessages.size
      val nFixable = repairable.size
      val nNonFixable = nonRepairable.size

      val summary =
        if (n == nFixable)
          s"$n fixable violations of EMOF [32] multiplicity range well-formedness"
        else
          s"""$n violations of EMOF [32] multiplicity range well-formedness
             | ($nFixable fixable / $nNonFixable require user repair)
           """.stripMargin

      val elementMessageActionGroups: Map[Element, Iterable[(Element, (String, List[NMAction]))]] =
        elementMessages.groupBy(_._1)

      val elementMessageActionPairs = for {
        (element, group) <- elementMessageActionGroups
        message: String = group.map(_._2).map { case (m: String, _: List[NMAction]) => m }.mkString("\n")
        actions: List[NMAction] = group.map(_._2).flatMap { case (_: String, as: List[NMAction]) => as } toList
      } yield element -> Tuple2(message, actions)

      val elementMessageActionMap = elementMessageActionPairs.toMap[Element, ( String, List[NMAction] )]

      guiLog.log( summary )
      makeMDIllegalArgumentExceptionValidation(
        p,
        summary,
        elementMessageActionMap,
        "*::MagicDrawOTIValidation",
        "*::InvalidMOFMultiplicity" )
    }
  }
}