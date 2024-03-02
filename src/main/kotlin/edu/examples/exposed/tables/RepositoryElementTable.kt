package edu.examples.exposed.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

open class RepositoryElementTable : UUIDTable(columnName = "uuid") {
  val uuid
    get() = id

  val type = varchar("type", 255)
  val createdBy = varchar("created_by", 255)
  val createdAt = timestamp("created_at")
  val lastUpdatedBy = varchar("last_updated_by", 255)
  val lastUpdatedAt = timestamp("last_updated_by")
}
