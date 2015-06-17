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

import scala.collection.JavaConversions.asJavaCollection
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.util.Success
import scala.util.Try

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.openapi.uml.SessionManager
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.magicdraw.uml.symbols.PresentationElement
import com.nomagic.magicdraw.uml.symbols.shapes.InstanceSpecificationView
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification

import org.omg.oti.api._
import org.omg.oti.magicdraw._

import gov.nasa.jpl.dynamicScripts._
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes._
import gov.nasa.jpl.dynamicScripts.magicdraw._
import gov.nasa.jpl.dynamicScripts.magicdraw.actions._

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object invokeDiagramContextMenuActionForSelection {

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

    val dsInstance = umlInstanceSpecification( triggerElement )
    for {
      comment <- dsInstance.ownedComment
      commentBody <- comment.body
      if commentBody == "selection"
      classNames <- dsInstance.getValuesOfFeatureSlot( "className" )
      methodNames <- dsInstance.getValuesOfFeatureSlot( "methodName" )
    } ( classNames.toList, methodNames.toList ) match {
      case ( List( c: UMLLiteralString[Uml] ), List( m: UMLLiteralString[Uml] ) ) =>
        ( c.value, m.value ) match {
          case ( Some( className ), Some( methodName ) ) =>
            val invokeTriggerElement = comment.annotatedElement.head
            val invokeTriggerView = dpe.findPresentationElement( umlMagicDrawUMLElement(invokeTriggerElement).getMagicDrawElement, null )
            val peSelection = for {
              e <- comment.annotatedElement
              pe = dpe.findPresentationElement( umlMagicDrawUMLElement(e).getMagicDrawElement, null )
            } yield pe

            return invoke(
              p, ev, triggerElement,
              className, methodName,
              dpe, invokeTriggerView, umlMagicDrawUMLElement(invokeTriggerElement).getMagicDrawElement, peSelection )
          case ( _, _ ) =>
            ()
        }
      case ( _, _ ) =>
        ()
    }

    makeMDIllegalArgumentExceptionValidation(
      p,
      s"*** Ill-formed DiagramContextMenuActionForSelection instance specification ***",
      Map( triggerElement -> Tuple2( "Check the instance specification details", List() ) ),
      "*::MagicDrawOTIValidation",
      "*::UnresolvedCrossReference" )
  }

  def invoke(
    p: Project,
    ev: ActionEvent,
    invocation: InstanceSpecification,
    className: String, methodName: String,
    dpe: DiagramPresentationElement,
    invokeTriggerView: PresentationElement,
    invokeTriggerElement: Element,
    selection: Iterable[PresentationElement] )( implicit umlUtil: MagicDrawUMLUtil ): Try[Option[MagicDrawValidationDataResults]] = {

    import umlUtil._

    val dsPlugin = DynamicScriptsPlugin.getInstance
    val actions = dsPlugin.getRelevantMetaclassActions(
      invokeTriggerElement.mofMetaclassName,
      {
        case d: DiagramContextMenuAction =>
          d.className.jname == className && d.methodName.sname == methodName
        case _ =>
          false
      } )

    if ( actions.size != 1 )
      makeMDIllegalArgumentExceptionValidation(
        p,
        s"*** Ambiguous invocation; there are ${actions.size} relevant dynamic script actions matching the class/method name criteria ***",
        Map( invocation -> Tuple2( "Check the instance specification details", List() ) ),
        "*::MagicDrawOTIValidation",
        "*::UnresolvedCrossReference" )

    else {
      val scripts = actions.head._2
      if ( scripts.size != 1 )
        makeMDIllegalArgumentExceptionValidation(
          p,
          s"*** Ambiguous invocation; there are ${scripts.size} relevant dynamic script actions matching the class/method name criteria ***",
          Map( invocation -> Tuple2( "Check the instance specification details", List() ) ),
          "*::MagicDrawOTIValidation",
          "*::UnresolvedCrossReference" )

      else scripts.head match {
        case d: DiagramContextMenuAction =>
          val action = DynamicDiagramContextMenuActionForTriggerAndSelection(
            p, dpe,
            invokeTriggerView, invokeTriggerElement, selection,
            d, null, null )
          val sm = SessionManager.getInstance
          if ( sm.isSessionCreated(p) )
            sm.closeSession(p)
            
          action.actionPerformed( ev )
          Success( None )

        case d: DynamicActionScript =>
          makeMDIllegalArgumentExceptionValidation(
            p,
            s"*** Invocation error: expected a DiagramContextMenuAction, got: ${d.prettyPrint( "  " )}",
            Map( invocation -> Tuple2( "Check the instance specification details", List() ) ),
            "*::MagicDrawOTIValidation",
            "*::UnresolvedCrossReference" )
      }
    }
  }
}