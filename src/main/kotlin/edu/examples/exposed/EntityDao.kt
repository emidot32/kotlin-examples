package edu.examples.exposed

import edu.examples.exposed.ExposedQueryMapper.applyQuery
import edu.examples.exposed.tables.Entities
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
import edu.examples.query.Query as CustomQuery
import org.jetbrains.exposed.sql.Query as ExposedQuery

class EntityDao(private var transaction: Transaction) {

    fun search(
        query: CustomQuery?,
    ): List<Any> =
        Entities.selectAll()
            .applyQuery(query)
            .toList()

}