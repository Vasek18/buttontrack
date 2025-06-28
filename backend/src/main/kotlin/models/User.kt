package com.buttontrack.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object UserTable : IntIdTable("users") {
    val name = text("name").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}

object UserAuthTable : IntIdTable("user_auth") {
    val userId = reference("user_id", UserTable)
    val provider = text("provider")
    val providerUserId = text("provider_user_id")
    val email = text("email")
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    
    init {
        uniqueIndex(provider, providerUserId)
    }
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(UserTable)
    
    var name by UserTable.name
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt
}

class UserAuth(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserAuth>(UserAuthTable)
    
    var userId by UserAuthTable.userId
    var provider by UserAuthTable.provider
    var providerUserId by UserAuthTable.providerUserId
    var email by UserAuthTable.email
    var createdAt by UserAuthTable.createdAt
    var updatedAt by UserAuthTable.updatedAt
    
    val user by User referencedOn UserAuthTable.userId
}