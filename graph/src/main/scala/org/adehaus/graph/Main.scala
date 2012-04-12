package org.adehaus.graph
import scala.io.Source
import collection.Iterator
import collection.immutable.IndexedSeq

object Main {

  val Dictionary = "/usr/share/dict/words"
  val WordSize = 4
  val start = "head"
  val finish = "tail"

  def main(args: Array[String]) {

    println("Loading words...")
    val fourLetterWords = Source.fromFile(Dictionary).getLines() filter { _.size == WordSize } map { _.toLowerCase }

    println("Building graph...")
    val graph = new Graph[String](fourLetterWords, connected)

    println("The size of the graph is %d".format(graph.size))

    println("Searching from %s to %s".format(start, finish))
    val results = graph.findPaths(start, finish)
    if (results.isEmpty) {
      println("No path :(")
    } else {
      println("Found %d results:".format(results.size))
      results map { path =>
        println(path.map(_.data).mkString("->"))
      }
    }

  }

  def connected(a: String, b: String): Boolean = {
    require(a.length == WordSize && b.length == WordSize)

    val characterDistances = (a zip b) map (t => if (t._1 == t._2) 0 else 1)

    val distance = (0 /: characterDistances) (_ + _)

    distance == 1
  }
}
