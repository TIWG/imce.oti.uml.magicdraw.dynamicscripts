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

import java.awt.event.ActionEvent
import java.io.File
import java.lang.Runnable
import javax.swing.SwingUtilities

import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.core.{Application, ApplicationEnvironment, Project}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.internal.MDValidationAPIHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.wildCardMatch
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml.OTIPrimitiveTypes._
import org.omg.oti.uml.UMLError
import org.omg.oti.uml.canonicalXMI.{CatalogURIMapper, UnresolvedElementCrossReference}
import org.omg.oti.uml.read.api._

import scala.Predef.{ArrowAssoc,String}
import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.language.postfixOps
import scalaz.Scalaz._
import scalaz._

import scala.{Boolean,Option,None,Some,StringContext,Tuple2,Tuple3,Unit}
object MDAPI {


  /**
   * Ignore OMG production-related elements pertaining to OMG SysML 1.4 spec.
   */
  def ignoreCrossReferencedElementFilter( e: UMLElement[MagicDrawUML] ): Boolean = {
    e match {
      case ne: UMLNamedElement[MagicDrawUML] =>
        ne.qualifiedName
          .fold[Boolean](false) { neQName =>
          wildCardMatch(neQName, "UML Standard Profile::MagicDraw Profile::*") ||
            wildCardMatch(neQName, "Specifications::SysML.profileAnnotations::*")
        }

      case e =>
        false
    }
  }

  def unresolvedElementMapper
  (umlUtil: MagicDrawUMLUtil)
  ( e: UMLElement[MagicDrawUML] )
  : Option[UMLElement[MagicDrawUML]] =
    e.toolSpecific_id.map(OTI_ID.unwrap).fold[Option[UMLElement[MagicDrawUML]]](None) {
      // @todo ???
      //case "_UML_" => Some( umlUtil.MDBuiltInUML.scope )
      //case "_StandardProfile_" => Some( umlUtil.MDBuiltInStandardProfile.scope )
      case _ => None
    }

  def getMDCatalogs
  (omgCatalogResourcePath: String = "dynamicScripts/org.omg.oti/resources/omgCatalog/omg.local.catalog.xml",
   mdCatalogResourcePath: String = "dynamicScripts/org.omg.oti.magicdraw/resources/md18Catalog/omg.magicdraw.catalog.xml")
  : NonEmptyList[java.lang.Throwable] \/ (CatalogURIMapper, CatalogURIMapper) = {

    val defaultOMGCatalogFile =
      new File(MDUML.getApplicationInstallDir.toURI.resolve(omgCatalogResourcePath))
    val omgCatalog =
      if (defaultOMGCatalogFile.exists()) Seq(defaultOMGCatalogFile)
      else MagicDrawFileChooser.chooseCatalogFile("Select the OMG UML 2.5 *.catalog.xml file").to[Seq]

    val defaultMDCatalogFile =
      new File(MDUML.getApplicationInstallDir.toURI.resolve(mdCatalogResourcePath))
    val mdCatalog =
      if (defaultMDCatalogFile.exists()) Seq(defaultMDCatalogFile)
      else MagicDrawFileChooser.chooseCatalogFile("Select the MagicDraw UML 2.5 *.catalog.xml file").to[Seq]

    CatalogURIMapper.createMapperFromCatalogFiles(omgCatalog.to[Seq])
    .flatMap { omgCatalogMapper: CatalogURIMapper =>
        CatalogURIMapper.createMapperFromCatalogFiles(mdCatalog.to[Seq])
        .map { mdCatalogMapper: CatalogURIMapper =>
          (omgCatalogMapper, mdCatalogMapper)
        }
      }
  }

  def getPackage(pv: PackageView) =
   getPackageOfView(pv)

  def getAllProfiles(p: Project)(implicit umlUtil: MagicDrawUMLUtil): Set[UMLProfile[MagicDrawUML]] = {
    import umlUtil._
    StereotypesHelper.getAllProfiles(p).to[Set]
  }

  type MDValidationElementResults = Map[Element, Tuple2[String, List[NMAction]]]

  type NelThrowable = NonEmptyList[java.lang.Throwable]

