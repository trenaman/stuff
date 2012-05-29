package org.adehaus.scalisp

case class Environment (map: Map[String, Lambda]) {
  def add(lambda: Pair[String, Lambda]): Environment = {
    Environment(map + lambda)
  }

  def get(key: String): Lambda = {
    map(key)
  }
}

object EmptyEnvironment extends Environment(Map.empty[String, Lambda])
