package com.buttontrack.service

import com.buttontrack.DatabaseFactory.dbQuery
import com.buttontrack.dto.ButtonResponse
import com.buttontrack.dto.CreateButtonRequest
import com.buttontrack.dto.UpdateButtonRequest
import com.buttontrack.dto.StatsResponse
import com.buttontrack.dto.ButtonPressStatsResponse
import com.buttontrack.dto.ButtonPressData
import com.buttontrack.models.Button
import com.buttontrack.models.ButtonTable
import com.buttontrack.models.ButtonPress
import com.buttontrack.models.ButtonPressTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.and
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class ButtonService {

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(ButtonTable, ButtonPressTable)
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

    suspend fun pressButton(buttonId: Int): Boolean = dbQuery {
        val button = Button.findById(buttonId)
        if (button != null) {
            ButtonPress.new {
                this.buttonId = buttonId
                pressedAt = Instant.now()
            }
            true
        } else {
            false
        }
    }

    suspend fun getButtonPressStats(userId: Int, startTimestamp: String?, endTimestamp: String?): StatsResponse = dbQuery {
        // Parse timestamps or use defaults (last 30 days if not provided)
        val endDate = if (endTimestamp != null) {
            try {
                Instant.parse(endTimestamp)
            } catch (e: Exception) {
                Instant.now()
            }
        } else {
            Instant.now()
        }
        
        val startDate = if (startTimestamp != null) {
            try {
                Instant.parse(startTimestamp)
            } catch (e: Exception) {
                endDate.minusSeconds(30 * 24 * 60 * 60) // Default to 30 days before end
            }
        } else {
            endDate.minusSeconds(30 * 24 * 60 * 60) // Default to 30 days before end
        }
        
        // Get all user's buttons
        val userButtons = Button.find { ButtonTable.userId eq userId }
        
        val buttonStats = userButtons.map { button ->
            // Get button presses for this button within the date range
            val presses = ButtonPress.find { 
                (ButtonPressTable.buttonId eq button.id.value) and 
                (ButtonPressTable.pressedAt greaterEq startDate) and
                (ButtonPressTable.pressedAt lessEq endDate)
            }.map { press ->
                val dateTime = LocalDateTime.ofInstant(press.pressedAt, ZoneOffset.UTC)
                ButtonPressData(
                    date = dateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    hour = dateTime.hour,
                    pressedAt = press.pressedAt.toString()
                )
            }
            
            ButtonPressStatsResponse(
                buttonId = button.id.value,
                buttonTitle = button.title,
                buttonColor = button.color,
                presses = presses
            )
        }
        
        StatsResponse(buttonStats = buttonStats)
    }

}