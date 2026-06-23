package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val walletBalance: Double = 0.0,
    val isBlocked: Boolean = false,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lottery_draws")
data class LotteryDraw(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val prizeAmount: Double,
    val ticketPrice: Double,
    val totalTickets: Int,
    val soldTickets: Int = 0,
    val drawDate: Long, // timestamp
    val status: String = "ACTIVE", // ACTIVE, COMPLETED
    val winningTicketNumber: String? = null,
    val winnerNickname: String? = null
)

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val drawId: Int,
    val ticketNumber: String,
    val purchaseDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val type: String, // DEPOSIT, TICKET_PURCHASE
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class SavedNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int?, // null for global notifications
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
