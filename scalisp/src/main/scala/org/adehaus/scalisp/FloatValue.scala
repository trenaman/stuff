package org.adehaus.scalisp

case class FloatValue(val value: Float) extends Value {
  def asFloat = value
  def asLong = value.toLong
  def asString = "%f".format(value)
}
