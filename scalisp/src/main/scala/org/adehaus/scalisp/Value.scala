package org.adehaus.scalisp

trait Value {
  def asFloat: Float
  def asLong: Long
  def asString: String
}
