package com.buttontrack.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateButtonRequest(
    val userId: String,
    val title: String,
    val color: String
)

@Serializable
data class UpdateButtonRequest(
    val title: String,
    val color: String
)

@Serializable
data class ButtonResponse(
    val id: String,
    val userId: String,
    val title: String,
    val color: String,
    val createdAt: String,
    val updatedAt: String
)