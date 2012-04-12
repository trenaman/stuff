package org.adehaus.graph

import collection.mutable.ListBuffer

case class Node[T](
  data: T,
  connections: ListBuffer[Node[T]] = ListBuffer.empty[Node[T]])
{
  override def toString = {
    "%s -> %s".format(data.toString, connections.map(_.data).mkString(","))
  }

  override def hashCode() = data.hashCode
}