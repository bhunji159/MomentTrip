package com.example.momenttrip.ui_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.viewmodel.NotificationItem
import com.example.momenttrip.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    // 이제 DI 없이도 viewModel() 호출
    val vm: NotificationViewModel = viewModel()
    val list by vm.notifications.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val fmt = remember { SimpleDateFormat("MM.dd  HH:mm", Locale.getDefault()) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (list.isEmpty()) {
                Text(
                    "새 알림이 없습니다",
                    Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(list, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    when (item) {
                                        is NotificationItem.FriendReq  ->
                                            "${item.fromName}님의 친구 추가 요청"
                                        is NotificationItem.TripInvite ->
                                            "${item.fromName}님의 여행 공유 요청"
                                    }
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    fmt.format(item.createdAt.toDate()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        vm.accept(item)
                                        scope.launch { snackbarHost.showSnackbar("수락 완료") }
                                    }) { Text("수락") }
                                    TextButton(onClick = {
                                        vm.reject(item)
                                        scope.launch { snackbarHost.showSnackbar("거절 완료") }
                                    }) { Text("거절") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
