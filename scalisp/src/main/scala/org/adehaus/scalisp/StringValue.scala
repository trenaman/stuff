package org.adehaus.scalisp

case class StringValue(value: String) extends Value {
  def asFloat = value.toFloat
  def asLong = value.toLong
  def asString = value
}
