package edu.examples.query.condition

data class AndCondition(val conditions: List<Condition>) : Condition {
  operator fun plus(condition: Condition) =
    when (condition) {
      is AndCondition -> AndCondition(this.conditions + condition.conditions)
      else -> AndCondition(this.conditions + condition)
    }

  operator fun plus(conditions: List<Condition>) = AndCondition(this.conditions + conditions)

  override fun toString() = conditions.joinToString(separator = " && ", prefix = "(", postfix = ")")
}

