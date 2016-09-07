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

package gov.nasa.jpl.imce.oti.magicdraw.dynamicScripts.utils

import scala.collection.immutable._
import scalaz._
import scala.util.{Failure,Try}

object OTIHelper {

  def toTry[U,V]
  (u: Set[java.lang.Throwable] \&/ U,
   f: U => Try[V])
  : Try[V]
  = u.fold[Try[V]](
    (errors: Set[java.lang.Throwable]) =>
      Failure(errors.head),
    (ru: U) =>
      f(ru),
    (errors: Set[java.lang.Throwable], ru: U) =>
      if (errors.nonEmpty)
        Failure(errors.head)
      else
        f(ru))

  def toTry[U,V]
  (u: Set[java.lang.Throwable] \/ U,
   f: U => Try[V])
  : Try[V]
  = u.fold[Try[V]](
    (errors: Set[java.lang.Throwable]) =>
      Failure(errors.head),
    (ru: U) =>
      f(ru))


}