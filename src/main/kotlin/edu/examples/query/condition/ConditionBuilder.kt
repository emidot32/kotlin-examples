package edu.examples.query.condition

import edu.examples.query.ConditionTypes.AND
import edu.examples.query.ConditionTypes.ATTRIB
import edu.examples.query.ConditionTypes.METADATA
import edu.examples.query.ConditionTypes.OR
import edu.examples.query.ConditionTypes.PROPERTY
import edu.examples.query.condition.ConditionOperator.EQUAL
import edu.examples.query.condition.ConditionOperator.NOT_EQUAL
import edu.examples.query.condition.ConditionOperator.NULL
import edu.examples.query.condition.ConditionOperator.NOT_NULL
import edu.examples.query.condition.ConditionOperator.GREATER
import edu.examples.query.condition.ConditionOperator.GREATER_EQUAL
import edu.examples.query.condition.ConditionOperator.LESS
import edu.examples.query.condition.ConditionOperator.LESS_EQUAL
import edu.examples.query.condition.ConditionOperator.LIKE
import edu.examples.query.condition.ConditionOperator.NOT_LIKE
import edu.examples.query.condition.ConditionOperator.IN
import edu.examples.query.condition.ConditionOperator.NOT_IN
import edu.examples.query.condition.ConditionOperator.BETWEEN

fun newCondition(stub: ConditionBuilder.() -> Unit): Condition? {
  val builder = ConditionBuilder()
  builder.stub()
  return builder.get()
}

class ConditionBuilder(
  private val type: String? = null,
) {
  private val conditions: MutableList<Condition> = mutableListOf()

  fun attr(stub: TerminalConditionBuilder.() -> Unit) {
    terminalCondition(ATTRIB, stub)
  }

  fun metadata(stub: TerminalConditionBuilder.() -> Unit) {
    terminalCondition(METADATA, stub)
  }

  fun prop(stub: TerminalConditionBuilder.() -> Unit) {
    terminalCondition(PROPERTY, stub)
  }

  fun all(stub: ConditionBuilder.() -> Unit) {
    compositeCondition(AND, stub)
  }

  fun any(stub: ConditionBuilder.() -> Unit) {
    compositeCondition(OR, stub)
  }

  fun get(): Condition? {
    if (type == null && conditions.size > 1) return AndCondition(conditions)
    return when (type) {
      null, ATTRIB, METADATA, PROPERTY -> conditions.firstOrNull()
      AND -> AndCondition(conditions)
      OR -> OrCondition(conditions)
      else -> throw IllegalArgumentException("Unsupported condition type '$type'")
    }
  }

  private fun terminalCondition(
    type: String,
    stub: TerminalConditionBuilder.() -> Unit,
  ) {
    val terminalConditionBuilder = TerminalConditionBuilder(type)
    terminalConditionBuilder.stub()
    val attrCondition = terminalConditionBuilder.get()
    conditions.add(attrCondition)
  }

  private fun compositeCondition(
    type: String,
    stub: ConditionBuilder.() -> Unit,
  ) {
    val conditionBuilder = ConditionBuilder(type)
    conditionBuilder.stub()
    conditionBuilder.get()?.let { conditions.add(it) }
  }
}

class TerminalConditionBuilder(private val type: String) {
  private lateinit var finalCondition: TerminalCondition

  infix fun String.eq(value: Any) {
    setCondition(this, EQUAL, value)
  }

  infix fun String.notEq(value: Any) {
    setCondition(this, NOT_EQUAL, value)
  }

  fun isNull(name: () -> String) {
    setCondition(name(), NULL, "")
  }

  fun isNotNull(name: () -> String) {
    setCondition(name(), NOT_NULL, "")
  }

  fun String.isNull() {
    setCondition(this, NULL, "")
  }

  fun String.isNotNull() {
    setCondition(this, NOT_NULL, "")
  }

  infix fun <T : Comparable<T>> String.greater(value: T) {
    setCondition(this, GREATER, value)
  }

  infix fun <T : Comparable<T>> String.greaterEq(value: T) {
    setCondition(this, GREATER_EQUAL, value)
  }

  infix fun <T : Comparable<T>> String.less(value: T) {
    setCondition(this, LESS, value)
  }

  infix fun <T : Comparable<T>> String.lessEq(value: T) {
    setCondition(this, LESS_EQUAL, value)
  }

  infix fun String.like(value: Any) {
    setCondition(this, LIKE, value)
  }

  infix fun String.notLike(value: Any) {
    setCondition(this, NOT_LIKE, value)
  }

  infix fun String.inList(value: Any) {
    setCondition(this, IN, value.toAnyList())
  }

  infix fun String.notInList(value: Any) {
    setCondition(this, NOT_IN, value.toAnyList())
  }

  infix fun <T : Comparable<T>> String.between(other: ClosedRange<T>) {
    setCondition(this, BETWEEN, other.start to other.endInclusive)
  }

  infix fun <T : Comparable<T>> String.between(pair: Pair<T, T>) {
    setCondition(this, BETWEEN, pair)
  }

  private fun setCondition(
    name: String,
    operator: ConditionOperator,
    value: Any,
  ) {
    finalCondition =
      when (type) {
        ATTRIB -> AttributeCondition(name, operator, value)
        METADATA -> MetadataCondition(name, operator, value)
        PROPERTY -> PropertyCondition(name, operator, value)
        else -> throw IllegalArgumentException("Unsupported condition type '$type'")
      }
  }

  private fun Any?.toAnyList(separatorForString: String? = null): List<*> =
    when (this) {
      null -> emptyList<Any>()
      is String ->
        separatorForString?.let {
          this.split(Regex("\\s*$it\\s*")).toList()
        } ?: listOf(this)
      is Array<*> -> this.toList()
      is List<*> -> this
      is Iterable<*> -> this.toList()
      else -> listOf(this)
    }

  fun get(): TerminalCondition {
    require(::finalCondition.isInitialized) { "The terminal condition is not initialized" }
    return finalCondition
  }
}
