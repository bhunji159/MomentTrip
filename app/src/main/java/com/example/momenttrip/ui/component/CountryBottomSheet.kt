package com.example.momenttrip.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.viewmodel.CountryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryBottomSheet(
    onDismiss: () -> Unit,
    onCountriesSelected: (List<String>) -> Unit
) {
    val viewModel: CountryViewModel = viewModel()
    val countries by viewModel.countries.collectAsState()
    val searchText = remember { mutableStateOf("") }
    val selected = remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.fetchCountries()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp) // BottomSheet가 너무 커지지 않도록 제한
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = searchText.value,
                onValueChange = { searchText.value = it },
                label = { Text("나라 검색") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // 스크롤 영역
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn {
                    items(countries.filter {
                        it.contains(searchText.value, ignoreCase = true)
                    }) { country ->
                        val isSelected = selected.value.contains(country)
                        ListItem(
                            headlineContent = { Text(country) },
                            trailingContent = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "선택됨"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected.value = if (isSelected) {
                                        selected.value - country
                                    } else {
                                        selected.value + country
                                    }
                                }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onCountriesSelected(selected.value) },
                enabled = selected.value.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("선택 완료 (${selected.value.size})")
            }
        }
    }
}

