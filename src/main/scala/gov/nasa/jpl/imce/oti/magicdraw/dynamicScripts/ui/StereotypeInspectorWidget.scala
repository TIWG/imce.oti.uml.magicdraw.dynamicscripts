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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.ui

import java.awt.event.ActionEvent

import com.nomagic.magicdraw.core.Project
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.designations.MagicDrawElementKindDesignation
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper

import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._
import org.omg.oti.magicdraw.uml.canonicalXMI.MagicDrawIDGenerator

import scala.collection.immutable._
import scala.util.Try

object StereotypeInspectorWidget {

  import ComputedDerivedWidgetHelper._

  def baseMetaProperties
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLProperty[MagicDrawUML]](
        derived, e,
        (s: UMLStereotype[MagicDrawUML]) => s.baseMetaProperties.toList.sortBy(_.qualifiedName.get),
        ordsa.otiAdapter.umlOps)
    })

  def baseMetaPropertiesExceptRedefined
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLProperty[MagicDrawUML]](
        derived, e,
        (s: UMLStereotype[MagicDrawUML]) => s.baseMetaPropertiesExceptRedefined.toList.sortBy(_.qualifiedName.get),
        ordsa.otiAdapter.umlOps)
    })

  def profile
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLProfile[MagicDrawUML]](
        derived, e,
        _.profile.toSet,
        ordsa.otiAdapter.umlOps)
    })

  def getSpecializedStereotypes
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
        derived, e,
        org.omg.oti.uml.getSpecializedStereotypes(_)(ordsa.otiAdapter.umlOps),
        ordsa.otiAdapter.umlOps)
    })

  def getSpecializedStereotypesOutsideProfile
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
        derived, e,
        org.omg.oti.uml.getSpecializedStereotypesOutsideProfile(_)(ordsa.otiAdapter.umlOps),
        ordsa.otiAdapter.umlOps)
    })

  def getSpecializedStereotypesWithinProfile
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
        derived, e,
        org.omg.oti.uml.getSpecializedStereotypesWithinProfile(_)(ordsa.otiAdapter.umlOps),
        ordsa.otiAdapter.umlOps)
    })

  def getAllSpecializedStereotypes
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
        derived, e,
        org.omg.oti.uml.getAllSpecializedStereotypes(_)(ordsa.otiAdapter.umlOps),
        ordsa.otiAdapter.umlOps)
    })

  def getAllSpecializedStereotypesWithinProfile
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
        derived, e,
        org.omg.oti.uml.getAllSpecializedStereotypesWithinProfile(_)(ordsa.otiAdapter.umlOps),
        ordsa.otiAdapter.umlOps)
    })

  def getSpecializedStereotypesFromOtherProfiles
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLStereotype[MagicDrawUML], UMLStereotype[MagicDrawUML]](
        derived, e,
        org.omg.oti.uml.getSpecializedStereotypesFromOtherProfiles(_)(ordsa.otiAdapter.umlOps),
        ordsa.otiAdapter.umlOps)
    })
}