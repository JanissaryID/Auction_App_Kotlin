package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.data.remote.viewmodel.InventoryViewModel
import com.polytron.auctionapp.view.components.LoginDialog
import com.polytron.auctionapp.view.components.PrinterListDialog
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
    bluetoothHelper: BluetoothHelper // Pastikan bluetoothHelper dipassing dari NavHost
) {
    // State dari ViewModel
    val isBluetoothConnected by inventoryViewModel.isBluetoothConnected.collectAsState()
    val selectedPrinter by inventoryViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by inventoryViewModel.showBluetoothDevice.collectAsState()

    val items = listOf(
        HomeMenu("Daftar Barang", Icons.AutoMirrored.Filled.List, "list_items"),
        HomeMenu("Lelang", Icons.Default.Gavel, "auction"),
        HomeMenu("Pembayaran", Icons.Default.Payments, "payment"),
        HomeMenu("Ambil Barang", Icons.Default.Inventory2, "take_items"),
        HomeMenu("Transaksi", Icons.Default.Receipt, "transactions"),
        HomeMenu("Pengaturan", Icons.Default.Settings, "settings")
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isNavigating by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unduh-Unduh GKJ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    TextButton(onClick = { showLoginDialog = true }) {
                        Text("Login", color = MaterialTheme.colorScheme.primary)
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
            // Section Status Bluetooth (Sama dengan ScreenSettings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        bluetoothHelper.requestBluetooth(
                            onReady = { inventoryViewModel.showBluetoothDevice(true) },
                            onFailure = { reason ->
                                Toast.makeText(context, "Bluetooth gagal: $reason", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isBluetoothConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isBluetoothConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedPrinter?.name ?: "Printer tidak tersambung",
                    color = if (isBluetoothConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Menu Utama
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

    // Dialog Login
    if (showLoginDialog) {
        LoginDialog(
            inventoryViewModel = inventoryViewModel,
            onDismiss = { showLoginDialog = false }
        )
    }

    // Dialog Pilih Printer Bluetooth
    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = { device ->
                inventoryViewModel.setSelectedPrinter(device)
                inventoryViewModel.showBluetoothDevice(false)

                val printer = BluetoothPrinter()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        printer.testPrinter(device)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            onDismiss = {
                inventoryViewModel.showBluetoothDevice(false)
            }
        )
    }
}