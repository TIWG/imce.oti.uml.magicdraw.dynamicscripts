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
