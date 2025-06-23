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
            userId = request.userId
            title = request.title
            color = request.color
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
        button.toResponse()
    }

    suspend fun getButton(id: Int): ButtonResponse? = dbQuery {
        Button.findById(id)?.toResponse()
    }


    suspend fun getButtonsByUser(userId: Int): List<ButtonResponse> = dbQuery {
        Button.find { ButtonTable.userId eq userId }.map { it.toResponse() }
    }

    suspend fun updateButton(id: Int, request: UpdateButtonRequest): ButtonResponse? = dbQuery {
        val button = Button.findById(id)
        button?.let {
            it.title = request.title
            it.color = request.color
            it.updatedAt = Instant.now()
            it.toResponse()
        }
    }

    suspend fun deleteButton(id: Int): Boolean = dbQuery {
        val button = Button.findById(id)
        button?.let {
            it.delete()
            true
        } ?: false
    }

}