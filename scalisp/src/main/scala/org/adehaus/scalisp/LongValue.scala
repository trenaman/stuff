package org.adehaus.scalisp

case class LongValue(value: Long) extends Value {
  def asFloat = value.toFloat

  def asLong = value

  def asString = "%s".format(value)
}
