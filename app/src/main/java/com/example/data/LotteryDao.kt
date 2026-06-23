package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LotteryDao {

    // --- Users ---
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)


    // --- Lottery Draws ---
    @Query("SELECT * FROM lottery_draws ORDER BY drawDate ASC")
    fun getAllDraws(): Flow<List<LotteryDraw>>

    @Query("SELECT * FROM lottery_draws WHERE status = 'ACTIVE' ORDER BY drawDate ASC")
    fun getActiveDraws(): Flow<List<LotteryDraw>>

    @Query("SELECT * FROM lottery_draws WHERE status = 'COMPLETED' ORDER BY drawDate DESC")
    fun getCompletedDraws(): Flow<List<LotteryDraw>>

    @Query("SELECT * FROM lottery_draws WHERE id = :drawId")
    suspend fun getDrawById(drawId: Int): LotteryDraw?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraw(draw: LotteryDraw): Long

    @Update
    suspend fun updateDraw(draw: LotteryDraw)


    // --- Tickets ---
    @Query("SELECT * FROM tickets ORDER BY purchaseDate DESC")
    fun getAllTickets(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE userId = :userId ORDER BY purchaseDate DESC")
    fun getTicketsByUser(userId: Int): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE drawId = :drawId")
    suspend fun getTicketsForDraw(drawId: Int): List<Ticket>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: Ticket): Long


    // --- Transactions ---
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsByUser(userId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long


    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId IS NULL OR userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: Int): Flow<List<SavedNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SavedNotification): Long


    // --- Combined DB Transactions ---

    @androidx.room.Transaction
    suspend fun depositFunds(userId: Int, amount: Double) {
        val user = getUserById(userId) ?: return
        val updatedUser = user.copy(walletBalance = user.walletBalance + amount)
        updateUser(updatedUser)

        val tx = Transaction(
            userId = userId,
            amount = amount,
            type = "DEPOSIT"
        )
        insertTransaction(tx)

        val notif = SavedNotification(
            userId = userId,
            title = "Deposit Successful",
            message = "Successfully deposited Eth Birr ${String.format("%.2f", amount)} via Telebirr/Chapa."
        )
        insertNotification(notif)
    }

    @androidx.room.Transaction
    suspend fun purchaseTickets(userId: Int, drawId: Int, ticketNumbers: List<String>): Boolean {
        val user = getUserById(userId) ?: return false
        val draw = getDrawById(drawId) ?: return false

        if (user.isBlocked) return false
        if (draw.status != "ACTIVE") return false

        val requiredAmount = draw.ticketPrice * ticketNumbers.size
        if (user.walletBalance < requiredAmount) return false

        val remainingSpots = draw.totalTickets - draw.soldTickets
        if (ticketNumbers.size > remainingSpots) return false

        // 1. Deduct balance
        val updatedUser = user.copy(walletBalance = user.walletBalance - requiredAmount)
        updateUser(updatedUser)

        // 2. Increment sold count
        val updatedDraw = draw.copy(soldTickets = draw.soldTickets + ticketNumbers.size)
        updateDraw(updatedDraw)

        // 3. Insert tickets
        ticketNumbers.forEach { number ->
            insertTicket(Ticket(userId = userId, drawId = drawId, ticketNumber = number))
        }

        // 4. Record transaction
        insertTransaction(
            Transaction(
                userId = userId,
                amount = -requiredAmount, // negative for purchase
                type = "TICKET_PURCHASE"
            )
        )

        // 5. Send Notification
        insertNotification(
            SavedNotification(
                userId = userId,
                title = "Ticket Purchased!",
                message = "Purchased ${ticketNumbers.size} ticket(s) for ${draw.title}. Numbers: ${ticketNumbers.joinToString(", ")}"
            )
        )

        return true
    }

    @androidx.room.Transaction
    suspend fun selectAndPublishWinner(drawId: Int): Boolean {
        val draw = getDrawById(drawId) ?: return false
        if (draw.status != "ACTIVE") return false

        val tickets = getTicketsForDraw(drawId)
        if (tickets.isEmpty()) {
            // No tickets sold, close the draw with no winner
            val updatedDraw = draw.copy(
                status = "COMPLETED",
                winningTicketNumber = "NONE",
                winnerNickname = "No Entrants"
            )
            updateDraw(updatedDraw)

            insertNotification(
                SavedNotification(
                    userId = null,
                    title = "Draw Cancelled",
                    message = "Draw '${draw.title}' has closed with no entrants."
                )
            )
            return true
        }

        // Pick random ticket
        val winningTicket = tickets.random()
        val winnerUser = getUserById(winningTicket.userId)
        val winnerName = winnerUser?.name ?: "User #${winningTicket.userId}"

        // Update Draw status
        val updatedDraw = draw.copy(
            status = "COMPLETED",
            winningTicketNumber = winningTicket.ticketNumber,
            winnerNickname = winnerName
        )
        updateDraw(updatedDraw)

        // Reward the winner! Add prize to winner's balance
        winnerUser?.let {
            val updatedWinner = it.copy(walletBalance = it.walletBalance + draw.prizeAmount)
            updateUser(updatedWinner)

            // Insert transaction for the prize
            insertTransaction(
                Transaction(
                    userId = it.id,
                    amount = draw.prizeAmount,
                    type = "LOTTERY_WIN"
                )
            )

            // Direct notification to standard winner
            insertNotification(
                SavedNotification(
                    userId = it.id,
                    title = "HUGE WIN! 🎉",
                    message = "Congratulations! Your ticket #${winningTicket.ticketNumber} won the draw '${draw.title}'! Eth Birr ${String.format("%.2f", draw.prizeAmount)} has been added to your wallet."
                )
            )
        }

        // Global notification of completion
        insertNotification(
            SavedNotification(
                userId = null,
                title = "Draw Completed!",
                message = "The lucky winner of '${draw.title}' is $winnerName with ticket #${winningTicket.ticketNumber}, winning Eth Birr ${String.format("%.2f", draw.prizeAmount)}!"
            )
        )

        return true
    }
}
