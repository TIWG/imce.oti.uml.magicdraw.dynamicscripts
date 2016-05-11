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
package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.ui

import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.uml.UUIDRegistry
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.{Element, NamedElement}
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.designations.MagicDrawElementKindDesignation
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.nodes.{AbstractTreeNodeInfo, HyperlinkTableCellValueEditorRenderer, LabelNodeInfo, ReferenceNodeInfo}
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.tables.GroupTableNodeUI
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper
import org.omg.oti.magicdraw.uml.canonicalXMI.MagicDrawIDGenerator
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._
import org.omg.oti.uml._
import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.{MagicDrawUML, MagicDrawUMLUtil}
import org.omg.oti.uml.xmi.IDGenerator

import scala.collection.immutable._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}
import scala.{None, Option, Some, StringContext, Unit}
import scala.Predef.{ArrowAssoc, String}


object ElementInspectorWidget {

  import AppliedStereotypeWidgetHelper._
  import ComputedDerivedWidgetHelper._

  def otiInfo
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawAdapterForProfileCharacteristics(project),
    otiInfo(derived, ek, Seq(e)) _)

  def otiInfo
  (derived: DynamicScriptsTypes.ComputedDerivedWidget, ek: MagicDrawElementKindDesignation, es: Seq[Element])
  (oa: MagicDrawOTIProfileAdapter)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = {

    val rows
    : Seq[Map[String, AbstractTreeNodeInfo]]
    = es.map { e =>
      Map(
        "Element" ->
          (e match {
            case ne: NamedElement =>
              ReferenceNodeInfo(ne.getHumanName, ne)
            case _ =>
              ReferenceNodeInfo(e.getID, e)
          }),
        "Metaclass" ->
          LabelNodeInfo(StereotypesHelper.getBaseClass(e).getName),
        "Tool-specific ID" ->
          LabelNodeInfo(e.getID),
        "Tool-specific UUID" ->
          LabelNodeInfo(UUIDRegistry.getUUID(e))
      )
    }

    val ui = GroupTableNodeUI(
      DynamicScriptsTypes.ComputedDerivedTree(
        derived.name, derived.icon, derived.context, derived.access,
        derived.className, derived.methodName, derived.refresh,
        columnValueTypes = Some(Seq(
          DynamicScriptsTypes.DerivedFeatureValueType(
            key = DynamicScriptsTypes.SName("Element"),
            typeName = DynamicScriptsTypes.HName("Element"),
            typeInfo = DynamicScriptsTypes.StringTypeDesignation()),
          DynamicScriptsTypes.DerivedFeatureValueType(
            key = DynamicScriptsTypes.SName("Metaclass"),
            typeName = DynamicScriptsTypes.HName("Metaclass"),
            typeInfo = DynamicScriptsTypes.StringTypeDesignation()),
          DynamicScriptsTypes.DerivedFeatureValueType(
            key = DynamicScriptsTypes.SName("Tool-specific ID"),
            typeName = DynamicScriptsTypes.HName("Tool-specific ID"),
            typeInfo = DynamicScriptsTypes.StringTypeDesignation()),
          DynamicScriptsTypes.DerivedFeatureValueType(
            key = DynamicScriptsTypes.SName("Tool-specific UUID"),
            typeName = DynamicScriptsTypes.HName("Tool-specific UUID"),
            typeInfo = DynamicScriptsTypes.StringTypeDesignation())))),
      rows,
      Seq("Element", "Metaclass", "Tool-specific ID", "Tool-specific UUID"))
    //ui._table.addMouseListener( DoubleClickMouseListener.createAbstractTreeNodeInfoDoubleClickMouseListener( ui._table ) )
    HyperlinkTableCellValueEditorRenderer.addRenderer4AbstractTreeNodeInfo(ui._table)

    val validationAnnotations = rows flatMap
      (_.values) flatMap
      AbstractTreeNodeInfo.collectAnnotationsRecursively

    Success((ui.panel, validationAnnotations))
  }

  def appliedStereotypes
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    appliedStereotypes(derived, ek, e) _)

  def appliedStereotypes
  (derived: DynamicScriptsTypes.ComputedDerivedWidget, ek: MagicDrawElementKindDesignation, e: Element)
  (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = {
    appliedStereotypeInstanceWidget[UMLElement[MagicDrawUML]](
      derived, e,
      ordsa.otiAdapter.umlOps)
  }

  def appliedStereotypesWithoutMetaclassProperties
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    appliedStereotypesWithoutMetaclassProperties(derived, ek, e) _)

  def appliedStereotypesWithoutMetaclassProperties
  (derived: DynamicScriptsTypes.ComputedDerivedWidget, ek: MagicDrawElementKindDesignation, e: Element)
  (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = {
    implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
    elementOperationWidget[UMLElement[MagicDrawUML], UMLStereotype[MagicDrawUML]](
      derived, e,
      _.getAppliedStereotypesWithoutMetaclassProperties.getOrElse(Set()),
      ordsa.otiAdapter.umlOps)
  }

  def allForwardReferencesFromStereotypeTagProperties
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    allForwardReferencesFromStereotypeTagProperties(derived, ek, e) _)

  def allForwardReferencesFromStereotypeTagProperties
  (derived: DynamicScriptsTypes.ComputedDerivedWidget, ek: MagicDrawElementKindDesignation, e: Element)
  (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = {
    implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      _.allForwardReferencesFromStereotypeTagProperties.getOrElse(Set()),
      ordsa.otiAdapter.umlOps)
  }

  def allForwardReferencesToElements
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    allForwardReferencesToElements(derived, ek, e) _)

  def allForwardReferencesToElements
  (derived: DynamicScriptsTypes.ComputedDerivedWidget, ek: MagicDrawElementKindDesignation, e: Element)
  (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = {
    implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      _.allForwardReferencesToElements.getOrElse(Set()),
      ordsa.otiAdapter.umlOps)
  }

  def allForwardReferencesToImportablePackageableElements
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    allForwardReferencesToImportablePackageableElements(derived, ek, e) _)

  def allForwardReferencesToImportablePackageableElements
  (derived: DynamicScriptsTypes.ComputedDerivedWidget, ek: MagicDrawElementKindDesignation, e: Element)
  (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = {
    implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      _.allForwardReferencesToImportablePackageableElements.getOrElse(Set()),
      ordsa.otiAdapter.umlOps)
  }

  def getPackageOwnerWithURI
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    getPackageOwnerWithURI(derived, ek, e) _)

  def getPackageOwnerWithURI
  (derived: DynamicScriptsTypes.ComputedDerivedWidget, ek: MagicDrawElementKindDesignation, e: Element)
  (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = {
    implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
    elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
      derived, e,
      (x: UMLElement[MagicDrawUML]) => {
        val r = x.getPackageOwnerWithEffectiveURI()(idg.otiCharacteristicsProvider)
        r.getOrElse(None).to[Iterable]
      },
      ordsa.otiAdapter.umlOps)
  }
  def packageOrProfileOwner
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    packageOrProfileOwner(derived, ek, e) _)

  def packageOrProfileOwner
  (derived: DynamicScriptsTypes.ComputedDerivedWidget, ek: MagicDrawElementKindDesignation, e: Element)
  (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = {
    implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
    elementOperationWidget[UMLElement[MagicDrawUML], UMLPackage[MagicDrawUML]](
      derived, e,
      getPackageOrProfileOwner(_).to[Iterable],
      ordsa.otiAdapter.umlOps)
  }

  def annotatingComments
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLElement[MagicDrawUML], UMLComment[MagicDrawUML]](
        derived, e,
        _.annotatedElement_comment,
        ordsa.otiAdapter.umlOps)
    })

  def ownedElements
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
        elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
          derived, e,
          _.ownedElement,
          ordsa.otiAdapter.umlOps)
    })

  def allOwnedElementsWithinPackageScope
  (project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
   ek: MagicDrawElementKindDesignation, e: Element)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForProfileCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForProfileProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
        elementOperationWidget[UMLElement[MagicDrawUML], UMLElement[MagicDrawUML]](
          derived, e,
          _.allOwnedElementsWithinPackageScope,
          ordsa.otiAdapter.umlOps)
    })
}