package com.buttontrack.service

import com.buttontrack.DatabaseFactory
import com.buttontrack.dto.CreateButtonRequest
import com.buttontrack.dto.UpdateButtonRequest
import com.buttontrack.models.ButtonTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ButtonServiceTest {

    private lateinit var buttonService: ButtonService
    private val testUserId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(ButtonTable)
        }
        buttonService = ButtonService()
    }

    @AfterEach
    fun teardown() {
        transaction {
            SchemaUtils.drop(ButtonTable)
        }
    }

    @Test
    fun `createButton should create and return a button`() = runBlocking {
        val request = CreateButtonRequest(
            userId = testUserId,
            title = "Test Button",
            color = "#FF0000"
        )

        val result = buttonService.createButton(request)

        assertNotNull(result)
        assertEquals(testUserId, result.userId)
        assertEquals("Test Button", result.title)
        assertEquals("#FF0000", result.color)
        assertNotNull(result.id)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `getButton should return button when it exists`() = runBlocking {
        val request = CreateButtonRequest(
            userId = testUserId,
            title = "Test Button",
            color = "#FF0000"
        )
        val created = buttonService.createButton(request)

        val result = buttonService.getButton(created.id)

        assertNotNull(result)
        assertEquals(created.id, result.id)
        assertEquals(created.title, result.title)
        assertEquals(created.color, result.color)
    }

    @Test
    fun `getButton should return null when button does not exist`() = runBlocking {
        val nonExistentId = UUID.randomUUID().toString()

        val result = buttonService.getButton(nonExistentId)

        assertNull(result)
    }

    @Test
    fun `getButton should throw exception for invalid UUID`() = runBlocking {
        assertThrows<IllegalArgumentException> {
            buttonService.getButton("invalid-uuid")
        }
    }

    @Test
    fun `getButtonsByUser should return buttons for specific user`() = runBlocking {
        val otherUserId = UUID.randomUUID().toString()
        
        buttonService.createButton(CreateButtonRequest(testUserId, "User1 Button1", "#FF0000"))
        buttonService.createButton(CreateButtonRequest(testUserId, "User1 Button2", "#00FF00"))
        buttonService.createButton(CreateButtonRequest(otherUserId, "User2 Button", "#0000FF"))

        val result = buttonService.getButtonsByUser(testUserId)

        assertEquals(2, result.size)
        assertTrue(result.all { it.userId == testUserId })
        assertTrue(result.any { it.title == "User1 Button1" })
        assertTrue(result.any { it.title == "User1 Button2" })
    }

    @Test
    fun `getButtonsByUser should return empty list when no buttons exist for user`() = runBlocking {
        val result = buttonService.getButtonsByUser(testUserId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getButtonsByUser should throw exception for invalid UUID`() = runBlocking {
        assertThrows<IllegalArgumentException> {
            buttonService.getButtonsByUser("invalid-uuid")
        }
    }

    @Test
    fun `updateButton should update title and color`() = runBlocking {
        val request = CreateButtonRequest(
            userId = testUserId,
            title = "Original Title",
            color = "#FF0000"
        )
        val created = buttonService.createButton(request)

        val updateRequest = UpdateButtonRequest(
            title = "Updated Title",
            color = "#00FF00"
        )
        val result = buttonService.updateButton(created.id, updateRequest)

        assertNotNull(result)
        assertEquals("Updated Title", result!!.title)
        assertEquals("#00FF00", result.color)
        assertEquals(created.id, result.id)
        assertEquals(created.userId, result.userId)
        assertEquals(created.createdAt, result.createdAt)
    }

    @Test
    fun `updateButton should return null when button does not exist`() = runBlocking {
        val nonExistentId = UUID.randomUUID().toString()
        val updateRequest = UpdateButtonRequest(
            title = "Updated Title",
            color = "#00FF00"
        )

        val result = buttonService.updateButton(nonExistentId, updateRequest)

        assertNull(result)
    }

    @Test
    fun `updateButton should throw exception for invalid UUID`() = runBlocking {
        val updateRequest = UpdateButtonRequest(
            title = "Updated Title",
            color = "#00FF00"
        )

        assertThrows<IllegalArgumentException> {
            buttonService.updateButton("invalid-uuid", updateRequest)
        }
    }

    @Test
    fun `deleteButton should delete existing button and return true`() = runBlocking {
        val request = CreateButtonRequest(
            userId = testUserId,
            title = "Test Button",
            color = "#FF0000"
        )
        val created = buttonService.createButton(request)

        val result = buttonService.deleteButton(created.id)

        assertTrue(result)
        
        val deletedButton = buttonService.getButton(created.id)
        assertNull(deletedButton)
    }

    @Test
    fun `deleteButton should return false when button does not exist`() = runBlocking {
        val nonExistentId = UUID.randomUUID().toString()

        val result = buttonService.deleteButton(nonExistentId)

        assertTrue(!result)
    }

    @Test
    fun `deleteButton should throw exception for invalid UUID`() = runBlocking {
        assertThrows<IllegalArgumentException> {
            buttonService.deleteButton("invalid-uuid")
        }
    }
}