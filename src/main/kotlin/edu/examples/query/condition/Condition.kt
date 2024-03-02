package edu.examples.query.condition

sealed interface Condition

enum class ConditionOperator(val str: String) {
  EQUAL("="),
  NOT_EQUAL("!="),
  NULL("0"),
  NOT_NULL("!0"),
  GREATER(">"),
  GREATER_EQUAL(">="),
  LESS("<"),
  LESS_EQUAL("<="),
  LIKE("~"),
  NOT_LIKE("!~"),
  IN("in"),
  NOT_IN("notIn"),
  BETWEEN("between");

  override fun toString() = str

  companion object {
    private val stringToOperator = entries.associateBy { it.str }

    fun fromString(str: String): ConditionOperator =
      stringToOperator[str] ?: throw IllegalArgumentException("Unsupported operator '$str'")
  }
}
