package edu.examples.exposed.tables

object Entities : RepositoryElementTable() {
  val entityId = varchar("id", 255)
}
