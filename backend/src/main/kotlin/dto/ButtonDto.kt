package com.buttontrack.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateButtonRequest(
    val title: String,
    val color: String
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (title.isBlank()) {
            errors.add("title cannot be empty")
        }
        
        if (title.length > 100) {
            errors.add("title cannot exceed 100 characters")
        }
        
        if (color.isBlank()) {
            errors.add("color cannot be empty")
        }
        
        if (!color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
            errors.add("color must be a valid hex color code (e.g., #FF5733)")
        }
        
        return errors
    }
}

@Serializable
data class UpdateButtonRequest(
    val title: String,
    val color: String
)

@Serializable
data class ButtonResponse(
    val id: Int,
    val userId: Int,
    val title: String,
    val color: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ButtonPressStatsResponse(
    val buttonId: Int,
    val buttonTitle: String,
    val buttonColor: String,
    val presses: List<ButtonPressData>
)

@Serializable
data class ButtonPressData(
    val date: String, // YYYY-MM-DD format
    val hour: Int,    // 0-23
    val pressedAt: String // ISO instant
)

@Serializable
data class StatsResponse(
    val buttonStats: List<ButtonPressStatsResponse>
)