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
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}

import scala.collection.JavaConversions._
import scala.collection.immutable.Vector
import scala.util.{Success, Try}
import scala.{Option,None,StringContext,Unit}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object addPackageImportsForNestedPackages {

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    addPackageImportsForNestedPackages(
      p,
      umlUtil,
      selection.to[Vector] selectByKindOf { case pv: PackageView => umlPackage( SymbolHelper.getPackageOfView(pv).get) } )
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

    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    addPackageImportsForNestedPackages( p, umlUtil, selection.to[Vector] selectByKindOf ( { case pv: PackageView => umlPackage( SymbolHelper.getPackageOfView(pv).get ) } ) )
    Success( None )
  }
       
  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    pkg: Profile, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {
        
    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    addPackageImportsForNestedPackages( p, umlUtil, selection.to[Vector] selectByKindOf ( { case pkg: Package => umlPackage( pkg ) } ) )
    Success( None )
  }
    
  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    pkg: Package, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {
        
    val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._
    
    addPackageImportsForNestedPackages( p, umlUtil, selection.to[Vector] selectByKindOf ( { case pkg: Package => umlPackage( pkg ) } ) )
    Success( None )
  }
    
  def addPackageImportsForNestedPackages
  ( p: Project,
    umlUtil: MagicDrawUMLUtil,
    pkgs: Vector[UMLPackage[MagicDrawUML]] )
  : Unit
  = {

    import umlUtil._
    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()
    
    val f = p.getElementsFactory
    
    val allPkgs = pkgs ++ pkgs.flatMap (_.allNestedPackages)
    allPkgs foreach { pkg =>
      val mdPkg = umlMagicDrawUMLPackage(pkg).getMagicDrawPackage
      val importedPackages = pkg.packageImport.flatMap(_.importedPackage)
      val nestedPackages2Import = pkg.nestedPackage -- importedPackages
      nestedPackages2Import foreach { npkg =>
          val i = f.createPackageImportInstance
          i.setImportingNamespace(mdPkg)
          i.setImportedPackage(umlMagicDrawUMLPackage(npkg).getMagicDrawPackage)
        guiLog.log(s"Add import: ${mdPkg.getQualifiedName} => ${npkg.qualifiedName.get}")
      }
    }
    guiLog.log("Done")
  }
}