package edu.examples.query.condition

data class OrCondition(val conditions: List<Condition>) : Condition {
  operator fun plus(condition: Condition) = OrCondition(this.conditions + condition)

  operator fun plus(conditions: List<Condition>) = OrCondition(this.conditions + conditions)

  override fun toString() = conditions.joinToString(separator = " || ", prefix = "(", postfix = ")")
}
