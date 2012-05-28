package org.adehaus.scalisp

import util.parsing.combinator._

class LispParser extends JavaTokenParsers {


  def addition: Parser[Any] = "("~"+"~rep(atom)~")" ^^ {
    case "("~"+"~atoms~")" => atoms.foldLeft(0)(0 + _.toLong)
  }

  def atom: Parser[Any] = (wholeNumber | decimalNumber | floatingPointNumber | sexpr )

  // def atom: Parser[Any] = stringLiteral | decimalNumber | wholeNumber | floatingPointNumber | ident | sexpr
//  def defun: Parser[Any] = "defun"~ident~sexpr
//  def op: Parser[Any] = "+" | "-" | "*" | "/" | "%" | "or" | "and"
}
