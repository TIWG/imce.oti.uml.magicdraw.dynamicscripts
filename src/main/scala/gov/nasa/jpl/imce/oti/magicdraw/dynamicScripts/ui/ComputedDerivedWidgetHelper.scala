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

import java.lang.{IllegalArgumentException,System}
import org.omg.oti.uml.xmi.IDGenerator

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.nodes._
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.tables._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._

import org.omg.oti.json.common.OTIPrimitiveTypes._
import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read._

import scala.collection.immutable._
import scala.util.{Failure, Success, Try}
import scala.{Option,None,Some,StringContext}
import scala.Predef.{require,ArrowAssoc,String}

import scala.reflect.ClassTag
import scalaz._

object ComputedDerivedWidgetHelper {

  def makeComputedDerivedTreeForPackageNameMetaclass
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget )
  : DynamicScriptsTypes.ComputedDerivedTree =
    DynamicScriptsTypes.ComputedDerivedTree(
      derived.name, derived.icon, derived.context, derived.access,
      derived.className, derived.methodName, derived.refresh,
      columnValueTypes = Some( Seq(
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "context" ),
          typeName = DynamicScriptsTypes.HName( "context" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "EMF eContainingFeature" ),
          typeName = DynamicScriptsTypes.HName( "EMF eContainingFeature" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "OTI containing meta-property" ),
          typeName = DynamicScriptsTypes.HName( "OTI containing meta-property" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "name" ),
          typeName = DynamicScriptsTypes.HName( "name" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "metaclass" ),
          typeName = DynamicScriptsTypes.HName( "metaclass" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ) ) ) )

  def createRowForElement
  ( e: UMLElement[MagicDrawUML] )
  ( implicit umlUtil: MagicDrawUMLUtil, idg: IDGenerator[MagicDrawUML] )
  : Map[String, AbstractTreeNodeInfo] = {
    import umlUtil._
    Map(
      "context" -> ( e.owner match {
        case None => LabelNodeInfo( "<none>" )
        case Some( o ) => o match {
          case parent: UMLNamedElement[Uml] =>
            ReferenceNodeInfo( parent.qualifiedName.get, umlMagicDrawUMLElement(parent).getMagicDrawElement )
          case parent                       =>
            toToolSpecificIDReferenceNodeInfo(parent)
        }
      } ),
      "EMF eContainingFeature" -> {
        Option.apply(umlUtil.umlMagicDrawUMLElement(e).getMagicDrawElement.eContainingFeature) match {
          case None => LabelNodeInfo("<none>")
          case Some(f) => LabelNodeInfo(f.getName)
        }
      },
      "OTI containing meta-property" -> {
        val meval = e.getContainingMetaPropertyEvaluator.getOrElse(None)
        meval match {
          case None => LabelNodeInfo("<none>")
          case Some(mp) => LabelNodeInfo(mp.propertyName)
        }
      },
      "name" ->
        ( e match {
          case ne: UMLNamedElement[Uml] =>
            ReferenceNodeInfo(
              ( ne, ne.name ) match {
                case ( l: UMLLiteralBoolean[Uml], _ )          => l.value.toString
                case ( l: UMLLiteralInteger[Uml], _ )          => l.value.toString
                case ( l: UMLLiteralReal[Uml], _ )             => l.value.toString
                case ( l: UMLLiteralString[Uml], _ )           => l.value.toString
                case ( l: UMLLiteralUnlimitedNatural[Uml], _ ) => l.value.toString
                case ( v: UMLInstanceValue[Uml], _ ) => v.instance match {
                  case None      => "<unbound element>"
                  case Some( e ) => s"=> ${e.mofMetaclassName}: ${e.toolSpecific_id}"
                }
                case ( v: MagicDrawUMLElementValue, _ ) => v.element match {
                  case None      => "<unbound element>"
                  case Some( e ) => s"=> ${e.mofMetaclassName}: ${e.toolSpecific_id}"
                }
                case ( _, Some( name ) ) => name
                case ( _, _ )            => TOOL_SPECIFIC_ID.unwrap(ne.toolSpecific_id)
              },
              umlMagicDrawUMLElement(e).getMagicDrawElement )
          case e: UMLElement[Uml] =>
            toToolSpecificIDReferenceNodeInfo(e)
        } ),
      "metaclass" -> LabelNodeInfo( e.xmiType.head ) )
  }

  def createGroupTableUIPanelForElements[U <: UMLElement[MagicDrawUML]]
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget,
    pes: Iterable[U] )
  ( implicit util: MagicDrawUMLUtil, idg: IDGenerator[MagicDrawUML] )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )] = {

    val rows: Seq[Map[String, AbstractTreeNodeInfo]] = pes.map ( createRowForElement ).to[Seq]
    System.out.println(s"* ${rows.size} rows")
    System.out.println(rows.map(_.toString).mkString("\n *","\n *","\n"))

    val ui = GroupTableNodeUI(
      makeComputedDerivedTreeForPackageNameMetaclass( derived ),
      rows,
      Seq( "context", "EMF eContainingFeature", "OTI containing meta-property", "name", "metaclass" ) )
    //ui._table.addMouseListener( DoubleClickMouseListener.createAbstractTreeNodeInfoDoubleClickMouseListener( ui._table ) )
    HyperlinkTableCellValueEditorRenderer.addRenderer4AbstractTreeNodeInfo( ui._table )

    val validationAnnotations = rows flatMap
      ( _.values ) flatMap
      AbstractTreeNodeInfo.collectAnnotationsRecursively

    Success( ( ui.panel, validationAnnotations ) )
  }

  def elementOperationWidget[U <: UMLElement[MagicDrawUML], V <: UMLElement[MagicDrawUML]]
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget,
    mdE: MagicDrawUML#Element,
    f: U => Iterable[V],
    util: MagicDrawUMLUtil )
  ( implicit uTag: ClassTag[U], idg: IDGenerator[MagicDrawUML] )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )] = {
      val e = util.umlElement( mdE )
      val uClass = uTag.runtimeClass
      require (uClass != null)
      if ( uClass.isInstance( e ) )
        createGroupTableUIPanelForElements[V]( derived, f( e.asInstanceOf[U] ) )( util, idg )
      else
        Failure( new IllegalArgumentException(s"${mdE.getHumanType}: ${mdE.getID} is not a kind of ${uClass.getName}"))
  }

  def makeComputedDerivedTreeForProperty
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget )
  : DynamicScriptsTypes.ComputedDerivedTree =
    DynamicScriptsTypes.ComputedDerivedTree(
      derived.name, derived.icon, derived.context, derived.access,
      derived.className, derived.methodName, derived.refresh,
      columnValueTypes = Some( Seq(
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "context" ),
          typeName = DynamicScriptsTypes.HName( "context" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "EMF eContainingFeature" ),
          typeName = DynamicScriptsTypes.HName( "EMF eContainingFeature" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "OTI containing meta-property" ),
          typeName = DynamicScriptsTypes.HName( "OTI containing meta-property" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "name" ),
          typeName = DynamicScriptsTypes.HName( "name" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "metaclass" ),
          typeName = DynamicScriptsTypes.HName( "metaclass" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "aggregation" ),
          typeName = DynamicScriptsTypes.HName( "aggregation" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "type" ),
          typeName = DynamicScriptsTypes.HName( "type" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "multiplicity" ),
          typeName = DynamicScriptsTypes.HName( "multiplicity" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "isLogicallyNavigable" ),
          typeName = DynamicScriptsTypes.HName( "isLogicallyNavigable" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ) ,
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "isSemanticallyNavigable" ),
          typeName = DynamicScriptsTypes.HName( "isSemanticallyNavigable" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() )  ) ) )

  def createRowForProperty
  ( e: UMLProperty[MagicDrawUML] )
  ( implicit umlUtil: MagicDrawUMLUtil, idg: IDGenerator[MagicDrawUML] )
  : Map[String, AbstractTreeNodeInfo] = {
    import umlUtil._
    Map(
      "context" -> ( e.owner match {
        case None => LabelNodeInfo( "<none>" )
        case Some( o ) => o match {
          case parent: UMLNamedElement[Uml] =>
            ReferenceNodeInfo( parent.qualifiedName.get, umlMagicDrawUMLElement(parent).getMagicDrawElement )
          case parent                       =>
            toToolSpecificIDReferenceNodeInfo(parent)
        }
      } ),
      "EMF eContainingFeature" -> {
        Option.apply(umlUtil.umlMagicDrawUMLElement(e).getMagicDrawElement.eContainingFeature) match {
          case None => LabelNodeInfo("<none>")
          case Some(f) => LabelNodeInfo(f.getName)
        }
      },
      "OTI containing meta-property" -> {
        val meval = e.getContainingMetaPropertyEvaluator.getOrElse(None)
        meval match {
          case None => LabelNodeInfo("<none>")
          case Some(mp) => LabelNodeInfo(mp.propertyName)
        }
      },
      "name" ->
        (e.name match {
          case Some( name ) => LabelNodeInfo(name)
          case None           => ReferenceNodeInfo(TOOL_SPECIFIC_ID.unwrap(e.toolSpecific_id), umlMagicDrawUMLElement(e).getMagicDrawElement)
        }),
      "metaclass" -> LabelNodeInfo( e.xmiType.head ),
      "aggregation" -> ( e.aggregation match {
        case None => LabelNodeInfo("<none>")
        case Some(a) => LabelNodeInfo(a.toString)
      }),
      "type" -> ( e._type match {
        case None => LabelNodeInfo("<none>")
        case Some(et) => ReferenceNodeInfo(
          et.name.head,
          umlUtil.umlMagicDrawUMLElement(et).getMagicDrawElement )
      }),
      "multiplicity" -> {
        val l = e.lower
        val u = e.upper
        if (u == -1)
          LabelNodeInfo(s"[$l..*]")
        else
          LabelNodeInfo(s"[$l..$u]")
      },
      "isLogicallyNavigable" -> LabelNodeInfo( e.isLogicallyNavigable.toString ),
      "isSemanticallyNavigable" -> LabelNodeInfo( e.isSemanticallyNavigable.toString )
    )
  }

  def createGroupTableUIPanelForProperties[U <: UMLProperty[MagicDrawUML]]
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget,
    pes: Iterable[U] )
  ( implicit util: MagicDrawUMLUtil, idg: IDGenerator[MagicDrawUML] )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )] = {

    val rows: Seq[Map[String, AbstractTreeNodeInfo]] = pes.map ( createRowForProperty ).to[Seq]
    System.out.println(s"* ${rows.size} rows")
    System.out.println(rows.map(_.toString).mkString("\n *","\n *","\n"))

    val ui = GroupTableNodeUI(
      makeComputedDerivedTreeForProperty( derived ),
      rows,
      Seq(
        "context", "EMF eContainingFeature", "OTI containing meta-property", "name",
        "metaclass", "aggregation", "type", "multiplicity", "isLogicallyNavigable", "isSemanticallyNavigable" ) )
    //ui._table.addMouseListener( DoubleClickMouseListener.createAbstractTreeNodeInfoDoubleClickMouseListener( ui._table ) )
    HyperlinkTableCellValueEditorRenderer.addRenderer4AbstractTreeNodeInfo( ui._table )

    val validationAnnotations = rows flatMap
      ( _.values ) flatMap
      AbstractTreeNodeInfo.collectAnnotationsRecursively

    Success( ( ui.panel, validationAnnotations ) )
  }

  def propertyOperationWidget[U <: UMLElement[MagicDrawUML], V <: UMLProperty[MagicDrawUML]]
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget,
    mdE: MagicDrawUML#Element,
    f: U => Iterable[V],
    util: MagicDrawUMLUtil )
  ( implicit uTag: ClassTag[U], idg: IDGenerator[MagicDrawUML] )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )] = {
    val e = util.umlElement( mdE )
    val uClass = uTag.runtimeClass
    require (uClass != null)
    if ( uClass.isInstance( e ) )
      createGroupTableUIPanelForProperties[V]( derived, f( e.asInstanceOf[U] ) )( util, idg )
    else
      Failure( new IllegalArgumentException(s"${mdE.getHumanType}: ${mdE.getID} is not a kind of ${uClass.getName}"))
  }


}