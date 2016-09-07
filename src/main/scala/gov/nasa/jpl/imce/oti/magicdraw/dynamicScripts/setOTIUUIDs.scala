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
import java.lang.System

import scala.collection.JavaConversions.asJavaCollection
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.util.Success
import scala.util.Try

import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.ui.browser.Node
import com.nomagic.magicdraw.ui.browser.Tree
import com.nomagic.magicdraw.uml.UUIDRegistry
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement
import com.nomagic.magicdraw.uml.symbols.PresentationElement
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.task.ProgressStatus
import com.nomagic.task.RunnableWithProgress
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile

import org.omg.oti.uml.characteristics._
import org.omg.oti.uml.read.api._

import org.omg.oti.magicdraw.uml.characteristics._
import org.omg.oti.magicdraw.uml.read._

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.{Success, Try}
import scala.{Option,None,StringContext,Unit}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object setOTIUUIDs {

  def doit(
    project: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    pkg: Profile,
    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( project, ev, selection )

  def doit(
    p: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    top: Package,
    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, ev, selection )

  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Package,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, ev, selection flatMap { case pv: PackageView => getPackageOfView(pv) } )

  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, ev, selection flatMap { case pv: PackageView => getPackageOfView(pv) } )

  def doit
  ( p: Project, ev: ActionEvent,
    selection: java.util.Collection[Element] )
  : Try[Option[MagicDrawValidationDataResults]]
  = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    implicit val otiCharacterizations
    : Option[Map[UMLPackage[Uml], UMLComment[Uml]]]
    = None

    implicit val otiCharacterizationProfileProvider
    : OTICharacteristicsProvider[MagicDrawUML]
    = MagicDrawOTICharacteristicsProfileProvider()

    val runnable = new RunnableWithProgress() {

      def run( progressStatus: ProgressStatus ): Unit = {

        val flag = p.enableResettingIDs()

        try {

          val selectedPackages: Set[UMLPackage[Uml]] =
            selection
            .to[Set]
            .selectByKindOf { case p: Package => umlPackage( p ) }

          progressStatus.setCurrent( 0 )
          progressStatus.setMax( 0 )
          progressStatus.setMax( selectedPackages.size.toLong )
          progressStatus.setLocked( true )

          for {
            pkg <- selectedPackages
            uuidPrefix <- pkg.oti_uuidPrefix
            _ = progressStatus.increase()
            _ = progressStatus.setDescription( s"Using $uuidPrefix to prefix OTI XMI:IDs for '${pkg.name.get}'..." )
            _ = guiLog.log( s"Using $uuidPrefix to prefix OTI XMI:IDs for '${pkg.name.get}'..." )
            _ = System.out.println( s"Using $uuidPrefix to prefix OTI XMI:IDs for '${pkg.name.get}'..." )
            _ = UUIDRegistry.setUUID( umlMagicDrawUMLElement(pkg).getMagicDrawElement, s"$uuidPrefix.${pkg.toolSpecific_id}" )
            e <- pkg.allOwnedElements
            _ = UUIDRegistry.setUUID( umlMagicDrawUMLElement(e).getMagicDrawElement, s"$uuidPrefix.${e.toolSpecific_id}" )
          } ()

          guiLog.log( s"Done" )
        }
        finally {
          p.restoreResettingIDs( flag )
        }
      }
    }

    MagicDrawProgressStatusRunner.runWithProgressStatus(
      runnable,
      s"Setting OTI XMI:UUIDs...",
      true, 0 )

    Success( None )
  }
}