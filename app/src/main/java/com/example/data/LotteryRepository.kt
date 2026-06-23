package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LotteryRepository(private val dao: LotteryDao) {

    // --- Users ---
    val allUsers: Flow<List<User>> = dao.getAllUsers()

    fun getUserByIdFlow(userId: Int): Flow<User?> = dao.getUserByIdFlow(userId)

    suspend fun getUserById(userId: Int): User? = dao.getUserById(userId)

    suspend fun getUserByPhone(phone: String): User? = dao.getUserByPhone(phone)

    suspend fun registerUser(user: User): Long = dao.insertUser(user)

    suspend fun updateUser(user: User) = dao.updateUser(user)


    // --- Draws ---
    val allDraws: Flow<List<LotteryDraw>> = dao.getAllDraws()
    val activeDraws: Flow<List<LotteryDraw>> = dao.getActiveDraws()
    val completedDraws: Flow<List<LotteryDraw>> = dao.getCompletedDraws()

    suspend fun createDraw(draw: LotteryDraw): Long = dao.insertDraw(draw)

    suspend fun getDrawById(drawId: Int): LotteryDraw? = dao.getDrawById(drawId)

    suspend fun updateDraw(draw: LotteryDraw) = dao.updateDraw(draw)


    // --- Tickets ---
    val allTickets: Flow<List<Ticket>> = dao.getAllTickets()

    fun getTicketsByUser(userId: Int): Flow<List<Ticket>> = dao.getTicketsByUser(userId)

    suspend fun insertTicket(ticket: Ticket) = dao.insertTicket(ticket)


    // --- Transactions ---
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()

    fun getTransactionsByUser(userId: Int): Flow<List<Transaction>> = dao.getTransactionsByUser(userId)


    // --- Notifications ---
    fun getNotificationsForUser(userId: Int): Flow<List<SavedNotification>> = dao.getNotificationsForUser(userId)

    suspend fun addNotification(notification: SavedNotification) = dao.insertNotification(notification)


    // --- Complex Business Actions ---
    suspend fun depositFunds(userId: Int, amount: Double) = dao.depositFunds(userId, amount)

    suspend fun buyTickets(userId: Int, drawId: Int, ticketsList: List<String>): Boolean {
        return dao.purchaseTickets(userId, drawId, ticketsList)
    }

    suspend fun pickAndPublishDrawWinner(drawId: Int): Boolean {
        return dao.selectAndPublishWinner(drawId)
    }

    // --- Seeding initial databases (Only if empty) ---
    suspend fun seedDatabaseIfEmpty() {
        val existingUsersList = dao.getAllUsers().first()
        if (existingUsersList.isEmpty()) {
            // Seed Admin User
            val adminId = dao.insertUser(
                User(
                    name = "Admin Master",
                    phone = "+251911223344",
                    walletBalance = 50000.0,
                    isAdmin = true
                )
            ).toInt()

            // Seed Test User
            val testUserId = dao.insertUser(
                User(
                    name = "Yared Kebede",
                    phone = "+251922334455",
                    walletBalance = 1250.0,
                    isAdmin = false
                )
            ).toInt()

            // Seed Another standard user
            val anotherUserId = dao.insertUser(
                User(
                    name = "Elena Tadesse",
                    phone = "+251944556677",
                    walletBalance = 350.0,
                    isAdmin = false
                )
            ).toInt()

            // Seed some lottery draws
            val draw1Id = dao.insertDraw(
                LotteryDraw(
                    title = "Monthly Mega Grand Jackpot 🇪🇹",
                    prizeAmount = 10000000.0, // 10 million birr
                    ticketPrice = 50.0,
                    totalTickets = 15000,
                    soldTickets = 1850,
                    drawDate = System.currentTimeMillis() + (86400000L * 15) // 15 days from now
                )
            ).toInt()

            val draw2Id = dao.insertDraw(
                LotteryDraw(
                    title = "Weekly Chapa Splash 🌟",
                    prizeAmount = 800000.0, // 800k birr
                    ticketPrice = 15.0,
                    totalTickets = 5000,
                    soldTickets = 3980,
                    drawDate = System.currentTimeMillis() + (86400000L * 3) // 3 days from now
                )
            ).toInt()

            val draw3Id = dao.insertDraw(
                LotteryDraw(
                    title = "Daily Telebirr Swift Draw ⚡",
                    prizeAmount = 150000.0, // 150k birr
                    ticketPrice = 5.0,
                    totalTickets = 1000,
                    soldTickets = 925,
                    drawDate = System.currentTimeMillis() + (3600000L * 4) // 4 hours from now
                )
            ).toInt()

            // Seed a past completed draw
            val draw4Id = dao.insertDraw(
                LotteryDraw(
                    title = "Daily Quickcash #104",
                    prizeAmount = 50000.0,
                    ticketPrice = 5.0,
                    totalTickets = 1000,
                    soldTickets = 1000,
                    drawDate = System.currentTimeMillis() - 86400000L, // 1 day ago
                    status = "COMPLETED",
                    winningTicketNumber = "739281",
                    winnerNickname = "Elena Tadesse"
                )
            ).toInt()

            // Seed some pre-purchased tickets for other users to make the DB realistic
            for (i in 1..20) {
                val randTicket = (100000..999999).random().toString()
                dao.insertTicket(
                    Ticket(
                        userId = anotherUserId,
                        drawId = draw2Id,
                        ticketNumber = randTicket,
                        purchaseDate = System.currentTimeMillis() - (12 * 3600000L)
                    )
                )
            }

            // Seed historical purchase for Elena Tadesse
            dao.insertTicket(
                Ticket(
                    userId = anotherUserId,
                    drawId = draw4Id,
                    ticketNumber = "739281",
                    purchaseDate = System.currentTimeMillis() - 86400000L - 3600000L
                )
            )

            // Seed transactions for testing
            dao.insertTransaction(
                Transaction(
                    userId = testUserId,
                    amount = 1250.0,
                    type = "DEPOSIT",
                    date = System.currentTimeMillis() - 1800000L
                )
            )
            dao.insertTransaction(
                Transaction(
                    userId = anotherUserId,
                    amount = 350.0,
                    type = "DEPOSIT",
                    date = System.currentTimeMillis() - 86400000L * 2
                )
            )
            dao.insertTransaction(
                Transaction(
                    userId = anotherUserId,
                    amount = 50000.0,
                    type = "LOTTERY_WIN",
                    date = System.currentTimeMillis() - 86400000L
                )
            )

            // Seed some global & local notifications
            dao.insertNotification(
                SavedNotification(
                    userId = null,
                    title = "Welcome to Ethiopia Lottery! 🌟",
                    message = "We support Telebirr and Chapa for premium high-fidelity instant deposits and cashouts.",
                    timestamp = System.currentTimeMillis() - 3600000L
                )
            )

            dao.insertNotification(
                SavedNotification(
                    userId = anotherUserId,
                    title = "Daily Quickcash #104 Completed!",
                    message = "Congratulations Elena Tadesse! You won Eth Birr 50,000.00 with ticket #739281!",
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            )
        }
    }
}
