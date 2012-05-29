package org.adehaus.scalisp

trait Sexpr {
  def eval(env: Environment = EmptyEnvironment): (Value, Environment)
}

case class Symbol(symbol: String) extends Sexpr {
  def eval(env: Environment) = env.get(symbol).eval(env)
}

case class StringConstant(value: String) extends Sexpr {
  def eval(env: Environment) = (StringValue(value), env)
}

case class LongConstant(value: Long) extends Sexpr {
  def eval(env: Environment) = (LongValue(value), env)
}

case class FloatConstant(value: Float) extends Sexpr {
  def eval(env: Environment) = (FloatValue(value), env)
}

case class Addition(args: List[Sexpr]) extends Sexpr {
  def eval(env: Environment) = {
    val sum: Long = args.map(_.eval(env)._1.asLong).foldLeft(0l)(_+_)
    (LongValue(sum), env)
  }
}

case class Noop() extends Sexpr {
  def eval(env: Environment) = (LongValue(0l), env)
}