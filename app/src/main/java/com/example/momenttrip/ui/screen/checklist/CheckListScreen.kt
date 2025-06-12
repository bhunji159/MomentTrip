package com.example.momenttrip.ui.screen

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.viewmodel.CheckListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    tripId: String,
    onBack: () -> Unit,
    viewModel: CheckListViewModel = viewModel()
) {
    val trip by viewModel.trip.collectAsState()
    val checklist by viewModel.items.collectAsState()
    var newItem by remember { mutableStateOf("") } //체크리스트 항목 추가
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    LaunchedEffect(tripId) {
        viewModel.loadTrip(tripId)
        viewModel.loadChecklist(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.title ?: "체크리스트") },
                navigationIcon = {
                    IconButton(onClick = {
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (trip != null) {
                Text("여행 국가: ${trip!!.countries.joinToString()}")
                Spacer(modifier = Modifier.height(16.dp))
                //항목 추가
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newItem,
                        onValueChange = { newItem = it },
                        label = { Text("새 항목") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (newItem.isNotBlank()) {
                            viewModel.addItem(tripId, newItem)
                            newItem = ""
                        }
                    }) {
                        Text("추가")
                    }
                }
                LazyColumn {
                    items(checklist) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = {
                                        viewModel.toggleItem(tripId, item)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item.content)
                            }
                            IconButton(onClick = { //삭제 아이콘
                                viewModel.deleteItem(tripId, item.id)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "삭제"
                                )
                            }
                        }
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

