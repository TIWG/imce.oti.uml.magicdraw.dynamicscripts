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

import scala.collection.JavaConversions.asJavaCollection
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.language.implicitConversions
import scala.language.postfixOps
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
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults

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
    doit( p, ev, selection flatMap { case pv: PackageView => Some( pv.getPackage ) } )

  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, ev, selection flatMap { case pv: PackageView => Some( pv.getPackage ) } )

  def doit(
    p: Project, ev: ActionEvent,
    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    implicit val otiCharacterizations: Option[Map[UMLPackage[Uml], UMLComment[Uml]]] = None

    implicit val otiCharacterizationProfileProvider: OTICharacteristicsProvider[MagicDrawUML] =
      MagicDrawOTICharacteristicsProfileProvider()

    val runnable = new RunnableWithProgress() {

      def run( progressStatus: ProgressStatus ): Unit = {

        val mdCounter = p.getCounter
        val flag = mdCounter.canResetIDForObject
        mdCounter.setCanResetIDForObject( true )

        try {

          val selectedPackages: Set[UMLPackage[Uml]] =
            selection
            .toIterable
            .selectByKindOf { case p: Package => umlPackage( p ) }
            .to[Set]

          progressStatus.setCurrent( 0 )
          progressStatus.setMax( 0 )
          progressStatus.setMax( selectedPackages.size )
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
          mdCounter.setCanResetIDForObject( flag )
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
