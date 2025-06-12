package com.example.momenttrip.ui_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.viewmodel.NotificationItem
import com.example.momenttrip.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

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
