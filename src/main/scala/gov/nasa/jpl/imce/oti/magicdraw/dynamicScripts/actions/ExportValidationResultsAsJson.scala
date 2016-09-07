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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.actions

import java.awt.event.ActionEvent
import java.io.File
import java.lang.System

import com.nomagic.magicdraw.core.{Application, Project}
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes.MainToolbarMenuAction
import gov.nasa.jpl.dynamicScripts.magicdraw.utils.MDUML._
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.json.magicDrawValidation.{DocumentValidationResults}
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

      // convert the string in 10K chunks.
      for ( segment <- s.grouped(10240) )
        zos.write(segment.getBytes(java.nio.charset.Charset.forName("UTF-8")))

      zos.closeEntry()
    }

    zos.close()
    System.out.println(s"Saved ${vDocuments.size} validation results to $validationResultsFile")
    Success(None)
  }
}