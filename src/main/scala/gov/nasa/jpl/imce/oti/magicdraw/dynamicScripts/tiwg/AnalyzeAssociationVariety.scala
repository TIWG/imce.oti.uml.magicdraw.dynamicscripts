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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.tiwg

import java.awt.event.ActionEvent
import java.lang.{System}

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil
import org.omg.oti.uml.read.api._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Success, Try}

import scala.Predef.String
import scala.{Option,None,Some,StringContext}

object AnalyzeAssociationVariety {

  def doit
  ( p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    top: Package, selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]]
  = doit( p, selection )

  def doit
  ( p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement] )
  : Try[Option[MagicDrawValidationDataResults]]
  = doit( p, selection flatMap { case pv: PackageView => getPackageOfView(pv) } )

  def doit
  ( p: Project,
    selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]]
  = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection
      .to[Set]
      .selectByKindOf { case p: Package => umlPackage(p) }

    val allPackages = selectedPackages.flatMap(_.allNestedPackages)

    val selectedAssociations: Set[UMLAssociation[Uml]] =
      allPackages.flatMap { pkg =>
        pkg.ownedType selectByKindOf {
          case a: UMLAssociation[Uml] => a
        }
      }

    val assocs = selectedAssociations.toList.sortBy(_.qualifiedName.get)
    assocs.foreach { a =>
      System.out.print(s"${a.qualifiedName.get},${a.isDerived},${a.general.size},")
      a.getDirectedAssociationEnd match {
        case Some((end1, end2)) =>
          val end1Subsets = end1.subsettedProperty.map(describeAssociationEnd)
          val end1Redefs = end1.redefinedProperty.map(describeAssociationEnd)
          val end2Subsets = end2.subsettedProperty.map(describeAssociationEnd)
          val end2Redefs = end2.redefinedProperty.map(describeAssociationEnd)

          System.out.println(s"true,${describeAssociationEnd(end1)},${describeAssociationEnd(end2)}")
        case None =>
          System.out.println("false")
      }
    }

    Success(None)
  }

  def describeAssociationEnd[Uml <: UML](p: UMLProperty[Uml]): String = {
    s"${p.isUnique},${p.isOrdered},${p.isDerived},${p.isDerivedUnion},${p.aggregation},${p.redefinedProperty.nonEmpty}"
  }
}