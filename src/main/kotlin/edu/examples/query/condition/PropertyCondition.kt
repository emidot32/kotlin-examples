package edu.examples.query.condition

data class PropertyCondition(
  override val name: String,
  override val operator: ConditionOperator,
  override val value: Any,
) : TerminalCondition(name, operator, value) {

  override fun getValue(elem: Any): Any? = elem
}
