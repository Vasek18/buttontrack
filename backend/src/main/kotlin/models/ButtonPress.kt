package com.buttontrack.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object ButtonPressTable : IntIdTable("button_press") {
    val buttonId = integer("button_id")
    val pressedAt = timestamp("pressed_at").default(Instant.now())
}

class ButtonPress(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ButtonPress>(ButtonPressTable)

    var buttonId by ButtonPressTable.buttonId
    var pressedAt by ButtonPressTable.pressedAt
}