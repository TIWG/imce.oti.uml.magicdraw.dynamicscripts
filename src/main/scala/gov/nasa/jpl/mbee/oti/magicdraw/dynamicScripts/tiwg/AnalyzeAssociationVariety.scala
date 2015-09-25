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
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.tiwg

import java.awt.event.ActionEvent
import java.io.File

import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.core.{Application, ApplicationEnvironment, Project}
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.task.{ProgressStatus, RunnableWithProgress}
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.{DynamicScriptsPlugin, MagicDrawValidationDataResults}
import org.omg.oti.uml.read.api._
import org.omg.oti.uml.canonicalXMI.{CatalogURIMapper, DocumentSet}
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}

import scala.collection.JavaConversions.{asJavaCollection, collectionAsScalaIterable}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}

object AnalyzeAssociationVariety {

  def doit(
            p: Project, ev: ActionEvent,
            script: DynamicScriptsTypes.BrowserContextMenuAction,
            tree: Tree, node: Node,
            top: Package, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, selection )

  def doit(
            p: Project, ev: ActionEvent,
            script: DynamicScriptsTypes.DiagramContextMenuAction,
            dpe: DiagramPresentationElement,
            triggerView: PackageView,
            triggerElement: Package,
            selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, selection flatMap { case pv: PackageView => Some( pv.getPackage ) } )

  def doit(
            p: Project,
            selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection
      .toIterable
      .selectByKindOf { case p: Package => umlPackage(p) }
      .to[Set]

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