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

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.uml.actions.SelectInContainmentTreeRunnable
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import org.omg.oti.api._
import org.omg.oti.magicdraw.{MagicDrawUML, MagicDrawUMLUtil}

import scala.collection.JavaConversions._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Success, Try}

/**
 * Select 1 package and N profiles in a diagram.
 * Invoke this dynamic script from the context menu of the package.
 * 
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object checkPrimaryPackageSelectionCanAccessSecondaryProfileSelection {

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {
    
    val app = Application.getInstance()
    val guiLog = app.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    val secondaryPackages = (selection.toSet - triggerView).toSet selectByKindOf ( { case pv: PackageView => umlPackage( pv.getPackage) } )
    
    checkPrimaryPackageSelectionCanAccessSecondaryProfileSelection( umlUtil, triggerElement, secondaryPackages )
    
    Success( None )
  }
      
  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {
    
    val app = Application.getInstance()
    val guiLog = app.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    val secondaryPackages = (selection.toSet - triggerView).toSet selectByKindOf ( { case pv: PackageView => umlPackage( pv.getPackage) } )
    
    checkPrimaryPackageSelectionCanAccessSecondaryProfileSelection( umlUtil, triggerElement, secondaryPackages )
    
    Success( None )
  }
      
      
  def checkPrimaryPackageSelectionCanAccessSecondaryProfileSelection(
      umlUtil: MagicDrawUMLUtil, 
      primaryPkg: UMLPackage[MagicDrawUML], 
      secondaryProfilesOrPackages: Iterable[UMLPackage[MagicDrawUML]] ): Unit = {
    
    import umlUtil._
    val app = Application.getInstance()
    val guiLog = app.getGUILog()

    val primaryAccessible = primaryPkg.allIndirectlyAppliedProfilesIncludingNestingPackagesTransitively.flatMap(_.allVisibleMembersTransitively)
    val secondaryContents = secondaryProfilesOrPackages.flatMap (_.allOwnedElements.selectByKindOf { case pe: UMLPackageableElement[Uml] => pe } toSet) toSet
    val secondaryVisible = secondaryProfilesOrPackages.flatMap (_.allVisibleMembersTransitively) toSet
    
    val excluded = (secondaryContents & secondaryVisible) -- primaryAccessible
    guiLog.log(s"OK?: ${excluded.isEmpty}")
        
    excluded.foreach { e =>
      val mdE = umlMagicDrawUMLElement(e).getMagicDrawElement
      val link=s"${mdE.getHumanType}: ${mdE.getHumanName}"
      guiLog.addHyperlinkedText(s" should be accessible: <A>${link}</A>", Map(link-> new SelectInContainmentTreeRunnable( mdE ) ) )
    }
  }
}