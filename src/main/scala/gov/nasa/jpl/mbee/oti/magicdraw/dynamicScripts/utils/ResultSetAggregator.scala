package gov.nasa.jpl.mbee.oti.magicdraw.dynamicScripts.utils

import scala.collection.immutable.Set
import scalaz.{Monoid, \&/, NonEmptyList, Semigroup}
import scalaz.Scalaz._

object ResultSetAggregator {
  def zero[A]:  NonEmptyList[java.lang.Throwable] \&/ Set[A] = \&/.That(Set[A]())
}

trait ResultSetAggregator[A] extends Monoid[NonEmptyList[java.lang.Throwable] \&/ Set[A]] {
  type F = NonEmptyList[java.lang.Throwable] \&/ Set[A]
  override def zero: F = ResultSetAggregator.zero[A]
  override def append(f1: F, f2: => F): F = f1 append f2
}
