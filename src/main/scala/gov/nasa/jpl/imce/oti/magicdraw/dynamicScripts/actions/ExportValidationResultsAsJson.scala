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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.actions

import java.awt.event.ActionEvent
import java.io.File
import java.lang.System

import com.nomagic.magicdraw.core.{Application, Project}
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes.MainToolbarMenuAction
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.json.magicDrawValidation.{DocumentValidationResults, RuleViolationDataResults}
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.{MDAPI, OTIHelper, ProjectValidationResultPanelInfo}
import org.omg.oti.json.common.OTIPrimitiveTypes
import org.omg.oti.json.extent.{DocumentLocation, ToolSpecificDocumentLocation}
import org.omg.oti.json.uml.serialization.OTIJsonElementHelper
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._
import play.api.libs.json._

import scala.collection.immutable._
import scala.{None, Option, StringContext}
import scala.Predef.augmentString
import scala.util.{Success, Try}

object ExportValidationResultsAsJson {

  /**
    * DynamicScript wrapper for [exportAllValidationWindowResultsForProfileAdapter]
    *
    * @param p An MD project (it must be open with zero or more MD validation result windows)
    * @param ev Java AWT Action Event
    * @param script The invoking Main Toolbar Menu DynamicScript
    * @return
    */
  def exportForProfileAdapter
  ( p: Project,
    ev: ActionEvent,
    script: MainToolbarMenuAction )
  : Try[Option[MagicDrawValidationDataResults]]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawAdapterForProfileCharacteristics(p),
    (oa: MagicDrawOTIProfileAdapter) => {

      val a = Application.getInstance()
      val guiLog = a.getGUILog
      guiLog.clearLog()

      val vpanels: Set[ProjectValidationResultPanelInfo] = MDAPI.getProjectValidationResults(p)
      guiLog.log(s"There are ${vpanels.size} validation result panels")

      saveFile(
        "Save MD Validation result panels to JSon archive",
        "Directory where to save the MD Validation results",
        ".zip").flatMap {
        _
          .fold[Try[Option[MagicDrawValidationDataResults]]] {
          guiLog.log("Cancelled")
          Success(None)
        } { file =>

          exportAllValidationWindowResultsForProfileAdapter(p, oa, file)
        }
      }
    })

  /**
    * For a given MD project, exports the contents of all the MD validation window results for that project to a file.
    *
    * @param p An MD project (it must be open with zero or more MD validation result windows)
    * @param oa A MagicDraw OTI Profile Adapter
    * @param validationResultsFile The exported file, a zip archive of json files, one for the results of each
    *                              currently open MD validation window for that project
    * @return
    */
  def exportAllValidationWindowResultsForProfileAdapter
  ( p: Project,
    oa: MagicDrawOTIProfileAdapter,
    validationResultsFile: File )
  : Try[Option[MagicDrawValidationDataResults]]
  = {
    implicit val ojeh: MagicDrawOTIJsonElementHelperForProfileAdapter = OTIJsonElementHelper(oa, None)

    val vpanels: Set[ProjectValidationResultPanelInfo] = MDAPI.getProjectValidationResults(p)

    val vpanelsByProject
    : Map[Project, Set[ProjectValidationResultPanelInfo]]
    = vpanels.groupBy(_.validatedProject)

    val vDocuments
    : Iterable[DocumentValidationResults]
    = vpanelsByProject.map { case (vProject, vps) =>
      DocumentValidationResults(
        documentLocation =
          ToolSpecificDocumentLocation(
            toolSpecificDocumentLocation =
              OTIPrimitiveTypes.TOOL_SPECIFIC_URL(vProject.getProjectLocationURI.toString)),
        validationResults =
          vps.map(_.toValidationResultsForProfileAdapter)
      )
    }

    val fos = new java.io.FileOutputStream(validationResultsFile)
    val bos = new java.io.BufferedOutputStream(fos, 100000)
    val cos = new java.util.zip.CheckedOutputStream(bos, new java.util.zip.Adler32())
    val zos = new java.util.zip.ZipOutputStream(new java.io.BufferedOutputStream(cos))

    zos.setMethod(java.util.zip.ZipOutputStream.DEFLATED)

    vDocuments.foreach { vDocument =>
      val entryName =
        OTIPrimitiveTypes.TOOL_SPECIFIC_URL.unwrap(
          DocumentLocation.toToolSpecificURL(vDocument.documentLocation))

      val entry = new java.util.zip.ZipEntry(entryName)
      zos.putNextEntry(entry)
      val s = Json.prettyPrint(Json.toJson(vDocument))
      zos.write(s.getBytes(java.nio.charset.Charset.forName("UTF-8")))

      zos.closeEntry()
    }

    zos.close()
    System.out.println(s"Saved ${vDocuments.size} validation results to $validationResultsFile")
    Success(None)
  }

  /**
    * DynamicScript wrapper for [exportAllValidationWindowResultsForDataAdapter]
    *
    * @param p An MD project (it must be open with zero or more MD validation result windows)
    * @param ev Java AWT Action Event
    * @param script The invoking Main Toolbar Menu DynamicScript
    * @return
    */
  def exportForDataAdapter
  ( p: Project,
    ev: ActionEvent,
    script: MainToolbarMenuAction )
  : Try[Option[MagicDrawValidationDataResults]]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawAdapterForDataCharacteristics(p),
    (oa: MagicDrawOTIDataAdapter) => {

      val a = Application.getInstance()
      val guiLog = a.getGUILog
      guiLog.clearLog()

      val vpanels: Set[ProjectValidationResultPanelInfo] = MDAPI.getProjectValidationResults(p)
      guiLog.log(s"There are ${vpanels.size} validation result panels")

      saveFile(
        "Save MD Validation result panels to JSon archive",
        "Directory where to save the MD Validation results",
        ".zip").flatMap {
        _
          .fold[Try[Option[MagicDrawValidationDataResults]]] {
          guiLog.log("Cancelled")
          Success(None)
        } { file =>

          exportAllValidationWindowResultsForDataAdapter(p, oa, file)
        }
      }
    })

  /**
    * For a given MD project, exports the contents of all the MD validation window results for that project to a file.
    *
    * @param p An MD project (it must be open with zero or more MD validation result windows)
    * @param oa A MagicDraw OTI Data Adapter
    * @param validationResultsFile The exported file, a zip archive of json files, one for the results of each
    *                              currently open MD validation window for that project
    * @return
    */
  def exportAllValidationWindowResultsForDataAdapter
  ( p: Project,
    oa: MagicDrawOTIDataAdapter,
    validationResultsFile: File )
  : Try[Option[MagicDrawValidationDataResults]]
  = {
    implicit val ojeh: MagicDrawOTIJsonElementHelperForDataAdapter = OTIJsonElementHelper(oa, None)

    val vpanels: Set[ProjectValidationResultPanelInfo] = MDAPI.getProjectValidationResults(p)

    val vpanelsByProject
    : Map[Project, Set[ProjectValidationResultPanelInfo]]
    = vpanels.groupBy(_.validatedProject)

    val vDocuments
    : Iterable[DocumentValidationResults]
    = vpanelsByProject.map { case (vProject, vps) =>
      DocumentValidationResults(
        documentLocation =
          ToolSpecificDocumentLocation(
            toolSpecificDocumentLocation =
              OTIPrimitiveTypes.TOOL_SPECIFIC_URL(vProject.getProjectLocationURI.toString)),
        validationResults =
          vps.map(_.toValidationResultsForDataAdapter)
      )
    }

    val fos = new java.io.FileOutputStream(validationResultsFile)
    val bos = new java.io.BufferedOutputStream(fos, 100000)
    val cos = new java.util.zip.CheckedOutputStream(bos, new java.util.zip.Adler32())
    val zos = new java.util.zip.ZipOutputStream(new java.io.BufferedOutputStream(cos))

    zos.setMethod(java.util.zip.ZipOutputStream.DEFLATED)

    vDocuments.foreach { vDocument =>
      val entryName =
        OTIPrimitiveTypes.TOOL_SPECIFIC_URL.unwrap(
          DocumentLocation.toToolSpecificURL(vDocument.documentLocation))

      val entry = new java.util.zip.ZipEntry(entryName)
      zos.putNextEntry(entry)
      val s = Json.prettyPrint(Json.toJson(vDocument))
      zos.write(s.getBytes(java.nio.charset.Charset.forName("UTF-8")))

      zos.closeEntry()
    }

    zos.close()
    System.out.println(s"Saved ${vDocuments.size} validation results to $validationResultsFile")
    Success(None)
  }
}