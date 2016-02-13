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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts

import java.awt.event.ActionEvent

import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField

import com.jidesoft.swing.JideBoxLayout
import com.nomagic.magicdraw.core.{Application, Project}
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.UUIDRegistry
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.task.{ProgressStatus, RunnableWithProgress}
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.MDAPI
import org.omg.oti.magicdraw.uml.read.MagicDrawUMLUtil

import scala.collection.JavaConversions._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Success, Try}
import scala.{Option,None,Some,StringContext,Unit}
import scala.Predef.{augmentString,ArrowAssoc}

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