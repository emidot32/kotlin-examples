package edu.examples.exposed

import edu.examples.query.SortDirection
import edu.examples.query.SortOrderRule
import edu.examples.query.condition.AndCondition
import edu.examples.query.condition.Condition
import edu.examples.query.condition.ConditionOperator.BETWEEN
import edu.examples.query.condition.ConditionOperator.EQUAL
import edu.examples.query.condition.ConditionOperator.GREATER
import edu.examples.query.condition.ConditionOperator.GREATER_EQUAL
import edu.examples.query.condition.ConditionOperator.IN
import edu.examples.query.condition.ConditionOperator.LESS
import edu.examples.query.condition.ConditionOperator.LESS_EQUAL
import edu.examples.query.condition.ConditionOperator.LIKE
import edu.examples.query.condition.ConditionOperator.NOT_EQUAL
import edu.examples.query.condition.ConditionOperator.NOT_IN
import edu.examples.query.condition.ConditionOperator.NOT_LIKE
import edu.examples.query.condition.ConditionOperator.NOT_NULL
import edu.examples.query.condition.ConditionOperator.NULL
import edu.examples.query.condition.OrCondition
import edu.examples.query.condition.TerminalCondition
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.AndOp
import org.jetbrains.exposed.sql.Between
import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.EntityIDColumnType
import org.jetbrains.exposed.sql.FloatColumnType
import org.jetbrains.exposed.sql.GreaterEqOp
import org.jetbrains.exposed.sql.GreaterOp
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.IsDistinctFromOp
import org.jetbrains.exposed.sql.IsNotDistinctFromOp
import org.jetbrains.exposed.sql.IsNotNullOp
import org.jetbrains.exposed.sql.IsNullOp
import org.jetbrains.exposed.sql.LessEqOp
import org.jetbrains.exposed.sql.LessOp
import org.jetbrains.exposed.sql.LikeEscapeOp
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.OrOp
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.StringColumnType
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.ops.SingleValueInListOp
import java.util.UUID
import edu.examples.query.Query as CustomQuery
import org.jetbrains.exposed.sql.Query as ExposedQuery

object ExposedQueryMapper {
  private val COMMA_SPACES_REGEX = Regex("\\s*,\\s*")

  fun ExposedQuery.applyQuery(
    query: CustomQuery?,
    vararg tables: UUIDTable,
  ): ExposedQuery {
    return this.applyCondition(query?.condition, tables).orderBy(query?.sortOrder, tables)
  }

  fun ExposedQuery.applyCondition(
    condition: Condition?,
    tables: Array<out UUIDTable>,
  ): ExposedQuery = this.andWhere { conditionToOp(condition, tables) }

  fun ExposedQuery.orderBy(
    order: List<SortOrderRule>?,
    tables: Array<out UUIDTable>,
  ): ExposedQuery {
    val columns =
      (order ?: emptyList())
        .flatMap { findColumns(it.name, tables)
          .map { column -> column to it.direction.toExposedOrder() }
        }
        .toTypedArray()
    return this.orderBy(*columns)
  }

  private fun conditionToOp(
    condition: Condition?,
    tables: Array<out UUIDTable>,
  ): Op<Boolean> =
    when (condition) {
      is TerminalCondition -> finalOp(condition, tables)
      is AndCondition -> AndOp(condition.conditions.map { conditionToOp(it, tables) })
      is OrCondition -> OrOp(condition.conditions.map { conditionToOp(it, tables) })
      null -> Op.TRUE
    }

  private fun finalOp(
    condition: TerminalCondition,
    tables: Array<out UUIDTable>,
  ): Op<Boolean> {
    val columns = findColumns(condition.name, tables).map { op(it, condition) }
    return if (columns.isEmpty()) Op.FALSE else OrOp(columns)
  }

  @Suppress("UNCHECKED_CAST")
  private fun op(
    column: Column<*>,
    condition: TerminalCondition,
  ): Op<Boolean> {
    val value = condition.value
    val type = column.columnType
    return when (condition.operator) {
      EQUAL -> IsNotDistinctFromOp(column, prepareParam(value, type))
      NOT_EQUAL -> IsDistinctFromOp(column, prepareParam(value, type))
      NULL -> IsNullOp(column)
      NOT_NULL -> IsNotNullOp(column)
      LESS -> LessOp(column, prepareParam(value, type))
      LESS_EQUAL -> LessEqOp(column, prepareParam(value, type))
      GREATER -> GreaterOp(column, prepareParam(value, type))
      GREATER_EQUAL -> GreaterEqOp(column, prepareParam(value, type))
      LIKE -> LikeEscapeOp(column, prepareParam(value, type), true, null)
      NOT_LIKE -> LikeEscapeOp(column, prepareParam(value, type), false, null)
      IN -> SingleValueInListOp(column, prepareListValue(value, type), true)
      NOT_IN -> SingleValueInListOp(column, prepareListValue(value, type), false)
      BETWEEN -> {
        val pair = value as Pair<Any, Any>
        Between(
          column,
          prepareParam(pair.first, type),
          prepareParam(pair.second, type),
        )
      }
    }
  }

  private fun findColumns(
    attrName: String,
    tables: Array<out UUIDTable>,
  ): List<Column<*>> =
    tables
      .mapNotNull { it.columns.find { column -> column.name == attrName } }

  private fun prepareParam(
    value: Any,
    type: IColumnType,
  ): QueryParameter<*> = QueryParameter(mapValue(value, type), type)

  @Suppress("UNCHECKED_CAST")
  private fun prepareListValue(
    value: Any,
    type: IColumnType,
  ): List<*> {
    val list = if (value is List<*>) value as List<Any> else value.toString().split(COMMA_SPACES_REGEX).toList()
    return list.map { mapValue(it, type) }
  }

  private fun mapValue(
    value: Any,
    type: IColumnType,
  ): Any? =
    when (type) {
      is StringColumnType -> value.toString()
      is EntityIDColumnType<*> -> mapValue(value, type.idColumn.columnType)
      is UUIDColumnType -> UUID.fromString(value.toString())
      else -> throw IllegalArgumentException("Invalid type '$type' of values")
    }

  private fun SortDirection.toExposedOrder(): SortOrder =
    when (this) {
      SortDirection.ASC -> SortOrder.ASC
      SortDirection.DESC -> SortOrder.DESC
    }
}
