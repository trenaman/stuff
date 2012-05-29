package org.adehaus.scalisp

case class Environment (map: Map[String, Sexpr]) {
  def add(lambda: Pair[String, Sexpr]): Environment = {
    Environment(map + lambda)
  }

  def get(key: String): Sexpr = {
    map(key)
  }
}

object EmptyEnvironment extends Environment(Map.empty[String, Sexpr])
