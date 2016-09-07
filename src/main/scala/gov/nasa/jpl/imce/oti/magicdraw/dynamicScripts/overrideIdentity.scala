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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent

import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField

import com.jidesoft.swing.JideBoxLayout
import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.task.{ProgressStatus, RunnableWithProgress}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil

import scala.collection.JavaConversions._
import scala.util.{Success, Try}
import scala.{Option,None,Some,StringContext,Unit}
import scala.Predef.augmentString

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object overrideIdentity {

  def doit(
    project: Project,
    ev: ActionEvent,
    script: DynamicScriptsTypes.BrowserContextMenuAction,
    tree: Tree,
    node: Node,
    e: Element,
    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( project, ev, selection )

  def doit(
    p: Project, ev: ActionEvent,
    script: DynamicScriptsTypes.DiagramContextMenuAction,
    dpe: DiagramPresentationElement,
    triggerView: PresentationElement,
    triggerElement: Element,
    selection: java.util.Collection[PresentationElement] ): Try[Option[MagicDrawValidationDataResults]] =
    doit( p, ev, selection flatMap { case v: PresentationElement => Some( v.getElement ) } )

  def doit(
    p: Project, ev: ActionEvent,
    selection: java.util.Collection[Element] ): Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog()
    guiLog.clearLog()

    val umlUtil = MagicDrawUMLUtil( p )
//    import umlUtil._

//    if (OTI_IDENTITY_S.isEmpty || OTI_IDENTITY_xmiID.isEmpty || OTI_IDENTITY_xmiUUID.isEmpty)
//      return Success( Some(
//        MagicDrawValidationDataResults.makeMDIllegalArgumentExceptionValidation(
//          p, "*** Missing OTI::Identity stereotype ***",
//          Map(p.getModel -> Tuple2("*** Missing OTI::Identity stereotype ***", Nil)),
//          "*::MagicDrawOTIValidation",
//          "*::UnresolvedCrossReference" ).validationDataResults ) )
//
//    val otiIdentityS = umlMagicDrawUMLStereotype(OTI_IDENTITY_S.get).getMagicDrawStereotype
//    val otiIdentityID = OTI_IDENTITY_xmiID.get.name.get
//    val otiIdentityUUID = OTI_IDENTITY_xmiUUID.get.name.get

    val runnable = new RunnableWithProgress() {

      def run( progressStatus: ProgressStatus ): Unit = {

        progressStatus.setCurrent( 0 )
        progressStatus.setMax( 0 )
        progressStatus.setMax( selection.size.toLong )
        progressStatus.setLocked( true )

        for {
          e <- selection
        } {
          val cpanel = new JPanel()
          cpanel.setLayout(new JideBoxLayout(cpanel, BoxLayout.Y_AXIS))

          cpanel.add(new JLabel(s"Override xmi:ID for ${e.getHumanName} : "), BorderLayout.BEFORE_LINE_BEGINS)

          val idField = new JTextField
          idField.setText("")
          idField.setColumns(120)
          idField.setEditable(true)
          idField.setFocusable(true)
          cpanel.add(idField)

          cpanel.add(new JLabel(s"Override xmi:ID for ${e.getHumanName} : "), BorderLayout.BEFORE_LINE_BEGINS)

          val uuidField = new JTextField
          uuidField.setText("")
          uuidField.setColumns(120)
          uuidField.setEditable(true)
          uuidField.setFocusable(true)
          cpanel.add(uuidField)

          cpanel.updateUI()

          val cstatus = JOptionPane.showConfirmDialog(
            Application.getInstance.getMainFrame,
            cpanel,
            s"Specify the xmi:ID, xmi:UUID or both for ${e.getHumanName}",
            JOptionPane.OK_CANCEL_OPTION)

          val xmiID = augmentString(idField.getText)
          val xmiUUID = augmentString(uuidField.getText)
          if (cstatus != JOptionPane.OK_OPTION || xmiID.isEmpty && xmiUUID.isEmpty) {
            guiLog.log(s"Skip ${e.getHumanName}")
          } else {
            guiLog.log(s"Override ${e.getHumanName} xmi:ID? ${xmiID.nonEmpty}, xmi:UUID? ${xmiUUID.nonEmpty}")
//            StereotypesHelper.addStereotype(e, otiIdentityS)
//            if (xmiID.nonEmpty) {
//              StereotypesHelper.setStereotypePropertyValue(e, otiIdentityS, otiIdentityID, xmiID.repr)
//              guiLog.log(s"Override ${e.getHumanName} xmi:ID=${xmiID.repr}")
//            }
//            if (xmiUUID.nonEmpty) {
//              StereotypesHelper.setStereotypePropertyValue(e, otiIdentityS, otiIdentityUUID, xmiUUID.repr)
//              guiLog.log(s"Override ${e.getHumanName} xmi:UUID=${xmiUUID.repr}")
//            }
          }

          progressStatus.increase()
        }

      }
    }

    MagicDrawProgressStatusRunner.runWithProgressStatus(
      runnable,
      s"Overriding OTI xmi:ID & xmi:UUID...",
      true, 0 )

    Success( None )
  }
}