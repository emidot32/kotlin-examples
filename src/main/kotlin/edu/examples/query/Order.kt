package edu.examples.query

enum class SortDirection { ASC, DESC }

data class SortOrderRule(val name: String, val type: String = ConditionTypes.ATTRIB, val direction: SortDirection = SortDirection.ASC)

fun newOrder(stub: OrderRuleBuilder.() -> Unit): List<SortOrderRule> {
  val builder = OrderRuleBuilder()
  builder.stub()
  return builder.get()
}

class OrderRuleBuilder {
  private var sortOrderRules: MutableList<SortOrderRule> = mutableListOf()

  fun attr(name: () -> String) = sortOrderRules.add(SortOrderRule(name(), ConditionTypes.ATTRIB, SortDirection.ASC))

  fun attrDesc(name: () -> String) = sortOrderRules.add(SortOrderRule(name(), ConditionTypes.ATTRIB, SortDirection.DESC))

  fun metadata(name: () -> String) = sortOrderRules.add(SortOrderRule(name(), ConditionTypes.METADATA, SortDirection.ASC))

  fun metadataDesc(name: () -> String) = sortOrderRules.add(SortOrderRule(name(), ConditionTypes.METADATA, SortDirection.DESC))

  fun prop(name: () -> String) = sortOrderRules.add(SortOrderRule(name(), ConditionTypes.PROPERTY, SortDirection.ASC))

  fun propDesc(name: () -> String) = sortOrderRules.add(SortOrderRule(name(), ConditionTypes.PROPERTY, SortDirection.DESC))

  fun get(): List<SortOrderRule> = sortOrderRules.toList()
}
