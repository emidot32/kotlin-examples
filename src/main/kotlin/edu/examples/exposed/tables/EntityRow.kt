package edu.examples.exposed.tables

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.Instant
import java.util.UUID

class EntityRow(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
  companion object : UUIDEntityClass<EntityRow>(Entities)

  var uuid by Entities.uuid
  var entityId by Entities.entityId
  var type by Entities.type
  var createdBy by Entities.createdBy
  var createdAt: Instant by Entities.createdAt
  var lastUpdatedBy by Entities.lastUpdatedBy
  var lastUpdatedAt: Instant by Entities.lastUpdatedAt
}
