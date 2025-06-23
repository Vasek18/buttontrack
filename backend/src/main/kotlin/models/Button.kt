package com.buttontrack.models

import com.buttontrack.dto.ButtonResponse
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

object ButtonTable : UUIDTable("buttons") {
    val userId = uuid("user_id")
    val title = text("title")
    val color = text("color")
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}

class Button(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Button>(ButtonTable)

    var userId by ButtonTable.userId
    var title by ButtonTable.title
    var color by ButtonTable.color
    var createdAt by ButtonTable.createdAt
    var updatedAt by ButtonTable.updatedAt

    fun toResponse(): ButtonResponse = ButtonResponse(
        id = id.value.toString(),
        userId = userId.toString(),
        title = title,
        color = color,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}