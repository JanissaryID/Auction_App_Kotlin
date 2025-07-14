package com.polytron.auctionapp.view.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.model.ItemResponse
import com.polytron.auctionapp.utils.formatCurrencyInput
import kotlinx.coroutines.launch

@Composable
fun AddOrEditItemBottomSheet(
    itemToEdit: ItemResponse? = null,
    onDismiss: () -> Unit,
    onSubmit: suspend (name: String, code: String, basePrice: String, maxPrice: String, jumlah: Int) -> Unit
) {
    val isEditMode = itemToEdit != null
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(itemToEdit?.nameItem?.trim() ?: "") }
    var code by remember { mutableStateOf(itemToEdit?.codeItem?.trim() ?: "") }

    var rawBasePrice by remember { mutableStateOf(itemToEdit?.basePrice ?: "") }
    var basePrice by remember { mutableStateOf(formatCurrencyInput(rawBasePrice)) }

    var rawMaxPrice by remember { mutableStateOf(itemToEdit?.maxPrice ?: "") }
    var maxPrice by remember { mutableStateOf(formatCurrencyInput(rawMaxPrice)) }

    var useAutoMaxPrice by remember { mutableStateOf(itemToEdit == null) }
    var quantity by remember { mutableStateOf("1") }

    var isSubmitting by remember { mutableStateOf(false) }

    // Auto kalkulasi maxPrice jika aktif
    LaunchedEffect(rawBasePrice, useAutoMaxPrice) {
        if (useAutoMaxPrice) {
            rawBasePrice.toIntOrNull()?.let {
                rawMaxPrice = (it * 3).toString()
                maxPrice = formatCurrencyInput(rawMaxPrice)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp )
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            if (isEditMode) "Edit Barang" else "Tambah Barang",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it.lowercase().split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { c -> c.uppercaseChar() } }
            },
            label = { Text("Nama Barang") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = code,
            onValueChange = {
                code = it.uppercase()
                    .trimStart()
                    .replace(Regex("\\s+"), "-") // ganti semua spasi jadi "-"
            },
            label = { Text("Kode Barang") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = basePrice,
            onValueChange = {
                rawBasePrice = it.filter { c -> c.isDigit() }
                basePrice = formatCurrencyInput(rawBasePrice)
            },
            label = { Text("Harga Dasar") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        )

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hitung Harga Atas Otomatis (x3)", modifier = Modifier.weight(1f))
            Switch(
                checked = useAutoMaxPrice,
                onCheckedChange = { useAutoMaxPrice = it },
                enabled = !isSubmitting
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = maxPrice,
            onValueChange = {
                if (!useAutoMaxPrice) {
                    rawMaxPrice = it.filter { c -> c.isDigit() }
                    maxPrice = formatCurrencyInput(rawMaxPrice)
                }
            },
            label = { Text("Harga Maksimal") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !useAutoMaxPrice && !isSubmitting
        )

        if (!isEditMode) {
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                label = { Text("Jumlah Barang") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Batal")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    val cleanBase = rawBasePrice.toIntOrNull()
                    val cleanMax = rawMaxPrice.toIntOrNull()
                    val jumlah = quantity.toIntOrNull() ?: 1

                    if (cleanBase == null || cleanMax == null || name.isBlank() || code.isBlank()) return@Button

                    isSubmitting = true
                    coroutineScope.launch {
                        try {
                            onSubmit(
                                name.trim(),
                                code.trim(),
                                cleanBase.toString(),
                                cleanMax.toString(),
                                jumlah
                            )
                            onDismiss()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = name.isNotBlank() &&
                        code.isNotBlank() &&
                        rawBasePrice.isNotBlank() &&
                        rawMaxPrice.isNotBlank() &&
                        (isEditMode || quantity.isNotBlank()) &&
                        !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Simpan")
                }
            }
        }
    }
}
