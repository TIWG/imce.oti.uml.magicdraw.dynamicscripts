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

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.MagicDrawValidationDataResults
import gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.utils.MDAPI
import gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.utils.ResultSetAggregator
import gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation

import org.omg.oti.uml.xmi._
import org.omg.oti.uml.canonicalXMI._
import org.omg.oti.uml.read.api._
import org.omg.oti.uml.read.operations._

import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.magicdraw.uml.canonicalXMI._

import scala.collection.JavaConversions.{asJavaCollection, collectionAsScalaIterable}
import scala.collection.immutable._
import scala.language.{implicitConversions, postfixOps}
import scalaz._, Scalaz._

object InspectOTIPackageInfo {

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.BrowserContextMenuAction,
   tree: Tree, node: Node,
   top: Package, selection: java.util.Collection[Element])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] =
    doit(p, selection)

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.BrowserContextMenuAction,
   tree: Tree, node: Node,
   top: Profile, selection: java.util.Collection[Element])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] =
    doit(p, selection)

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.BrowserContextMenuAction,
   tree: Tree, node: Node,
   top: Model, selection: java.util.Collection[Element])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] =
    doit(p, selection)

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Package,
   selection: java.util.Collection[PresentationElement])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] =
    doit(p, selection flatMap { case pv: PackageView => Some(pv.getPackage) })

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Profile,
   selection: java.util.Collection[PresentationElement])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] =
    doit(p, selection flatMap { case pv: PackageView => Some(pv.getPackage) })

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Model,
   selection: java.util.Collection[PresentationElement])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] =
    doit(p, selection flatMap { case pv: PackageView => Some(pv.getPackage) })

  def doit2
  (p: Project,
   selection: java.util.Collection[Element])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] = {


    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    implicit def Package2CommentMapSemigroup
    : Semigroup[Map[UMLPackage[Uml], UMLComment[Uml]]] =
      Semigroup.instance(_ ++ _)

    val selectedPackages: List[UMLPackage[Uml]] =
      selection
        .toIterable
        .selectByKindOf { case p: Package => umlPackage(p) }
        .toList
        .sortBy(_.xmiElementLabel)

    val otiMDCharacterizations
    : NonEmptyList[java.lang.Throwable] \/ Map[UMLPackage[Uml], UMLComment[Uml]] =
      (Map[UMLPackage[Uml], UMLComment[Uml]]()
        .right[NonEmptyList[java.lang.Throwable]] /: selectedPackages.to[Set]) {
        (acc, mdPkg) =>
          acc +++
            mdPkg
              .getSpecificationRootAnnotatingComment
              .map { doc =>
                doc
                  .fold[Map[UMLPackage[Uml], UMLComment[Uml]]]({
                  guiLog.log(s"${mdPkg.qualifiedName.get}: no OTI characterization")
                  Map()
                }) { d =>
                  guiLog.log(s"${mdPkg.qualifiedName.get}: has OTI characterization")
                  Map(mdPkg -> d)
                }
              }
      }

    implicit val otiCharacterizations: Option[Map[UMLPackage[Uml], UMLComment[Uml]]] =
      otiMDCharacterizations.toOption

    selectedPackages.foreach { pkg =>
      val anns = pkg.annotatedElement_comment
      guiLog.log(s"${pkg.qualifiedName.get}: annotating comments: ${anns.size}")
      guiLog.log(s"${pkg.qualifiedName.get}: URI: ${pkg.URI}")
      guiLog.log(s"${pkg.qualifiedName.get}: effective URI: ${pkg.getEffectiveURI()}")
      guiLog.log(s"${pkg.qualifiedName.get}: oti pkg URI: ${pkg.oti_packageURI()}")
      guiLog.log(s"${pkg.qualifiedName.get}: oti pkg nsPrefix: ${pkg.oti_nsPrefix()}")
      guiLog.log(s"${pkg.qualifiedName.get}: oti pkg uuidPrefix: ${pkg.oti_uuidPrefix()}")
      guiLog.log(s"${pkg.qualifiedName.get}: oti doc URL: ${pkg.oti_documentURL()}")

      val ann: String =
        pkg.getSpecificationRootAnnotatingComment
          .fold[String](
          l = (nels: NonEmptyList[java.lang.Throwable]) => {
            nels.toList.map(_.getMessage)
              .mkString(s"${nels.size} errors\n", "\n", "\n")
          },
          r = (c: Option[UMLComment[Uml]]) => {
            c.fold[String](
              "no annotation"
            ) { cc: UMLComment[Uml] =>
              s"OTI annotation... $cc"
            }
          })
      guiLog.log(s"${pkg.qualifiedName.get}: $ann")
    }

    val result1
    : NonEmptyList[java.lang.Throwable] \/ (CatalogURIMapper, CatalogURIMapper) =
      MDAPI.getMDCatalogs()

    val result2
    : NonEmptyList[java.lang.Throwable] \&/ Unit =
      result1
        .toThese
        .map { case (documentURIMapper, builtInURIMapper) =>

          val info =
            resolvedMagicDrawOTISymbols
            .toThese
            .flatMap{ mdOTISymbols =>
              val mdBuiltIns: Set[BuiltInDocument[Uml]] =
                Set( MDBuiltInPrimitiveTypes, MDBuiltInUML, MDBuiltInStandardProfile )

              val mdBuiltInEdges: Set[DocumentEdge[Document[Uml]]] =
                Set( MDBuiltInUML2PrimitiveTypes, MDBuiltInStandardProfile2UML )

              implicit val mdDocOps = new MagicDrawDocumentOps()(umlUtil, otiCharacterizations)

              val allPkgs: Set[UMLPackage[Uml]] =
                selectedPackages.to[Set] ++ getAllOTISerializableDocumentPackages(mdOTISymbols)

              allPkgs.foreach { pkg =>
                guiLog.log(s" ===> Pkg: ${pkg.qualifiedName.get}")
              }

              DocumentSet.constructDocumentSetCrossReferenceGraph[Uml](
                specificationRootPackages = allPkgs,
                documentURIMapper, builtInURIMapper,
                builtInDocuments = mdBuiltIns,
                builtInDocumentEdges = mdBuiltInEdges,
                ignoreCrossReferencedElementFilter = MDAPI.ignoreCrossReferencedElementFilter,
                unresolvedElementMapper = MDAPI.unresolvedElementMapper(umlUtil),
                aggregate = MagicDrawDocumentSetAggregate() )
                .flatMap { case (( resolved, unresolved )) =>
                  resolved.ds match {
                    case mdDS: MagicDrawDocumentSet =>
                      \&/.That((mdOTISymbols, resolved, mdDS, unresolved))
                  }
                }
            }

          info
            .map {
              case (otiSymbols: MagicDrawOTISymbols,
              rds: ResolvedDocumentSet[MagicDrawUML],
              ds: MagicDrawDocumentSet,
              xrefs: Iterable[UnresolvedElementCrossReference[MagicDrawUML]]) =>

                implicit val mdDocOps = new MagicDrawDocumentOps()
                implicit val idg: MagicDrawIDGenerator = MagicDrawIDGenerator(rds)

                selectedPackages.foreach { pkg =>
                  guiLog.log(s"# OTI info: ${pkg.qualifiedName.get}")
                  guiLog.log(s"-- is OTI Specification Root? ${DocumentSet.isPackageRootOfSpecificationDocument(pkg)}")

                }
                ()
            }
        }

    result2
      .a
      .fold[scala.util.Try[Option[MagicDrawValidationDataResults]]](
      scala.util.Success(Option.empty[MagicDrawValidationDataResults])
    ){ nels: NonEmptyList[java.lang.Throwable] =>
        val errors
        : Map[com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element, (String, List[com.nomagic.actions.NMAction])] =
          nels
            .toList
            .map { error =>
              p.getModel -> Tuple2(error.getMessage, List.empty[com.nomagic.actions.NMAction])
            }
            .toMap


        scala.util.Success(
          MagicDrawValidationDataResults.makeMDIllegalArgumentExceptionValidation(
            p, s"*** OTI/OMF Exporter Error ***",
            errors,
            "*::MagicDrawOTIValidation",
            "*::UnresolvedCrossReference"
          )
            .validationDataResults
            .some
        )
      }
  }

  def doit
  (p: Project,
   selection: java.util.Collection[Element])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] = {


    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    implicit def Package2CommentMapSemigroup
    : Semigroup[Map[UMLPackage[Uml], UMLComment[Uml]]] =
      Semigroup.instance(_ ++ _)

    val selectedPackages: List[UMLPackage[Uml]] =
      selection
        .toIterable
        .selectByKindOf { case p: Package => umlPackage(p) }
        .toList
        .sortBy(_.xmiElementLabel)

    val otiMDCharacterizations
    : NonEmptyList[java.lang.Throwable] \/ Map[UMLPackage[Uml], UMLComment[Uml]] =
      (Map[UMLPackage[Uml], UMLComment[Uml]]()
        .right[NonEmptyList[java.lang.Throwable]] /: selectedPackages.to[Set]) {
        (acc, mdPkg) =>
          acc +++
            mdPkg
              .getSpecificationRootAnnotatingComment
              .map { doc =>
                doc
                  .fold[Map[UMLPackage[Uml], UMLComment[Uml]]]({
                  guiLog.log(s"${mdPkg.qualifiedName.get}: no OTI characterization")
                  Map()
                }) { d =>
                  guiLog.log(s"${mdPkg.qualifiedName.get}: has OTI characterization")
                  Map(mdPkg -> d)
                }
              }
      }

    implicit val otiCharacterizations: Option[Map[UMLPackage[Uml], UMLComment[Uml]]] =
      otiMDCharacterizations.toOption

    selectedPackages.foreach { pkg =>
      val anns = pkg.annotatedElement_comment
      guiLog.log(s"${pkg.qualifiedName.get}: annotating comments: ${anns.size}")
      guiLog.log(s"${pkg.qualifiedName.get}: URI: ${pkg.URI}")
      guiLog.log(s"${pkg.qualifiedName.get}: effective URI: ${pkg.getEffectiveURI()}")
      guiLog.log(s"${pkg.qualifiedName.get}: oti pkg URI: ${pkg.oti_packageURI()}")
      guiLog.log(s"${pkg.qualifiedName.get}: oti pkg nsPrefix: ${pkg.oti_nsPrefix()}")
      guiLog.log(s"${pkg.qualifiedName.get}: oti pkg uuidPrefix: ${pkg.oti_uuidPrefix()}")
      guiLog.log(s"${pkg.qualifiedName.get}: oti doc URL: ${pkg.oti_documentURL()}")

      val ann: String =
        pkg.getSpecificationRootAnnotatingComment
          .fold[String](
          l = (nels: NonEmptyList[java.lang.Throwable]) => {
            nels.toList.map(_.getMessage)
              .mkString(s"${nels.size} errors\n", "\n", "\n")
          },
          r = (c: Option[UMLComment[Uml]]) => {
            c.fold[String](
              "no annotation"
            ) { cc: UMLComment[Uml] =>
              s"OTI annotation... $cc"
            }
          })
      guiLog.log(s"${pkg.qualifiedName.get}: $ann")
    }

    val result1
    : NonEmptyList[java.lang.Throwable] \/ (CatalogURIMapper, CatalogURIMapper) =
      MDAPI.getMDCatalogs()

    val result2
    : NonEmptyList[java.lang.Throwable] \&/ Unit =
      result1
        .toThese
        .map { case (documentURIMapper, builtInURIMapper) =>

          val info =
          MagicDrawDocumentSet
            .createMagicDrawProjectDocumentSet(
              additionalSpecificationRootPackages=selectedPackages.to[Set],
              documentURIMapper, builtInURIMapper,
              otiCharacterizations,
              ignoreCrossReferencedElementFilter = MDAPI.ignoreCrossReferencedElementFilter,
              unresolvedElementMapper = MDAPI.unresolvedElementMapper(umlUtil))

          info.a.fold[Unit](
            guiLog.log(s"MagicDrawDocumentSet.createMagicDrawProjectDocumentSet: no errors")
          ){ nels =>
            guiLog.log(s"MagicDrawDocumentSet.createMagicDrawProjectDocumentSet: ${nels.size} errors")
            nels.foreach { error =>
              guiLog.log(s"==> $error")
            }
          }

          info
            .map {
              case (otiSymbols: MagicDrawOTISymbols,
                    rds: ResolvedDocumentSet[MagicDrawUML],
                    ds: MagicDrawDocumentSet,
                    xrefs: Iterable[UnresolvedElementCrossReference[MagicDrawUML]]) =>

                implicit val mdDocOps = new MagicDrawDocumentOps()
                implicit val idg: MagicDrawIDGenerator = MagicDrawIDGenerator(rds)

                selectedPackages.foreach { pkg =>
                  guiLog.log(s"# OTI info: ${pkg.qualifiedName.get}")
                  guiLog.log(s"-- is OTI Specification Root? ${DocumentSet.isPackageRootOfSpecificationDocument(pkg)}")
                  guiLog.log(s"-- ${pkg.qualifiedName.get}: xmiID: ${pkg.xmiID()}")
                  guiLog.log(s"-- ${pkg.qualifiedName.get}: xmiUUID: ${pkg.xmiUUID()}")

                }
                ()
            }
        }

    val otiV = OTIMagicDrawValidation(p)
    otiV.toTryOptionMDValidationDataResults(p, "*** OTI Package Inspector ***", result2.a)
  }

}