package org.adehaus.palindromicpi

import collection.Iterator


class PiSubsequenceIterator(fileName: String, segmentSize: Int) extends Iterator[(Long, String)] {
  val piIterator = new PiDigitIterator(fileName)

  var nextSubsequenceOpt: Option[Vector[Char]] = nextFullSubsequence
  var n: Long = 1

  def hasNext = nextSubsequenceOpt.isDefined

  def next() = {
    val ret = nextSubsequenceOpt map { nextSubsequence =>
      (n, new String(nextSubsequence.toArray))
    } getOrElse {
      throw new IllegalStateException("next() called on empty iterator.")
    }

    prepareNextSubsequence

    ret
  }

  private def prepareNextSubsequence {
    nextSubsequenceOpt = nextSubsequenceOpt map ( _.tail :+ piIterator.next.toChar )
    n = n + 1
  }

  private def nextFullSubsequence: Option[Vector[Char]] = {
    var nextSegmentAsVector = Vector.empty[Char]

    piIterator.take(segmentSize) foreach { digit =>
      nextSegmentAsVector = nextSegmentAsVector :+ digit.toChar
    }

    if (nextSegmentAsVector.size == segmentSize) {
      Some(nextSegmentAsVector)
    } else {
      None
    }
  }

}
