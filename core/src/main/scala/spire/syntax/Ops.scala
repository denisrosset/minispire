package spire
package syntax

import spire.algebra._
import spire.macros.Ops

final class EqOps[A](lhs:A)(implicit ev:Eq[A]) {
  def ===[B](rhs:B)(implicit ev: B =:= A): Boolean = macro Ops.eqv[A, B]
  def =!=[B](rhs:B)(implicit ev: B =:= A): Boolean = macro Ops.neqv[A, B]
}

final class PartialOrderOps[A](lhs: A)(implicit ev: PartialOrder[A]) {
  def >(rhs: A): Boolean = macro Ops.binop[A, Boolean]
  def >=(rhs: A): Boolean = macro Ops.binop[A, Boolean]
  def <(rhs: A): Boolean = macro Ops.binop[A, Boolean]
  def <=(rhs: A): Boolean = macro Ops.binop[A, Boolean]

  def partialCompare(rhs: A): Double = macro Ops.binop[A, Double]
  def tryCompare(rhs: A): Option[Int] = macro Ops.binop[A, Option[Int]]
}

final class OrderOps[A](lhs: A)(implicit ev: Order[A]) {
  def compare(rhs: A): Int = macro Ops.binop[A, Int]
}

final class CRigOps[A](lhs: A)(implicit ev: CRig[A]) {
  def +(rhs:A): A = macro Ops.binop[A, A]
  def isZero(implicit ev1: Eq[A]): Boolean = macro Ops.unopWithEv2[Eq[A], Boolean]
  def *(rhs:A): A = macro Ops.binop[A, A]
  def isOne(implicit ev1: Eq[A]): Boolean = macro Ops.unopWithEv2[Eq[A], Boolean]
}

final class CRingOps[A](lhs: A)(implicit ev: CRing[A]) {
  def unary_-(): A = macro Ops.unop[A]
  def -(rhs:A): A = macro Ops.binop[A, A]
}

final class FieldOps[A](lhs: A)(implicit ev: Field[A]) {
  def reciprocal(): A = macro Ops.unop[A]
  def /(rhs:A): A = macro Ops.binop[A, A]
}
