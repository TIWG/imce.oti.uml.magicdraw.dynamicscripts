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

import gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils.OTIHelper
import com.nomagic.magicdraw.core.Project
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.designations.MagicDrawElementKindDesignation
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read.MagicDrawUML
import org.omg.oti.magicdraw.uml.canonicalXMI.MagicDrawIDGenerator
import org.omg.oti.magicdraw.uml.canonicalXMI.helper._

import scala.collection.immutable._
import scala.util.Try

object PropertyInspectorWidget {

  import ComputedDerivedWidgetHelper._

  def info
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      propertyOperationWidget[UMLProperty[MagicDrawUML], UMLProperty[MagicDrawUML]](
        derived, e,
        Iterable(_),
        ordsa.otiAdapter.umlOps)
    })

  def redefinedProperty
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
        elementOperationWidget[UMLProperty[MagicDrawUML], UMLProperty[MagicDrawUML]](
          derived, e,
          _.redefinedProperty,
          ordsa.otiAdapter.umlOps)
    })

  def redefiningdProperty
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLProperty[MagicDrawUML], UMLProperty[MagicDrawUML]](
        derived, e,
        _.redefinedProperty_property,
        ordsa.otiAdapter.umlOps)
    })

  def subsettedProperty
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLProperty[MagicDrawUML], UMLProperty[MagicDrawUML]](
        derived, e,
        _.subsettedProperty,
        ordsa.otiAdapter.umlOps)
    })

  def subsettingProperty
  ( project: Project, ev: ActionEvent, derived: DynamicScriptsTypes.ComputedDerivedWidget,
    ek: MagicDrawElementKindDesignation, e: Element )
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = OTIHelper.toTry(
    MagicDrawOTIHelper.getOTIMagicDrawInfoForDataCharacteristics(project),
    (ordsa: MagicDrawOTIResolvedDocumentSetAdapterForDataProvider) => {
      implicit val idg = MagicDrawIDGenerator()(ordsa.rds.ds)
      elementOperationWidget[UMLProperty[MagicDrawUML], UMLProperty[MagicDrawUML]](
        derived, e,
        _.subsettedProperty_property,
        ordsa.otiAdapter.umlOps)
    })
    
}