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
///*
// *
// * License Terms
// *
// * Copyright (c) 2014-2016, California Institute of Technology ("Caltech").
// * U.S. Government sponsorship acknowledged.
// *
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are
// * met:
// *
// * *   Redistributions of source code must retain the above copyright
// *    notice, this list of conditions and the following disclaimer.
// *
// * *   Redistributions in binary form must reproduce the above copyright
// *    notice, this list of conditions and the following disclaimer in the
// *    documentation and/or other materials provided with the
// *    distribution.
// *
// * *   Neither the name of Caltech nor its operating division, the Jet
// *    Propulsion Laboratory, nor the names of its contributors may be
// *    used to endorse or promote products derived from this software
// *    without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
// * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
// * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts
//
//import java.awt.BorderLayout
//import java.awt.event.ActionEvent
//import java.io.{File, FileWriter, PrintWriter}
//import java.lang.{IllegalArgumentException,System}
//import javax.swing.{BoxLayout, JLabel, JOptionPane, JPanel, JTextField}
//
//import com.jidesoft.swing.JideBoxLayout
//import com.nomagic.actions.NMAction
//import com.nomagic.magicdraw.core.{Application, ApplicationEnvironment, Project, ProjectUtilities}
//import com.nomagic.magicdraw.ui.MagicDrawProgressStatusRunner
//import com.nomagic.magicdraw.ui.browser.{Node => MDNode, Tree => MDTree}
//import com.nomagic.magicdraw.uml.symbols.shapes.PackageView
//import com.nomagic.magicdraw.uml.symbols.{DiagramPresentationElement, PresentationElement}
//import com.nomagic.task.{ProgressStatus, RunnableWithProgress}
//import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, Package}
//import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile
//import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
//import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
//import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.{MDAPI, OTIHelper}
//import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.validation.OTIMagicDrawValidation
//import org.eclipse.emf.common.util.URI
//import org.eclipse.emf.ecore.xmi.XMLResource
//import org.omg.oti.changeMigration._
//import org.omg.oti.magicdraw.uml.canonicalXMI._
//import org.omg.oti.magicdraw.uml.read._
//import org.omg.oti.uml.OTIPrimitiveTypes._
//import org.omg.oti.uml._
//import org.omg.oti.uml.canonicalXMI._
//import org.omg.oti.uml.read.api._
//import org.omg.oti.uml.xmi._
//
//import scala.Predef.{augmentString, identity, require, ArrowAssoc, String}
//import scala.collection.JavaConversions._
//import scala.collection.immutable._
//import scala.language.{implicitConversions, postfixOps}
//import scala.reflect.runtime.universe._
//import scala.util.{Failure, Success, Try}
//import scala.{Boolean,Function1,Option,None,Some,StringContext,Unit}
//import scalaz.Scalaz._
//import scalaz._
//
///**
// * @author Nicolas.F.Rouquette@jpl.nasa.gov
// */
//object generateIDsForPackageExtents {
//
//  def doit
//  (p: Project, ev: ActionEvent,
//   script: DynamicScriptsTypes.BrowserContextMenuAction,
//   tree: MDTree, node: MDNode,
//   pkg: Package, selection: java.util.Collection[Element])
//  : Try[Option[MagicDrawValidationDataResults]] =
//    doit(p, ev, selection)
//
//  def doit
//  (p: Project, ev: ActionEvent,
//   script: DynamicScriptsTypes.BrowserContextMenuAction,
//   tree: MDTree, node: MDNode,
//   pkg: Profile, selection: java.util.Collection[Element])
//  : Try[Option[MagicDrawValidationDataResults]] =
//    doit(p, ev, selection)
//
//  def doit
//  (p: Project, ev: ActionEvent,
//   script: DynamicScriptsTypes.DiagramContextMenuAction,
//   dpe: DiagramPresentationElement,
//   triggerView: PackageView,
//   triggerElement: Package,
//   selection: java.util.Collection[PresentationElement])
//  : Try[Option[MagicDrawValidationDataResults]] =
//    doit(p, ev, selection flatMap { case pv: PackageView => Some(pv.getPackage) })
//
//  def doit
//  (p: Project, ev: ActionEvent,
//   script: DynamicScriptsTypes.DiagramContextMenuAction,
//   dpe: DiagramPresentationElement,
//   triggerView: PackageView,
//   triggerElement: Profile,
//   selection: java.util.Collection[PresentationElement])
//  : Try[Option[MagicDrawValidationDataResults]] =
//    doit(p, ev, selection flatMap { case pv: PackageView => Some(pv.getPackage) })
//
//  def doit
//  (p: Project, ev: ActionEvent,
//   selection: java.util.Collection[Element])
//  : Try[Option[MagicDrawValidationDataResults]] = {
//
//    implicit val umlUtil = MagicDrawUMLUtil( p )
//    import umlUtil._
//
//    val otiV = OTIMagicDrawValidation(p)
//    val selectedPackages: Set[UMLPackage[Uml]] =
//      selection
//        .toIterable
//        .selectByKindOf { case p: Package => umlPackage( p ) }
//        .to[Set]
//
//    OTIHelper.getOTIMDInfo().fold[Try[Option[MagicDrawValidationDataResults]]](
//      l = (nels) =>
//        otiV
//          .toTryOptionMDValidationDataResults(p, "generateIDsForPackageExtents", nels.some),
//
//      r = (info) => {
//
//        var result: Option[Try[Option[MagicDrawValidationDataResults]]] = None
//        val runnable = new RunnableWithProgress() {
//
//          def run(progressStatus: ProgressStatus): Unit =
//            result = Some(
//              doit(
//                progressStatus,
//                p, selectedPackages,
//                info,
//                ignoreCrossReferencedElementFilter = MDAPI.ignoreCrossReferencedElementFilter,
//                unresolvedElementMapper = MDAPI.unresolvedElementMapper(umlUtil)))
//
//        }
//
//        MagicDrawProgressStatusRunner.runWithProgressStatus(
//          runnable,
//          s"Export ${selectedPackages.size} packages to OTI Canonical XMI...",
//          true, 0)
//
//        require(result.isDefined)
//        result.get
//      })
//  }
//
//  def doit
//  (progressStatus: ProgressStatus,
//   p: Project,
//   selectedPackages: Set[UMLPackage[MagicDrawUML]],
//   info: OTIHelper.OTIMDInfo,
//   ignoreCrossReferencedElementFilter: Function1[UMLElement[MagicDrawUML], Boolean],
//   unresolvedElementMapper: Function1[UMLElement[MagicDrawUML], Option[UMLElement[MagicDrawUML]]] )
//  : Try[Option[MagicDrawValidationDataResults]] = {
//
//    val a = Application.getInstance()
//    val guiLog = a.getGUILog()
//    guiLog.clearLog()
//
//    val mdInstallRoot = ApplicationEnvironment.getInstallRoot
//    val mdInstallDir = new File(mdInstallRoot)
//    require(mdInstallDir.exists && mdInstallDir.isDirectory)
//
//    val pp = p.getPrimaryProject
//    if (!ProjectUtilities.isStandardSystemProfile(pp))
//      return Failure(new IllegalArgumentException(s"The project must be a standard/system profile project"))
//
//    val uri = pp.getLocationURI
//    require(uri.isFile)
//
//    val modulePath = uri.deresolve(URI.createFileURI(mdInstallRoot))
//    require(modulePath.isRelative)
//
//    val panel = new JPanel()
//    panel.setLayout(new JideBoxLayout(panel, BoxLayout.Y_AXIS))
//
//    panel.add(new JLabel("Enter MD root-relative path of the previous version of the Standard/System Profile module: "), BorderLayout.BEFORE_LINE_BEGINS)
//
//    val modulePathField = new JTextField
//    modulePathField.setText(modulePath.path)
//    modulePathField.setColumns(modulePath.path.length() + 10)
//    modulePathField.setEditable(true)
//    modulePathField.setFocusable(true)
//    panel.add(modulePathField)
//
//    panel.updateUI()
//
//    val status = JOptionPane.showConfirmDialog(
//      Application.getInstance.getMainFrame,
//      panel,
//      "Specify the relative path of the previous module version",
//      JOptionPane.OK_CANCEL_OPTION)
//
//    val projectFilename = augmentString(modulePathField.getText)
//    if (status != JOptionPane.OK_OPTION || projectFilename.isEmpty) {
//      guiLog.log("Cancelled")
//      return Success(None)
//    }
//
//    implicit val umlUtil = MagicDrawUMLUtil(p)
//
//    implicit val otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] =
//      None
//
//    var result: Option[Try[Option[MagicDrawValidationDataResults]]] = None
//    val runnable = new RunnableWithProgress() {
//
//      def run(progressStatus: ProgressStatus): Unit =
//        result = Some(
//          generateIDs(
//            progressStatus,
//            p, mdInstallDir, projectFilename.repr,
//            selectedPackages,
//            info,
//            ignoreCrossReferencedElementFilter,
//            unresolvedElementMapper) )
//
//    }
//
//    MagicDrawProgressStatusRunner.runWithProgressStatus(
//      runnable,
//      s"Generating OTI Canonical XMI:IDs for ${selectedPackages.size} packages...",
//      true, 0)
//
//    require(result.isDefined)
//    result.get
//  }
//
//  def generateIDs[U <: MagicDrawUML]
//  (progressStatus: ProgressStatus,
//   p: Project, mdInstallDir: File, projectFilename: String,
//   specificationRootPackages: Set[UMLPackage[MagicDrawUML]],
//   info: OTIHelper.OTIMDInfo,
//   ignoreCrossReferencedElementFilter: Function1[UMLElement[MagicDrawUML], Boolean],
//   unresolvedElementMapper: Function1[UMLElement[MagicDrawUML], Option[UMLElement[MagicDrawUML]]])
//  (implicit umlUtil: MagicDrawUMLUtil,
//    uTag: TypeTag[U],
//    mdTag: TypeTag[MagicDrawUML],
//    tag: TypeTag[UMLError.IllegalElementException[MagicDrawUML, UMLElement[MagicDrawUML]]])
//  : Try[Option[MagicDrawValidationDataResults]] = {
//    import umlUtil._
//
//    val a = Application.getInstance()
//    val guiLog = a.getGUILog
//
//    progressStatus.setCurrent(0)
//    progressStatus.setMax(0)
//    progressStatus.setMax(specificationRootPackages.size.toLong + 1)
//    progressStatus.setLocked(true)
//
//    // @todo populate...
//    implicit val otiCharacterizations: Option[Map[UMLPackage[MagicDrawUML], UMLComment[MagicDrawUML]]] =
//      None
//
//    // @todo review this...
////    val mdBuiltIns: Set[BuiltInDocument[Uml]] =
////      Set( MDBuiltInPrimitiveTypes, MDBuiltInUML, MDBuiltInStandardProfile )
////
////    val mdBuiltInEdges: Set[DocumentEdge[Document[Uml]]] =
////      Set( MDBuiltInUML2PrimitiveTypes, MDBuiltInStandardProfile2UML )
//
//    implicit val mdIdGenerator = info._1
//    implicit val otiCharacteristicsProvider = mdIdGenerator.otiCharacteristicsProvider
//
//    val mdDS: MagicDrawDocumentSet = info._3
//
//    MDAPI.showUnresolvedCrossReferencesAsMagicDrawValidationResults(
//      p, unresolved=info._4, ignoreCrossReferencedElementFilter)
//
//    val mdErrors = for {
//      pkg <- specificationRootPackages
//      _ = progressStatus.increase()
//      _ = progressStatus.setDescription(s"Generating OTI Canonical XMI:IDs for '${pkg.name.get}'...")
//      _ = System.out.println(s"Generating OTI Canonical XMI:IDs for '${pkg.name.get}'...")
//      e <- pkg.allOwnedElements
//      mdError <- mdIdGenerator
//        .getXMI_ID(e)
//        .fold[Option[UMLError.ThrowableNel]](
//        l = { nels =>
//          nels.some
//        },
//        r = (_: String @@ OTI_ID) =>
//          None
//        )
//    } yield mdError
//    val allErrors = if (mdErrors.isEmpty) None else (mdErrors.head /: mdErrors.tail)(_ append _).some
//    MDAPI.showOTIUMLErrors(p, "ID Generation", allErrors)
//
//    val elementIDs = mdIdGenerator.getElement2IDMap
//    val errors = elementIDs.flatMap { case (e, error_or_id) =>
//      error_or_id.fold[Option[(UMLElement[MagicDrawUML], NonEmptyList[java.lang.Throwable])]](
//        l = (nels) =>
//          Some(e -> nels),
//        r = (_) =>
//          None
//      )
//    }
//    if (errors.nonEmpty) {
//      guiLog.log(s"${errors.size} errors when computing OTI XMI IDs for the package extent(s) of:")
//      for {pkg <- specificationRootPackages} {
//        guiLog.log(s" => ${pkg.qualifiedName.get}")
//      }
//      errors.keys.toList sortBy (u => OTI_ID.unwrap(u.toolSpecific_id.get)) foreach { e =>
//        val t = errors(e).head
//        val id = OTI_ID.unwrap(e.toolSpecific_id.get)
//        if (errors.size > 100) System.out.println(s" $id => $t")
//        else guiLog.addHyperlinkedText(
//          s" <A>$id</A> => $t",
//          Map(id -> umlMagicDrawUMLElement(e).selectInContainmentTreeRunnable))
//      }
//    }
//    else {
//      guiLog.log(s"No errors when computing OTI XMI IDs for the package extent(s) of:")
//      for {pkg <- specificationRootPackages} {
//        guiLog.log(s" => ${pkg.qualifiedName.get}")
//      }
//    }
//
//    progressStatus.increase()
//    progressStatus.setDescription(s"Constructing old/new migration map...")
//
//    val id2element = elementIDs.flatMap { case (e, error_or_id) =>
//      error_or_id.fold[Option[(String, UMLElement[MagicDrawUML])]](
//        l = (_) => None,
//        r = (newID) => {
//
//          val isAncestor = specificationRootPackages.exists(p => p.isAncestorOf(e).fold[Boolean](
//            l=(_) => false,
//            r=identity
//          ))
//
//          if (isAncestor)
//            Some(OTI_ID.unwrap(newID) -> e)
//          else
//            None
//        }
//      )
//    }
//
//    val sortedIDs = id2element.keys.toList filter {
//      !_.contains("appliedStereotypeInstance")
//    } sorted
//
//    val otiChangeMigrationDir = new File(mdInstallDir, "dynamicScripts/org.omg.oti.changeMigration/resources")
//    require(otiChangeMigrationDir.exists && otiChangeMigrationDir.isDirectory)
//    val migrationMM = Metamodel(otiChangeMigrationDir)
//
//    val old2newMapping = migrationMM.makeOld2NewIDMapping(projectFilename)
//    val old2newDeltaMapping = migrationMM.makeOld2NewIDMapping(projectFilename)
//
//    System.out.println(s" elementIDs has ${elementIDs.size} entries")
//    System.out.println(s" element2id map has ${sortedIDs.size} entries")
//    guiLog.log(s" element2id map has ${sortedIDs.size} entries")
//    val tooLong = sortedIDs.size > 500
//    for {
//      n <- sortedIDs.indices
//      id = sortedIDs(n)
//      e = id2element(id)
//    } {
//      val oldID = e.toolSpecific_id.get
//      val old2newEntry = migrationMM.makeOld2NewIDEntry
//      old2newEntry.setOldID(OTI_ID.unwrap(oldID))
//      old2newEntry.setNewID(id)
//      old2newMapping.addEntry(old2newEntry)
//
//      if (id != oldID) {
//        val old2newDelta = migrationMM.makeOld2NewIDEntry
//        old2newDelta.setOldID(OTI_ID.unwrap(oldID))
//        old2newDelta.setNewID(id)
//        old2newDeltaMapping.addEntry(old2newDelta)
//      }
//      if (!tooLong)
//        guiLog.addHyperlinkedText(
//          s" $n: <A>$id</A> => $oldID",
//          Map(id -> e.asInstanceOf[MagicDrawUMLElement].selectInContainmentTreeRunnable))
//    }
//
//    val dir = new File(project.getDirectory)
//    require(dir.exists() && dir.isDirectory)
//
//    val options = Map(XMLResource.OPTION_ENCODING -> "UTF-8")
//
//    val migrationF = new File(dir, project.getName + ".migration.xmi")
//    val migrationURI = URI.createFileURI(migrationF.getAbsolutePath)
//    val r = migrationMM.rs.createResource(migrationURI)
//    r.getContents.add(old2newMapping.eObject)
//    r.save(options)
//
//    val migrationDeltaF = new File(dir, project.getName + ".migration.delta.xmi")
//    val migrationDeltaURI = URI.createFileURI(migrationDeltaF.getAbsolutePath)
//    val rd = migrationMM.rs.createResource(migrationDeltaURI)
//    rd.getContents.add(old2newDeltaMapping.eObject)
//    rd.save(options)
//
//    guiLog.log(s" Saved migration model at: $migrationF ")
//
//    val unique = mdIdGenerator.checkIDs()
//    guiLog.log(s"Unique IDs? $unique")
//
//    val cpanel = new JPanel()
//    cpanel.setLayout( new JideBoxLayout( cpanel, BoxLayout.Y_AXIS ) )
//
//    cpanel.add( new JLabel( s"Enter the URI of ${project.getName} : " ), BorderLayout.BEFORE_LINE_BEGINS )
//
//    val uriField = new JTextField
//    uriField.setText( "http://...." )
//    uriField.setColumns( 80 )
//    uriField.setEditable( true )
//    uriField.setFocusable( true )
//    cpanel.add( uriField )
//
//    cpanel.updateUI()
//
//    val cstatus = JOptionPane.showConfirmDialog(
//      Application.getInstance.getMainFrame,
//      cpanel,
//      "Specify the URI to map the project's URIs to:",
//      JOptionPane.OK_CANCEL_OPTION )
//
//    val curi = augmentString( uriField.getText )
//    if ( cstatus != JOptionPane.OK_OPTION || curi.isEmpty ) {
//      guiLog.log( s"No catalog generated" )
//      return Success( None )
//    }
//
//    val catalogF = new File( dir, project.getName + ".catalog.xml" )
//    guiLog.log( s"generate full catalog: $catalogF" )
//    val deltaF = new File( dir, project.getName + ".delta.catalog.xml" )
//    guiLog.log( s"generate delta catalog: $deltaF" )
//
//    val catalogDir = catalogF.getParentFile
//    catalogDir.mkdirs()
//
//    val pw = new PrintWriter( new FileWriter( catalogF ) )
//    pw.println( """<?xml version='1.0' encoding='UTF-8'?>""" )
//    pw.println( """<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" prefer="public">""" )
//
//    val dw = new PrintWriter( new FileWriter( deltaF ) )
//    dw.println( """<?xml version='1.0' encoding='UTF-8'?>""" )
//    dw.println( """<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" prefer="public">""" )
//
//    for {
//      n <- sortedIDs.indices
//      newID = sortedIDs( n )
//      e = id2element( newID )
//      oldID = OTI_ID.unwrap(e.toolSpecific_id.get)
//    } e.getPackageOwnerWithEffectiveURI.getOrElse(None) match {
//      case Some( pkg ) =>
//        val pkgEffectiveURI = pkg.getEffectiveURI.getOrElse(None).get
//        pw.println( s"""<uri uri="$curi#$newID" name="$pkgEffectiveURI#$oldID"/>""")
//        if (newID != oldID) dw.println( s"""<uri uri="$curi#$newID" name="$pkgEffectiveURI#$oldID"/>""")
//      case None =>
//        System.out.println(s"*** no owner package with URI found for $oldID (metaclass=${e.xmiType.head})")
//    }
//
//    pw.println( """</catalog>""" )
//    pw.close()
//
//    dw.println( """</catalog>""" )
//    dw.close()
//
//    guiLog.log( s"Catalog generated at: $catalogF" )
//    guiLog.log( s"Delta Catalog generated at: $deltaF" )
//
//    val elementMessages = errors map { case (u,nels) =>
//      val mdU = umlMagicDrawUMLElement (u).getMagicDrawElement
//      val a =
//        new NMAction (
//          s"Select${u.hashCode}",
//          s"Select ${mdU.getHumanType}: ${mdU.getHumanName}",
//          0) {
//          def actionPerformed (ev: ActionEvent): Unit =
//            umlMagicDrawUMLElement (u).selectInContainmentTreeRunnable.run
//        }
//      mdU -> Tuple2 (nels.head.getMessage, List (a) )
//    } toMap
//
//    if (elementMessages.nonEmpty)
//      Success (Some (
//        MagicDrawValidationDataResults.makeMDIllegalArgumentExceptionValidation (
//          p,
//          s"*** ${errors.size} id generation errors ***",
//          elementMessages,
//          "*::MagicDrawOTIValidation",
//          "*::UnresolvedCrossReference").validationDataResults) )
//    else
//      Success(None)
//  }
//
//}