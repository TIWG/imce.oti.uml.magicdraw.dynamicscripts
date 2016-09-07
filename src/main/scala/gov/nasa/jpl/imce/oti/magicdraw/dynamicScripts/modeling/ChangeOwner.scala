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
//package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.modeling
//
//import java.awt.event.ActionEvent
//import java.lang.IllegalArgumentException
//
//import com.nomagic.magicdraw.core.{Application, Project}
//import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
//import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
//import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
//import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML._
//import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
//import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.{MDAPI, ResultSetAggregator}
//import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation
//import org.omg.oti.magicdraw.uml.canonicalXMI._
//import org.omg.oti.magicdraw.uml.characteristics._
//import org.omg.oti.magicdraw.uml.read._
//import org.omg.oti.magicdraw.uml.write.MagicDrawUMLUpdate
//import org.omg.oti.uml.UMLError
//import org.omg.oti.uml.canonicalXMI._
//import org.omg.oti.uml.characteristics._
//import org.omg.oti.uml.read.api._
//import org.omg.oti.uml.xmi._
//
//import scala.collection.JavaConversions._
//import scala.collection.immutable._
//import scala.language.{implicitConversions, postfixOps}
//import scala.util.{Failure, Try}
//import scalaz.Scalaz._
//import scalaz._
//
//import scala.{Option,None,Some,StringContext,Unit}
///**
//* Diagram selection: the elements whose ownership will be changed to the browser-selected element
//* Browser selection: the single element that will be the new owner of the diagram-selected elements
//*/
//object ChangeOwner {
//
//  def doit
//  (p: Project,
//   ev: ActionEvent,
//   script: DynamicScriptsTypes.DiagramContextMenuAction,
//   dpe: DiagramPresentationElement,
//   triggerView: PresentationElement,
//   triggerElement: Element,
//   selection: java.util.Collection[PresentationElement] )
//  : Try[Option[MagicDrawValidationDataResults]] = {
//
//    val app = Application.getInstance()
//    val guiLog = app.getGUILog
//    guiLog.clearLog()
//
//    implicit val umlUtil = MagicDrawUMLUtil( p )
//    import umlUtil._
//
//    // @todo populate...
//    implicit val otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] =
//      None
//
//    implicit val otiCharacterizationProfileProvider: OTICharacteristicsProvider[MagicDrawUML] =
//      MagicDrawOTICharacteristicsProfileProvider()
//
//    implicit val documentOps = new MagicDrawDocumentOps()
//    val upd = MagicDrawUMLUpdate(umlUtil)
//
//    val newOwner = p.getBrowserTreeSelection() match {
//      case None =>
//        return Failure(new IllegalArgumentException("Select a single element (new owner) in the browser tree"))
//      case Some(bInfo) if 1 == bInfo.selection.size =>
//        umlElement( bInfo.selection.head.e )
//      case _ =>
//        return Failure(new IllegalArgumentException("Select a single element (new owner) in the browser tree"))
//    }
//
//    val selectedElements =
//      selection
//      .toIterable
//      .selectByKindOf { case pe: PresentationElement => umlElement( pe.getElement ) }
//      .toList
//
//    val result: NonEmptyList[java.lang.Throwable] \&/ Unit =
//      MDAPI
//      .getMDCatalogs()
//      .toThese
//      .flatMap { case (documentURIMapper, builtInURIMapper) =>
//
//        val extraSpecificationRootPkgs
//        : ResultSetAggregator[UMLPackage[Uml]]#F =
//          (ResultSetAggregator.zero[UMLPackage[Uml]] /: (selectedElements.to[Set] + newOwner)) { (acc, e) =>
//            acc append
//              e
//                .getPackageOwnerWithEffectiveURI
//                .map { opkg =>
//                  opkg.fold[Set[UMLPackage[Uml]]](
//                    Set[UMLPackage[Uml]]()
//                  ) { pkg =>
//                    Set(pkg)
//                  }
//                }
//                .toThese
//          }
//
//        val mdDS =
//          MagicDrawDocumentSet
//            .createMagicDrawProjectDocumentSet(
//              additionalSpecificationRootPackages = extraSpecificationRootPkgs.b,
//              documentURIMapper = documentURIMapper,
//              builtInURIMapper = builtInURIMapper,
//              ignoreCrossReferencedElementFilter = MDAPI.ignoreCrossReferencedElementFilter,
//              unresolvedElementMapper = MDAPI.unresolvedElementMapper(umlUtil))
//
//        val maybeErrors =
//          mdDS.flatMap {
//            case (rds: ResolvedDocumentSet[MagicDrawUML],
//                  ds: MagicDrawDocumentSet,
//                  xrefs: Iterable[UnresolvedElementCrossReference[MagicDrawUML]]) =>
//
//              implicit val idg: MagicDrawIDGenerator = MagicDrawIDGenerator(rds)
//
//              val result0: NonEmptyList[java.lang.Throwable] \&/ Unit = \&/.That(())
//              val results: NonEmptyList[java.lang.Throwable] \&/ Unit =
//                ( result0 /: selectedElements ) { (acc, e) =>
//                  acc append changeOwner(newOwner, e).toThese
//                }
//
//              results
//          }
//
//        maybeErrors
//      }
//
//    val otiV = OTIMagicDrawValidation(p)
//    otiV.toTryOptionMDValidationDataResults(p, s"*** OTI Change Owner Errors ***", result.a)
//
//  }
//
//  def changeOwner[Uml <: UML]
//  (newParent: UMLElement[Uml],
//   relocatedChild: UMLElement[Uml])
//  (implicit idg: IDGenerator[Uml])
//  : NonEmptyList[java.lang.Throwable] \/ Unit =
//    relocatedChild.getContainingMetaPropertyEvaluator
//    .flatMap { ev: Option[relocatedChild.MetaPropertyEvaluator] =>
//        ev.fold[NonEmptyList[java.lang.Throwable] \/ Unit](
//          -\/(
//            NonEmptyList(
//            UMLError.illegalElementError[Uml, UMLElement[Uml]](
//              "cannot change the ownership of the root element",
//              Iterable(relocatedChild, newParent))))
//        ){ evaluator =>
//          if (!newParent.compositeMetaProperties.contains(evaluator))
//            -\/(
//              NonEmptyList(
//                UMLError.illegalElementError[Uml, UMLElement[Uml]](
//                  "Different ownership properties!",
//                  Iterable(relocatedChild, newParent))))
//          else {
//            // @todo invoke the OTI UML Update API...
//            \/-(Unit)
//          }
//        }
//      }
//}