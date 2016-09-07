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
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDGUILogHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.language.postfixOps
import scala.util.{Success, Try}
import scala.{Option,None,StringContext,Unit}
import scala.Predef.ArrowAssoc

/**
 * Select 1 package and N profiles in a diagram.
 * Invoke this dynamic script from the context menu of the package.
 * 
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object checkPrimaryPackageSelectionCannotAccessSecondaryProfileSelection {

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] = {
    
    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    val secondaryPackages = (selection.toSet - triggerView) selectByKindOf { case pv: PackageView =>
      umlPackage(SymbolHelper.getPackageOfView(pv).get)
    }
    
    checkPrimaryPackageSelectionCannotAccessSecondaryProfileSelection( umlUtil, triggerElement, secondaryPackages )
    
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
    val guiLog = app.getGUILog
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    val secondaryPackages = (selection.toSet - triggerView) selectByKindOf { case pv: PackageView =>
      umlPackage(SymbolHelper.getPackageOfView(pv).get)
    }
    
    checkPrimaryPackageSelectionCannotAccessSecondaryProfileSelection( umlUtil, triggerElement, secondaryPackages )
    
    Success( None )
  }
      
      
  def checkPrimaryPackageSelectionCannotAccessSecondaryProfileSelection(
      umlUtil: MagicDrawUMLUtil, 
      primaryPkg: UMLPackage[MagicDrawUML], 
      secondaryProfilesOrPackages: Iterable[UMLPackage[MagicDrawUML]] ): Unit = {
    
    import umlUtil._
    val app = Application.getInstance()
    val guiLog = app.getGUILog

    val primaryAccessible = primaryPkg.allIndirectlyAppliedProfilesIncludingNestingPackagesTransitively.flatMap(_.allVisibleMembersTransitively)
    val secondaryContents = secondaryProfilesOrPackages.flatMap (_.allOwnedElements.selectByKindOf { case pe: UMLPackageableElement[Uml] => pe } toSet) toSet
    val secondaryVisible = secondaryProfilesOrPackages.flatMap (_.allVisibleMembersTransitively) toSet
    
    val included = (secondaryContents & secondaryVisible) & primaryAccessible
    guiLog.log(s"OK?: ${included.isEmpty}")
    
    included.foreach { e =>
      val mdE = umlMagicDrawUMLElement(e).getMagicDrawElement
      val link=s"${mdE.getHumanType}: ${mdE.getHumanName}"
      guiLog.addGUILogHyperlink(s" should not be accessible: <A>$link</A>",
        link->mdE )
    }

  }
}