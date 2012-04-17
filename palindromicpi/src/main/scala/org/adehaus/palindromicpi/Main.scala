package org.adehaus.palindromicpi

import scala.math.sqrt
import io.BufferedSource
import collection.immutable.Set

object Main {
  def main(args: Array[String]) {
    val piIter = new PiSubsequenceIterator("/Users/ade/Downloads/PI1MILDS.TXT", 7)

    val msg =  piIter.find( segment => palindromes.contains(segment._2)) map { solution =>
      "The solution is segment %d '%s'".format(solution._1, solution._2)
    } getOrElse "No solution found :("

    println(msg)
  }

  // Fun way to generate the palindromes
  //
  val palindromes: Set[String] = {
    (1000 to 9999) map {
      n =>
        val stem = "%d".format(n)
        "%s%s".format(stem, stem.substring(0, 3).reverse).toLong
    } filter (isPrime(_)) map (_.toString) toSet
  }

  /**
   * Very naive test for primality of a number n
   * @param n
   * @return
   */
  def isPrime(n: Long) = {
    ((2l to sqrt(n).toLong) filter (n % _ == 0)).size == 0
  }
}
