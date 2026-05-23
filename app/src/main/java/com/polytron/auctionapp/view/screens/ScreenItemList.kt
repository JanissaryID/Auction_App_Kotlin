package com.polytron.auctionapp.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import com.polytron.auctionapp.bluetooth.BluetoothHelper
import com.polytron.auctionapp.bluetooth.BluetoothPrinter
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.ui.viewmodel.AuthViewModel
import com.polytron.auctionapp.ui.viewmodel.ItemsViewModel
import com.polytron.auctionapp.ui.viewmodel.PrinterViewModel
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.view.components.EmptyItemState
import com.polytron.auctionapp.view.components.TopAppBarCustom
import com.polytron.auctionapp.view.components.bottomsheet.AddOrEditItemBottomSheet
import com.polytron.auctionapp.view.components.dialog.PrinterListDialog
import com.polytron.auctionapp.view.components.fab.FabWithDelete
import com.polytron.auctionapp.view.components.itemcard.ItemCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

private fun normalizeItemGroupName(name: String?): String {
    if (name.isNullOrBlank()) return "Tanpa Nama"
    return name.replace(Regex("\\s*[-–]?\\s*\\d+$"), "").trim().ifBlank { "Tanpa Nama" }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenItemList(
    itemsViewModel: ItemsViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    authViewModel: AuthViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    printerViewModel: PrinterViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    bluetoothHelper: BluetoothHelper,
    navBack: () -> Unit
) {
    val context = LocalContext.current
    val idUser by authViewModel.idUser.collectAsState()
    val items by itemsViewModel.items.collectAsState()
    val isLoading by itemsViewModel.isLoading.collectAsState()
    val printerDevice by printerViewModel.selectedPrinter.collectAsState()
    val showBluetoothDevice by printerViewModel.showBluetoothDevice.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    var selectedItem by remember { mutableStateOf<ItemResponse?>(null) }
    var showAddEditBottomSheet by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<ItemResponse>() }
    val isSelectionMode = selectedItems.isNotEmpty()
    var isDeleting by remember { mutableStateOf(false) }
    var isPrinting by remember { mutableStateOf(false) }
    var printingItemId by remember { mutableStateOf<String?>(null) }
    var showSelectionMenu by remember { mutableStateOf(false) }
    var expandedGroupName by remember { mutableStateOf<String?>(null) }
    var showPrintConfirmDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun requestPrinterConnection() {
        Toast.makeText(context, "Belum ada printer yang terhubung", Toast.LENGTH_SHORT).show()
        bluetoothHelper.requestBluetooth(
            onReady = { printerViewModel.showBluetoothDevice(true) },
            onFailure = { Toast.makeText(context, "Bluetooth gagal: $it", Toast.LENGTH_SHORT).show() }
        )
    }

    fun printItems(itemsToPrint: List<ItemResponse>) {
        if (isPrinting) return
        if (itemsToPrint.isEmpty()) {
            Toast.makeText(context, "Pilih barang yang ingin dicetak", Toast.LENGTH_SHORT).show()
            return
        }

        val device = printerDevice
        if (device == null) {
            requestPrinterConnection()
            return
        }

        coroutineScope.launch {
            isPrinting = true
            printingItemId = itemsToPrint.singleOrNull()?.id
            val success = try {
                withContext(Dispatchers.IO) {
                    BluetoothPrinter().printBarcodeLabels(device = device, items = itemsToPrint)
                }
            } catch (_: Exception) {
                false
            }
            isPrinting = false
            printingItemId = null

            Toast.makeText(
                context,
                if (success) "${itemsToPrint.size} label berhasil dicetak" else "Gagal mencetak label",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val totalBase = items.sumOf {
        it.basePrice?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L
    }.toString()
    val totalMax = items.sumOf {
        it.maxPrice?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L
    }.toString()

    val filteredItems = items.filter {
        it.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                it.codeItem?.contains(searchQuery, ignoreCase = true) == true
    }
    val groupedItems = filteredItems.groupBy { normalizeItemGroupName(it.nameItem) }
    val sortedGroups = groupedItems.toSortedMap().entries.toList()
    val selectionMenuItemCount = sortedGroups.size + 1
    val selectionMenuVisibleItems = selectionMenuItemCount.coerceAtMost(4).coerceAtLeast(1)
    val selectionMenuItemHeight = 48.dp
    val selectionMenuHeight = selectionMenuItemHeight * selectionMenuVisibleItems.toFloat()
    val showSelectionScrollbar = selectionMenuItemCount > 4
    val selectionMenuScrollState = rememberScrollState()
    val scrollbarTrackHeight = selectionMenuHeight - 12.dp
    val scrollbarThumbHeight = (scrollbarTrackHeight * (selectionMenuVisibleItems.toFloat() / selectionMenuItemCount.toFloat())).coerceAtLeast(32.dp)
    val scrollbarProgress = if (showSelectionScrollbar) {
        val maxScroll = selectionMenuScrollState.maxValue.coerceAtLeast(1)
        (selectionMenuScrollState.value.toFloat() / maxScroll).coerceIn(0f, 1f)
    } else {
        0f
    }
    val scrollbarThumbOffset = (scrollbarTrackHeight - scrollbarThumbHeight) * scrollbarProgress

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarCustom(
                title = "${items.size} Barang Lelang",
                onBack = { navBack() },
                showRefresh = true,
                isRefreshing = isLoading,
                onRefresh = { itemsViewModel.fetchItems() },
            )
        },
        bottomBar = {
            if (filteredItems.isNotEmpty()) {
                BottomAppBar(tonalElevation = 4.dp, containerColor = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Awal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(formatRupiah(totalBase), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Maksimal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(formatRupiah(totalMax), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isSelectionMode) {
                    ExtendedFloatingActionButton(
                        onClick = { if (!isPrinting) showPrintConfirmDialog = true },
                        icon = {
                            if (isPrinting) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(Icons.Default.Print, contentDescription = "Cetak")
                            }
                        },
                        text = { Text(if (isPrinting) "Mencetak..." else "Cetak") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                FabWithDelete(
                    isSelectionMode = isSelectionMode,
                    isDeleting = isDeleting,
                    onDelete = {
                        isDeleting = true
                        selectedItems.forEach {
                            itemsViewModel.deleteItem(it.id!!)
                            delay(500)
                        }
                        selectedItems.clear()
                        itemsViewModel.fetchItems()
                        isDeleting = false
                    },
                    onAddClick = {
                        showAddEditBottomSheet = true
                        selectedItem = null
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari berdasarkan nama atau kode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                singleLine = true
            )
            if (isSelectionMode) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedItems.size} barang dipilih",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box {
                                TextButton(onClick = { showSelectionMenu = true }) {
                                    Text("Pilih")
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = showSelectionMenu,
                                    onDismissRequest = { showSelectionMenu = false }
                                ) {
                                    Box(modifier = Modifier.height(selectionMenuHeight)) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(end = if (showSelectionScrollbar) 8.dp else 0.dp)
                                                .verticalScroll(selectionMenuScrollState)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Pilih Semua (${filteredItems.size})") },
                                                onClick = {
                                                    selectedItems.clear()
                                                    selectedItems.addAll(filteredItems)
                                                    showSelectionMenu = false
                                                }
                                            )
                                            sortedGroups.forEach { (groupName, groupedItemList) ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "Group: $groupName (${groupedItemList.size})",
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedItems.clear()
                                                        selectedItems.addAll(groupedItemList)
                                                        expandedGroupName = groupName
                                                        showSelectionMenu = false
                                                    }
                                                )
                                            }
                                        }
                                        if (showSelectionScrollbar) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .padding(end = 4.dp)
                                                    .width(3.dp)
                                                    .height(scrollbarTrackHeight)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                                        shape = MaterialTheme.shapes.extraSmall
                                                    )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(top = 6.dp, end = 4.dp)
                                                    .offset(y = scrollbarThumbOffset)
                                                    .width(3.dp)
                                                    .height(scrollbarThumbHeight)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                                                        shape = MaterialTheme.shapes.extraSmall
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                            TextButton(onClick = { selectedItems.clear() }) {
                                Text("Batal")
                            }
                        }
                    }
                }
            }
            if (filteredItems.isEmpty()) {
                EmptyItemState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    sortedGroups.forEach { (groupName, groupedItemList) ->
                        item(key = groupName) {
                            val isExpanded = expandedGroupName == groupName
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = groupName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${groupedItemList.size} item",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            if (isSelectionMode) {
                                                selectedItems.clear()
                                            }
                                            expandedGroupName = if (isExpanded) null else groupName
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                        Text(if (isExpanded) "Tutup" else "Detail")
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        groupedItemList.forEach { item ->
                                            ItemCard(
                                                item = item,
                                                isSelected = selectedItems.contains(item),
                                                isPrinting = printingItemId == item.id,
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        if (selectedItems.contains(item)) selectedItems.remove(item) else selectedItems.add(item)
                                                    } else {
                                                        selectedItem = item
                                                        showAddEditBottomSheet = true
                                                    }
                                                },
                                                onLongClick = { if (!selectedItems.contains(item)) selectedItems.add(item) },
                                                onPrintClick = if (isSelectionMode) null else ({
                                                    printItems(listOf(item))
                                                })
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEditBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showAddEditBottomSheet = false }, sheetState = sheetState) {
            AddOrEditItemBottomSheet(
                itemToEdit = selectedItem,
                onDismiss = { showAddEditBottomSheet = false },
                onSubmit = { name, code, base, max, quantity ->
                    if (selectedItem != null) {
                        selectedItem!!.id?.let { id ->
                            itemsViewModel.patchItem(id, selectedItem!!.copy(nameItem = name, codeItem = code, basePrice = base, maxPrice = max))
                        }
                    } else {
                        repeat(quantity) { index ->
                            val suffix = index + 1
                            itemsViewModel.createItem(ItemResponse(nameItem = "$name $suffix", codeItem = "$code $suffix", basePrice = base, maxPrice = max, admin = "admin", user = idUser))
                            delay(500)
                        }
                    }
                    itemsViewModel.fetchItems()
                }
            )
        }
    }

    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = { device -> printerViewModel.setSelectedPrinter(device); printerViewModel.showBluetoothDevice(false) },
            onDismiss = { printerViewModel.showBluetoothDevice(false) }
        )
    }

    if (showPrintConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPrintConfirmDialog = false },
            icon = { Icon(Icons.Default.Print, contentDescription = null) },
            title = { Text("Cetak Label?") },
            text = { Text("${selectedItems.size} barang terpilih akan dicetak.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPrintConfirmDialog = false
                        printItems(selectedItems.toList())
                    }
                ) {
                    Text("Cetak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrintConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
