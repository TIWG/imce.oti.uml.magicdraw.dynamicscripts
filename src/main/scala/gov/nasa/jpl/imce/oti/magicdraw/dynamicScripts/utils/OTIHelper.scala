/*
 *
 * License Terms
 *
 * Copyright (c) 2014-2016, California Institute of Technology ("Caltech").
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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils

import org.omg.oti.magicdraw.uml.canonicalXMI._
import org.omg.oti.magicdraw.uml.characteristics.MagicDrawOTICharacteristicsProfileProvider
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml.{RelationTriple, UMLError}
import org.omg.oti.uml.canonicalXMI.{DocumentSet, CatalogURIMapper, UnresolvedElementCrossReference, ResolvedDocumentSet}
import org.omg.oti.uml.characteristics._
import org.omg.oti.uml.read.api.{UMLElement, UMLComment, UMLPackage}
import org.omg.oti.uml.xmi.Document

import scala.language.{implicitConversions, postfixOps}
import scala.collection.immutable._
import scalaz._

import scala.{Boolean,Option,None,StringContext}
import scala.Predef.{identity}

object OTIHelper {

  def initializeDocumentSet
  (implicit
   umlUtil: MagicDrawUMLUtil,
   otiCharacteristicsProvider: OTICharacteristicsProvider[MagicDrawUML])
  : UMLError.ThrowableNel \&/ MagicDrawDocumentSet
  = {

    for {
      mdCatalogManager <- MagicDrawCatalogManager.createMagicDrawCatalogManager().toThese
      otiInfo = MagicDrawOTIInfo(
        mdCatalogManager,
        umlUtil,
        otiCharacteristicsProvider)
      documentOps = new MagicDrawDocumentOps(otiInfo)

      ds <- documentOps.initializeDocumentSet()
    } yield ds
  }

  type OTIMDInfo =
  ( MagicDrawIDGenerator,
    ResolvedDocumentSet[MagicDrawUML],
    MagicDrawDocumentSet,
    Iterable[UnresolvedElementCrossReference[MagicDrawUML]])

  def getOTIMDInfo
  ()
  (implicit umlUtil: MagicDrawUMLUtil)
  : NonEmptyList[java.lang.Throwable] \/ OTIMDInfo = {
    val specificationRootPackages
    : Set[UMLPackage[MagicDrawUML]]
    = Set()

    val documentURIMapper
    : CatalogURIMapper
    = null

    val builtInURIMapper
    : CatalogURIMapper
    = null

    val ignoreCrossReferencedElementFilter
    : UMLElement[MagicDrawUML] => Boolean
    = (_: UMLElement[MagicDrawUML]) => false

    val unresolvedElementMapper
    : UMLElement[MagicDrawUML] => Option[UMLElement[MagicDrawUML]]
    = (_: UMLElement[MagicDrawUML]) => None

    val includeAllForwardRelationTriple
    : (Document[MagicDrawUML], RelationTriple[MagicDrawUML], Document[MagicDrawUML]) => Boolean
    = (_: Document[MagicDrawUML], _: RelationTriple[MagicDrawUML], _: Document[MagicDrawUML]) => true

    val element2documentTable
    : Map[UMLElement[MagicDrawUML], Document[MagicDrawUML]]
    = Map()

    getOTIMagicDrawInfo(
      specificationRootPackages,
      documentURIMapper,
      builtInURIMapper,
      ignoreCrossReferencedElementFilter,
      unresolvedElementMapper,
      includeAllForwardRelationTriple,
      element2documentTable) match {
      case \&/.This(errors) =>
        -\/(NonEmptyList[java.lang.Throwable](errors.head, errors.tail.to[Seq]: _*))
      case \&/.That(result) =>
        \/-(result)
      case \&/.Both(errors, _) =>
        -\/(NonEmptyList[java.lang.Throwable](errors.head, errors.tail.to[Seq]: _*))
    }
  }
  implicit def setThrowableSemigroup: Semigroup[Set[java.lang.Throwable]] =
  Semigroup.instance(_ ++ _)

  def getOTIMagicDrawInfo
  (specificationRootPackages: Set[UMLPackage[MagicDrawUML]],
   documentURIMapper: CatalogURIMapper,
   builtInURIMapper: CatalogURIMapper,
   ignoreCrossReferencedElementFilter: UMLElement[MagicDrawUML] => Boolean,
   unresolvedElementMapper: UMLElement[MagicDrawUML] => Option[UMLElement[MagicDrawUML]],
   includeAllForwardRelationTriple: (Document[MagicDrawUML], RelationTriple[MagicDrawUML], Document[MagicDrawUML]) => Boolean,
   element2documentTable: Map[UMLElement[MagicDrawUML], Document[MagicDrawUML]])
  (implicit umlUtil: MagicDrawUMLUtil)
  : Set[java.lang.Throwable] \&/ OTIMDInfo
  = MagicDrawCatalogManager.createMagicDrawCatalogManager().fold[Set[java.lang.Throwable] \&/ OTIMDInfo](
      l = (nels: UMLError.ThrowableNel) => \&/.This(nels.list.to[Set]),
      r = (mdCatalogMgr: MagicDrawCatalogManager) => {

        type ResolvedDocumentSet_UnresolvedCrossReferences =
        (ResolvedDocumentSet[MagicDrawUML], Iterable[UnresolvedElementCrossReference[MagicDrawUML]])

        val otiCharacterizationProfileProvider: OTICharacteristicsProfileProvider[MagicDrawUML] =
          MagicDrawOTICharacteristicsProfileProvider()(
            Option.empty[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]],
            umlUtil)

        val otiInfo = MagicDrawOTIInfo(mdCatalogMgr, umlUtil, otiCharacterizationProfileProvider)
        val documentOps = new MagicDrawDocumentOps(otiInfo)

        val ds1 =
          documentOps
            .initializeDocumentSet(documentURIMapper, builtInURIMapper)
            .leftMap(_.list.to[Set])

        val ds2 =
          ds1.flatMap { ds: MagicDrawDocumentSet =>
            val documents
            : Set[java.lang.Throwable] \&/ Set[MagicDrawDocument]
            = ds.documentOps.createDocumentsFromExistingRootPackages(specificationRootPackages)

            val result
            : Set[java.lang.Throwable] \&/ MagicDrawDocumentSet
            = documents
              .flatMap { mdocs: Set[MagicDrawDocument] =>
                val docsm: Set[Document[MagicDrawUML]] = for {mdoc <- mdocs} yield mdoc
                val dsm: Set[java.lang.Throwable] \&/ MagicDrawDocumentSet =
                  documentOps
                    .addDocuments(ds, docsm)
                  .flatMap {
                    case mdSet: MagicDrawDocumentSet =>
                      \&/.That(mdSet)
                    case dSet =>
                      \&/.This(Set[java.lang.Throwable](
                        UMLError.umlAdaptationError(s"The document set, $dSet, should be a MagicDrawDocumentSet")
                      ))
                  }
                dsm
              }
            result
          }

        val result =
          ds2.flatMap { ds: MagicDrawDocumentSet =>
            ds
              .resolve(ignoreCrossReferencedElementFilter, unresolvedElementMapper, includeAllForwardRelationTriple)
              .leftMap(_.list.to[Set])
              .map { case (rds, unresolved) =>
                val idg = MagicDrawIDGenerator(element2documentTable)(umlUtil, ds, documentOps)
                (idg, rds, ds, unresolved)

              }
          }

        result
      }
  )
}