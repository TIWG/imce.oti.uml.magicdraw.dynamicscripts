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
import gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.utils._

import org.omg.oti.uml.read.api._
import scala.collection.immutable._
import org.omg.oti.uml.xmi._
import org.omg.oti.magicdraw.uml.canonicalXMI._
import org.omg.oti.uml.canonicalXMI.{CatalogURIMapper, DocumentEdge, DocumentSet}
import org.omg.oti.magicdraw.uml.read._

import scala.collection.JavaConversions.{asJavaCollection, collectionAsScalaIterable}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}

/**
 * Should be functionally equivalent to exportPackageExtents but simpler thanks to the methods in DocumentSet's companion object
 *
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object exportPackageExtents2OTICanonicalXMI {

  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    pkg: Profile, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, selection )

  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    top: Package, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, selection )

  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree, node: Node,
    top: Model, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, selection )

  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PackageView,
    triggerElement: Profile,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, selection flatMap { case pv: PackageView => Some( pv.getPackage ) } )

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

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection
      .toIterable
      .selectByKindOf { case p: Package => umlPackage( p ) }
      .to[Set]

    MDAPI
    .getMDCatalogs()
    .flatMap { case (documentURIMapper, builtInURIMapper) =>

          var result: Option[Try[Option[MagicDrawValidationDataResults]]] = None
          val runnable = new RunnableWithProgress() {

            def run( progressStatus: ProgressStatus ): Unit =
              result = Some(
                exportPackageExtents(
                  progressStatus,
                  p, selectedPackages,
                  documentURIMapper, builtInURIMapper,
                  ignoreCrossReferencedElementFilter = MDAPI.ignoreCrossReferencedElementFilter,
                  unresolvedElementMapper = MDAPI.unresolvedElementMapper(umlUtil)) )

          }

          MagicDrawProgressStatusRunner.runWithProgressStatus(
            runnable,
            s"Export ${selectedPackages.size} packages to OTI Canonical XMI...",
            true, 0 )

          require( result.isDefined )
          result.get
      }
  }

  def exportPackageExtents
  ( progressStatus: ProgressStatus,
    p: Project,
    specificationRootPackages: Set[UMLPackage[MagicDrawUML]],
    documentURIMapper: CatalogURIMapper,
    builtInURIMapper: CatalogURIMapper,
    ignoreCrossReferencedElementFilter: Function1[UMLElement[MagicDrawUML], Boolean],
    unresolvedElementMapper: Function1[UMLElement[MagicDrawUML], Option[UMLElement[MagicDrawUML]]] )
  ( implicit umlUtil: MagicDrawUMLUtil )
  : Try[Option[MagicDrawValidationDataResults]] = {
    import umlUtil._

    val a = Application.getInstance()
    val guiLog = a.getGUILog

    progressStatus.setCurrent( 0 )
    progressStatus.setMax( 0 )
    progressStatus.setMax( 1 )
    progressStatus.setLocked( true )

    // @todo populate...
    implicit val otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] =
      None

    val mdBuiltIns: Set[BuiltInDocument[Uml]] =
      Set( MDBuiltInPrimitiveTypes, MDBuiltInUML, MDBuiltInStandardProfile )

    val mdBuiltInEdges: Set[DocumentEdge[Document[Uml]]] =
      Set( MDBuiltInUML2PrimitiveTypes, MDBuiltInStandardProfile2UML )

    implicit val mdDocOps = new MagicDrawDocumentOps()

    DocumentSet.constructDocumentSetCrossReferenceGraph[Uml](
      specificationRootPackages,
      documentURIMapper, builtInURIMapper,
      builtInDocuments = mdBuiltIns,
      builtInDocumentEdges = mdBuiltInEdges,
      ignoreCrossReferencedElementFilter,
      unresolvedElementMapper,
      aggregate = MagicDrawDocumentSetAggregate() )
    .flatMap { case (( resolved, unresolved )) =>

       implicit val mdIdGenerator: MagicDrawIDGenerator = MagicDrawIDGenerator(resolved)

          if ( unresolved.isEmpty ) {
            resolved.serialize.flatMap { _ =>
              Try[Option[MagicDrawValidationDataResults]]({
                progressStatus.increase()
                guiLog.log(s"Graph: ${
                  resolved.g
                }")
                guiLog.log(s"Done...")
                None
              })
            }
          } else resolved.ds match {

            case mdDS: MagicDrawDocumentSet =>

              guiLog.log(s"*** ${
                unresolved.size
              } unresolved cross-references ***")

              val elementMessages =
                unresolved.map {
                u =>
                  val mdXRef = umlMagicDrawUMLElement(u.externalReference).getMagicDrawElement
                  val a = new NMAction(s"Select${
                    u.hashCode
                  }", s"Select ${
                    mdXRef.getHumanType
                  }: ${
                    mdXRef.getHumanName
                  }", 0) {
                    def actionPerformed(ev: ActionEvent): Unit = umlMagicDrawUMLElement(u.externalReference)
                      .selectInContainmentTreeRunnable.run
                  }
                  umlMagicDrawUMLElement(u.documentElement).getMagicDrawElement ->
                    Tuple2(s"cross-reference to: ${
                      mdXRef.getHumanType
                    }: ${
                      mdXRef.getHumanName
                    } (ID=${
                      mdXRef.getID
                    })",
                      List(a))
              }.toMap

              Success(Some(
                MagicDrawValidationDataResults.makeMDIllegalArgumentExceptionValidation(
                  p, s"*** ${
                    unresolved.size
                  } unresolved cross-references ***",
                  elementMessages,
                  "*::MagicDrawOTIValidation",
                  "*::UnresolvedCrossReference").validationDataResults))


          }
      }
  }
}