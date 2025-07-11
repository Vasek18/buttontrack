package com.buttontrack.service

import com.buttontrack.DatabaseFactory
import com.buttontrack.dto.CreateButtonRequest
import com.buttontrack.dto.UpdateButtonRequest
import com.buttontrack.models.ButtonTable
import com.buttontrack.models.ButtonPressTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ButtonServiceTest {

    private lateinit var buttonService: ButtonService
    private val testUserId = 1

    @BeforeEach
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(ButtonTable, ButtonPressTable)
        }
        buttonService = ButtonService()
    }

    @AfterEach
    fun teardown() {
        transaction {
            SchemaUtils.drop(ButtonPressTable, ButtonTable)
        }
    }

    @Test
    fun `createButton should create and return a button`() = runBlocking {
        val request = CreateButtonRequest(
            title = "Test Button",
            color = "#FF0000"
        )

        val result = buttonService.createButton(request, testUserId)

        assertNotNull(result)
        assertEquals(testUserId, result.userId)
        assertEquals("Test Button", result.title)
        assertEquals("#FF0000", result.color)
        assertTrue(result.id > 0)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `getButton should return button when it exists`() = runBlocking {
        val request = CreateButtonRequest(
            title = "Test Button",
            color = "#FF0000"
        )
        val created = buttonService.createButton(request, testUserId)

        val result = buttonService.getButton(created.id)

        assertNotNull(result)
        assertEquals(created.id, result.id)
        assertEquals(created.title, result.title)
        assertEquals(created.color, result.color)
    }

    @Test
    fun `getButton should return null when button does not exist`() = runBlocking {
        val nonExistentId = 999

        val result = buttonService.getButton(nonExistentId)

        assertNull(result)
    }


    @Test
    fun `getButtonsByUser should return buttons for specific user`() = runBlocking {
        val otherUserId = 2

        buttonService.createButton(CreateButtonRequest("User1 Button1", "#FF0000"), testUserId)
        buttonService.createButton(CreateButtonRequest("User1 Button2", "#00FF00"), testUserId)
        buttonService.createButton(CreateButtonRequest("User2 Button", "#0000FF"), otherUserId)

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
    fun `updateButton should update title and color`() = runBlocking {
        val request = CreateButtonRequest(
            title = "Original Title",
            color = "#FF0000"
        )
        val created = buttonService.createButton(request, testUserId)

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
        val nonExistentId = 999
        val updateRequest = UpdateButtonRequest(
            title = "Updated Title",
            color = "#00FF00"
        )

        val result = buttonService.updateButton(nonExistentId, updateRequest)

        assertNull(result)
    }


    @Test
    fun `deleteButton should delete existing button and return true`() = runBlocking {
        val request = CreateButtonRequest(
            title = "Test Button",
            color = "#FF0000"
        )
        val created = buttonService.createButton(request, testUserId)

        val result = buttonService.deleteButton(created.id)

        assertTrue(result)

        val deletedButton = buttonService.getButton(created.id)
        assertNull(deletedButton)
    }

    @Test
    fun `deleteButton should return false when button does not exist`() = runBlocking {
        val nonExistentId = 999

        val result = buttonService.deleteButton(nonExistentId)

        assertTrue(!result)
    }

    @Test
    fun `pressButton should return true when button exists`() = runBlocking {
        val request = CreateButtonRequest(
            title = "Test Button",
            color = "#FF0000"
        )
        val created = buttonService.createButton(request, testUserId)

        val result = buttonService.pressButton(created.id)

        assertTrue(result)
    }

    @Test
    fun `pressButton should return false when button does not exist`() = runBlocking {
        val nonExistentId = 999

        val result = buttonService.pressButton(nonExistentId)

        assertTrue(!result)
    }

    @Test
    fun `getButtonPressStats should return stats with timestamp parameters`() = runBlocking {
        val request = CreateButtonRequest(
            title = "Test Button",
            color = "#FF0000"
        )
        val created = buttonService.createButton(request, testUserId)

        buttonService.pressButton(created.id)

        // Use current date range to include the button press we just made
        val now = java.time.Instant.now()
        val startTime = now.minusSeconds(60 * 60).toString() // 1 hour ago
        val endTime = now.plusSeconds(60 * 60).toString()   // 1 hour from now
        val result = buttonService.getButtonPressStats(testUserId, startTime, endTime)

        assertNotNull(result)
        assertEquals(1, result.buttonStats.size)
        assertEquals("Test Button", result.buttonStats[0].buttonTitle)
        assertEquals("#FF0000", result.buttonStats[0].buttonColor)
        assertTrue(result.buttonStats[0].presses.isNotEmpty())
    }

    @Test
    fun `getButtonPressStats should return stats with default time range when no timestamps provided`() = runBlocking {
        val request = CreateButtonRequest(
            title = "Test Button",
            color = "#FF0000"
        )
        val created = buttonService.createButton(request, testUserId)

        buttonService.pressButton(created.id)

        val result = buttonService.getButtonPressStats(testUserId, null, null)

        assertNotNull(result)
        assertEquals(1, result.buttonStats.size)
        assertEquals("Test Button", result.buttonStats[0].buttonTitle)
        assertTrue(result.buttonStats[0].presses.isNotEmpty())
    }

    @Test
    fun `getButtonPressStats should return empty stats for user with no buttons`() = runBlocking {
        val result = buttonService.getButtonPressStats(testUserId, null, null)

        assertNotNull(result)
        assertTrue(result.buttonStats.isEmpty())
    }

}