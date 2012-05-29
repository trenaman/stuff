package org.adehaus.scalisp


import util.parsing.combinator._

object Main extends LispParser {

  val Prompt = "scalisp> "

  def main(args: Array[String]) = {

    var cmd = readCmd
    var result = eval(cmd)

    while (result._1 ==  0) {
      Console.println(result._2)
      Console.flush
      result = eval(readCmd)
    }
  }

  def readCmd: String = {
    Console.print(Prompt)
    Console.flush
    var cmd = Console.readLine()

    while (cmd.isEmpty) {
      Console.print(Prompt)
      Console.flush
      cmd = Console.readLine()
    }
    cmd
  }

  def helpMessage: String = {
    ":q to quit\n:h this message"
  }

  def eval(cmd: String): (Int, String) = {
    cmd.trim match {
      case ":q"         => (-1, "Bye Bye")
      case ":h"         => ( 0, helpMessage)
      case trimmedCmd =>
        val x: ParseResult[Sexpr] = parseAll(sexpr, trimmedCmd)

        val msg = if (x.successful)
          x.get.eval(EmptyEnvironment)._1.asString
        else
          "Parser error"

        (0, msg)
    }
  }

}
