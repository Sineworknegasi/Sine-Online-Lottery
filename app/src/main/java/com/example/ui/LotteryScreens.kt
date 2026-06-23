package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotteryAppMainScreen(viewModel: LotteryViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val fakeSmsOverlay by viewModel.fakeSmsOverlayMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                // Show bottom navigation bar only when logged-in AND not on registration/login screen
                if (currentUser != null) {
                    if (currentUser?.isAdmin == true) {
                        AdminBottomNavigation(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    } else {
                        UserBottomNavigation(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is LotteryViewModel.ActiveScreen.Login -> LoginScreen(viewModel)
                        is LotteryViewModel.ActiveScreen.Home -> HomeScreen(viewModel)
                        is LotteryViewModel.ActiveScreen.MyTickets -> MyTicketsScreen(viewModel)
                        is LotteryViewModel.ActiveScreen.Results -> ResultsScreen(viewModel)
                        is LotteryViewModel.ActiveScreen.Notifications -> NotificationsScreen(viewModel)
                        is LotteryViewModel.ActiveScreen.AdminDashboard -> AdminDashboardScreen(viewModel)
                        is LotteryViewModel.ActiveScreen.AdminCreateDraw -> AdminCreateDrawScreen(viewModel)
                        is LotteryViewModel.ActiveScreen.AdminManageUsers -> AdminManageUsersScreen(viewModel)
                    }
                }
            }
        }

        // Floating Simulated SMS Notification Overlay for Real OTP verification
        fakeSmsOverlay?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.dismissSmsOverlay() },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sms,
                                contentDescription = "SMS icon",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = "SIMULATED MESSAGING SERVICE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissSmsOverlay() },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss SMS",
                                tint = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Navigation Components ---
@Composable
fun UserBottomNavigation(
    currentScreen: LotteryViewModel.ActiveScreen,
    onNavigate: (LotteryViewModel.ActiveScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF2B2930),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen is LotteryViewModel.ActiveScreen.Home,
            onClick = { onNavigate(LotteryViewModel.ActiveScreen.Home) },
            icon = { Icon(Icons.Filled.Casino, contentDescription = "Home") },
            label = { Text("Home", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFEADDFF),
                selectedTextColor = Color(0xFFEADDFF),
                unselectedIconColor = Color(0xFFCAC4D0),
                unselectedTextColor = Color(0xFFCAC4D0),
                indicatorColor = Color(0xFF4F378B)
            )
        )
        NavigationBarItem(
            selected = currentScreen is LotteryViewModel.ActiveScreen.MyTickets,
            onClick = { onNavigate(LotteryViewModel.ActiveScreen.MyTickets) },
            icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = "My Tickets") },
            label = { Text("Tickets", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFEADDFF),
                selectedTextColor = Color(0xFFEADDFF),
                unselectedIconColor = Color(0xFFCAC4D0),
                unselectedTextColor = Color(0xFFCAC4D0),
                indicatorColor = Color(0xFF4F378B)
            )
        )
        NavigationBarItem(
            selected = currentScreen is LotteryViewModel.ActiveScreen.Results,
            onClick = { onNavigate(LotteryViewModel.ActiveScreen.Results) },
            icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Results") },
            label = { Text("Results", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFEADDFF),
                selectedTextColor = Color(0xFFEADDFF),
                unselectedIconColor = Color(0xFFCAC4D0),
                unselectedTextColor = Color(0xFFCAC4D0),
                indicatorColor = Color(0xFF4F378B)
            )
        )
        NavigationBarItem(
            selected = currentScreen is LotteryViewModel.ActiveScreen.Notifications,
            onClick = { onNavigate(LotteryViewModel.ActiveScreen.Notifications) },
            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") },
            label = { Text("Alerts", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFEADDFF),
                selectedTextColor = Color(0xFFEADDFF),
                unselectedIconColor = Color(0xFFCAC4D0),
                unselectedTextColor = Color(0xFFCAC4D0),
                indicatorColor = Color(0xFF4F378B)
            )
        )
    }
}

