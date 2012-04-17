package org.adehaus.palindromicpi


class PiDigitIterator(fileName: String) extends Iterator[Int] {
  private val digits = scala.io.Source.fromFile(fileName).bufferedReader()

  var nextDigit: Int = digits.read

  def hasNext = nextDigit != -1

  def next() = {
    val ret = nextDigit
    nextDigit = digits.read
    ret
  }
}
