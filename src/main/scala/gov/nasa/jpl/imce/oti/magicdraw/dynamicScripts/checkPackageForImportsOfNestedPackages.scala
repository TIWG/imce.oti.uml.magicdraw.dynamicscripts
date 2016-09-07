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
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.actions.AddMissingImportFromParentPackage
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}
import org.omg.oti.uml.read.api._

import scala.Predef.ArrowAssoc
import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.Try
import scala.{Option,Some,StringContext}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object checkPackageForImportsOfNestedPackages {

  def doit
  ( p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages = selection.toSet selectByKindOf { case pv: PackageView =>
      umlPackage( SymbolHelper.getPackageOfView(pv).get ) }
    checkPackageForImportsOfNestedPackages( p, selectedPackages )
  }

  def doit
  ( p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    val app = Application.getInstance()
    val guiLog = app.getGUILog()
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages = selection.toSet selectByKindOf { case pv: PackageView =>
      umlPackage(SymbolHelper.getPackageOfView(pv).get)
    }
    checkPackageForImportsOfNestedPackages( p, selectedPackages )
  }

  def doit
  ( p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    pkg: Package, selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]] = {

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages = selection.toSet selectByKindOf { case pkg: Package => umlPackage( pkg ) }
    checkPackageForImportsOfNestedPackages( p, selectedPackages )
  }

  def checkPackageForImportsOfNestedPackages
  ( p: Project,
    pkgs: Iterable[UMLPackage[MagicDrawUML]] )
  ( implicit umlUtil: MagicDrawUMLUtil )
  : Try[Option[MagicDrawValidationDataResults]] = {

    import umlUtil._

    val otiV = OTIMagicDrawValidation(p)

    val app = Application.getInstance()
    val guiLog = app.getGUILog
    guiLog.clearLog()

    guiLog.log( s"Checking ${pkgs.size} package(s)..." )

    val elementMessages = for {
      pkg <- pkgs ++ pkgs.flatMap( _.allNestedPackages )
      missingImport <- pkg.nonImportedNestedPackages
      vInfo = otiV.constructValidationInfo(
        otiV.MD_OTI_ValidationConstraint_UnresolvedCrossReference,
        Some(s"Add import from parent package, ${pkg.name.get}"),
        List( AddMissingImportFromParentPackage() )).get
    } yield
      umlMagicDrawUMLPackage(missingImport).getMagicDrawPackage -> List(vInfo)

    otiV.toTryOptionMagicDrawValidationDataResults(
      p,
      "checkPackageForImportsOfNestedPackages" ,
      otiV.makeMDIllegalArgumentExceptionValidation(
        "Validate for missing imports of nested packages",
        elementMessages.toMap))
  }
}