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
import scala.collection.JavaConversions._
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.util.Success
import scala.util.Try
import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.browser.Node
import com.nomagic.magicdraw.ui.browser.Tree
import com.nomagic.magicdraw.uml.actions.SelectInContainmentTreeRunnable
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import org.omg.oti.magicdraw.MagicDrawUMLUtil
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import org.omg.oti.changeMigration.Metamodel
import com.nomagic.magicdraw.core.ApplicationEnvironment
import java.io.File
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.xmi.XMLResource
import scala.util.Failure
import com.nomagic.magicdraw.uml.UUIDRegistry
import com.nomagic.magicdraw.core.utils.ChangeElementID
import com.nomagic.task.RunnableWithProgress
import com.nomagic.task.ProgressStatus
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.core.ProjectUtilitiesInternal
import java.util.UUID
import com.nomagic.ci.persistence.local.spi.localproject.LocalPrimaryProject

import org.omg.oti.api._
import org.omg.oti.magicdraw.MagicDrawUML
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.magicdraw.uml.symbols.paths.AssociationView
import com.nomagic.magicdraw.uml.symbols.PresentationElement

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
    val guiLog = app.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    val selectedAssociations = selection.toIterator selectByKindOf ( { case a: AssociationView => umlAssociation( a.getAssociation ) } ) toList;
    
    selectedAssociations foreach ( nameAssociationLinkInstances( umlUtil, _ ) )

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

    val a = Application.getInstance()
    val guiLog = a.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedAssociations = selection.toIterator selectByKindOf ( { case a: Uml#Association => umlAssociation( a ) } )

    selectedAssociations foreach ( nameAssociationLinkInstances( umlUtil, _ ) )

    Success( None )
  }

  def nameAssociationLinkInstances( umlUtil: MagicDrawUMLUtil, a: UMLAssociation[MagicDrawUML] ): Unit = {
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
          val sourceSlot = slots.find( _.definingFeature == Some( sourceEnd ) ) getOrElse { throw new IllegalArgumentException( s"Broken Link ${a.name.get} from '${sourceEnd.name}' to '${targetEnd.name}'" ) }
          val sourceInstance = sourceSlot.value.head match {
            case iv: UMLInstanceValue[Uml] => iv.instance.get
          }
          val targetSlot = slots.find( _.definingFeature == Some( targetEnd ) ) getOrElse { throw new IllegalArgumentException( s"Broken Link ${a.name.get} from '${sourceEnd.name}' to '${targetEnd.name}'" ) }
          val targetInstance = targetSlot.value.head match {
            case iv: UMLInstanceValue[Uml] => iv.instance.get
          }
          ( sourceInstance.name, targetInstance.name ) match {
            case ( Some( sName ), Some( tName ) ) =>
              val linkName = prefix + sName + "," + tName + ")"
              link.name match {
                case Some( lName ) if ( lName == linkName ) => 
                  guiLog.log( s" Link (id=${link.id}) - already named!: '${linkName}'")
                  ()
                case _ =>
                  guiLog.log( s" Link (id=${link.id}) set name to: '${linkName}'")
                  umlMagicDrawUMLInstanceSpecification(link).getMagicDrawInstanceSpecification.setName( linkName )
                  count = count + 1
              }
            case ( Some( sName ), None ) => 
                  guiLog.log( s" Link (id=${link.id}) - source named: '${sName}' but target is unnamed! (id=${targetInstance.id})")
            case ( None, Some( tName ) ) => 
                  guiLog.log( s" Link (id=${link.id}) - target named: '${tName}' but source is unnamed! (id=${sourceInstance.id})")
            case ( None, None ) =>
                  guiLog.log( s" Link (id=${link.id}) - source is unnamed: (id=${sourceInstance.id}) target is unnamed: (id=${sourceInstance.id})")              
          }
        }

        guiLog.log( s"Done (renamed ${count} links) !" )
    }
  }
}