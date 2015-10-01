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
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts

import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.{BoxLayout, JLabel, JOptionPane, JPanel, JTextField}

import com.jidesoft.swing.JideBoxLayout
import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.core.{Application, ApplicationEnvironment, Project, ProjectUtilities}
import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
import com.nomagic.magicdraw.ui.browser.{Node, Tree}
import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
import com.nomagic.task.{ProgressStatus, RunnableWithProgress}
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.{DynamicScriptsPlugin, MagicDrawValidationDataResults}
import gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.utils._
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.xmi.XMLResource
import org.omg.oti.changeMigration._
import org.omg.oti.magicdraw.uml.canonicalXMI._
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml._
import org.omg.oti.uml.canonicalXMI._
import org.omg.oti.uml.read.api._
import org.omg.oti.uml.xmi._
import org.omg.oti.magicdraw.uml.canonicalXMI._
import org.omg.oti.uml.canonicalXMI._
import org.omg.oti.magicdraw.uml.read._

import scala.collection.immutable._
import scala.collection.JavaConversions.{asJavaCollection, collectionAsScalaIterable, mapAsJavaMap}
import scala.reflect.runtime.universe._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}

/**
 * @author Nicolas.F.Rouquette@jpl.nasa.gov
 */
object generateIDsForPackageExtents {

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.BrowserContextMenuAction,
   tree: Tree, node: Node,
   pkg: Package, selection: java.util.Collection[Element])
  : Try[Option[MagicDrawValidationDataResults]] =
    doit(p, ev, selection)

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.BrowserContextMenuAction,
   tree: Tree, node: Node,
   pkg: Profile, selection: java.util.Collection[Element])
  : Try[Option[MagicDrawValidationDataResults]] =
    doit(p, ev, selection)

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Package,
   selection: java.util.Collection[PresentationElement])
  : Try[Option[MagicDrawValidationDataResults]] =
    doit(p, ev, selection flatMap { case pv: PackageView => Some(pv.getPackage) })

  def doit
  (p: Project, ev: ActionEvent,
   script: DynamicScriptsTypes.DiagramContextMenuAction,
   dpe: DiagramPresentationElement,
   triggerView: PackageView,
   triggerElement: Profile,
   selection: java.util.Collection[PresentationElement])
  : Try[Option[MagicDrawValidationDataResults]] =
    doit(p, ev, selection flatMap { case pv: PackageView => Some(pv.getPackage) })

  def doit
  (p: Project, ev: ActionEvent,
   selection: java.util.Collection[Element])
  : Try[Option[MagicDrawValidationDataResults]] = {

    val a = Application.getInstance()
    val guiLog = a.getGUILog()
    guiLog.clearLog()

    val mdInstallRoot = ApplicationEnvironment.getInstallRoot
    val mdInstallDir = new File(mdInstallRoot)
    require(mdInstallDir.exists && mdInstallDir.isDirectory)

    val pp = p.getPrimaryProject
    if (!ProjectUtilities.isStandardSystemProfile(pp))
      return Failure(new IllegalArgumentException(s"The project must be a standard/system profile project"))

    val uri = pp.getLocationURI
    require(uri.isFile)

    val modulePath = uri.deresolve(URI.createFileURI(mdInstallRoot))
    require(modulePath.isRelative)

    val panel = new JPanel()
    panel.setLayout(new JideBoxLayout(panel, BoxLayout.Y_AXIS))

    panel.add(new JLabel("Enter MD root-relative path of the previous version of the Standard/System Profile module: "), BorderLayout.BEFORE_LINE_BEGINS)

    val modulePathField = new JTextField
    modulePathField.setText(modulePath.path)
    modulePathField.setColumns(modulePath.path.length() + 10)
    modulePathField.setEditable(true)
    modulePathField.setFocusable(true)
    panel.add(modulePathField)

    panel.updateUI()

    val status = JOptionPane.showConfirmDialog(
      Application.getInstance.getMainFrame,
      panel,
      "Specify the relative path of the previous module version",
      JOptionPane.OK_CANCEL_OPTION)

    val projectFilename = augmentString(modulePathField.getText)
    if (status != JOptionPane.OK_OPTION || projectFilename.isEmpty) {
      guiLog.log("Cancelled")
      return Success(None)
    }

    implicit val umlUtil = MagicDrawUMLUtil(p)
    import umlUtil._

    implicit val otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] =
      None

    implicit val mdDocOps = new MagicDrawDocumentOps()

    val selectedPackages: Set[UMLPackage[Uml]] =
      selection.toIterable
      .selectByKindOf { case p: Package => umlPackage(p) }
      .to[Set]

    MDAPI
      .getMDCatalogs()
      .flatMap { case (documentURIMapper, builtInURIMapper) =>

        var result: Option[Try[Option[MagicDrawValidationDataResults]]] = None
        val runnable = new RunnableWithProgress() {

          def run(progressStatus: ProgressStatus): Unit =
            result = Some(
              generateIDs(
                progressStatus,
                p, mdInstallDir, projectFilename,
                selectedPackages,
                documentURIMapper, builtInURIMapper,
                ignoreCrossReferencedElementFilter = MDAPI.ignoreCrossReferencedElementFilter,
                unresolvedElementMapper = MDAPI.unresolvedElementMapper(umlUtil)) )

        }

        MagicDrawProgressStatusRunner.runWithProgressStatus(
          runnable,
          s"Generating OTI Canonical XMI:IDs for ${selectedPackages.size} packages...",
          true, 0)

        require(result.isDefined)
        result.get
    }
  }

  def generateIDs
  (progressStatus: ProgressStatus,
   p: Project, mdInstallDir: File, projectFilename: String,
   specificationRootPackages: Set[UMLPackage[MagicDrawUML]],
   documentURIMapper: CatalogURIMapper,
   builtInURIMapper: CatalogURIMapper,
   ignoreCrossReferencedElementFilter: Function1[UMLElement[MagicDrawUML], Boolean],
   unresolvedElementMapper: Function1[UMLElement[MagicDrawUML], Option[UMLElement[MagicDrawUML]]])
  (implicit umlUtil: MagicDrawUMLUtil,
    mdTag: TypeTag[IllegalElementException[MagicDrawUML, _]],
    tag: TypeTag[IllegalElementException[MagicDrawUML, UMLElement[MagicDrawUML]]])
  : Try[Option[MagicDrawValidationDataResults]] = {
    import umlUtil._

    val a = Application.getInstance()
    val guiLog = a.getGUILog

    progressStatus.setCurrent(0)
    progressStatus.setMax(0)
    progressStatus.setMax(specificationRootPackages.size + 1)
    progressStatus.setLocked(true)

    // @todo populate...
    implicit val otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] =
      None

    val mdBuiltIns: Set[BuiltInDocument[Uml]] =
      Set( MDBuiltInPrimitiveTypes, MDBuiltInUML, MDBuiltInStandardProfile )

    val mdBuiltInEdges: Set[DocumentEdge[Document[Uml]]] =
      Set( MDBuiltInUML2PrimitiveTypes, MDBuiltInStandardProfile2UML )

    implicit val mdDocOps = new MagicDrawDocumentOps()

    DocumentSet.constructDocumentSetCrossReferenceGraph[Uml](
      specificationRootPackages,
      documentURIMapper, builtInURIMapper,
      builtInDocuments = mdBuiltIns,
      builtInDocumentEdges = mdBuiltInEdges,
      ignoreCrossReferencedElementFilter,
      unresolvedElementMapper,
      aggregate = MagicDrawDocumentSetAggregate() )
      .flatMap { case (( resolved, unresolved )) =>

      implicit val mdIdGenerator: MagicDrawIDGenerator = MagicDrawIDGenerator(resolved)

      val unresolvedResult: Option[MagicDrawValidationDataResults] =
        if ( unresolved.nonEmpty ) {

        guiLog.log(s"*** ${unresolved.size} unresolved cross-references ***")
        val elementMessages = unresolved map { u =>
          val mdXRef = umlMagicDrawUMLElement(u.externalReference).getMagicDrawElement
          val a = new NMAction(
            s"Select${u.hashCode}",
            s"Select ${mdXRef.getHumanType}: ${mdXRef.getHumanName}", 0) {
            def actionPerformed(ev: ActionEvent): Unit =
              umlMagicDrawUMLElement(u.externalReference).selectInContainmentTreeRunnable.run()
          }
          umlMagicDrawUMLElement(u.documentElement).getMagicDrawElement ->
            Tuple2(s"cross-reference to: ${mdXRef.getHumanType}: ${mdXRef.getHumanName} (ID=${mdXRef.getID})",
              List(a))
        } toMap

        Some(
          MagicDrawValidationDataResults.makeMDIllegalArgumentExceptionValidation(
            p, s"*** ${unresolved.size} unresolved cross-references ***",
            elementMessages,
            "*::MagicDrawOTIValidation",
            "*::UnresolvedCrossReference").validationDataResults)

      } else
        None

        val mdErrors = for {
          pkg <- specificationRootPackages
          _ = progressStatus.increase()
          _ = progressStatus.setDescription(s"Generating OTI Canonical XMI:IDs for '${pkg.name.get}'...")
          _ = System.out.println(s"Generating OTI Canonical XMI:IDs for '${pkg.name.get}'...")
          e <- pkg.allOwnedElements
          mdError = mdIdGenerator
            .getXMI_ID(e)
            .transform[Option[IllegalElementException[MagicDrawUML, _]]](
            s = { (_: String) => Success(None) },
            f = {
              case t: IllegalElementException[MagicDrawUML, _] =>
                System.out.println(s"MD ID Generation: ${t.element.head}")
                Success(Some(t))
              case _ =>
                Success(None)
            })
        } yield mdError

        val elementIDs = mdIdGenerator.getElement2IDMap
        val errors = elementIDs filter (_._2.isFailure)
        if (errors.nonEmpty) {
          guiLog.log(s"${errors.size} errors when computing OTI XMI IDs for the package extent(s) of:")
          for {pkg <- specificationRootPackages} {
            guiLog.log(s" => ${pkg.qualifiedName.get}")
          }
          (errors.keys toList) sortBy (_.toolSpecific_id) foreach { e =>
            val t = errors(e).failed.get
            if (errors.size > 100) System.out.println(s" ${e.toolSpecific_id} => $t")
            else guiLog.addHyperlinkedText(
              s" <A>${e.toolSpecific_id}</A> => $t",
              Map(e.toolSpecific_id.get -> umlMagicDrawUMLElement(e).selectInContainmentTreeRunnable))
          }
        }
        else {
          guiLog.log(s"No errors when computing OTI XMI IDs for the package extent(s) of:")
          for {pkg <- specificationRootPackages} {
            guiLog.log(s" => ${pkg.qualifiedName.get}")
          }
        }

        progressStatus.increase()
        progressStatus.setDescription(s"Constructing old/new migration map...")

        val id2element = elementIDs filter { case (e, _) =>
          specificationRootPackages.exists(p => p.isAncestorOf(e))
        } flatMap {
          case (e, Success(newID)) => Some(newID -> e)
          case (_, _) => None
        }
        val sortedIDs = id2element.keys.toList filter {
          !_.contains("appliedStereotypeInstance")
        } sorted

        val otiChangeMigrationDir = new File(mdInstallDir, "dynamicScripts/org.omg.oti.changeMigration/resources")
        require(otiChangeMigrationDir.exists && otiChangeMigrationDir.isDirectory)
        val migrationMM = Metamodel(otiChangeMigrationDir)

        val old2newMapping = migrationMM.makeOld2NewIDMapping(projectFilename)
        val old2newDeltaMapping = migrationMM.makeOld2NewIDMapping(projectFilename)

        System.out.println(s" elementIDs has ${elementIDs.size} entries")
        System.out.println(s" element2id map has ${sortedIDs.size} entries")
        guiLog.log(s" element2id map has ${sortedIDs.size} entries")
        val tooLong = sortedIDs.size > 500
        for {
          n <- sortedIDs.indices
          id = sortedIDs(n)
          e = id2element(id)
        } {
          val oldID = e.toolSpecific_id.get
          val old2newEntry = migrationMM.makeOld2NewIDEntry
          old2newEntry.setOldID(oldID)
          old2newEntry.setNewID(id)
          old2newMapping.addEntry(old2newEntry)

          if (id != oldID) {
            val old2newDelta = migrationMM.makeOld2NewIDEntry
            old2newDelta.setOldID(oldID)
            old2newDelta.setNewID(id)
            old2newDeltaMapping.addEntry(old2newDelta)
          }
          if (!tooLong)
            guiLog.addHyperlinkedText(
              s" $n: <A>$id</A> => $oldID",
              Map(id -> e.asInstanceOf[MagicDrawUMLElement].selectInContainmentTreeRunnable))
        }

        val dir = new File(project.getDirectory)
        require(dir.exists() && dir.isDirectory)

        val options = Map(XMLResource.OPTION_ENCODING -> "UTF-8")

        val migrationF = new File(dir, project.getName + ".migration.xmi")
        val migrationURI = URI.createFileURI(migrationF.getAbsolutePath)
        val r = migrationMM.rs.createResource(migrationURI)
        r.getContents.add(old2newMapping.eObject)
        r.save(options)

        val migrationDeltaF = new File(dir, project.getName + ".migration.delta.xmi")
        val migrationDeltaURI = URI.createFileURI(migrationDeltaF.getAbsolutePath)
        val rd = migrationMM.rs.createResource(migrationDeltaURI)
        rd.getContents.add(old2newDeltaMapping.eObject)
        rd.save(options)

        guiLog.log(s" Saved migration model at: $migrationF ")

        val unique = mdIdGenerator.checkIDs()
        guiLog.log(s"Unique IDs? $unique")

        //          val cpanel = new JPanel()
        //          cpanel.setLayout( new JideBoxLayout( cpanel, BoxLayout.Y_AXIS ) )
        //
        //          cpanel.add( new JLabel( s"Enter the URI of ${project.getName} : " ), BorderLayout.BEFORE_LINE_BEGINS )
        //
        //          val uriField = new JTextField
        //          uriField.setText( "http://...." )
        //          uriField.setColumns( 80 )
        //          uriField.setEditable( true )
        //          uriField.setFocusable( true )
        //          cpanel.add( uriField )
        //
        //          cpanel.updateUI()
        //
        //          val cstatus = JOptionPane.showConfirmDialog(
        //            Application.getInstance.getMainFrame,
        //            cpanel,
        //            "Specify the URI to map the project's URIs to:",
        //            JOptionPane.OK_CANCEL_OPTION )
        //
        //          val curi = augmentString( uriField.getText )
        //          if ( cstatus != JOptionPane.OK_OPTION || curi.isEmpty ) {
        //            guiLog.log( s"No catalog generated" )
        //            return Success( None )
        //          }
        //
        //          val catalogF = new File( dir, project.getName + ".catalog.xml" )
        //          guiLog.log( s"generate full catalog: $catalogF" )
        //          val deltaF = new File( dir, project.getName + ".delta.catalog.xml" )
        //          guiLog.log( s"generate delta catalog: $deltaF" )
        //
        //          val catalogDir = catalogF.getParentFile
        //          catalogDir.mkdirs()
        //
        //          val pw = new PrintWriter( new FileWriter( catalogF ) )
        //          pw.println( """<?xml version='1.0' encoding='UTF-8'?>""" )
        //          pw.println( """<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" prefer="public">""" )
        //
        //          val dw = new PrintWriter( new FileWriter( deltaF ) )
        //          dw.println( """<?xml version='1.0' encoding='UTF-8'?>""" )
        //          dw.println( """<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" prefer="public">""" )
        //
        //          for {
        //            n <- sortedIDs.indices
        //            newID = sortedIDs( n )
        //            e = id2element( newID )
        //            oldID = e.id
        //          } e.getPackageOwnerWithEffectiveURI match {
        //            case Some( pkg ) =>
        //              pw.println( s"""<uri uri="$curi#$newID" name="${pkg.getEffectiveURI.get}#$oldID"/>""")
        //              if (newID != oldID) dw.println( s"""<uri uri="$curi#$newID" name="${pkg.getEffectiveURI.get}#$oldID"/>""")
        //            case None =>
        //              System.out.println(s"*** no owner package with URI found for ${e.id} (metaclass=${e.xmiType.head})")
        //          }
        //
        //          pw.println( """</catalog>""" )
        //          pw.close()
        //
        //          dw.println( """</catalog>""" )
        //          dw.close()
        //
        //          guiLog.log( s"Catalog generated at: $catalogF" )
        //          guiLog.log( s"Delta Catalog generated at: $deltaF" )

        // val elementMessages = t.element map { u =>
//        val mdU = umlMagicDrawUMLElement (u).getMagicDrawElement
//      val a =
//        new NMAction (
//          s"Select${u.hashCode}",
//          s"Select ${mdU.getHumanType}: ${mdU.getHumanName}",
//          0) {
//          def actionPerformed (ev: ActionEvent): Unit =
//            umlMagicDrawUMLElement (u).selectInContainmentTreeRunnable.run
//        }
//      mdU -> Tuple2 (t.getMessage, List (a) )
//    } toMap
//
//    Success (Some (
//      MagicDrawValidationDataResults.makeMDIllegalArgumentExceptionValidation (
//        p,
//        s"*** ${unresolved.size} unresolved references ***",
//        elementMessages,
//        "*::MagicDrawOTIValidation",
//        "*::UnresolvedCrossReference").validationDataResults) )


    Success(None)
    }
  }

}