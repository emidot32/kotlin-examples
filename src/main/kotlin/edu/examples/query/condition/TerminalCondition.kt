package edu.examples.query.condition

sealed class TerminalCondition(open val name: String, open val operator: ConditionOperator, open val value: Any) : Condition {

  override fun toString(): String = "$name $operator '$value'"

  // Business logic for getting the value for comparison
  abstract fun getValue(elem: Any): Any?
}
