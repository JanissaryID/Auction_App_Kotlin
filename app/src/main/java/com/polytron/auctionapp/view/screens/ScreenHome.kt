package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.bluetooth.displayNameWithAddressSuffix
import com.polytron.auctionapp.ui.viewmodel.AuthViewModel
import com.polytron.auctionapp.ui.viewmodel.AuctionViewModel
import com.polytron.auctionapp.ui.viewmodel.PrinterViewModel
import com.polytron.auctionapp.view.components.dialog.LoginDialog
import com.polytron.auctionapp.view.components.dialog.PrinterListDialog
import com.polytron.auctionapp.view.components.dialog.ProfileDialog
import com.polytron.auctionapp.view.components.itemcard.HomeMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHome(
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    auctionViewModel: AuctionViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    printerViewModel: PrinterViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    bluetoothHelper: BluetoothHelper
) {
    val isBluetoothConnected by printerViewModel.isBluetoothConnected.collectAsState()
    val selectedPrinter by printerViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by printerViewModel.showBluetoothDevice.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val idUser by authViewModel.idUser.collectAsState()
    val avatarFileName by authViewModel.avatarFileName.collectAsState()
    val token by authViewModel.token.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isNavigating by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showAuthReminder by remember { mutableStateOf(false) }

    val avatarUrl = remember(idUser, avatarFileName) {
        if (!idUser.isNullOrBlank() && !avatarFileName.isNullOrBlank()) {
            "https://pb.janissaryid.com/api/files/_pb_users_auth_/$idUser/$avatarFileName"
        } else null
    }
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(avatarUrl)
        .apply { if (!token.isNullOrEmpty()) addHeader("Authorization", "Bearer $token") }
        .crossfade(true)
        .build()

    val menuItems = listOf(
        HomeMenu("Daftar Barang", Icons.AutoMirrored.Filled.List, "list_items"),
        HomeMenu("Lelang", Icons.Default.Gavel, "auction"),
        HomeMenu("Pembayaran", Icons.Default.Payments, "payment"),
        HomeMenu("Ambil Barang", Icons.Default.Inventory2, "take_items"),
        HomeMenu("Transaksi", Icons.Default.Receipt, "transactions"),
    )

    // Clear selection when returning to home
    LaunchedEffect(Unit) {
        auctionViewModel.clearSelectedItems()
    }

    // Show toast events from auth
    LaunchedEffect(Unit) {
        authViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                // SOLUSI: Tambahkan padding navigasi bar di sini agar tidak tertutup
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {

            // --- 1. HEADER CARD WITH FLOATING ICON ---
            item(span = { GridItemSpan(2) }) {
                Card(
                    onClick = { if (isLoggedIn) showProfileDialog = true else showLoginDialog = true },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isLoggedIn) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp),
                            tonalElevation = 2.dp
                        ) {
                            if (isLoggedIn) {
                                AsyncImage(
                                    model = imageRequest,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    error = rememberVectorPainter(Icons.Default.AccountCircle),
                                    placeholder = rememberVectorPainter(Icons.Default.AccountCircle)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Login",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isLoggedIn) "Selamat Pelayanan" else "Mulai Pelayanan",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isLoggedIn) (userName ?: "Petugas") else "Ketuk untuk login",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- 2. PRINTER STATUS CARD ---
            item(span = { GridItemSpan(2) }) {
                Card(
                    onClick = {
                        bluetoothHelper.requestBluetooth(
                            onReady = { printerViewModel.showBluetoothDevice(true) },
                            onFailure = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBluetoothConnected) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(color = if (isBluetoothConnected) Color(0xFF4CAF50) else Color(0xFFFF9800), shape = CircleShape), contentAlignment = Alignment.Center) {
                            Icon(imageVector = if (isBluetoothConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = if (isBluetoothConnected) "Printer Siap Cetak" else "Printer Belum Siap", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isBluetoothConnected) Color(0xFF2E7D32) else Color(0xFFE65100))
                            Text(text = if (isBluetoothConnected) (selectedPrinter?.displayNameWithAddressSuffix() ?: "Connected") else "Hubungkan printer bluetooth", style = MaterialTheme.typography.bodySmall, color = if (isBluetoothConnected) Color(0xFF388E3C) else Color(0xFFEF6C00))
                        }
                    }
                }
            }

            // --- 3. SECTION TITLE ---
            item(span = { GridItemSpan(2) }) {
                Text(text = "Layanan Utama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
            }

            // --- 4. GRID MENU ---
            items(menuItems) { menu ->
                Card(
                    onClick = {
                        if (isLoggedIn) {
                            if (!isNavigating) {
                                isNavigating = true
                                onNavigate(menu.route)
                                coroutineScope.launch { delay(500); isNavigating = false }
                            }
                        } else {
                            showAuthReminder = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(56.dp), shadowElevation = 1.dp) {
                            Icon(imageVector = menu.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(14.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = menu.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    // --- 5. DIALOG PENGINGAT ---
    if (showAuthReminder) {
        AlertDialog(
            onDismissRequest = { showAuthReminder = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(text = "Akses Terbatas", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Silakan login untuk masuk ke menu ini. Tombol login tersedia di bagian atas layar (ikon biru).", textAlign = TextAlign.Center) },
            confirmButton = { TextButton(onClick = { showAuthReminder = false }) { Text("Kembali") } },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // --- Dialogs Lainnya ---
    if (showLoginDialog) LoginDialog(onDismiss = { showLoginDialog = false })
    if (showBluetoothDevice) PrinterListDialog(bluetoothHelper, { device ->
        printerViewModel.setSelectedPrinter(device)
        printerViewModel.showBluetoothDevice(false)
        CoroutineScope(Dispatchers.IO).launch { try { BluetoothPrinter().testPrinter(device) } catch (e: Exception) {} }
    }, { printerViewModel.showBluetoothDevice(false) })
    if (showProfileDialog) ProfileDialog(authViewModel, { showProfileDialog = false }, { authViewModel.logout() })
}
