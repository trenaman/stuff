package org.adehaus.graph

import collection.Iterable
import annotation.tailrec
import collection.mutable.ListBuffer
import collection.immutable.Set

class Graph[T] (items: Iterator[T], connected: (T, T) => Boolean) {
  private val nodes: scala.collection.mutable.Map[T, Node[T]] = scala.collection.mutable.Map.empty[T, Node[T]]
  items foreach { add(_) }

  private def add(item: T) {
    val newNode = new Node(item)

    nodes.values foreach  { node =>
      if (connected(node.data, newNode.data)) {
        connect(node, newNode)
      }
    }

    nodes += (item -> newNode)
  }

  private def connect(a: Node[T], b: Node[T]) = {
    a.connections += b
    b.connections += a
  }

  override def toString = {
    nodes.values.map(_.toString).mkString("\n")
  }

  def findPaths(a: T, b: T): Iterable[List[Node[T]]] = {
    nodes.get(a).toIterable flatMap { startNode: Node[T] =>
      val startingPoint = NodeSearchPoint(startNode, Nil, Set.empty[Node[T]])
      search(b, Seq(startingPoint), Nil)
    }
  }

  case class NodeSearchPoint[T](node: Node[T], path: List[Node[T]], visited: Set[Node[T]])

  @tailrec
  private def search(target: T, searchPoints: Seq[NodeSearchPoint[T]], accum: Seq[List[Node[T]]]): Seq[List[Node[T]]] = {
    if (searchPoints.isEmpty) {
      accum
    } else {
      val searchPoint = searchPoints.head
      if (searchPoint.path.length > 3) { // Ignore long paths
        search(target, searchPoints.tail, accum)
      } else if (searchPoint.node.data == target) {
        val path = (searchPoint.node :: searchPoint.path)
        println("%s".format(path map (_.data) mkString(" -> ")))
        search(target, searchPoints.tail, accum :+ (searchPoint.node :: searchPoint.path)  )
      } else {
        val path = searchPoint.node :: searchPoint.path
        val visited = searchPoint.visited + searchPoint.node
        val newSearchPoints = (searchPoint.node.connections -- visited) map { node =>
          NodeSearchPoint(node, path, visited)
        }
        search(target, newSearchPoints ++ searchPoints.tail, accum)
      }
    }

  }

  def size = nodes.keys.size
}
