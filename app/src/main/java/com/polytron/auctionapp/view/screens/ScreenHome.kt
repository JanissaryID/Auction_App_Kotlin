package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.data.remote.viewmodel.InventoryViewModel
import com.polytron.auctionapp.view.components.LoginDialog
import com.polytron.auctionapp.view.components.PrinterListDialog
import com.polytron.auctionapp.view.components.ProfileDialog
import com.polytron.auctionapp.view.components.itemcard.HomeMenu
import com.polytron.auctionapp.view.components.itemcard.MenuCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHome(
    onNavigate: (String) -> Unit,
    inventoryViewModel: InventoryViewModel = koinInject(),
    bluetoothHelper: BluetoothHelper
) {
    // --- State dari ViewModel ---
    val isBluetoothConnected by inventoryViewModel.isBluetoothConnected.collectAsState()
    val selectedPrinter by inventoryViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by inventoryViewModel.showBluetoothDevice.collectAsState()

    val userName by inventoryViewModel.userName.collectAsState()
    val isLoggedIn by inventoryViewModel.isLoggedIn.collectAsState()
    val idUser by inventoryViewModel.idUser.collectAsState()
    val avatarFileName by inventoryViewModel.avatarFileName.collectAsState()
    val token by inventoryViewModel.token.collectAsState()

    // --- State UI Lokal ---
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var isNavigating by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // Logic Snackbar: Menggunakan SharedFlow agar tidak muncul berulang saat navigasi back
    LaunchedEffect(Unit) {
        inventoryViewModel.toastEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // URL Avatar PocketBase: Harus menggunakan ID dan Nama File asli dari database
    val avatarUrl = remember(idUser, avatarFileName) {
        if (!idUser.isNullOrBlank() && !avatarFileName.isNullOrBlank()) {
            "https://pb.janissaryid.com/api/files/_pb_users_auth_/$idUser/$avatarFileName"
        } else null
    }

    // Menggunakan ImageRequest untuk menyertakan header Authorization
    // Penting karena API Rule PocketBase di-set: id = @request.auth.id
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(avatarUrl)
        .apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
        }
        .crossfade(true)
        .build()

    val items = listOf(
        HomeMenu("Daftar Barang", Icons.AutoMirrored.Filled.List, "list_items"),
        HomeMenu("Lelang", Icons.Default.Gavel, "auction"),
        HomeMenu("Pembayaran", Icons.Default.Payments, "payment"),
        HomeMenu("Ambil Barang", Icons.Default.Inventory2, "take_items"),
        HomeMenu("Transaksi", Icons.Default.Receipt, "transactions"),
        HomeMenu("Pengaturan", Icons.Default.Settings, "settings")
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Unduh-Unduh GKJ", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    if (isLoggedIn && userName != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showProfileDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            // Avatar menggunakan Coil dengan ImageRequest (Header Token)
                            AsyncImage(
                                model = imageRequest,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentScale = ContentScale.Crop,
                                error = rememberVectorPainter(Icons.Default.AccountCircle),
                                placeholder = rememberVectorPainter(Icons.Default.AccountCircle)
                            )
                        }
                    } else {
                        TextButton(onClick = { showLoginDialog = true }) {
                            Text("Login", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- Section Status Bluetooth ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        bluetoothHelper.requestBluetooth(
                            onReady = { inventoryViewModel.showBluetoothDevice(true) },
                            onFailure = { reason ->
                                Toast.makeText(context, "Bluetooth gagal: $reason", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isBluetoothConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isBluetoothConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = selectedPrinter?.name ?: "Printer tidak tersambung",
                        color = if (isBluetoothConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Menu Grid ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(items) { menu ->
                    MenuCard(
                        menu = menu,
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                onNavigate(menu.route)
                                coroutineScope.launch {
                                    delay(500)
                                    isNavigating = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // --- Dialogs ---
    if (showLoginDialog) {
        LoginDialog(
            inventoryViewModel = inventoryViewModel,
            onDismiss = { showLoginDialog = false }
        )
    }

    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = { device ->
                inventoryViewModel.setSelectedPrinter(device)
                inventoryViewModel.showBluetoothDevice(false)
                val printer = BluetoothPrinter()
                CoroutineScope(Dispatchers.IO).launch {
                    try { printer.testPrinter(device) } catch (e: Exception) { e.printStackTrace() }
                }
            },
            onDismiss = { inventoryViewModel.showBluetoothDevice(false) }
        )
    }

    if (showProfileDialog) {
        ProfileDialog(
            inventoryViewModel = inventoryViewModel,
            onDismiss = { showProfileDialog = false },
            onLogout = { inventoryViewModel.logout() }
        )
    }
}