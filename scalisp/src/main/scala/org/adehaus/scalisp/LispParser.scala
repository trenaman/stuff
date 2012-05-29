package org.adehaus.scalisp

import util.parsing.combinator._

class LispParser extends JavaTokenParsers {

  def sexpr: Parser[Lambda] = (
      "("~"+"~>rep(atom)<~")"       ^^ { Addition(_) }
    | "("~"defun"~>rep(atom)<~")"   ^^ { x => LongConstant(0l) }
  )

  def atom: Parser[Lambda] = (
        ident ^^ { x: String =>  Symbol(x) }
      | wholeNumber ^^ { x: String => LongConstant(x.toLong) }
      | floatingPointNumber ^^ { x: String => FloatConstant(x.toFloat) }
      | stringLiteral ^^ { x: String => StringConstant(x.toString) }
      | sexpr
  )
}
