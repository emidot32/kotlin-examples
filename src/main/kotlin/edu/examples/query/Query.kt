package edu.examples.query

import edu.examples.query.condition.Condition
import edu.examples.query.condition.ConditionBuilder

object ConditionTypes {
  const val OR = "or"
  const val AND = "and"
  const val ATTRIB = "attrib"
  const val METADATA = "metadata"
  const val PROPERTY = "property"
}

data class Query(val condition: Condition?, val sortOrder: List<SortOrderRule>?)


fun newQuery(stub: QueryBuilder.() -> Unit): Query {
  val builder = QueryBuilder()
  builder.stub()
  return builder.get()
}

class QueryBuilder {
  private var condition: Condition? = null
  private var sortOrderRules: List<SortOrderRule>? = null

  fun where(stub: ConditionBuilder.() -> Unit) {
    val conditionBuilder = ConditionBuilder()
    conditionBuilder.stub()
    condition = conditionBuilder.get()
  }

  fun where(condition: Condition) {
    this.condition = condition
  }

  fun orderBy(stub: OrderRuleBuilder.() -> Unit) {
    val orderBuilder = OrderRuleBuilder()
    orderBuilder.stub()
    sortOrderRules = orderBuilder.get()
  }

  fun get(): Query = Query(condition, sortOrderRules)
}
