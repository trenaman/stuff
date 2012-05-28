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
    val trimmedCmd = cmd.trim
    if (trimmedCmd.equals(":q")) {
      (-1, "Bye Bye")
    } else if (trimmedCmd.equals(":h")) {
      (0, helpMessage)
    } else {
      val x: ParseResult[Any] = parseAll(sexpr, trimmedCmd)

      val msg = if (x.successful) x.toString else "Parser error"
      (0, msg)
    }
  }

}
