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

import java.lang.IllegalArgumentException

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.nodes._
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.tables._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.json.common.OTIPrimitiveTypes._
import org.omg.oti.uml.read.UMLStereotypeTagValue
import org.omg.oti.uml.read.api._

import scala.Predef.ArrowAssoc
import scala.collection.immutable._
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import scala.{None,Some,StringContext}
import scala.Predef.{???,require,String}


object AppliedStereotypeWidgetHelper {

  def makeComputedDerivedTreeForAppliedStereotype(derived: DynamicScriptsTypes.ComputedDerivedWidget): DynamicScriptsTypes.ComputedDerivedTree =
    DynamicScriptsTypes.ComputedDerivedTree(
      derived.name, derived.icon, derived.context, derived.access,
      derived.className, derived.methodName, derived.refresh,
      columnValueTypes = Some(Seq(
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName("extended element"),
          typeName = DynamicScriptsTypes.HName("Element"),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation()),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName("applied stereotype"),
          typeName = DynamicScriptsTypes.HName("Stereotype"),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation()),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName("tag property"),
          typeName = DynamicScriptsTypes.HName("Property"),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation()),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName("tag type"),
          typeName = DynamicScriptsTypes.HName("Type"),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation()),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName("tag value"),
          typeName = DynamicScriptsTypes.HName("tag value"),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation()))))

  def createRowForTagValue
  (tagValue: UMLStereotypeTagValue[MagicDrawUML])
  (implicit umlUtil: MagicDrawUMLUtil)
  : Map[String, AbstractTreeNodeInfo] = {
    tagValue match {
      case tv: MagicDrawUMLStereotypeTagExtendedMetaclassPropertyElementReference =>
        Map(
          "extended element" ->
            (tv.extendedElement match {
              case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
                ReferenceNodeInfo(ne.qualifiedName.get, ne.getMagicDrawElement)
              case _ =>
                ReferenceNodeInfo(
                  TOOL_SPECIFIC_ID.unwrap(tv.extendedElement.toolSpecific_id),
                  tv.extendedElement.getMagicDrawElement)
            }),
          "applied stereotype" ->
            ReferenceNodeInfo(tv.appliedStereotype.name.get, tv.appliedStereotype.getMagicDrawElement),
          "tag property" ->
            ReferenceNodeInfo(tv.stereotypeTagProperty.name.get, tv.stereotypeTagProperty.getMagicDrawElement),
          "tag type" ->
            ReferenceNodeInfo(tv.stereotypeTagPropertyType.name.get, tv.stereotypeTagPropertyType.getMagicDrawElement),
          "tag value" ->
            (tv.extendedElement match {
              case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
                ReferenceNodeInfo(ne.qualifiedName.get, ne.getMagicDrawElement)
              case _ =>
                ReferenceNodeInfo(
                  TOOL_SPECIFIC_ID.unwrap(tv.extendedElement.toolSpecific_id),
                  tv.extendedElement.getMagicDrawElement)
            })
        )
      case tv: MagicDrawUMLStereotypeTagPropertyMetaclassElementReference =>
        Map(
          "extended element" ->
            (tv.extendedElement match {
              case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
                ReferenceNodeInfo(ne.name.get, ne.getMagicDrawElement)
              case _ =>
                ReferenceNodeInfo(
                  TOOL_SPECIFIC_ID.unwrap(tv.extendedElement.toolSpecific_id),
                  tv.extendedElement.getMagicDrawElement)
            }),
          "applied stereotype" ->
            ReferenceNodeInfo(tv.appliedStereotype.name.get, tv.appliedStereotype.getMagicDrawElement),
          "tag property" ->
            ReferenceNodeInfo(tv.stereotypeTagProperty.name.get, tv.stereotypeTagProperty.getMagicDrawElement),
          "tag type" ->
            ReferenceNodeInfo(tv.stereotypeTagPropertyType.name.get, tv.stereotypeTagPropertyType.getMagicDrawElement),
          "tag value" ->
            (tv.tagPropertyValueElementReferences.head match {
              case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
                ReferenceNodeInfo(ne.qualifiedName.get, ne.getMagicDrawElement)
              case e =>
                ReferenceNodeInfo(
                  TOOL_SPECIFIC_ID.unwrap(e.toolSpecific_id),
                  e.getMagicDrawElement)
            })
          //            TreeNodeInfo(
          //              identifier = "values",
          //              nested = tv.tagPropertyValueElementReferences.toSeq map { e => e match {
          //                case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
          //                  Tuple2(ReferenceNodeInfo(ne.qualifiedName.get, e.getMagicDrawElement), Map[String, AbstractTreeNodeInfo]())
          //                case _ =>
          //                  Tuple2(ReferenceNodeInfo(e.xmiID.head, e.getMagicDrawElement), Map[String, AbstractTreeNodeInfo]())
          //              }
          //              })
        )
      case tv: MagicDrawUMLStereotypeTagStereotypeInstanceValue =>
        Map(
          "extended element" ->
            (tv.extendedElement match {
              case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
                ReferenceNodeInfo(ne.name.get, ne.getMagicDrawElement)
              case _ =>
                toToolSpecificIDReferenceNodeInfo(tv.extendedElement)
            }),
          "applied stereotype" ->
            ReferenceNodeInfo(tv.appliedStereotype.name.get, tv.appliedStereotype.getMagicDrawElement),
          "tag property" ->
            ReferenceNodeInfo(tv.stereotypeTagProperty.name.get, tv.stereotypeTagProperty.getMagicDrawElement),
          "tag type" ->
            ReferenceNodeInfo(tv.stereotypeTagPropertyType.name.get, tv.stereotypeTagPropertyType.getMagicDrawElement),
          "tag value" ->
            (tv.tagPropertyValueElementReferences.head match {
              case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
                ReferenceNodeInfo(ne.qualifiedName.get, ne.getMagicDrawElement)
              case e =>
                toToolSpecificIDReferenceNodeInfo(e)
            })
          //            TreeNodeInfo(
          //              identifier = "values",
          //              nested = tv.tagPropertyValueElementReferences.toSeq map { e => e match {
          //                case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
          //                  Tuple2(ReferenceNodeInfo(ne.qualifiedName.get, e.getMagicDrawElement), Map[String, AbstractTreeNodeInfo]())
          //                case _ =>
          //                  Tuple2(ReferenceNodeInfo(e.xmiID.head, e.getMagicDrawElement), Map[String, AbstractTreeNodeInfo]())
          //              }
          //              })
        )
      case tv: MagicDrawUMLStereotypeTagPropertyClassifierValue =>
        Map(
          "extended element" ->
            (tv.extendedElement match {
              case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
                ReferenceNodeInfo(ne.name.get, ne.getMagicDrawElement)
              case _ =>
                toToolSpecificIDReferenceNodeInfo(tv.extendedElement)
            }),
          "applied stereotype" ->
            ReferenceNodeInfo(tv.appliedStereotype.name.get, tv.appliedStereotype.getMagicDrawElement),
          "tag property" ->
            ReferenceNodeInfo(tv.stereotypeTagProperty.name.get, tv.stereotypeTagProperty.getMagicDrawElement),
          "tag type" ->
            ReferenceNodeInfo(tv.stereotypeTagPropertyType.name.get, tv.stereotypeTagPropertyType.getMagicDrawElement),
          "tag value" ->
            (tv.values.headOption match {
              case None =>
                LabelNodeInfo("<no value>")
              case Some(tagValue) =>
                tagValue match {
                  case _v: MagicDrawTagPropertyEnumerationLiteralValue =>
                    ReferenceNodeInfo(_v.value.name.get, _v.value.getMagicDrawEnumerationLiteral)
                  case _v: MagicDrawTagPropertyInstanceSpecificationValue =>
                    ReferenceNodeInfo(_v.value.name.get, _v.value.getMagicDrawInstanceSpecification)
                  case _v: MagicDrawTagPropertyBooleanValue =>
                    LabelNodeInfo(_v.value.toString)
                  case _v: MagicDrawTagPropertyIntegerValue =>
                    LabelNodeInfo(_v.value.toString)
                  case _v: MagicDrawTagPropertyUnlimitedNaturalValue =>
                    LabelNodeInfo(_v.value.toString)
                  case _v: MagicDrawTagPropertyRealValue =>
                    LabelNodeInfo(_v.value.toString)
                  case _v: MagicDrawTagPropertyStringValue =>
                    LabelNodeInfo(_v.value.toString)
                  case _ =>
                    ???
                }
            })
          //            TreeNodeInfo(
          //              identifier = "literals",
          //              nested = tv.tagPropertyValueElementReferences.toSeq map { lit =>
          //                Tuple2(ReferenceNodeInfo(lit.qualifiedName.get, lit.getMagicDrawElement), Map[String, AbstractTreeNodeInfo]())
          //              })
        )
      case _ =>
        Map()
    }
  }

  def createGroupTableUIPanelForElements
  (derived: DynamicScriptsTypes.ComputedDerivedWidget,
   tagValues: Seq[UMLStereotypeTagValue[MagicDrawUML]])
  (implicit util: MagicDrawUMLUtil)
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] = {

    val rows: Seq[Map[String, AbstractTreeNodeInfo]] = tagValues.map( createRowForTagValue )

    val ui = GroupTableNodeUI(
      makeComputedDerivedTreeForAppliedStereotype(derived),
      rows,
      Seq("extended element", "applied stereotype", "tag property", "tag type", "tag value"))
    //ui._table.addMouseListener( DoubleClickMouseListener.createAbstractTreeNodeInfoDoubleClickMouseListener( ui._table ) )
    HyperlinkTableCellValueEditorRenderer.addRenderer4AbstractTreeNodeInfo(ui._table)

    val validationAnnotations = rows flatMap
      (_.values) flatMap
      AbstractTreeNodeInfo.collectAnnotationsRecursively

    Success((ui.panel, validationAnnotations))
  }

  def appliedStereotypeInstanceWidget[U <: UMLElement[MagicDrawUML]]
  (derived: DynamicScriptsTypes.ComputedDerivedWidget,
   mdE: MagicDrawUML#Element,
   util: MagicDrawUMLUtil)
  (implicit uTag: ClassTag[U])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])]
  = {
    val e = util.umlElement(mdE)
    val uClass = uTag.runtimeClass
    require(uClass != null)
    if (uClass.isInstance(e))
      createGroupTableUIPanelForElements(derived, e.tagValues.getOrElse(Seq()))(util)
    else
      Failure(new IllegalArgumentException(s"${mdE.getHumanType}: ${mdE.getID} is not a kind of ${uClass.getName}"))
  }


}