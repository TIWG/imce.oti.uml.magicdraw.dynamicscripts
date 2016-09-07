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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils

import java.awt.event.ActionEvent
import java.lang.Runnable
import javax.swing.SwingUtilities

import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.ProjectWindow
import com.nomagic.magicdraw.uml.symbols.shapes.{CommentView, PackageView}
import com.nomagic.magicdraw.validation.ui.ValidationResultPanel
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Comment, Element}
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.symbols.internal.SymbolHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.internal.MDValidationAPIHelper._
import gov.nasa.jpl.dynamicScripts.magicdraw.wildCardMatch
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.json.common.OTIPrimitiveTypes._
import org.omg.oti.uml.UMLError
import org.omg.oti.uml.canonicalXMI.UnresolvedElementCrossReference
import org.omg.oti.uml.read.api._

import scala.Predef.{ArrowAssoc, String}
import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.util.Try
import scalaz.Scalaz._
import scalaz._
import scala.{Boolean, None, Option, Some, StringContext, Tuple2, Unit}

@scala.deprecated("", "")
trait MDAPI {}

object MDAPI {

  def thrwoables2MagicDrawValidationDataResults
  (p: Project, message: String)
  (nels: Set[java.lang.Throwable])
  : Try[Option[MagicDrawValidationDataResults]] =
    scala.util.Success(
      Some(
        p.makeMDIllegalArgumentExceptionValidation(
          s"*** ${nels.size} catalog-related errors prevented the exporter to run ***",
          Map(p.getModel -> Tuple2( nels.map(_.getMessage).toList.mkString("\n"), Nil)),
          "*::MagicDrawOTIValidation",
          "*::UnresolvedCrossReference").validationDataResults))


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
  : Option[UMLElement[MagicDrawUML]]
  = TOOL_SPECIFIC_ID.unwrap(e.toolSpecific_id) match {
      // @todo ???
      //case "_UML_" => Some( umlUtil.MDBuiltInUML.scope )
      //case "_StandardProfile_" => Some( umlUtil.MDBuiltInStandardProfile.scope )
      case _ => None
    }

  def getComment(cv: CommentView)
  : Comment
  = cv.getElement.asInstanceOf[Comment]

  def getPackage(pv: PackageView) =
   getPackageOfView(pv)

  def getAllProfiles(p: Project)(implicit umlUtil: MagicDrawUMLUtil): Set[UMLProfile[MagicDrawUML]] = {
    import umlUtil._
    StereotypesHelper.getAllProfiles(p).to[Set]
  }

  def getProjectWindows
  (p: Project)
  : Set[ProjectWindow]
  = p.getWindows.to[Set]

  def getProjectValidationResults
  (p: Project)
  : Set[ProjectValidationResultPanelInfo]
  = getProjectWindows(p).flatMap { pw =>
    Option.apply(pw.getContent).flatMap { pwc =>
      pwc.getWindowComponent match {
        case vrp: ValidationResultPanel =>
          Some(ProjectValidationResultPanelInfo(
            validatedProject = vrp.getValidatedProject,
            validationID = vrp.getValidationID,
            validationRunData = vrp.getValidationRunData,
            results = vrp.getResults.to[Set],
            projectWindow = pw,
            uiPanel = vrp))
        case _ =>
          None
      }
    }
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