@Composable
fun AdminBottomNavigation(
    currentScreen: LotteryViewModel.ActiveScreen,
    onNavigate: (LotteryViewModel.ActiveScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF2B2930),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen is LotteryViewModel.ActiveScreen.AdminDashboard,
            onClick = { onNavigate(LotteryViewModel.ActiveScreen.AdminDashboard) },
            icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFEADDFF),
                selectedTextColor = Color(0xFFEADDFF),
                unselectedIconColor = Color(0xFFCAC4D0),
                unselectedTextColor = Color(0xFFCAC4D0),
                indicatorColor = Color(0xFF4F378B)
            )
        )
        NavigationBarItem(
            selected = currentScreen is LotteryViewModel.ActiveScreen.AdminCreateDraw,
            onClick = { onNavigate(LotteryViewModel.ActiveScreen.AdminCreateDraw) },
            icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Create Draw") },
            label = { Text("Add Draw", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFEADDFF),
                selectedTextColor = Color(0xFFEADDFF),
                unselectedIconColor = Color(0xFFCAC4D0),
                unselectedTextColor = Color(0xFFCAC4D0),
                indicatorColor = Color(0xFF4F378B)
            )
        )
        NavigationBarItem(
            selected = currentScreen is LotteryViewModel.ActiveScreen.AdminManageUsers,
            onClick = { onNavigate(LotteryViewModel.ActiveScreen.AdminManageUsers) },
            icon = { Icon(Icons.Filled.People, contentDescription = "Users") },
            label = { Text("Users", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFEADDFF),
                selectedTextColor = Color(0xFFEADDFF),
                unselectedIconColor = Color(0xFFCAC4D0),
                unselectedTextColor = Color(0xFFCAC4D0),
                indicatorColor = Color(0xFF4F378B)
            )
        )
    }
}

// --- Screens ---

