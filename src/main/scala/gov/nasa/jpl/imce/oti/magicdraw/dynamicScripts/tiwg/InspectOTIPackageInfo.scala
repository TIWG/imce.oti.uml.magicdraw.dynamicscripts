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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.tiwg

import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.MDAPI
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation
import org.omg.oti.magicdraw.uml.canonicalXMI._
import org.omg.oti.magicdraw.uml.characteristics._
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml.UMLError
import org.omg.oti.uml.canonicalXMI._
import org.omg.oti.uml.read.api._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scalaz.Scalaz._
import scalaz._
import scala.Predef.String
import scala.{None, Option, StringContext, Unit}

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
    doit(p, selection flatMap { case pv: PackageView => getPackageOfView(pv) })

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Profile,
   selection: java.util.Collection[PresentationElement])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] =
    doit(p, selection flatMap { case pv: PackageView => getPackageOfView(pv) })

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Model,
   selection: java.util.Collection[PresentationElement])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] =
    doit(p, selection flatMap { case pv: PackageView => getPackageOfView(pv) })

  def doit
  (p: Project,
   selection: java.util.Collection[Element])
  : scala.util.Try[Option[MagicDrawValidationDataResults]] =
    MagicDrawCatalogManager
      .createMagicDrawCatalogManager()
      .fold[scala.util.Try[Option[MagicDrawValidationDataResults]]](
      l = (nels: UMLError.ThrowableNel) =>
        MDAPI.thrwoables2MagicDrawValidationDataResults(p, "InspectOTIPackageInfo")(nels),
      r = (mdCatalogMgr: MagicDrawCatalogManager) =>
        doit(p, selection, mdCatalogMgr))

  def doit
  (p: Project,
   selection: java.util.Collection[Element],
   mdCatalogMgr: MagicDrawCatalogManager)
  : scala.util.Try[Option[MagicDrawValidationDataResults]]
  = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog
    guiLog.clearLog()

    val otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] = None

    MagicDrawOTIAdapters.initializeWithProfileCharacterizations(p, otiCharacterizations)()
      .fold[scala.util.Try[Option[MagicDrawValidationDataResults]]](
      (nels: Set[java.lang.Throwable]) =>
        scala.util.Failure(nels.head),
      (oa: MagicDrawOTIProfileAdapter) => {

        implicit val umlOps = oa.umlOps

        val result
        : Set[java.lang.Throwable] \&/ Unit
        = for {
          odsa <- MagicDrawOTIAdapters.withInitialDocumentSetForProfileAdapter(oa)

          selectedPackages = {
            implicit val umlOps: MagicDrawUMLUtil = oa.umlOps
            import umlOps._

            selection
              .to[Seq]
              .selectByKindOf { case pkg: Package => umlPackage(pkg) }
              .sortBy(_.xmiElementLabel)
          }

          _ = {
            oa.otiCharacteristicsProvider match {
              case otiProfileCharacteristicsProvider: MagicDrawOTICharacteristicsProfileProvider =>

                implicit val otiChProvider = otiProfileCharacteristicsProvider
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
                    otiProfileCharacteristicsProvider.getSpecificationRootAnnotatingComment(pkg)
                      .fold[String](
                      l = (nels: Set[java.lang.Throwable]) => {
                        nels.toList.map(_.getMessage)
                          .mkString(s"${nels.size} errors\n", "\n", "\n")
                      },
                      r = (c: Option[UMLComment[MagicDrawUML]]) => {
                        c.fold[String](
                          "no annotation"
                        ) { cc: UMLComment[MagicDrawUML] =>
                          s"OTI annotation... $cc"
                        }
                      })
                  guiLog.log(s"${pkg.qualifiedName.get}: $ann")
                }
              case _ =>
                ()
            }
            ()
          }

          documents <- odsa.documentOps.createDocumentsFromExistingRootPackages(
            allRoots = selectedPackages.to[Set],
            extentOfPkg = MagicDrawOTIHelper.defaultExtentOfPkg)

          odsa2 <- odsa.documentOps.addDocuments(odsa.ds, documents).flatMap {
            case mdSet: MagicDrawDocumentSet =>
              \&/.That(odsa.copy(ds = mdSet))
            case dSet =>
              \&/.This(Set[java.lang.Throwable](
                UMLError.umlAdaptationError(s"The document set, $dSet, should be a MagicDrawDocumentSet")
              ))
          }

        } yield {

          implicit val idg: MagicDrawIDGenerator = MagicDrawIDGenerator()(odsa2.ds)

          implicit val otiChProvider = oa.otiCharacteristicsProvider

          selectedPackages.foreach { pkg =>
            guiLog.log(s"# OTI info: ${pkg.qualifiedName.get}")
            guiLog.log(s"-- is OTI Specification Root? ${DocumentSet.isPackageRootOfSpecificationDocument(pkg)}")
            guiLog.log(s"-- ${pkg.qualifiedName.get}: xmiID: ${pkg.xmiID()}")
            guiLog.log(s"-- ${pkg.qualifiedName.get}: xmiUUID: ${pkg.xmiUUID()}")
          }

          ()
        }

        val otiV = OTIMagicDrawValidation(p)
        otiV.errorSet2TryOptionMDValidationDataResults(p, "*** OTI Package Inspector ***", result.a)

      })
  }
}