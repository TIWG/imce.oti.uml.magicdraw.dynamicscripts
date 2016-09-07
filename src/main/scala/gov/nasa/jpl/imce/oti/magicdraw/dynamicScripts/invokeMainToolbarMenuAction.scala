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

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.openapi.uml.SessionManager
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.magicdraw.uml.symbols.PresentationElement
import com.nomagic.magicdraw.uml.symbols.shapes.InstanceSpecificationView
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation

import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read._

import gov.nasa.jpl.dynamicScripts._
import gov.nasa.jpl.dynamicScripts.magicdraw._
import gov.nasa.jpl.dynamicScripts.magicdraw.actions._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

import scala.collection.immutable._
import scala.util.{Success, Try}
import scala.{Option,None,Some,StringContext}
import scala.Predef.{ArrowAssoc,String}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object invokeMainToolbarMenuAction {

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: InstanceSpecificationView,
    triggerElement: InstanceSpecification,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val otiV = OTIMagicDrawValidation(p)

    val dsInstance = umlInstanceSpecification( triggerElement )
    for {
      classNames <- dsInstance.getValuesOfFeatureSlot( "className" )
      methodNames <- dsInstance.getValuesOfFeatureSlot( "methodName" )
    } ( classNames.toList, methodNames.toList ) match {
      case ( List( c: UMLLiteralString[Uml] ), List( m: UMLLiteralString[Uml] ) ) =>
        ( c.value, m.value ) match {
          case ( Some( className ), Some( methodName ) ) =>
            return invoke(
              p, ev, triggerElement,
              className, methodName )
          case ( _, _ ) =>
            ()
        }
      case ( _, _ ) =>
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
      otiV.toTryOptionMagicDrawValidationDataResults(p, "invokeMainToolbarMenuAction", validation)
    } yield result

  }

  def invoke
  ( p: Project,
    ev: ActionEvent,
    invocation: InstanceSpecification,
    className: String, methodName: String )
  ( implicit umlUtil: MagicDrawUMLUtil )
  : Try[Option[MagicDrawValidationDataResults]] = {

    val otiV = OTIMagicDrawValidation(p)

    val dsPlugin = DynamicScriptsPlugin.getInstance()
    val reg: DynamicScriptsRegistry = dsPlugin.getDynamicScriptsRegistry

    val scripts = for {
      ( _, menus ) <- reg.toolbarMenuPathActions
      menu <- menus
      script <- menu.scripts
      if script.className.jname == className && script.methodName.sname == methodName
    } yield script

    if ( scripts.size != 1 )
      for {
        vInfo <-
        otiV.constructValidationInfo(
          otiV.MD_OTI_ValidationConstraint_UnresolvedCrossReference,
          Some("Check the instance specification details"),
          Nil)
        validation =
        otiV.makeMDIllegalArgumentExceptionValidation(
          s"*** Ambiguous invocation; there are ${scripts.size} relevant dynamic script actions matching the class/method name criteria ***",
          Map(invocation -> List(vInfo)))
        result <-
        otiV.toTryOptionMagicDrawValidationDataResults(p, "invokeMainToolbarMenuAction", validation)
      } yield result

    else {
      val script = scripts.head
      val action = DynamicScriptsLaunchToolbarMenuAction( script, script.name.hname )

      val sm = SessionManager.getInstance
      if ( sm.isSessionCreated( p ) )
        sm.closeSession( p )

      action.actionPerformed( ev )
      Success( None )
    }
  }
}