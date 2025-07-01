package com.polytron.auctionapp.view.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.polytron.auctionapp.utils.formatCurrencyInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddItemBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: suspend (name: String, code: String, basePrice: String, maxPrice: String, jumlah: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var basePrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var useAutoMaxPrice by remember { mutableStateOf(true) }
    var quantity by remember { mutableStateOf("1") }

    var isSubmitting by remember { mutableStateOf(false) }

    // Auto-calculate max price
    LaunchedEffect(basePrice, useAutoMaxPrice) {
        if (useAutoMaxPrice) {
            val base = basePrice.replace(".", "").toIntOrNull()
            if (base != null) {
                maxPrice = formatCurrencyInput((base * 3).toString())
            } else {
                maxPrice = ""
            }
        }
    }

    Surface(
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Tambah Item", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(16.dp))

            // Nama Barang
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

            // Kode Barang
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase() },
                label = { Text("Kode Barang") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            )

            Spacer(Modifier.height(12.dp))

            // Harga Dasar
            OutlinedTextField(
                value = basePrice,
                onValueChange = { basePrice = formatCurrencyInput(it) },
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

            // Harga Maksimal
            OutlinedTextField(
                value = maxPrice,
                onValueChange = {
                    if (!useAutoMaxPrice) maxPrice = formatCurrencyInput(it)
                },
                label = { Text("Harga Maksimal") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !useAutoMaxPrice && !isSubmitting
            )

            Spacer(Modifier.height(24.dp))

            // Jumlah Barang
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                label = { Text("Jumlah Barang") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting
            )

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
                        val cleanBase = basePrice.replace(".", "")
                        val cleanMax = maxPrice.replace(".", "")
                        val jumlah = quantity.toIntOrNull() ?: 1

                        isSubmitting = true

                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                onSubmit(
                                    name.trim(),
                                    code.trim(),
                                    cleanBase,
                                    cleanMax,
                                    jumlah
                                )
                            } finally {
                                isSubmitting = false
                                onDismiss()
                            }
                        }
                    },
                    enabled = name.isNotBlank() &&
                            code.isNotBlank() &&
                            basePrice.isNotBlank() &&
                            maxPrice.isNotBlank() &&
                            quantity.isNotBlank() &&
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
}



