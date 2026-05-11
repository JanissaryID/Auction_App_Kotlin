package com.polytron.auctionapp.desktop.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material3.Switch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.polytron.auctionapp.desktop.components.DesktopDimens
import com.polytron.auctionapp.desktop.components.DesktopButton as Button
import com.polytron.auctionapp.desktop.components.DesktopOutlinedButton as OutlinedButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.window.DialogWindow
import com.polytron.auctionapp.desktop.navigation.DesktopDialog
import com.polytron.auctionapp.domain.model.ItemResponse
import com.polytron.auctionapp.presentation.auth.AuthViewModel
import com.polytron.auctionapp.presentation.items.ItemsViewModel
import com.polytron.auctionapp.presentation.auction.AuctionViewModel
import com.polytron.auctionapp.presentation.payment.PaymentViewModel
import com.polytron.auctionapp.presentation.pickup.PickupViewModel
import com.polytron.auctionapp.utils.formatCurrencyInput
import com.polytron.auctionapp.utils.formatRupiah
import com.polytron.auctionapp.utils.generateRandomAlphanumeric
import com.polytron.auctionapp.domain.model.PaymentMethod
import com.polytron.auctionapp.desktop.components.StatusBadge
import com.polytron.auctionapp.desktop.components.StatusTone
import kotlinx.coroutines.launch

