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
import java.lang.IllegalArgumentException

import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.browser.Node
import com.nomagic.magicdraw.ui.browser.Tree
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.magicdraw.uml.symbols.paths.AssociationView
import com.nomagic.magicdraw.uml.symbols.PresentationElement

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Success, Try}
import scala.{Option,None,Some,StringContext,Unit}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object nameAssociationLinkInstancesWithinPackageExtent {

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: AssociationView,
    triggerElement: Association,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {
    
    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    val selectedAssociations = 
      selection
      .toIterable
      .selectByKindOf { case av: AssociationView => umlAssociation( getAssociationOfView(av).get ) }
      .to[List]
    
    selectedAssociations foreach ( nameAssociationLinkInstances( umlUtil, _ ) )

    Success( None )
  }
  
  def doit
  (p: Project,
   ev: ActionEvent,
   script: DynamicScriptsTypes.BrowserContextMenuAction, 
   tree: Tree,
   node: Node,
   top: Association,
   selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedAssociations = 
      selection
      .toIterable
      .selectByKindOf { case a: Uml#Association => umlAssociation( a ) }

    selectedAssociations foreach ( nameAssociationLinkInstances( umlUtil, _ ) )

    Success( None )
  }

  def nameAssociationLinkInstances
  ( umlUtil: MagicDrawUMLUtil, a: UMLAssociation[MagicDrawUML] )
  : Unit = {
    import umlUtil._
    val app = Application.getInstance()
    val guiLog = app.getGUILog()

    a.getDirectedAssociationEnd match {
      case None =>
        guiLog.log( "Not a directed association! " )

      case Some( ( sourceEnd, targetEnd ) ) =>
        guiLog.log( s" association: ${a.qualifiedName.get}" )

        val links = a.classifier_instanceSpecification.toList
        guiLog.log( s" Refactor ${links.size} instance specifications..." )

        var count = 0
        val prefix = a.name.get + "("
        links foreach { link =>
          val slots = link.slot
          val sourceSlot = 
            slots
            .find( _.definingFeature == Some( sourceEnd ) )
            .getOrElse { 
              throw new IllegalArgumentException( 
                s"Broken Link ${a.name.get} from '${sourceEnd.name}' to '${targetEnd.name}'" ) 
            }
          val sourceInstance = sourceSlot.value.head match {
            case iv: UMLInstanceValue[Uml] => iv.instance.get
          }
          val targetSlot = 
            slots
            .find( _.definingFeature == Some( targetEnd ) )
            .getOrElse { 
              throw new IllegalArgumentException( 
                s"Broken Link ${a.name.get} from '${sourceEnd.name}' to '${targetEnd.name}'" ) 
            }
          val targetInstance = targetSlot.value.head match {
            case iv: UMLInstanceValue[Uml] => iv.instance.get
          }
          ( sourceInstance.name, targetInstance.name ) match {
            case ( Some( sName ), Some( tName ) ) =>
              val linkName = prefix + sName + "," + tName + ")"
              link.name match {
                case Some( lName ) if ( lName == linkName ) => 
                  guiLog.log( s" Link (id=${link.toolSpecific_id}) - already named!: '${linkName}'")
                  ()
                case _ =>
                  guiLog.log( s" Link (id=${link.toolSpecific_id}) set name to: '${linkName}'")
                  umlMagicDrawUMLInstanceSpecification(link).getMagicDrawInstanceSpecification.setName( linkName )
                  count = count + 1
              }
            case ( Some( sName ), None ) => 
                  guiLog.log( s" Link (id=${link.toolSpecific_id}) - source named: '${sName}' but target is unnamed! (id=${targetInstance.toolSpecific_id})")
            case ( None, Some( tName ) ) => 
                  guiLog.log( s" Link (id=${link.toolSpecific_id}) - target named: '${tName}' but source is unnamed! (id=${sourceInstance.toolSpecific_id})")
            case ( None, None ) =>
                  guiLog.log( s" Link (id=${link.toolSpecific_id}) - source is unnamed: (id=${sourceInstance.toolSpecific_id}) target is unnamed: (id=${sourceInstance.toolSpecific_id})")
          }
        }

        guiLog.log( s"Done (renamed ${count} links) !" )
    }
  }
}