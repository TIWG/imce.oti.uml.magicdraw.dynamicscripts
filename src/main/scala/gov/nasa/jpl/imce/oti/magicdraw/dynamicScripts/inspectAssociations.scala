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

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.paths.AssociationView
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Association, Element}
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}
import org.omg.oti.uml.read.api._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Success, Try}
import scala.{Option,None,Some,StringContext,Unit}
/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object inspectAssociations {

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: AssociationView,
    triggerElement: Association,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {
    
    val app = Application.getInstance()
    val guiLog = app.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    val selectedAssociations =
      selection
      .toIterable
      .selectByKindOf { case av: AssociationView =>
        umlAssociation( getAssociationOfView(av).get )
      }
      .to[List]
    
    selectedAssociations foreach { a =>      
      a.memberEnd.toList match {
        case end1 :: end2 :: Nil => inspectAssociation( umlUtil, a, end1, end2 )
        case _                   => ()
      }
    }

    guiLog.log("- Done")
    Success( None )
  }
      
      
  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    top: Association,
    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {

    val app = Application.getInstance()
    val guiLog = app.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedAssociations =
      selection
      .toIterable
      .selectByKindOf { case a: Uml#Association => umlAssociation( a ) }
      .to[List]

    selectedAssociations foreach { a =>
      a.ownedEnd.toList match {
        case end1 :: end2 :: Nil => inspectAssociation( umlUtil, a, end1, end2 )
        case _                   => ()
      }
    }

    Success( None )
  }

  def inspectAssociation
  ( umlUtil: MagicDrawUMLUtil,
    a: UMLAssociation[MagicDrawUML],
    end1: UMLProperty[MagicDrawUML],
    end2: UMLProperty[MagicDrawUML] )
  : Unit = {
    val app = Application.getInstance()
    val guiLog = app.getGUILog()

    guiLog.log( s" association: ${a.qualifiedName.get}")

    val mdA = umlUtil.umlMagicDrawUMLAssociation(a)

    guiLog.log( s" md association owner: ${mdA.getMagicDrawElement.eContainingFeature}")
    a.getDirectedAssociationEnd match {
      case None =>
        guiLog.log( "Not a directed association! " )
        guiLog.log( s" end1: ${end1.qualifiedName.get}")
        guiLog.log( s" end2: ${end2.qualifiedName.get}")

      case Some( ( sourceEnd, targetEnd ) ) =>
        guiLog.log( s" sourceEnd: ${sourceEnd.qualifiedName} ")
        guiLog.log( s" source ID: ${sourceEnd.toolSpecific_id} ")
        guiLog.log(s"source type: ${sourceEnd._type.get.qualifiedName.get}")
        guiLog.log( s" targetEnd: ${targetEnd.qualifiedName} ")
        guiLog.log( s" target ID: ${targetEnd.toolSpecific_id} ")
        guiLog.log(s"target type: ${targetEnd._type.get.qualifiedName.get}")
        
    }
  }
}