package org.adehaus.scalisp

trait Lambda {
  def eval(env: Environment = EmptyEnvironment): Value
}

case class Symbol(symbol: String) extends Lambda {
  def eval(env: Environment): Value = env.get(symbol).eval(env)
}

case class StringConstant(value: String) extends Lambda {
  def eval(env: Environment) = StringValue(value)
}

case class LongConstant(value: Long) extends Lambda {
  def eval(env: Environment) = LongValue(value)
}

case class FloatConstant(value: Float) extends Lambda {
  def eval(env: Environment) = FloatValue(value)
}

case class Addition(args: List[Lambda]) extends Lambda {
  def eval(env: Environment): Value = {
    LongValue(args.map(_.eval(env).asLong).foldLeft(0l)(_+_))
  }
}

case class Noop() extends Lambda {
  def eval(env: Environment) = LongValue(0l)
}