@Composable
fun DesktopDialogHost(
    dialog: DesktopDialog?,
    authViewModel: AuthViewModel,
    itemsViewModel: ItemsViewModel,
    auctionViewModel: AuctionViewModel,
    paymentViewModel: PaymentViewModel,
    pickupViewModel: PickupViewModel,
    onDismiss: () -> Unit
) {
    when (dialog) {
        DesktopDialog.Login -> LoginDialog(
            authViewModel = authViewModel,
            onDismiss = onDismiss
        )

        DesktopDialog.Profile -> ProfileDialog(
            authViewModel = authViewModel,
            onDismiss = onDismiss
        )

        DesktopDialog.AddItem -> AddEditItemDialog(
            itemsViewModel = itemsViewModel,
            onDismiss = onDismiss
        )

        is DesktopDialog.EditItem -> {
            val items by itemsViewModel.items.collectAsState()
            val item = items.find { it.id == dialog.itemId }
            if (item != null) {
                AddEditItemDialog(
                    itemToEdit = item,
                    itemsViewModel = itemsViewModel,
                    onDismiss = onDismiss
                )
            } else {
                onDismiss()
            }
        }

        is DesktopDialog.ItemDetail -> {
            val items by itemsViewModel.items.collectAsState()
            val item = items.find { it.id == dialog.itemId }
            if (item != null) {
                ItemDetailDialog(
                    item = item,
                    onDismiss = onDismiss
                )
            } else {
                onDismiss()
            }
        }

        is DesktopDialog.ConfirmDelete -> ConfirmDeleteDialog(
            itemIds = dialog.itemIds,
            itemsViewModel = itemsViewModel,
            onDismiss = onDismiss
        )

        DesktopDialog.SelectAuctionItems -> SelectAuctionItemsDialog(
            itemsViewModel = itemsViewModel,
            auctionViewModel = auctionViewModel,
            onDismiss = onDismiss
        )

        DesktopDialog.SelectPaymentItems -> SelectPaymentItemsDialog(
            itemsViewModel = itemsViewModel,
            paymentViewModel = paymentViewModel,
            onDismiss = onDismiss
        )

        DesktopDialog.SelectPickupItems -> SelectPickupItemsDialog(
            itemsViewModel = itemsViewModel,
            pickupViewModel = pickupViewModel,
            onDismiss = onDismiss
        )

        DesktopDialog.BarcodeEntry -> BarcodeEntryDialog(
            itemsViewModel = itemsViewModel,
            auctionViewModel = auctionViewModel,
            paymentViewModel = paymentViewModel,
            pickupViewModel = pickupViewModel,
            onDismiss = onDismiss
        )

        DesktopDialog.PaymentMethod -> PaymentMethodDialog(
            paymentViewModel = paymentViewModel,
            itemsViewModel = itemsViewModel,
            onDismiss = onDismiss
        )

        is DesktopDialog.TransactionDetail -> {
            val items by itemsViewModel.items.collectAsState()
            val orderItems = items.filter { it.orderID == dialog.orderId }
            if (orderItems.isNotEmpty()) {
                TransactionDetailDialog(
                    orderId = dialog.orderId,
                    items = orderItems,
                    onDismiss = onDismiss
                )
            } else {
                onDismiss()
            }
        }

        null -> Unit
        else -> BasicDialog(
            title = "Dialog",
            onDismiss = onDismiss
        ) {
            Text(
                text = "Aksi ini akan tersedia pada fase layar fitur.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SelectAuctionItemsDialog(
    itemsViewModel: ItemsViewModel,
    auctionViewModel: AuctionViewModel,
    onDismiss: () -> Unit
) {
    val items by itemsViewModel.items.collectAsState()
    val selectedAuctionItems by auctionViewModel.selectedItems.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var tempSelectedIds by remember { 
        mutableStateOf(selectedAuctionItems.mapNotNull { it.id }.toSet()) 
    }

    val availableItems = remember(items, searchQuery) {
        items.filter { item ->
            item.status == 0 && (
                searchQuery.isBlank() ||
                item.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                item.codeItem?.contains(searchQuery, ignoreCase = true) == true
            )
        }
    }

    BasicDialog(
        title = "Pilih Barang Lelang",
        onDismiss = onDismiss,
        maxWidth = DesktopDimens.DialogMediumWidth,
        actions = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    val selected = items.filter { tempSelectedIds.contains(it.id) }
                    auctionViewModel.setSelectedItems(selected)
                    onDismiss()
                }
            ) {
                Text("Konfirmasi (${tempSelectedIds.size})")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari kode atau nama...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (availableItems.isEmpty()) {
                        Text(
                            text = "Tidak ada barang tersedia.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        availableItems.forEach { item ->
                            val itemId = item.id ?: ""
                            val isSelected = tempSelectedIds.contains(itemId)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        tempSelectedIds = if (checked) {
                                            tempSelectedIds + itemId
                                        } else {
                                            tempSelectedIds - itemId
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.nameItem.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = item.codeItem.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = formatRupiah(item.basePrice),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectPickupItemsDialog(
    itemsViewModel: ItemsViewModel,
    pickupViewModel: PickupViewModel,
    onDismiss: () -> Unit
) {
    val items by itemsViewModel.items.collectAsState()
    val selectedPickupItems by pickupViewModel.selectedItems.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var tempSelectedIds by remember { 
        mutableStateOf(selectedPickupItems.mapNotNull { it.id }.toSet()) 
    }

    val availableItems = remember(items, searchQuery) {
        items.filter { item ->
            item.status == 2 && (
                searchQuery.isBlank() ||
                item.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                item.codeItem?.contains(searchQuery, ignoreCase = true) == true ||
                item.buyer?.contains(searchQuery, ignoreCase = true) == true ||
                item.orderID?.contains(searchQuery, ignoreCase = true) == true
            )
        }
    }

    BasicDialog(
        title = "Pilih Barang Pengambilan",
        onDismiss = onDismiss,
        maxWidth = DesktopDimens.DialogMediumWidth,
        actions = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    val selected = items.filter { tempSelectedIds.contains(it.id) }
                    pickupViewModel.setSelectedItems(selected)
                    onDismiss()
                }
            ) {
                Text("Konfirmasi (${tempSelectedIds.size})")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari kode, nama, pemenang, atau order id...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (availableItems.isEmpty()) {
                        Text(
                            text = "Tidak ada barang siap diambil.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        availableItems.forEach { item ->
                            val itemId = item.id ?: ""
                            val isSelected = tempSelectedIds.contains(itemId)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        tempSelectedIds = if (checked) {
                                            tempSelectedIds + itemId
                                        } else {
                                            tempSelectedIds - itemId
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.nameItem.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = item.codeItem.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = item.orderID.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = item.buyer.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectPaymentItemsDialog(
    itemsViewModel: ItemsViewModel,
    paymentViewModel: PaymentViewModel,
    onDismiss: () -> Unit
) {
    val items by itemsViewModel.items.collectAsState()
    val selectedPaymentItems by paymentViewModel.selectedItems.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var tempSelectedIds by remember { 
        mutableStateOf(selectedPaymentItems.mapNotNull { it.id }.toSet()) 
    }

    val availableItems = remember(items, searchQuery) {
        items.filter { item ->
            item.status == 1 && (
                searchQuery.isBlank() ||
                item.nameItem?.contains(searchQuery, ignoreCase = true) == true ||
                item.codeItem?.contains(searchQuery, ignoreCase = true) == true ||
                item.buyer?.contains(searchQuery, ignoreCase = true) == true
            )
        }
    }

    BasicDialog(
        title = "Pilih Barang Pembayaran",
        onDismiss = onDismiss,
        maxWidth = DesktopDimens.DialogMediumWidth,
        actions = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    val selected = items.filter { tempSelectedIds.contains(it.id) }
                    paymentViewModel.setSelectedItems(selected)
                    onDismiss()
                }
            ) {
                Text("Konfirmasi (${tempSelectedIds.size})")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari kode, nama, atau pemenang...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (availableItems.isEmpty()) {
                        Text(
                            text = "Tidak ada barang menunggu pembayaran.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        availableItems.forEach { item ->
                            val itemId = item.id ?: ""
                            val isSelected = tempSelectedIds.contains(itemId)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        tempSelectedIds = if (checked) {
                                            tempSelectedIds + itemId
                                        } else {
                                            tempSelectedIds - itemId
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.nameItem.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = item.codeItem.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = item.buyer.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = formatRupiah(item.price),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarcodeEntryDialog(
    itemsViewModel: ItemsViewModel,
    auctionViewModel: AuctionViewModel? = null,
    paymentViewModel: PaymentViewModel? = null,
    pickupViewModel: PickupViewModel? = null,
    onDismiss: () -> Unit
) {
    val items by itemsViewModel.items.collectAsState()
    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val handleAdd = {
        val match = items.find { it.codeItem?.equals(code.trim(), ignoreCase = true) == true }
        when {
            match == null -> errorMessage = "Barang tidak ditemukan"
            auctionViewModel != null && match.status != 0 -> errorMessage = "Barang sudah tidak tersedia (Status: ${match.status})"
            paymentViewModel != null && match.status != 1 -> errorMessage = "Barang tidak menunggu pembayaran (Status: ${match.status})"
            pickupViewModel != null && match.status != 2 -> errorMessage = "Barang tidak menunggu pengambilan (Status: ${match.status})"
            else -> {
                auctionViewModel?.addSelectedItem(match)
                paymentViewModel?.addSelectedItem(match)
                pickupViewModel?.addSelectedItem(match)
                onDismiss()
            }
        }
    }

    BasicDialog(
        title = "Input Kode Barang",
        onDismiss = onDismiss,
        maxWidth = 420.dp,
        actions = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = handleAdd,
                enabled = code.isNotBlank()
            ) {
                Text("Tambah")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Masukkan kode barang secara manual atau gunakan scanner barcode.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = code,
                onValueChange = { 
                    code = it
                    errorMessage = null
                },
                label = { Text("Kode Barang") },
                modifier = Modifier.fillMaxWidth().onKeyEvent {
                    if (it.key == Key.Enter && code.isNotBlank()) {
                        handleAdd()
                        true
                    } else false
                },
                singleLine = true,
                isError = errorMessage != null
            )
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodDialog(
    paymentViewModel: PaymentViewModel,
    itemsViewModel: ItemsViewModel,
    onDismiss: () -> Unit
) {
    val selectedItems by paymentViewModel.selectedItems.collectAsState()
    val totalAmount = selectedItems.sumOf { it.price?.toLongOrNull() ?: 0L }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.Cash) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BasicDialog(
        title = "Metode Pembayaran",
        onDismiss = onDismiss,
        maxWidth = 420.dp,
        actions = {
            OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Batal")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val orderId = "Order-${generateRandomAlphanumeric()}"
                            selectedItems.forEach { item ->
                                itemsViewModel.patchItem(
                                    id = item.id!!,
                                    item = item.copy(
                                        status = 2,
                                        orderID = orderId,
                                        typePayment = selectedMethod.label
                                    )
                                )
                            }
                            paymentViewModel.clearSelectedItems()
                            onDismiss()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Konfirmasi Bayar")
                }
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Total Pembayaran", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = formatRupiah(totalAmount.toString()),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text("Pilih Metode:", style = MaterialTheme.typography.titleSmall)
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.all.forEach { method ->
                    val isSelected = method == selectedMethod
                    Surface(
                        onClick = { selectedMethod = method },
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = method.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEditItemDialog(
    itemToEdit: ItemResponse? = null,
    itemsViewModel: ItemsViewModel,
    onDismiss: () -> Unit
) {
    val isEditMode = itemToEdit != null
    val scope = rememberCoroutineScope()

    var name by remember {
        mutableStateOf(itemToEdit?.nameItem ?: "")
    }
    var code by remember {
        mutableStateOf(itemToEdit?.codeItem ?: "")
    }
    var rawBasePrice by remember {
        mutableStateOf(itemToEdit?.basePrice ?: "")
    }
    var basePriceFormatted by remember {
        mutableStateOf(formatCurrencyInput(rawBasePrice))
    }
    var useAutoMaxPrice by remember {
        mutableStateOf(itemToEdit == null)
    }
    var rawMaxPrice by remember {
        mutableStateOf(itemToEdit?.maxPrice ?: "")
    }
    var maxPriceFormatted by remember {
        mutableStateOf(formatCurrencyInput(rawMaxPrice))
    }
    var quantity by remember {
        mutableStateOf("1")
    }
    var isSubmitting by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(rawBasePrice, useAutoMaxPrice) {
        if (useAutoMaxPrice) {
            rawBasePrice.toLongOrNull()?.let {
                rawMaxPrice = (it * 3).toString()
                maxPriceFormatted = formatCurrencyInput(rawMaxPrice)
            }
        }
    }

    BasicDialog(
        title = if (isEditMode) "Edit Barang" else "Tambah Barang",
        onDismiss = onDismiss,
        maxWidth = DesktopDimens.DialogSmallWidth,
        actions = {
            OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Batal")
            }
            Spacer(Modifier.weight(1f))
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
            }
            Button(
                onClick = {
                    val base = rawBasePrice.toLongOrNull()
                    val max = rawMaxPrice.toLongOrNull()
                    val qty = quantity.toIntOrNull() ?: 1

                    if (base != null && max != null && name.isNotBlank() && code.isNotBlank()) {
                        isSubmitting = true
                        scope.launch {
                            try {
                                if (isEditMode) {
                                    itemsViewModel.patchItem(
                                        id = itemToEdit!!.id!!,
                                        item = itemToEdit.copy(
                                            nameItem = name.trim(),
                                            codeItem = code.trim(),
                                            basePrice = base.toString(),
                                            maxPrice = max.toString()
                                        )
                                    )
                                } else {
                                    val baseName = name.trim()
                                    val baseCode = code.trim()
                                    for (i in 1..qty) {
                                        val finalName = if (qty > 1) "$baseName $i" else baseName
                                        val finalCode = if (qty > 1) "$baseCode-$i" else baseCode
                                        itemsViewModel.createItem(
                                            ItemResponse(
                                                nameItem = finalName,
                                                codeItem = finalCode,
                                                basePrice = base.toString(),
                                                maxPrice = max.toString(),
                                                status = 0
                                            )
                                        )
                                    }
                                }
                                onDismiss()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                },
                enabled = !isSubmitting && name.isNotBlank() && code.isNotBlank() && rawBasePrice.isNotBlank() && rawMaxPrice.isNotBlank()
            ) {
                Text("Simpan")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { input ->
                    name = input.lowercase().split(" ")
                        .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                },
                label = { Text("Nama Barang") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting
            )

            OutlinedTextField(
                value = code,
                onValueChange = { input ->
                    code = input.uppercase().trimStart().replace(Regex("\\s+"), "-")
                },
                label = { Text("Kode Barang") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting
            )

            OutlinedTextField(
                value = basePriceFormatted,
                onValueChange = { input ->
                    rawBasePrice = input.filter { it.isDigit() }
                    basePriceFormatted = formatCurrencyInput(rawBasePrice)
                },
                label = { Text("Harga Dasar") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Harga Atas Otomatis (x3)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = useAutoMaxPrice,
                    onCheckedChange = { useAutoMaxPrice = it },
                    enabled = !isSubmitting
                )
            }

            OutlinedTextField(
                value = maxPriceFormatted,
                onValueChange = { input ->
                    if (!useAutoMaxPrice) {
                        rawMaxPrice = input.filter { it.isDigit() }
                        maxPriceFormatted = formatCurrencyInput(rawMaxPrice)
                    }
                },
                label = { Text("Harga Maksimal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !useAutoMaxPrice && !isSubmitting
            )

            if (!isEditMode) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { it.isDigit() } },
                    label = { Text("Jumlah Barang") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting
                )
            }
        }
    }
}

@Composable
private fun ItemDetailDialog(
    item: ItemResponse,
    onDismiss: () -> Unit
) {
    BasicDialog(
        title = "Detail Barang",
        onDismiss = onDismiss,
        maxWidth = DesktopDimens.DialogSmallWidth
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailRow("Kode Barang", item.codeItem.orEmpty())
            DetailRow("Nama Barang", item.nameItem.orEmpty())
            DetailRow("Status", "") {
                StatusBadge(
                    text = when (item.status) {
                        0 -> "Tersedia"
                        1 -> "Lelang"
                        2 -> "Dibayar"
                        3 -> "Diambil"
                        else -> "-"
                    },
                    tone = when (item.status) {
                        0 -> StatusTone.Ready
                        1 -> StatusTone.Warning
                        2 -> StatusTone.Paid
                        3 -> StatusTone.Complete
                        else -> StatusTone.Neutral
                    }
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow("Harga Dasar", formatRupiah(item.basePrice))
            DetailRow("Harga Maks", formatRupiah(item.maxPrice))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow("Pemenang", item.buyer.orEmpty())
            DetailRow("Harga Lelang", formatRupiah(item.price))
            DetailRow("Order ID", item.orderID.orEmpty())
            DetailRow("Metode Bayar", item.typePayment.orEmpty())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow("Dibuat", item.created.orEmpty())
            DetailRow("Diperbarui", item.updated.orEmpty())
        }
    }
}

@Composable
private fun TransactionDetailDialog(
    orderId: String,
    items: List<ItemResponse>,
    onDismiss: () -> Unit
) {
    val representative = items.first()
    val totalAmount = items.sumOf { it.price?.toLongOrNull() ?: 0L }

    BasicDialog(
        title = "Detail Transaksi",
        onDismiss = onDismiss,
        maxWidth = DesktopDimens.DialogMediumWidth
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Order ID", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(orderId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                StatusBadge(
                    text = if (items.all { it.status == 3 }) "Selesai" else "Proses",
                    tone = if (items.all { it.status == 3 }) StatusTone.Complete else StatusTone.Warning
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pemenang", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(representative.buyer.orEmpty(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Metode Bayar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(representative.typePayment.orEmpty().ifBlank { "-" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }

            Text("Daftar Barang:", style = MaterialTheme.typography.titleSmall)

            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.nameItem.orEmpty(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(item.codeItem.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(formatRupiah(item.price), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = formatRupiah(totalAmount.toString()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    content: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (content != null) {
            content()
        } else {
            Text(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    itemIds: List<String>,
    itemsViewModel: ItemsViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    BasicDialog(
        title = "Konfirmasi Hapus",
        onDismiss = onDismiss,
        maxWidth = 420.dp,
        actions = {
            OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Batal")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            itemIds.forEach { id ->
                                itemsViewModel.deleteItem(id)
                            }
                            onDismiss()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                } else {
                    Text("Hapus (${itemIds.size})")
                }
            }
        }
    ) {
        Text(
            text = "Apakah Anda yakin ingin menghapus ${itemIds.size} barang terpilih? Tindakan ini tidak dapat dibatalkan.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun LoginDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val handleLogin = {
        errorMessage = null
        authViewModel.login(
            onSuccess = onDismiss,
            onError = { message -> errorMessage = message }
        )
    }

    BasicDialog(
        title = "Login",
        onDismiss = onDismiss,
        actions = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Batal")
            }
            Spacer(Modifier.weight(1f))
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(22.dp),
                    strokeWidth = 2.dp
                )
            }
            Button(
                onClick = handleLogin,
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Login")
            }
        }
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = authViewModel::onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent {
                    if (it.key == Key.Enter && email.isNotBlank() && password.isNotBlank()) {
                        handleLogin()
                        true
                    } else false
                }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = authViewModel::onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent {
                    if (it.key == Key.Enter && email.isNotBlank() && password.isNotBlank()) {
                        handleLogin()
                        true
                    } else false
                }
        )
        errorMessage?.let { message ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ProfileDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val email by authViewModel.email.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        BasicDialog(
            title = "Konfirmasi Logout",
            onDismiss = { showLogoutConfirm = false },
            actions = {
                OutlinedButton(onClick = { showLogoutConfirm = false }) {
                    Text("Batal")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        authViewModel.logout()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Ya, Logout")
                }
            }
        ) {
            Text(
                text = "Apakah Anda yakin ingin keluar dari akun ini?",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        BasicDialog(
            title = "Profil",
            onDismiss = onDismiss,
            actions = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Tutup")
                }
                if (isLoggedIn) {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { showLogoutConfirm = true },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Logout")
                    }
                }
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userName?.firstOrNull()?.uppercase()?.toString() ?: "U",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(
                        text = userName?.takeIf { it.isNotBlank() } ?: "Pengguna",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = email.ifBlank { "-" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicDialog(
    title: String,
    onDismiss: () -> Unit,
    maxWidth: androidx.compose.ui.unit.Dp = DesktopDimens.DialogSmallWidth,
    actions: @Composable RowScope.() -> Unit = {
        OutlinedButton(onClick = onDismiss) {
            Text("Tutup")
        }
    },
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 480.dp, max = maxWidth)
                .heightIn(max = DesktopDimens.DialogMaxHeight)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* block click-through */ },
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 20.dp, end = 16.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup dialog"
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    content()
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}
