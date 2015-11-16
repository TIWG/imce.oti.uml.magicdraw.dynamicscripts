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

import java.io.File
import javax.swing.filechooser.FileFilter
import javax.swing.{JFileChooser, SwingUtilities}

import com.nomagic.magicdraw.core.{Application, ApplicationEnvironment, Project}
import com.nomagic.magicdraw.ui.browser.BrowserTabTree
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.magicdraw.utils.MDLog
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import gov.nasa.jpl.dynamicScripts.magicdraw.{MagicDrawValidationDataResults, DynamicScriptsPlugin}
import org.apache.log4j.Logger


import org.omg.oti.magicdraw.uml.canonicalXMI._
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml.canonicalXMI.CatalogURIMapper
import org.omg.oti.magicdraw.uml.canonicalXMI._
import org.omg.oti.uml.OTIPrimitiveTypes._
import org.omg.oti.uml.read.api._

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.language.postfixOps
import scalaz._, Scalaz._
import scala.util.{Failure, Success, Try}

object MDAPI {


  /**
   * Ignore OMG production-related elements pertaining to OMG SysML 1.4 spec.
   */
  def ignoreCrossReferencedElementFilter( e: UMLElement[MagicDrawUML] ): Boolean = {
    e match {
      case ne: UMLNamedElement[MagicDrawUML] =>
        ne.qualifiedName
          .fold[Boolean](false) { neQName =>
          DynamicScriptsPlugin.wildCardMatch(neQName, "UML Standard Profile::MagicDraw Profile::*") ||
            DynamicScriptsPlugin.wildCardMatch(neQName, "Specifications::SysML.profileAnnotations::*")
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
      case "_UML_" => Some( umlUtil.MDBuiltInUML.scope )
      case "_StandardProfile_" => Some( umlUtil.MDBuiltInStandardProfile.scope )
      case _ => None
    }

  def getMDCatalogs
  (omgCatalogResourcePath: String = "dynamicScripts/org.omg.oti/resources/omgCatalog/omg.local.catalog.xml",
   mdCatalogResourcePath: String = "dynamicScripts/org.omg.oti.magicdraw/resources/md18Catalog/omg.magicdraw.catalog.xml")
  : NonEmptyList[java.lang.Throwable] \/ (CatalogURIMapper, CatalogURIMapper) = {

    val defaultOMGCatalogFile =
      new File(
        new File(
          ApplicationEnvironment.getInstallRoot).
          toURI.resolve(omgCatalogResourcePath))
    val omgCatalog =
      if (defaultOMGCatalogFile.exists()) Seq(defaultOMGCatalogFile)
      else MagicDrawFileChooser.chooseCatalogFile("Select the OMG UML 2.5 *.catalog.xml file").to[Seq]

    val defaultMDCatalogFile =
      new File(
        new File(ApplicationEnvironment.getInstallRoot).
          toURI.resolve(mdCatalogResourcePath))
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

  def getMDPluginsLog(): Logger =
    MDLog.getPluginsLog()

  def getApplicationRoot(): String =
    ApplicationEnvironment.getInstallRoot()

  def getApplicationInstallDir(): File =
    new File(ApplicationEnvironment.getInstallRoot)

  def getProjectActiveBrowserTabTree( p: Project ): BrowserTabTree =
    p.getBrowser.getActiveTree

  def getPackage(pv: PackageView) =
    Option.apply(pv.getPackage)

  def getPrimaryProjectID(p: Project): String =
    p.getPrimaryProject.getProjectID

  def getAllProfiles(p: Project)(implicit umlUtil: MagicDrawUMLUtil): Set[UMLProfile[MagicDrawUML]] = {
    import umlUtil._
    StereotypesHelper.getAllProfiles(p).toSet[MagicDrawUML#Profile]
  }

  /**
   *
   */
  def chooseFile
  (title: String,
   description: String,
   fileNameSuffix: String,
   dir: File = getApplicationInstallDir())
  : Try[Option[File]] =

    Try {
      var result: Option[File] = None

      def chooser = new Runnable {
        override def run(): Unit = {

          val ff = new FileFilter() {

            def getDescription: String = description

            def accept(f: File): Boolean =
              f.isDirectory ||
                (f.isFile && f.getName.endsWith(fileNameSuffix))

          }

          val fc = new JFileChooser(dir) {

            override def getFileSelectionMode: Int = JFileChooser.FILES_ONLY

            override def getDialogTitle = title
          }

          fc.setFileFilter(ff)
          fc.setFileHidingEnabled(true)
          fc.setAcceptAllFileFilterUsed(false)

          fc.showOpenDialog(Application.getInstance().getMainFrame) match {
            case JFileChooser.APPROVE_OPTION =>
              val openFile = fc.getSelectedFile
              result = Some(openFile)
            case _ =>
              result = None
          }
        }
      }

      if (SwingUtilities.isEventDispatchThread) chooser.run
      else SwingUtilities.invokeAndWait(chooser)

      result
    }

  /**
   *
   */
  def saveFile
  (title: String,
   description: String,
   fileNameSuffix: String,
   dir: File = getApplicationInstallDir())
  : Try[Option[File]] =

    Try {
      var result: Option[File] = None

      def chooser = new Runnable {
        override def run(): Unit = {

          val ff = new FileFilter() {

            def getDescription: String = description

            def accept(f: File): Boolean =
              f.isDirectory ||
                (f.isFile && f.getName.endsWith(fileNameSuffix))

          }

          val fc = new JFileChooser(dir) {

            override def getFileSelectionMode: Int = JFileChooser.FILES_ONLY

            override def getDialogTitle = title
          }

          fc.setFileFilter(ff)
          fc.setFileHidingEnabled(true)
          fc.setAcceptAllFileFilterUsed(false)

          fc.showSaveDialog(Application.getInstance().getMainFrame) match {
            case JFileChooser.APPROVE_OPTION =>
              val saveFile = fc.getSelectedFile
              result = Some(saveFile)
            case _ =>
              result = None
          }
        }
      }

      if (SwingUtilities.isEventDispatchThread) chooser.run
      else SwingUtilities.invokeAndWait(chooser)

      result
    }

}