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
import org.omg.oti.uml.read.api._
import org.omg.oti.uml.canonicalXMI.{CatalogURIMapper, DocumentSet}
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}

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
    val guiLog = a.getGUILog()
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil( p )
    import umlUtil._

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection.toIterator selectByKindOf { case p: Package => umlPackage( p ) } toSet

    /**
     * Ignore OMG production-related elements pertaining to OMG SysML 1.4 spec.
     */
    def ignoreCrossReferencedElementFilter( e: UMLElement[Uml] ): Boolean = e match {

      case ne: UMLNamedElement[Uml] =>
        val neQName = ne.qualifiedName.get
          DynamicScriptsPlugin.wildCardMatch( neQName, "UML Standard Profile::MagicDraw Profile::*" ) ||
          DynamicScriptsPlugin.wildCardMatch( neQName, "*OMG*" ) ||
          DynamicScriptsPlugin.wildCardMatch( neQName, "Specifications::SysML.profileAnnotations::*" )

      case e =>
        false
    }

    def unresolvedElementMapper( e: UMLElement[Uml] ): Option[UMLElement[Uml]] = e.id match {
      case "_UML_" => Some( MDBuiltInUML.scope )
      case "_StandardProfile_" => Some( MDBuiltInStandardProfile.scope )
      case _ => None
    }
    
    val defaultOMGCatalogFile =
      new File(
        new File(
          ApplicationEnvironment.getInstallRoot ).
          toURI.resolve( "dynamicScripts/org.omg.oti/resources/omgCatalog/omg.local.catalog.xml" ) )
    val omgCatalog =
      if ( defaultOMGCatalogFile.exists() ) Seq( defaultOMGCatalogFile )
      else chooseCatalogFile( "Select the OMG UML 2.5 *.catalog.xml file" ).toSeq

    val defaultMDCatalogFile =
      new File(
        new File( ApplicationEnvironment.getInstallRoot ).
          toURI.resolve( "dynamicScripts/org.omg.oti.magicdraw/resources/md18Catalog/omg.magicdraw.catalog.xml" ) )
    val mdCatalog =
      if ( defaultMDCatalogFile.exists() ) Seq( defaultMDCatalogFile )
      else chooseCatalogFile( "Select the MagicDraw UML 2.5 *.catalog.xml file" ).toSeq

    ( CatalogURIMapper.createCatalogURIMapper( omgCatalog ),
      CatalogURIMapper.createCatalogURIMapper( mdCatalog ) ) match {
        case ( Failure( t ), _ ) => Failure( t )
        case ( _, Failure( t ) ) => Failure( t )
        case ( Success( documentURIMapper ), Success( builtInURIMapper ) ) =>

          var result: Option[Try[Option[MagicDrawValidationDataResults]]] = None
          val runnable = new RunnableWithProgress() {

            def run( progressStatus: ProgressStatus ): Unit =
              result = Some(
                exportPackageExtents(
                  progressStatus,
                  p, selectedPackages,
                  documentURIMapper, builtInURIMapper,
                  ignoreCrossReferencedElementFilter,
                  unresolvedElementMapper ) )

          }

          MagicDrawProgressStatusRunner.runWithProgressStatus(
            runnable,
            s"Export ${selectedPackages.size} packages to OTI Canonical XMI...",
            true, 0 )

          require( result.isDefined )
          result.get
      }
  }

  def exportPackageExtents(
    progressStatus: ProgressStatus,
    p: Project,
    specificationRootPackages: Set[UMLPackage[MagicDrawUML]],
    documentURIMapper: CatalogURIMapper,
    builtInURIMapper: CatalogURIMapper,
    ignoreCrossReferencedElementFilter: Function1[UMLElement[MagicDrawUML], Boolean],
    unresolvedElementMapper: Function1[UMLElement[MagicDrawUML], Option[UMLElement[MagicDrawUML]]] )( implicit umlUtil: MagicDrawUMLUtil ): Try[Option[MagicDrawValidationDataResults]] = {
    import umlUtil._

    val a = Application.getInstance()
    val guiLog = a.getGUILog

    progressStatus.setCurrent( 0 )
    progressStatus.setMax( 0 )
    progressStatus.setMax( 1 )
    progressStatus.setLocked( true )

    DocumentSet.constructDocumentSetCrossReferenceGraph(
      specificationRootPackages,
      documentURIMapper, builtInURIMapper,
      builtInDocuments = Set( MDBuiltInPrimitiveTypes, MDBuiltInUML, MDBuiltInStandardProfile ),
      builtInDocumentEdges = Set( MDBuiltInUML2PrimitiveTypes, MDBuiltInStandardProfile2UML ),
      ignoreCrossReferencedElementFilter,
      unresolvedElementMapper ) match {
        case Failure( t ) => Failure( t )
        case Success( ( resolved, unresolved ) ) =>

          val result = if ( unresolved.isEmpty ) Success( None )
          else {
            guiLog.log( s"*** ${unresolved.size} unresolved cross-references ***" )
            val elementMessages = unresolved map { u =>
              val mdXRef = umlMagicDrawUMLElement(u.externalReference).getMagicDrawElement
              val a = new NMAction( s"Select${u.hashCode}", s"Select ${mdXRef.getHumanType}: ${mdXRef.getHumanName}", 0 ) {
                def actionPerformed( ev: ActionEvent ): Unit = umlMagicDrawUMLElement(u.externalReference).selectInContainmentTreeRunnable.run
              }
              umlMagicDrawUMLElement(u.documentElement).getMagicDrawElement ->
                Tuple2( s"cross-reference to: ${mdXRef.getHumanType}: ${mdXRef.getHumanName} (ID=${mdXRef.getID})",
                  List( a ) )
            } toMap

            Success( Some(
              MagicDrawValidationDataResults.makeMDIllegalArgumentExceptionValidation(
                p, s"*** ${unresolved.size} unresolved cross-references ***",
                elementMessages,
                "*::MagicDrawOTIValidation",
                "*::UnresolvedCrossReference" ).validationDataResults ) )
          }

          resolved.serialize match {
            case Success( _ ) =>
              progressStatus.increase()
              guiLog.log( s"Graph: ${resolved.g}" )
              guiLog.log( s"Done..." )
              result

            case Failure( t ) =>
              Failure( t )
          }
      }
  }
}