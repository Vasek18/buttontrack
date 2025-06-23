package com.buttontrack.models

import com.buttontrack.dto.ButtonResponse
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object ButtonTable : IntIdTable("buttons") {
    val userId = integer("user_id")
    val title = text("title")
    val color = text("color")
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}

class Button(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Button>(ButtonTable)

    var userId by ButtonTable.userId
    var title by ButtonTable.title
    var color by ButtonTable.color
    var createdAt by ButtonTable.createdAt
    var updatedAt by ButtonTable.updatedAt

    fun toResponse(): ButtonResponse = ButtonResponse(
        id = id.value,
        userId = userId,
        title = title,
        color = color,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}