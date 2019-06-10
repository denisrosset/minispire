package spire
package syntax

import spire.algebra.{Field, CRing}

import spire.macros.Macros

object primitives {

  implicit class IntAs(n:Int) {
    def as[A](implicit ev:CRing[A]):A = macro Macros.intAs[A]
  }

  implicit class DoubleAs(n:Double) {
    def as[A](implicit ev:Field[A]):A = macro Macros.dblAs[A]
  }

}
