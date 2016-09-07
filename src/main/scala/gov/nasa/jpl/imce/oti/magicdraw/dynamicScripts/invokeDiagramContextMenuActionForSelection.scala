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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent
import java.lang.System

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.openapi.uml.SessionManager
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.InstanceSpecificationView
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, InstanceSpecification}
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes._
import gov.nasa.jpl.dynamicScripts._
import gov.nasa.jpl.dynamicScripts.magicdraw._
import gov.nasa.jpl.dynamicScripts.magicdraw.actions._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation

import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml.read.api._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Success, Try}
import scala.{Option,None,Some,StringContext}
import scala.Predef.{ArrowAssoc,String}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object invokeDiagramContextMenuActionForSelection {

  def doit
  (p: Project,
   ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: InstanceSpecificationView,
   triggerElement: InstanceSpecification,
   selection: java.util.Collection[PresentationElement])
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    val otiV = OTIMagicDrawValidation(p)

    val dsInstance = umlInstanceSpecification(triggerElement)
    for {
      comment <- dsInstance.ownedComment
      commentBody <- comment.body
      if commentBody == "selection"
      classNames <- dsInstance.getValuesOfFeatureSlot("className")
      methodNames <- dsInstance.getValuesOfFeatureSlot("methodName")
    } (classNames.toList, methodNames.toList) match {
      case (List(c: UMLLiteralString[Uml]), List(m: UMLLiteralString[Uml])) =>
        (c.value, m.value) match {
          case (Some(className), Some(methodName)) =>
            val invokeTriggerElement = comment.annotatedElement.head
            val invokeTriggerView =
              dpe.findPresentationElement(umlMagicDrawUMLElement(invokeTriggerElement).getMagicDrawElement, null)
            val peSelection = for {
              e <- comment.annotatedElement
              pe = dpe.findPresentationElement(umlMagicDrawUMLElement(e).getMagicDrawElement, null)
              _ = if (null == pe) {
                System.out.println(s"Invoking $className / $methodName: there is no presentation element for ${e.toolSpecific_id}")
              }
              if null != pe
            } yield pe

            return invoke(
              p, ev, triggerElement,
              className, methodName,
              dpe, invokeTriggerView, umlMagicDrawUMLElement(invokeTriggerElement).getMagicDrawElement, peSelection)
          case (_, _) =>
            ()
        }
      case (_, _) =>
        ()
    }

    for {
      vInfo <-
      otiV.constructValidationInfo(
        otiV.MD_OTI_ValidationConstraint_UnresolvedCrossReference,
        Some("Check the instance specification details"),
        Nil)
      validation =
      otiV.makeMDIllegalArgumentExceptionValidation(
        s"*** Ill-formed DiagramContextMenuActionForSelection instance specification ***",
        Map(triggerElement -> List(vInfo)))
      result <-
      otiV.toTryOptionMagicDrawValidationDataResults(p, "invokeDiagramContextMenuActionForSelection", validation)
    } yield result

  }

  def invoke
  (p: Project,
   ev: ActionEvent,
   invocation: InstanceSpecification,
   className: String, methodName: String,
   dpe: DiagramPresentationElement,
   invokeTriggerView: PresentationElement,
   invokeTriggerElement: Element,
   selection: Iterable[PresentationElement])
  (implicit umlUtil: MagicDrawUMLUtil)
  : Try[Option[MagicDrawValidationDataResults]] = {

    import umlUtil._

    val otiV = OTIMagicDrawValidation(p)

    val dsPlugin = DynamicScriptsPlugin.getInstance
    val actions = dsPlugin.getRelevantMetaclassActions(
    invokeTriggerElement.mofMetaclassName, {
      case d: DiagramContextMenuAction =>
        d.className.jname == className && d.methodName.sname == methodName
      case _ =>
        false
    })

    if (actions.size != 1) {
      for {
        vInfo <-
        otiV.constructValidationInfo(
          otiV.MD_OTI_ValidationConstraint_UnresolvedCrossReference,
          Some("Check the instance specification details"),
          Nil)
        validation =
        otiV.makeMDIllegalArgumentExceptionValidation(
          s"*** Ambiguous invocation; there are ${actions.size} relevant dynamic script actions matching the class/method name criteria ***",
          Map(invocation -> List(vInfo)))
        result <-
        otiV.toTryOptionMagicDrawValidationDataResults(p, "invokeDiagramContextMenuActionForSelection", validation)
      } yield result
    }
    else {
      val scripts = actions.head._2
      if (scripts.size != 1) {
        for {
          vInfo <-
          otiV.constructValidationInfo(
            otiV.MD_OTI_ValidationConstraint_UnresolvedCrossReference,
            Some("Check the instance specification details"),
            Nil)
          validation =
          otiV.makeMDIllegalArgumentExceptionValidation(
            s"*** Ambiguous invocation; there are ${actions.size} relevant dynamic script actions matching the class/method name criteria ***",
            Map(invocation -> List(vInfo)))
          result <-
          otiV.toTryOptionMagicDrawValidationDataResults(p, "invokeDiagramContextMenuActionForSelection", validation)
        } yield result
      }
      else scripts.head match {
        case d: DiagramContextMenuAction =>
          System.out.println(s"invokeTriggerView: $invokeTriggerView")
          System.out.println(s"invokeTriggerElement: $invokeTriggerElement")
          System.out.println(s"selection: ${selection.size}")
          selection.foreach { s =>
            System.out.println(s"- selected: $s")
          }
          val action = DynamicDiagramContextMenuActionForTriggerAndSelection(
            p, dpe,
            invokeTriggerView, invokeTriggerElement, selection,
            d, null, null)
          val sm = SessionManager.getInstance
          if (sm.isSessionCreated(p))
            sm.closeSession(p)

          action.actionPerformed(ev)
          Success(None)

        case d: DynamicActionScript =>
          for {
            vInfo <-
            otiV.constructValidationInfo(
              otiV.MD_OTI_ValidationConstraint_UnresolvedCrossReference,
              Some("Check the instance specification details"),
              Nil)
            validation = otiV.makeMDIllegalArgumentExceptionValidation(
              s"*** Invocation error: expected a DiagramContextMenuAction, got: ${d.prettyPrint("  ")}",
              Map(invocation -> List(vInfo)))
            result <-
            otiV.toTryOptionMagicDrawValidationDataResults(p, "invokeDiagramContextMenuActionForSelection", validation)
          } yield result
      }
    }
  }
}