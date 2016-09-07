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

import java.lang.System

import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.nodes._
import gov.nasa.jpl.dynamicScripts.magicdraw.ui.tables._
import gov.nasa.jpl.dynamicScripts.magicdraw.utils._
import org.omg.oti.uml._
import org.omg.oti.json.common.OTIPrimitiveTypes._
import org.omg.oti.uml.read.api._
import org.omg.oti.magicdraw.uml.read._

import scala.collection.immutable._
import scala.util.{Failure, Success, Try}
import scala.{None,Some,StringContext}
import scala.Predef.{ArrowAssoc,String}


object RelationTripleWidgetHelper {

  def makeComputedDerivedTreeForRelationTriple
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget )
  : DynamicScriptsTypes.ComputedDerivedTree =
    DynamicScriptsTypes.ComputedDerivedTree(
      derived.name, derived.icon, derived.context, derived.access,
      derived.className, derived.methodName, derived.refresh,
      columnValueTypes = Some( Seq(
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "sContext" ),
          typeName = DynamicScriptsTypes.HName( "sContext" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "subject" ),
          typeName = DynamicScriptsTypes.HName( "subject" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "sMetaclass" ),
          typeName = DynamicScriptsTypes.HName( "sMetaclass" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "relation" ),
          typeName = DynamicScriptsTypes.HName( "relation" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "object" ),
          typeName = DynamicScriptsTypes.HName( "object" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "oMetaclass" ),
          typeName = DynamicScriptsTypes.HName( "oMetaclass" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "oNamespace" ),
          typeName = DynamicScriptsTypes.HName( "oNamespace" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ),
        DynamicScriptsTypes.DerivedFeatureValueType(
          key = DynamicScriptsTypes.SName( "oPackage" ),
          typeName = DynamicScriptsTypes.HName( "oPackage" ),
          typeInfo = DynamicScriptsTypes.StringTypeDesignation() ) ) ) )

  def createRowForRelationTriple
  ( r: RelationTriple[MagicDrawUML] )
  ( implicit umlUtil: MagicDrawUMLUtil )
  : Map[String, AbstractTreeNodeInfo] = {
    import umlUtil._
    Map(
      "sContext" -> ( r.sub.owner match {
        case None => LabelNodeInfo( "<none>" )
        case Some( o ) => o match {
          case parent: UMLNamedElement[Uml] =>
            ReferenceNodeInfo( parent.qualifiedName.get, umlMagicDrawUMLElement(parent).getMagicDrawElement )
          case parent =>
            toToolSpecificIDReferenceNodeInfo(parent)
        }
      } ),
      "subject" ->
        ( r.sub match {
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
              umlMagicDrawUMLElement(r.sub).getMagicDrawElement )
          case e: UMLElement[Uml] =>
            toToolSpecificIDReferenceNodeInfo(e)
        } ),
      "sMetaclass" -> LabelNodeInfo( r.sub.xmiType.head ),
      "relation" ->
        ( r match {
          case a: AssociationTriple[Uml, _, _]  =>
            LabelNodeInfo( a.relf.propertyName )
          case s: StereotypePropertyTriple[Uml] =>
            ReferenceNodeInfo(
              s"${s.rels.name.get}::${s.relp.name.get}", umlMagicDrawUMLElement(s.relp).getMagicDrawElement )
        } ),
      "object" ->
        ( r.obj match {
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
              umlMagicDrawUMLElement(r.obj).getMagicDrawElement )
          case e: UMLElement[Uml] =>
            toToolSpecificIDReferenceNodeInfo(e)
        } ),
      "oMetaclass" -> LabelNodeInfo( r.obj.xmiType.head ),
      "oNamespace" -> ( r.obj.owningNamespace match {
        case None       =>
          LabelNodeInfo( "<none>" )
        case Some( ns ) =>
          ReferenceNodeInfo( ns.qualifiedName.get, umlMagicDrawUMLElement(ns).getMagicDrawElement )
      } ),
      "oPackage" -> ( getPackageOrProfileOwner( r.obj ) match {
        case None =>
          LabelNodeInfo( "<none>" )
        case Some( pkg ) =>
          ReferenceNodeInfo( pkg.qualifiedName.get, umlMagicDrawUMLElement(pkg).getMagicDrawElement )
      } ) )
  }

  def createGroupTableUIPanelForRelationTriples
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget,
    pes: Iterable[RelationTriple[MagicDrawUML]] )
  ( implicit util: MagicDrawUMLUtil )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )] = {

    val rows: Seq[Map[String, AbstractTreeNodeInfo]] =
      pes.map(RelationTripleWidgetHelper.createRowForRelationTriple).to[Seq]

    System.out.println(s"createGroupTableUIPanelForRelationTriples => ${rows.size} rows")
    val ui = GroupTableNodeUI(
      makeComputedDerivedTreeForRelationTriple( derived ),
      rows,
      Seq( "sContext", "subject", "sMetaclass", "relation", "object", "oMetaclass", "oNamespace", "oPackage" ) )
    //ui._table.addMouseListener( DoubleClickMouseListener.createAbstractTreeNodeInfoDoubleClickMouseListener( ui._table ) )
    HyperlinkTableCellValueEditorRenderer.addRenderer4AbstractTreeNodeInfo( ui._table )

    val validationAnnotations = rows flatMap
      ( _.values ) flatMap
      AbstractTreeNodeInfo.collectAnnotationsRecursively

    Success( ( ui.panel, validationAnnotations ) )
  }

  def packageRelationTripleWidget
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget,
    mdPkg: MagicDrawUML#Package,
    f: UMLPackage[MagicDrawUML] => Try[Set[RelationTriple[MagicDrawUML]]],
    util: MagicDrawUMLUtil )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    f( util.umlPackage( mdPkg ) ) match {
      case Failure( t ) => Failure( t )
      case Success( triples ) =>
        createGroupTableUIPanelForRelationTriples( derived, triples )( util )
    }

  def namespaceRelationTripleWidget
  ( derived: DynamicScriptsTypes.ComputedDerivedWidget,
    mdNs: MagicDrawUML#Namespace,
    f: UMLNamespace[MagicDrawUML] => Try[Set[RelationTriple[MagicDrawUML]]],
    util: MagicDrawUMLUtil )
  : Try[( java.awt.Component, Seq[ValidationAnnotation] )] =
    f( util.umlNamespace( mdNs ) ) match {
      case Failure( t ) => Failure( t )
      case Success( triples ) =>
        createGroupTableUIPanelForRelationTriples( derived, triples )( util )
    }
}