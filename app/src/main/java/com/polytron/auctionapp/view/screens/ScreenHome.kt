package com.polytron.auctionapp.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.data.datastore.UserPreferences
import com.polytron.auctionapp.view.components.itemcard.HomeMenu
import com.polytron.auctionapp.view.components.itemcard.MenuCard
import com.polytron.auctionapp.viewmodel.MainViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHome(
    mainViewModel: MainViewModel = koinInject(),
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        HomeMenu("Daftar Barang", Icons.AutoMirrored.Filled.List, "list_items"),
        HomeMenu("Lelang", Icons.Default.Gavel, "auction"),
        HomeMenu("Pembayaran", Icons.Default.Payments, "payment"),
        HomeMenu("Ambil Barang", Icons.Default.Inventory2, "take_items"),
        HomeMenu("Transaksi", Icons.Default.Receipt, "transactions"),
        HomeMenu("Pengaturan", Icons.Default.Settings, "settings")
    )

    val coroutineScope = rememberCoroutineScope()
    var isNavigating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unduh-Unduh GKJ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
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
}