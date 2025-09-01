package com.polytron.auctionapp.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.data.remote.viewmodel.InventoryViewModel
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.view.components.SelectedItemsBottomBar
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.camera.CameraPreviewView
import com.polytron.auctionapp.view.components.itemcard.ItemCardBarcode
import org.koin.compose.koinInject

@Composable
fun ScreenScanBarcode(
    inventoryViewModel: InventoryViewModel = koinInject(),
    navBack: () -> Unit,
    typeScreen: String?
) {
    val items by inventoryViewModel.items.collectAsState()
    val filteredItemsStat = if (typeScreen == "Payment") {
        items.filter { it.status == 1 }
    } else {
        items.filter { it.status == 0 }
    }

    val selectedItems = remember { mutableStateListOf<ItemResponse>() }

    var result by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedItemsState by inventoryViewModel.selectedItems.collectAsState()
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        inventoryViewModel.fetchItems()
    }

    LaunchedEffect(selectedItemsState) {
        if (!isInitialized) {
            selectedItems.clear()
            selectedItems.addAll(selectedItemsState)
            isInitialized = true
        }
    }

    // ✅ Jika result berubah, cari di filtered dan tambahkan ke selected
    LaunchedEffect(result) {
        result?.let { code ->
            val matchedItem = filteredItemsStat.find { it.codeItem == code }

            if (matchedItem != null && selectedItems.none { it.id == matchedItem.id }) {
                selectedItems.add(matchedItem)
                snackbarHostState.showSnackbar("Item ditemukan dan ditambahkan")
            } else if (matchedItem == null) {
                snackbarHostState.showSnackbar("Item tidak ditemukan")
            } else {
                snackbarHostState.showSnackbar("Item sudah dipilih")
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarCustom(
                title = "Scan Barcode",
                onBack = navBack
            )
        },
        bottomBar = {
            SelectedItemsBottomBar(
                selectedCount = selectedItems.size,
                buttonText = "Proses",
                onClick = {
                    inventoryViewModel.setSelectedItems(selectedItems.toList())
                    navBack()
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 📷 Kamera Preview (Compact Height)
            CameraPreviewView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp)),
                onBarcodeScanned = { scanned ->
                    if (result != scanned) result = scanned
                }
            )

            // ✅ Daftar Selected Items
            if (selectedItems.isEmpty()) {
                Text(
                    text = "Belum ada hasil scan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(selectedItems) { item ->
                        ItemCardBarcode(
                            item = item,
                            onClickDelete = {
                                selectedItems.remove(item)
                            }
                        )
                    }
                }
            }
        }
    }
}
