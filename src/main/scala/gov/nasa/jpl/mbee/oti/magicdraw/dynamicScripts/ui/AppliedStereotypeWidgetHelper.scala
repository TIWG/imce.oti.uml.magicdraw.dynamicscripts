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
package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.ui

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.nodes._
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.tables._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import org.omg.oti.magicdraw.uml.read._
import org.omg.oti.uml.read.UMLStereotypeTagValue
import org.omg.oti.uml.read.api._

import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}


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
                ReferenceNodeInfo(tv.extendedElement.toolSpecific_id.head, tv.extendedElement.getMagicDrawElement)
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
                ReferenceNodeInfo(tv.extendedElement.toolSpecific_id.head, tv.extendedElement.getMagicDrawElement)
            })
        )
      case tv: MagicDrawUMLStereotypeTagPropertyMetaclassElementReference =>
        Map(
          "extended element" ->
            (tv.extendedElement match {
              case ne: MagicDrawUMLNamedElement if ne.qualifiedName.isDefined =>
                ReferenceNodeInfo(ne.name.get, ne.getMagicDrawElement)
              case _ =>
                ReferenceNodeInfo(tv.extendedElement.toolSpecific_id.head, tv.extendedElement.getMagicDrawElement)
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
                ReferenceNodeInfo(e.toolSpecific_id.head, e.getMagicDrawElement)
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
                ReferenceNodeInfo(tv.extendedElement.toolSpecific_id.head, tv.extendedElement.getMagicDrawElement)
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
                ReferenceNodeInfo(e.toolSpecific_id.head, e.getMagicDrawElement)
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
                ReferenceNodeInfo(tv.extendedElement.toolSpecific_id.head, tv.extendedElement.getMagicDrawElement)
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
   util: MagicDrawUMLUtil)(implicit uTag: ClassTag[U])
  : Try[(java.awt.Component, Seq[ValidationAnnotation])] = {
    val e = util.umlElement(mdE)
    val uClass = uTag.runtimeClass
    require(uClass != null)
    if (uClass.isInstance(e))
      createGroupTableUIPanelForElements(derived, e.tagValues)(util)
    else
      Failure(new IllegalArgumentException(s"${mdE.getHumanType}: ${mdE.getID} is not a kind of ${uClass.getName}"))
  }


}