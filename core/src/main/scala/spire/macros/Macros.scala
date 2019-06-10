package spire
package macros

import spire.algebra.{Field, CRing}
import spire.macros.compat.Context
import spire.math.{Rational, UInt, ULong}

object Macros {

  def intAs[A : c.WeakTypeTag](c:Context)(ev : c.Expr[CRing[A]]):c.Expr[A] = {
    import c.universe._
    c.Expr[A](c.prefix.tree match {
      case Apply((_, List(Literal(Constant(0))))) => q"$ev.zero"
      case Apply((_, List(Literal(Constant(1))))) => q"$ev.one"
      case Apply((_, List(n))) => q"$ev.fromInt($n)"
    })
  }

  def dblAs[A : c.WeakTypeTag](c:Context)(ev : c.Expr[Field[A]]):c.Expr[A]= {
    import c.universe._
    c.Expr[A](c.prefix.tree match {
      case Apply((_, List(Literal(Constant(0.0))))) => q"$ev.zero"
      case Apply((_, List(Literal(Constant(1.0))))) => q"$ev.one"
      case Apply((_, List(n))) => q"$ev.fromDouble($n)"
    })
  }
}
