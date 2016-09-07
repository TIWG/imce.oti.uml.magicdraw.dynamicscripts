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

///*
// *
// * License Terms
// *
// * Copyright (c) 2014-2016, California Institute of Technology ("Caltech").
// * U.S. Government sponsorship acknowledged.
// *
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are
// * met:
// *
// * *   Redistributions of source code must retain the above copyright
// *    notice, this list of conditions and the following disclaimer.
// *
// * *   Redistributions in binary form must reproduce the above copyright
// *    notice, this list of conditions and the following disclaimer in the
// *    documentation and/or other materials provided with the
// *    distribution.
// *
// * *   Neither the name of Caltech nor its operating division, the Jet
// *    Propulsion Laboratory, nor the names of its contributors may be
// *    used to endorse or promote products derived from this software
// *    without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
// * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
// * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts
//
//import java.awt.event.ActionEvent
//import java.io.File
//
//import com.nomagic.actions.NMAction
//import com.nomagic.magicdraw.core.{Application, Project}
//import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
//import com.nomagic.magicdraw.ui.browser.{Node, Tree}
//import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
//import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
//import com.nomagic.task.{ProgressStatus, RunnableWithProgress}
//import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model
//import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
//import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
//import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
//import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
//import gov.nasa.jpl.dynamicScripts.magicdraw.validation.internal.MDValidationAPIHelper._
//import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.{MDAPI, OTIHelper}
//import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation
//import org.omg.oti.magicdraw.uml.canonicalXMI._
//import org.omg.oti.magicdraw.uml.read._
//import org.omg.oti.uml.canonicalXMI.UnresolvedElementCrossReference
//import org.omg.oti.uml.read.api._
//import org.omg.oti.uml.xmi._
//
//import scala.Predef.{ArrowAssoc, require}
//import scala.collection.JavaConversions._
//import scala.collection.immutable._
//import scala.language.{implicitConversions, postfixOps}
//import scala.util.{Success, Try}
//import scala.{Boolean,Function1,Option,None,Some,StringContext,Tuple2,Unit}
//import scalaz.Scalaz._
//import scalaz._
//
///**
// * Should be functionally equivalent to exportPackageExtents but simpler thanks to the methods in DocumentSet's companion object
// *
// * @author Nicolas.F.Rouquette@jpl.nasa.gov
// */
//object exportPackageExtents2OTICanonicalXMI {
//
//  def doit(
//    p: Project, ev: ActionEvent,
//    script: DynamicScriptsTypes.BrowserContextMenuAction,
//    tree: Tree, node: Node,
//    pkg: Profile, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
//    doit( p, selection )
//
//  def doit(
//    p: Project, ev: ActionEvent,
//    script: DynamicScriptsTypes.BrowserContextMenuAction,
//    tree: Tree, node: Node,
//    top: Package, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
//    doit( p, selection )
//
//  def doit(
//    p: Project, ev: ActionEvent,
//    script: DynamicScriptsTypes.BrowserContextMenuAction,
//    tree: Tree, node: Node,
//    top: Model, selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
//    doit( p, selection )
//
//  def doit(
//    p: Project, ev: ActionEvent,
//    script: DynamicScriptsTypes.DiagramContextMenuAction,
//    dpe: DiagramPresentationElement,
//    triggerView: PackageView,
//    triggerElement: Profile,
//    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] =
//    doit( p, selection flatMap { case pv: PackageView => Some( pv.getPackage ) } )
//
//  def doit(
//    p: Project, ev: ActionEvent,
//    script: DynamicScriptsTypes.DiagramContextMenuAction,
//    dpe: DiagramPresentationElement,
//    triggerView: PackageView,
//    triggerElement: Package,
//    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] =
//    doit( p, selection flatMap { case pv: PackageView => Some( pv.getPackage ) } )
//
//  def doit(
//    p: Project,
//    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {
//
//    val a = Application.getInstance()
//    val guiLog = a.getGUILog
//    guiLog.clearLog()
//
//    implicit val umlUtil = MagicDrawUMLUtil( p )
//    import umlUtil._
//
//    val otiV = OTIMagicDrawValidation(p)
//    val selectedPackages: Set[UMLPackage[Uml]] =
//      selection
//      .toIterable
//      .selectByKindOf { case p: Package => umlPackage( p ) }
//      .to[Set]
//
//    OTIHelper.getOTIMDInfo().fold[Try[Option[MagicDrawValidationDataResults]]](
//      l = (nels) =>
//        otiV
//          .toTryOptionMDValidationDataResults(p, "exportPackageExtents2OTICanonicalXMI", nels.some),
//
//      r = (info) => {
//
//        var result: Option[Try[Option[MagicDrawValidationDataResults]]] = None
//        val runnable = new RunnableWithProgress() {
//
//          def run(progressStatus: ProgressStatus): Unit =
//            result = Some(
//              exportPackageExtents(
//                progressStatus,
//                p, selectedPackages,
//                info,
//                ignoreCrossReferencedElementFilter = MDAPI.ignoreCrossReferencedElementFilter,
//                unresolvedElementMapper = MDAPI.unresolvedElementMapper(umlUtil)))
//
//        }
//
//        MagicDrawProgressStatusRunner.runWithProgressStatus(
//          runnable,
//          s"Export ${selectedPackages.size} packages to OTI Canonical XMI...",
//          true, 0)
//
//        require(result.isDefined)
//        result.get
//      })
//  }
//
//  def exportPackageExtents
//  ( progressStatus: ProgressStatus,
//    p: Project,
//    specificationRootPackages: Set[UMLPackage[MagicDrawUML]],
//    info: OTIHelper.OTIMDInfo,
//    ignoreCrossReferencedElementFilter: Function1[UMLElement[MagicDrawUML], Boolean],
//    unresolvedElementMapper: Function1[UMLElement[MagicDrawUML], Option[UMLElement[MagicDrawUML]]] )
//  ( implicit umlUtil: MagicDrawUMLUtil )
//  : Try[Option[MagicDrawValidationDataResults]] = {
//    import umlUtil._
//
//    val otiV = OTIMagicDrawValidation(p)
//    val a = Application.getInstance()
//    val guiLog = a.getGUILog
//
//    progressStatus.setCurrent( 0 )
//    progressStatus.setMax( 0 )
//    progressStatus.setMax( 1 )
//    progressStatus.setLocked( true )
//
//    // @todo populate...
//    implicit val otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] =
//      None
//
//    implicit val mdIdGenerator = info._1
//    val mdDS: MagicDrawDocumentSet = info._3
//
//    val unresolved: Iterable[UnresolvedElementCrossReference[MagicDrawUML]] = info._4
//
//    if ( unresolved.isEmpty ) {
//
//      val resolved = info._2
//      guiLog.log(s"Graph: ${resolved.g.nodes.size} nodes, ${resolved.g.edges.size} edges")
//      resolved.g.nodes.foreach { n =>
//        n.value match {
//          case doc: MagicDrawBuiltInImmutableDocument =>
//            guiLog.log(s"Tool BuiltIn R/O: ${doc.scope.qualifiedName.get}")
//          case doc: MagicDrawBuiltInMutableDocument =>
//            guiLog.log(s"Tool BuiltIn R/W: ${doc.scope.qualifiedName.get}")
//          case doc: MagicDrawSerializableImmutableDocument =>
//            guiLog.log(s"Serializable R/O: ${doc.scope.qualifiedName.get}")
//          case doc: MagicDrawSerializableMutableDocument =>
//            guiLog.log(s"Serializable R/W: ${doc.scope.qualifiedName.get}")
//        }
//      }
//      resolved.serialize.fold[Try[Option[MagicDrawValidationDataResults]]](
//        l = (nels) =>
//          otiV.toTryOptionMDValidationDataResults(p, "exportPackageExtents2OTICanonicalXMI", nels.some),
//
//        r = (serialized: Set[(Document[MagicDrawUML], File)]) => {
//          guiLog.log(s"Finished serializing ${serialized.size} documents")
//          serialized.foreach { case (doc, file) =>
//            guiLog.log(s"Document root: ${doc.scope.qualifiedName.get}")
//            guiLog.log(s"Document file: $file")
//          }
//          Success(Option.empty[MagicDrawValidationDataResults])
//        }
//      )
//    } else {
//      guiLog.log(s"*** ${
//        unresolved.size
//      } unresolved cross-references ***")
//
//      val elementMessages =
//        unresolved.map {
//          u =>
//            val mdXRef = umlMagicDrawUMLElement(u.relationTriple.obj).getMagicDrawElement
//            val a = new NMAction(s"Select${
//              u.hashCode
//            }", s"Select ${
//              mdXRef.getHumanType
//            }: ${
//              mdXRef.getHumanName
//            }", 0) {
//              def actionPerformed(ev: ActionEvent): Unit = umlMagicDrawUMLElement(u.relationTriple.obj)
//                .selectInContainmentTreeRunnable.run
//            }
//            umlMagicDrawUMLElement(u.relationTriple.sub).getMagicDrawElement ->
//              Tuple2(s"cross-reference to: ${
//                mdXRef.getHumanType
//              }: ${
//                mdXRef.getHumanName
//              } (ID=${
//                mdXRef.getID
//              })",
//                List(a))
//        }.toMap
//
//      Success(Some(
//        p.makeMDIllegalArgumentExceptionValidation(
//          s"*** ${
//            unresolved.size
//          } unresolved cross-references ***",
//          elementMessages,
//          "*::MagicDrawOTIValidation",
//          "*::UnresolvedCrossReference").validationDataResults))
//    }
//  }
//}