  def error2MDElementMessage
  ( error: java.lang.Throwable )
  ( implicit umlUtil: MagicDrawUMLUtil, rootModel: Model )
  : Map[Element, List[String]] = error match {

    case ue: UMLError.UElementException[_, _] =>
      val umlElement = ue.element.head.asInstanceOf[UMLElement[umlUtil.Uml]]
      Map(umlUtil.umlMagicDrawUMLElement(umlElement).getMagicDrawElement -> (ue.toString :: Nil))

    case ue: UMLError.UException =>
      Map(rootModel -> (ue.getClass.getName + ": " + ue.toString :: Nil))
  }

  def showOTIUMLErrors
  ( p: Project,
    title: String,
    maybeErrors: UMLError.OptionThrowableNel )
  ( implicit umlUtil: MagicDrawUMLUtil )
  : Unit = {


    val a = Application.getInstance()
    val guiLog = a.getGUILog
    implicit val rootModel = p.getModel

    maybeErrors.fold[Unit](
      SwingUtilities.invokeLater(new Runnable {
        def run(): Unit = {
          guiLog.log(s"*** $title: 0 errors ***")
        }
      })
    ){ nels: UMLError.ThrowableNel =>
      val element2messageList: Map[Element, List[String]] =

        ( Map[Element, List[String]]() /: nels.toList ) { (acc, error) =>
          acc |+| error2MDElementMessage(error)
        }

      val element2messages: Map[Element, (String, List[NMAction])] =
        element2messageList.map { case (e, messages) =>
          e -> Tuple2(messages.mkString("\n"), List.empty[NMAction])
        }

      SwingUtilities.invokeLater(new Runnable {
        def run(): Unit = {
          guiLog.log(s"*** $title: ${element2messages.size} errors ***")
        }
      })

      val mdValidationDataResults =
        p.makeMDIllegalArgumentExceptionValidation(
          s"*** $title: ${element2messages.size} errors ***",
          element2messages,
          "*::MagicDrawOTIValidation",
          "*::UnresolvedCrossReference"
        )
          .validationDataResults

      p.showMDValidationDataResults(mdValidationDataResults)
    }

  }

  def showUnresolvedCrossReferencesAsMagicDrawValidationResults
  ( p: Project,
    unresolved: Iterable[UnresolvedElementCrossReference[MagicDrawUML]],
    ignoreCrossReferencedElementFilter: UMLElement[MagicDrawUML] => Boolean)
  (implicit umlUtil: MagicDrawUMLUtil)
  : Unit = {
    import umlUtil._

    val a = Application.getInstance()
    val guiLog = a.getGUILog

    val elementResults: MDValidationElementResults =
      unresolved.map {
        u: UnresolvedElementCrossReference[Uml] =>
          val uRef = u.relationTriple.obj
          val mdXRef = umlMagicDrawUMLElement(uRef).getMagicDrawElement
          val a = new NMAction(
            s"Select${u.hashCode}",
            s"Select ${mdXRef.getHumanType}: ${mdXRef.getHumanName}",
            0) {
            def actionPerformed(ev: ActionEvent): Unit =
              umlMagicDrawUMLElement(uRef).selectInContainmentTreeRunnable.run
          }
          val message =
            if (ignoreCrossReferencedElementFilter(uRef))
              "Filtered cross-reference to: "
            else
              "Unfiltered cross-reference to: "

          val fullMessage =
            message + s"${mdXRef.getHumanType}: ${mdXRef.getHumanName} (ID=${mdXRef.getID})"

          umlMagicDrawUMLElement(u.relationTriple.sub).getMagicDrawElement -> Tuple2(fullMessage, List(a))
      }.toMap

    if (elementResults.isEmpty) {

      SwingUtilities.invokeAndWait(new Runnable {
        def run(): Unit = {
          guiLog.log(s"*** OTI Document Graph analysis: 0 unresolved external document cross-reference errors ***")
        }
      })

    } else {

      SwingUtilities.invokeAndWait(new Runnable {
        def run(): Unit = {
          guiLog.log(s"*** OTI Document Graph analysis: ${unresolved.size} unresolved external document cross-references ***")
        }
      })

      val mdValidationDataResults =
        p.makeMDIllegalArgumentExceptionValidation(
          s"*** OTI Document Graph analysis: ${unresolved.size} unresolved cross-references ***",
          elementResults,
          "*::MagicDrawOTIValidation",
          "*::UnresolvedCrossReference"
        )
          .validationDataResults

      p.showMDValidationDataResults(mdValidationDataResults)
    }
  }
}