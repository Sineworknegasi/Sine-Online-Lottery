package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class LotteryViewModel(application: Application, private val repository: LotteryRepository) : AndroidViewModel(application) {

    // --- Navigation / Global Screens ---
    sealed class ActiveScreen {
        object Login : ActiveScreen()
        object Home : ActiveScreen()
        object MyTickets : ActiveScreen()
        object Results : ActiveScreen()
        object Notifications : ActiveScreen()
        object AdminDashboard : ActiveScreen()
        object AdminCreateDraw : ActiveScreen()
        object AdminManageUsers : ActiveScreen()
    }

    private val _currentScreen = MutableStateFlow<ActiveScreen>(ActiveScreen.Login)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: ActiveScreen) {
        _currentScreen.value = screen
    }

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Form states
    var loginPhoneInput = MutableStateFlow("")
    var loginNameInput = MutableStateFlow("")
    var isRegisterState = MutableStateFlow(false)
    var otpSentCode = MutableStateFlow<String?>(null)
    var otpEnteredValue = MutableStateFlow("")
    var otpErrorMsg = MutableStateFlow<String?>(null)
    var fakeSmsOverlayMessage = MutableStateFlow<String?>(null)

    // --- Wallet Deposit Form State ---
    var depositAmountInput = MutableStateFlow("100")
    var selectedWalletPaymentMethod = MutableStateFlow("Telebirr") // Telebirr, Chapa

    // --- Create Draw Form State ---
    var createDrawTitle = MutableStateFlow("")
    var createDrawPrize = MutableStateFlow("")
    var createDrawTicketPrice = MutableStateFlow("")
    var createDrawLimit = MutableStateFlow("")
    var createDrawHoursAhead = MutableStateFlow("24")

    // --- Flows ---
    val allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDraws = repository.allDraws.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeDraws = repository.activeDraws.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val completedDraws = repository.completedDraws.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTransactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTickets = repository.allTickets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Secondary Flow - user-dependent queries
    val currentUserNotifications: Flow<List<SavedNotification>> = _currentUser.flatMapLatest { user ->
        repository.getNotificationsForUser(user?.id ?: 0)
    }

    val currentUserTickets: Flow<List<Ticket>> = _currentUser.flatMapLatest { user ->
        repository.getTicketsByUser(user?.id ?: 0)
    }

    val currentUserTransactions: Flow<List<Transaction>> = _currentUser.flatMapLatest { user ->
        repository.getTransactionsByUser(user?.id ?: 0)
    }

    init {
        // Seed initial database
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // --- Login Actions ---
    fun sendOtp() {
        val phone = loginPhoneInput.value.trim()
        if (phone.isEmpty()) {
            otpErrorMsg.value = "Please enter a phone number"
            return
        }

        viewModelScope.launch {
            val existingUser = repository.getUserByPhone(phone)
            if (existingUser != null) {
                if (existingUser.isBlocked) {
                    otpErrorMsg.value = "This account has been blocked by administrators."
                    return@launch
                }
                isRegisterState.value = false
            } else {
                isRegisterState.value = true
                if (loginNameInput.value.trim().isEmpty() && isRegisterState.value) {
                    otpErrorMsg.value = "First-time user. Please enter nickname first."
                    return@launch
                }
            }

            // Generate 4-digit OTP
            val otp = (1000..9999).random().toString()
            otpSentCode.value = otp
            otpErrorMsg.value = null
            otpEnteredValue.value = ""

            // Trigger simulated SMS Dialog overlay
            fakeSmsOverlayMessage.value = "💬 SMS from Ethiopia Lottery: Your verification OTP code is $otp. Do not share."
        }
    }

    fun dismissSmsOverlay() {
        fakeSmsOverlayMessage.value = null
    }

    fun verifyOtp() {
        val entered = otpEnteredValue.value.trim()
        val sent = otpSentCode.value
        val phone = loginPhoneInput.value.trim()

        if (sent == null) {
            otpErrorMsg.value = "Request an OTP first"
            return
        }

        if (entered != sent && entered != "1234") { // Allow 1234 backdoor bypass for easy exploring!
            otpErrorMsg.value = "Invalid selection or OTP code!"
            return
        }

        viewModelScope.launch {
            val existingUser = repository.getUserByPhone(phone)
            if (existingUser != null) {
                _currentUser.value = existingUser
                otpSentCode.value = null
                fakeSmsOverlayMessage.value = null
                otpErrorMsg.value = null

                if (existingUser.isAdmin) {
                    navigateTo(ActiveScreen.AdminDashboard)
                } else {
                    navigateTo(ActiveScreen.Home)
                }
            } else {
                // Register new user
                val newName = loginNameInput.value.trim()
                if (newName.isEmpty()) {
                    otpErrorMsg.value = "Please enter a display name/nickname"
                    return@launch
                }

                val newUser = User(
                    name = newName,
                    phone = phone,
                    walletBalance = 100.0 // Give a free initial login bonus!
                )
                val newId = repository.registerUser(newUser).toInt()
                val retrieved = repository.getUserById(newId)
                _currentUser.value = retrieved
                otpSentCode.value = null
                fakeSmsOverlayMessage.value = null
                otpErrorMsg.value = null

                repository.addNotification(
                    SavedNotification(
                        userId = newId,
                        title = "Welcome Bonus! 🎉",
                        message = "Welcome $newName! You received Eth Birr 100.00 registration bonus."
                    )
                )

                navigateTo(ActiveScreen.Home)
            }
        }
    }

    // Direct Login cheat bypass for easy testing
    fun loginAsBypass(isAdmin: Boolean) {
        viewModelScope.launch {
            val phone = if (isAdmin) "+251911223344" else "+251922334455"
            val user = repository.getUserByPhone(phone)
            if (user != null) {
                _currentUser.value = user
                if (user.isAdmin) {
                    navigateTo(ActiveScreen.AdminDashboard)
                } else {
                    navigateTo(ActiveScreen.Home)
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        loginPhoneInput.value = ""
        loginNameInput.value = ""
        otpSentCode.value = null
        isRegisterState.value = false
        navigateTo(ActiveScreen.Login)
    }

    // --- User Actions ---
    fun depositFundsViaPayment() {
        val user = _currentUser.value ?: return
        val amount = depositAmountInput.value.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) return

        viewModelScope.launch {
            repository.depositFunds(user.id, amount)
            // Refresh currentUser state
            val freshUser = repository.getUserById(user.id)
            _currentUser.value = freshUser
        }
    }

    fun buyLotteryTickets(drawId: Int, numberOfTickets: Int): Boolean {
        val user = _currentUser.value ?: return false
        val draw = activeDraws.value.firstOrNull { it.id == drawId } ?: return false

        if (numberOfTickets <= 0) return false
        val totalCost = draw.ticketPrice * numberOfTickets
        if (user.walletBalance < totalCost) return false

        // Generate tickets
        val generatedTicketsList = mutableListOf<String>()
        repeat(numberOfTickets) {
            generatedTicketsList.add(Random.nextInt(100000, 999999).toString())
        }

        viewModelScope.launch {
            val success = repository.buyTickets(user.id, drawId, generatedTicketsList)
            if (success) {
                // Refresh local user state
                val freshUser = repository.getUserById(user.id)
                _currentUser.value = freshUser
            }
        }
        return true
    }

    // --- Admin Actions ---
    fun adminCreateDraw() {
        val title = createDrawTitle.value.trim()
        val prize = createDrawPrize.value.trim().toDoubleOrNull() ?: 0.0
        val price = createDrawTicketPrice.value.trim().toDoubleOrNull() ?: 10.0
        val limit = createDrawLimit.value.trim().toIntOrNull() ?: 1000
        val hours = createDrawHoursAhead.value.trim().toLongOrNull() ?: 24L

        if (title.isEmpty() || prize <= 0.0) return

        val drawDateMillis = System.currentTimeMillis() + (hours * 3600000L)

        viewModelScope.launch {
            repository.createDraw(
                LotteryDraw(
                    title = title,
                    prizeAmount = prize,
                    ticketPrice = price,
                    totalTickets = limit,
                    soldTickets = 0,
                    drawDate = drawDateMillis
                )
            )

            // Emit notification global
            repository.addNotification(
                SavedNotification(
                    userId = null,
                    title = "New Draw Launched! 🚀",
                    message = "'$title' is now live with a prize of ETB ${String.format("%.2f", prize)}! Buy tickets now!"
                )
            )

            // Clear fields
            createDrawTitle.value = ""
            createDrawPrize.value = ""
            createDrawTicketPrice.value = ""
            createDrawLimit.value = ""
            createDrawHoursAhead.value = "24"

            navigateTo(ActiveScreen.AdminDashboard)
        }
    }

    fun adminToggleUserBlocked(user: User) {
        viewModelScope.launch {
            val updated = user.copy(isBlocked = !user.isBlocked)
            repository.updateUser(updated)
        }
    }

    fun adminPickWinnerAndPublish(drawId: Int) {
        viewModelScope.launch {
            repository.pickAndPublishDrawWinner(drawId)
        }
    }
}

// Factory Provider
class LotteryViewModelFactory(
    private val application: Application,
    private val repository: LotteryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LotteryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LotteryViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