// 1. LOGIN & VERIFICATION SCREEN
@Composable
fun LoginScreen(viewModel: LotteryViewModel) {
    val phoneInput by viewModel.loginPhoneInput.collectAsState()
    val nameInput by viewModel.loginNameInput.collectAsState()
    val isRegister by viewModel.isRegisterState.collectAsState()
    val otpSentCode by viewModel.otpSentCode.collectAsState()
    val otpEntered by viewModel.otpEnteredValue.collectAsState()
    val errorMsg by viewModel.otpErrorMsg.collectAsState()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            // Elegant Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Casino,
                    contentDescription = "Lottery logo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sine Premium Lottery",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Secure Mobile Lottery Platform",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (otpSentCode == null) "Sign Up or Sign In" else "Enter Verification OTP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (otpSentCode == null) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { viewModel.loginPhoneInput.value = it },
                            label = { Text("Phone Number (+251...)") },
                            placeholder = { Text("+251 9...") },
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dynamic registration state detection (shows nickname box if new name entry)
                        AnimatedVisibility(visible = isRegister) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { viewModel.loginNameInput.value = it },
                                label = { Text("First-Time Nickname") },
                                leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        }

                        Button(
                            onClick = { viewModel.sendOtp() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Send OTP Verification", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "OTP sent to ${phoneInput}. Enter code to verify. (Simulated SMS shown above)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = otpEntered,
                            onValueChange = { viewModel.otpEnteredValue.value = it },
                            label = { Text("Simulated 4-Digit OTP") },
                            placeholder = { Text("Enter OTP") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { viewModel.otpSentCode.value = null },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                            ) {
                                Text("Back")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { viewModel.verifyOtp() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Verify", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    errorMsg?.let { msg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Evaluator Demo Shortcut Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚡ QUICK DEMO BYPASS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Instant log-in with pre-seeded test data accounts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.loginAsBypass(isAdmin = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Standard User", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.loginAsBypass(isAdmin = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Admin Console", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// 2. USER HOME SCREEN
@Composable
fun HomeScreen(viewModel: LotteryViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeDraws by viewModel.activeDraws.collectAsState()

    var showBuyDialogDraw by remember { mutableStateOf<LotteryDraw?>(null) }
    var showDepositDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // High fidelity custom header
        item {
            HeaderBar(
                user = currentUser,
                onLogout = { viewModel.logout() },
                onDeposit = { showDepositDialog = true }
            )
        }

        // Stunning hero banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_lottery_hero_1782201380848),
                    contentDescription = "Lottery Hero Jackpot Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Next Mega Jackpot",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ETB 10,000,000.00",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }
            }
        }

        // Draws List Header
        item {
            PaddingMedium {
                Text(
                    text = "🏆 Active Lottery Draws",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Interactive draw list
        if (activeDraws.filter { it.status == "ACTIVE" }.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Inbox,
                            contentDescription = "No Drawings",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Active Drawings Currently",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Log back into Admin to create some drawings instantly!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeDraws.filter { it.status == "ACTIVE" }) { draw ->
                ActiveDrawItemCard(
                    draw = draw,
                    onBuyClick = { showBuyDialogDraw = draw }
                )
            }
        }
    }

    // Interactive Buy Ticket dialog
    showBuyDialogDraw?.let { draw ->
        BuyTicketDialog(
            draw = draw,
            onDismiss = { showBuyDialogDraw = null },
            viewModel = viewModel
        )
    }

    // Telebirr / Chapa Simulated Deposit Drawer Dialog
    if (showDepositDialog) {
        DepositSimulatedDialog(
            onDismiss = { showDepositDialog = false },
            viewModel = viewModel
        )
    }
}

// 3. USER TICKETS SCREEN
@Composable
fun MyTicketsScreen(viewModel: LotteryViewModel) {
    val tickets by viewModel.currentUserTickets.collectAsState(initial = emptyList())
    val draws by viewModel.allDraws.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "🎟️ My Purchased Tickets",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Track real-time drawings and draw results below.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (tickets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.ConfirmationNumber,
                            contentDescription = "No tickets icon",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You haven't bought any tickets yet!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Explore active draws above and purchase using Telebirr or Chapa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(tickets) { ticket ->
                val draw = draws.find { it.id == ticket.drawId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = draw?.title ?: "Lottery Draw #${ticket.drawId}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ticket Number: #${ticket.ticketNumber}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val isCompleted = draw?.status == "COMPLETED"
                            val isWin = isCompleted && draw?.winningTicketNumber == ticket.ticketNumber
                            val statusText = when {
                                !isCompleted -> "Draw Active - Pending Close"
                                isWin -> "🏆 WON ETB ${String.format("%.2f", draw?.prizeAmount ?: 0.0)}"
                                else -> "Ended (Better luck next time)"
                            }
                            val statusColor = when {
                                !isCompleted -> MaterialTheme.colorScheme.secondary
                                isWin -> MaterialTheme.colorScheme.tertiary
                                else -> Color.Gray
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. RESULTS SCREEN: Winners announced
@Composable
fun ResultsScreen(viewModel: LotteryViewModel) {
    val completedDraws by viewModel.completedDraws.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "🏆 Winning Ticket Results",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Historical draws and congratulations announcements.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (completedDraws.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "No trophy",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Published Winners Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Admin must close drawings and Pick Lucky Winners to see results here!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(completedDraws) { draw ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = draw.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "COMPLETED",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Divider(color = Color.LightGray.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(0.5f)) {
                                Text("LUCKY WINNER", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    text = draw.winnerNickname ?: "No Winner Identified",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(modifier = Modifier.weight(0.5f)) {
                                Text("WINNING TICKET", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    text = "#${draw.winningTicketNumber ?: "N/A"}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("TOTAL JACKPOT PAYOUT", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    text = "Eth Birr ${String.format("%.2f", draw.prizeAmount)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. USER NOTIFICATIONS SCREEN
@Composable
fun NotificationsScreen(viewModel: LotteryViewModel) {
    val notifications by viewModel.currentUserNotifications.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "🔔 Notifications & Logs",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (notifications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(notifications) { notif ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (notif.title.contains("WIN", ignoreCase = true)) Icons.Filled.EmojiEvents else Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = if (notif.title.contains("WIN", ignoreCase = true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}

// 6. ADMIN DASHBOARD SCREEN
@Composable
fun AdminDashboardScreen(viewModel: LotteryViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val activeDraws by viewModel.activeDraws.collectAsState()
    val completedDraws by viewModel.completedDraws.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()

    // Dashboard calculations
    val totalUsers = users.size
    val totalRevenue = transactions.filter { it.type == "TICKET_PURCHASE" }.sumOf { -it.amount }
    val totalTicketsSold = activeDraws.sumOf { it.soldTickets } + completedDraws.sumOf { it.soldTickets }
    val activeDrawsCount = activeDraws.filter { it.status == "ACTIVE" }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "⚙️ Admin Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sine Premium Lottery Management Terminal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Stats Grid
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AdminStatCard(
                        title = "REGISTERED USERS",
                        value = totalUsers.toString(),
                        icon = Icons.Filled.People,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AdminStatCard(
                        title = "TOTAL REVENUE",
                        value = "ETB ${String.format("%.1f", totalRevenue)}",
                        icon = Icons.Filled.MonetizationOn,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    AdminStatCard(
                        title = "TICKETS BOUGHT",
                        value = totalTicketsSold.toString(),
                        icon = Icons.Filled.ConfirmationNumber,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AdminStatCard(
                        title = "ACTIVE DRAWS",
                        value = activeDrawsCount.toString(),
                        icon = Icons.Filled.Casino,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Drawing Winner Picking Panel
        item {
            Text(
                text = "🗳️ Action Center: Pick Lucky Winner",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val activeList = activeDraws.filter { it.status == "ACTIVE" }
        if (activeList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "No active drawings to settle. Go to 'Add Draw' page to generate a new sweepstake.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(activeList) { draw ->
                var pickingInProgress by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = draw.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tickets Sold: ${draw.soldTickets} / ${draw.totalTickets}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        pickingInProgress = true
                                        delay(3000) // Beautiful artificial simulator delay
                                        viewModel.adminPickWinnerAndPublish(draw.id)
                                        pickingInProgress = false
                                    }
                                },
                                enabled = !pickingInProgress,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (pickingInProgress) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Pick Winner", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



// 7. ADMIN CREATE DRAW SCREEN
@Composable
fun AdminCreateDrawScreen(viewModel: LotteryViewModel) {
    val title by viewModel.createDrawTitle.collectAsState()
    val prize by viewModel.createDrawPrize.collectAsState()
    val price by viewModel.createDrawTicketPrice.collectAsState()
    val limit by viewModel.createDrawLimit.collectAsState()
    val hours by viewModel.createDrawHoursAhead.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Text(
                text = "🆕 Launch New Lottery Draw",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.createDrawTitle.value = it },
                label = { Text("Draw Title / Event Name") },
                placeholder = { Text("e.g. Telebirr Weekend Special 🇪🇹") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = prize,
                onValueChange = { viewModel.createDrawPrize.value = it },
                label = { Text("Prize Amount (ETB)") },
                placeholder = { Text("e.g. 1000000.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { viewModel.createDrawTicketPrice.value = it },
                label = { Text("Ticket Price (ETB)") },
                placeholder = { Text("e.g. 15.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = limit,
                onValueChange = { viewModel.createDrawLimit.value = it },
                label = { Text("Max Tickets Limit") },
                placeholder = { Text("e.g. 2500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = hours,
                onValueChange = { viewModel.createDrawHoursAhead.value = it },
                label = { Text("Hours until draw") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.adminCreateDraw() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Launch Sweepstakes Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 8. ADMIN MANAGE USERS SCREEN
@Composable
fun AdminManageUsersScreen(viewModel: LotteryViewModel) {
    val users by viewModel.allUsers.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "👥 Manage Platform Users",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "View user registrations and suspend/block access.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        items(users) { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = user.name + if (user.isAdmin) " (ADMIN)" else "",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(text = "Phone: " + user.phone, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        Text(
                            text = "Balance: ETB " + String.format("%.2f", user.walletBalance),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!user.isAdmin) {
                        Button(
                            onClick = { viewModel.adminToggleUserBlocked(user) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isBlocked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = if (user.isBlocked) "Unblock" else "Block", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// Helper design compositions
@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
fun HeaderBar(
    user: User?,
    onLogout: () -> Unit,
    onDeposit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Upper row: Welcome and Profile avatar with initials
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WELCOME BACK,",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFFCAC4D0)
                )
                Text(
                    text = user?.name ?: "Guest User",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    color = Color(0xFFD0BCFF)
                )
            }
            
            // Circular profile badge with user's initials
            val initials = if (!user?.name.isNullOrBlank()) {
                user?.name!!.trim().split(" ").take(2).map { it.take(1).uppercase() }.joinToString("")
            } else "GU"
            
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4F378B))
                    .border(BorderStroke(2.dp, Color(0xFFD0BCFF).copy(alpha = 0.2f)), CircleShape)
                    .clickable { onLogout() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // Lower balance card - styled like bg-[#49454F]/30 rounded-2xl border border-[#49454F]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF49454F).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, Color(0xFF49454F)), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WALLET BALANCE",
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFFCAC4D0),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ETB " + String.format("%.2f", user?.walletBalance ?: 0.0),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDeposit,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deposit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFCAC4D0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveDrawItemCard(
    draw: LotteryDraw,
    onBuyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp), // elegant rounded-[2rem]
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4F378B), Color(0xFF21005D))
                    )
                )
                .padding(20.dp)
        ) {
            // First Row: Badge & Ends In
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Pink Badge Card
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFD8E4), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = draw.title.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF31111D),
                        letterSpacing = 1.sp
                    )
                }

                // Ends In info
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ENDS IN",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFFEADDFF)
                    )
                    CountdownTimerView(targetTime = draw.drawDate)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center area: Prize Pool
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WIN JACKPOT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFFEADDFF).copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ETB " + formatCompactAmount(draw.prizeAmount),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Progress Section
            val fraction = if (draw.totalTickets > 0) draw.soldTickets.toFloat() / draw.totalTickets else 0.0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${draw.soldTickets} Sold",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEADDFF)
                )
                Text(
                    text = "${draw.totalTickets - draw.soldTickets} Left",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEADDFF)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFD0BCFF),
                trackColor = Color(0xFF381E72)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Actions Section inside card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TICKET PRICE",
                        fontSize = 8.sp,
                        color = Color(0xFFEADDFF).copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "ETB ${String.format("%.2f", draw.ticketPrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(24.dp), // Fully rounded-pill
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Buy Ticket Now",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CountdownTimerView(targetTime: Long) {
    var timeLeftString by remember { mutableStateOf("00:00:00") }

    LaunchedEffect(key1 = targetTime) {
        while (true) {
            val remain = targetTime - System.currentTimeMillis()
            if (remain <= 0) {
                timeLeftString = "00:00:00 (Settle Pending)"
                break
            } else {
                val sec = remain / 1000 % 60
                val min = remain / (60 * 1000) % 60
                val hor = remain / (3600 * 1000) % 24
                val day = remain / (24 * 3600 * 1000)
                timeLeftString = if (day > 0) {
                    "${day}d ${String.format("%02d", hor)}h"
                } else {
                    "${String.format("%02d", hor)}:${String.format("%02d", min)}:${String.format("%02d", sec)}"
                }
            }
            delay(1000)
        }
    }

    Text(
        text = timeLeftString,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFFD0BCFF),
        fontFamily = FontFamily.Monospace
    )
}

@Composable
fun BuyTicketDialog(
    draw: LotteryDraw,
    onDismiss: () -> Unit,
    viewModel: LotteryViewModel
) {
    var ticketCount by remember { mutableIntStateOf(1) }
    var chosenMethod by remember { mutableStateOf("Telebirr") } // Telebirr / Chapa
    var isProcessing by remember { mutableStateOf(false) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (successMsg == null) {
                    Text(
                        text = "🎟️ Purchase Sweepstakes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = draw.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Choose Ticket Quantity", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledIconButton(
                            onClick = { if (ticketCount > 1) ticketCount-- },
                            enabled = !isProcessing,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            text = ticketCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = Color.White
                        )
                        FilledIconButton(
                            onClick = { if (ticketCount < (draw.totalTickets - draw.soldTickets)) ticketCount++ },
                            enabled = !isProcessing,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(text = "Payment Gateway Channel", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isProcessing) { chosenMethod = "Telebirr" }
                                .padding(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (chosenMethod == "Telebirr") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.5.dp,
                                color = if (chosenMethod == "Telebirr") MaterialTheme.colorScheme.primary else Color.DarkGray
                            )
                        ) {
                            Text(
                                text = "Telebirr 💳",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = if (chosenMethod == "Telebirr") MaterialTheme.colorScheme.primary else Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isProcessing) { chosenMethod = "Chapa" }
                                .padding(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (chosenMethod == "Chapa") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.5.dp,
                                color = if (chosenMethod == "Chapa") MaterialTheme.colorScheme.primary else Color.DarkGray
                            )
                        ) {
                            Text(
                                text = "Chapa ⚡",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = if (chosenMethod == "Chapa") MaterialTheme.colorScheme.primary else Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color.LightGray.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(16.dp))

                    val priceSum = draw.ticketPrice * ticketCount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount Due", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text("ETB " + String.format("%.2f", priceSum), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    delay(2000) // Beautiful API simulated connection callback
                                    val success = viewModel.buyLotteryTickets(draw.id, ticketCount)
                                    isProcessing = false
                                    if (success) {
                                        successMsg = "Successfully purchased $ticketCount tickets! Track draws on My Tickets page."
                                    } else {
                                        successMsg = "Checkout failed! Please ensure you have sufficient balance. Deposit some funds."
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Pay Securely", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Icon(
                        imageVector = if (successMsg?.contains("success", ignoreCase = true) == true) Icons.Filled.CheckCircle else Icons.Filled.Error,
                        contentDescription = null,
                        tint = if (successMsg?.contains("success", ignoreCase = true) == true) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Checkout Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = successMsg ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Awesome")
                    }
                }
            }
        }
    }
}

@Composable
fun DepositSimulatedDialog(
    onDismiss: () -> Unit,
    viewModel: LotteryViewModel
) {
    val amount by viewModel.depositAmountInput.collectAsState()
    val payMethod by viewModel.selectedWalletPaymentMethod.collectAsState()
    var isProcessing by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (resultText == null) {
                    Text(
                        text = "💳 Instant Wallet TopUp",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Simulated safe-billing gateway integration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { viewModel.depositAmountInput.value = it },
                        label = { Text("Enter Amount (ETB)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Gateway Network Processor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectedWalletPaymentMethod.value = "Telebirr" }
                                .padding(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (payMethod == "Telebirr") MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.5.dp,
                                color = if (payMethod == "Telebirr") MaterialTheme.colorScheme.secondary else Color.DarkGray
                            )
                        ) {
                            Text(
                                text = "Telebirr 💳",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = if (payMethod == "Telebirr") MaterialTheme.colorScheme.secondary else Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectedWalletPaymentMethod.value = "Chapa" }
                                .padding(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (payMethod == "Chapa") MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.5.dp,
                                color = if (payMethod == "Chapa") MaterialTheme.colorScheme.secondary else Color.DarkGray
                            )
                        ) {
                            Text(
                                text = "Chapa ⚡",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = if (payMethod == "Chapa") MaterialTheme.colorScheme.secondary else Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                        ) {
                            Text("Dismiss")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    delay(2000)
                                    viewModel.depositFundsViaPayment()
                                    isProcessing = false
                                    resultText = "Successfully deposited ETB $amount via $payMethod!"
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Inward pay", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Successful Deposit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = resultText ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

// Compact helper
fun formatCompactAmount(value: Double): String {
    return if (value >= 1000000.0) {
        String.format("%.1fM", value / 1000000.0)
    } else if (value >= 1000.0) {
        String.format("%.1fK", value / 1000.0)
    } else {
        String.format("%.0f", value)
    }
}

@Composable
fun PaddingMedium(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        content()
    }
}
