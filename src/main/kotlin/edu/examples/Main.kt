package edu.examples

import edu.examples.query.Query
import edu.examples.query.SortDirection
import edu.examples.query.SortOrderRule
import edu.examples.query.condition.*
import edu.examples.query.newQuery
import java.util.*

fun main() {

}

fun createQueryWithoutCondition(): Query {
    val someList = emptyList<String>()
    val id: UUID? = UUID.randomUUID()
    val orConditions = mutableListOf<Condition>()
    val andConditions = mutableListOf<Condition>(
        MetadataCondition("created_at", ConditionOperator.BETWEEN, "2023-09-09T20:20:20" to "2023-10-09T20:20:20")
    )
    if (!someList.isNullOrEmpty()) {
        andConditions.add(PropertyCondition("uuid", ConditionOperator.IN, someList ))
    }
    id?.let { orConditions.add(AttributeCondition("id", ConditionOperator.EQUAL, id)) }
    orConditions.add(MetadataCondition("executed_at", ConditionOperator.NULL, ""))
    orConditions.add(AndCondition(andConditions))
    val order = listOf(SortOrderRule("name", "attr"), SortOrderRule("name", "metadata", SortDirection.DESC))
    return Query(OrCondition(orConditions), order)
}

fun creatQueryWithoutCondition(): Query {
    val someList = emptyList<String>()
    val id: UUID? = UUID.randomUUID()
    return newQuery {
        where {
            any {
                if (id != null) attr { "id" eq id }
                metadata { isNull { "executed_at" } }
                all {
                    metadata { "created_at" between "2023-09-09T20:20:20".."2023-10-09T20:20:20" }
                    prop { "uuid" inList someList }
                }
            }
        }
        orderBy {
            attr { "name" }
            metadataDesc { "created_at" }
        }
    }
}