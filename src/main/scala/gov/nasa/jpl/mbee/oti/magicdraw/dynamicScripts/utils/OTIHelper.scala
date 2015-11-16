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
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.utils

import com.nomagic.magicdraw.core.Project


import org.omg.oti.uml.UMLError
import org.omg.oti.uml.canonicalXMI._
import org.omg.oti.uml.characteristics._
import org.omg.oti.uml.read.api._
import org.omg.oti.uml.read.operations._

import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.magicdraw.uml.canonicalXMI._
import org.omg.oti.magicdraw.uml.characteristics._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.language.{implicitConversions, postfixOps}
import scalaz._, Scalaz._


object OTIHelper {

  type OTIMDInfo =
  (MagicDrawIDGenerator,
    ResolvedDocumentSet[MagicDrawUML],
    MagicDrawDocumentSet,
    Iterable[UnresolvedElementCrossReference[MagicDrawUML]])

  def getOTIMDInfo
  (additionalSpecificationRootPackages: Option[Set[UMLPackage[MagicDrawUML]]] = None)
  (implicit umlUtil: MagicDrawUMLUtil)
  : UMLError.ThrowableNel \/ OTIMDInfo
  = {

    import umlUtil._

    implicit val otiCharacterizations: Option[Map[UMLPackage[Uml], UMLComment[Uml]]] =
      None

    implicit val otiCharacterizationProfileProvider: OTICharacteristicsProfileProvider[MagicDrawUML] =
      MagicDrawOTICharacteristicsProfileProvider()

    implicit val documentOps = new MagicDrawDocumentOps()

    MDAPI.getMDCatalogs().flatMap {
      case (documentURIMapper, builtInURIMapper) =>

        val info =
          MagicDrawDocumentSet
          .createMagicDrawProjectDocumentSet(
            additionalSpecificationRootPackages,
            documentURIMapper, builtInURIMapper,
            ignoreCrossReferencedElementFilter = MDAPI.ignoreCrossReferencedElementFilter,
            unresolvedElementMapper = MDAPI.unresolvedElementMapper(umlUtil))

        info.a.fold[UMLError.ThrowableNel \/ OTIMDInfo](
          info.b.fold[UMLError.ThrowableNel \/ OTIMDInfo](
            NonEmptyList(UMLError.umlAdaptationError("Failed to resolve OTI/MD Info")).left
          ){
            case (rds: ResolvedDocumentSet[MagicDrawUML],
            ds: MagicDrawDocumentSet,
            xrefs: Iterable[UnresolvedElementCrossReference[MagicDrawUML]]) =>

              val idg: MagicDrawIDGenerator = MagicDrawIDGenerator(rds)

              (idg, rds, ds, xrefs).right
          }
        ){ nels =>
          nels.left
        }
    }
  }
}