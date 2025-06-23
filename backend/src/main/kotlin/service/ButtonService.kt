package com.buttontrack.service

import com.buttontrack.DatabaseFactory.dbQuery
import com.buttontrack.dto.ButtonResponse
import com.buttontrack.dto.CreateButtonRequest
import com.buttontrack.dto.UpdateButtonRequest
import com.buttontrack.models.Button
import com.buttontrack.models.ButtonTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class ButtonService {

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(ButtonTable)
        }
    }

    suspend fun createButton(request: CreateButtonRequest): ButtonResponse = dbQuery {
        val button = Button.new {
            userId = UUID.fromString(request.userId)
            title = request.title
            color = request.color
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
        button.toResponse()
    }

    suspend fun getButton(id: String): ButtonResponse? = dbQuery {
        Button.findById(UUID.fromString(id))?.toResponse()
    }


    suspend fun getButtonsByUser(userId: String): List<ButtonResponse> = dbQuery {
        Button.find { ButtonTable.userId eq UUID.fromString(userId) }.map { it.toResponse() }
    }

    suspend fun updateButton(id: String, request: UpdateButtonRequest): ButtonResponse? = dbQuery {
        val button = Button.findById(UUID.fromString(id))
        button?.let {
            request.title?.let { title -> it.title = title }
            request.color?.let { color -> it.color = color }
            it.updatedAt = Instant.now()
            it.toResponse()
        }
    }

    suspend fun deleteButton(id: String): Boolean = dbQuery {
        val button = Button.findById(UUID.fromString(id))
        button?.let {
            it.delete()
            true
        } ?: false
    }

}