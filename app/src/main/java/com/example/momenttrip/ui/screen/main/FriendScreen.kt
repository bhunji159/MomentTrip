package com.example.momenttrip.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.viewmodel.FriendViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)   // SwipeToDismiss 는 experimental
@Composable
fun FriendScreen() {

    /* ───── 상태 / 뷰모델 ───── */
    val vm: FriendViewModel = viewModel()
    val friends       by vm.friendList.collectAsState()
    val searchedUser  by vm.searchedUser.collectAsState()
    val snackbarState = remember { SnackbarHostState() }
    val scope         = rememberCoroutineScope()

    /* 첫 진입 시 로드 */
    LaunchedEffect(Unit) { vm.loadFriendList() }

    var searchQuery   by remember { mutableStateOf("") }
    var showResultCard by remember { mutableStateOf(false) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarState) }) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            /* ─── 검색창 ─── */
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("이메일 / 전화번호 / 닉네임") },
                trailingIcon = {
                    IconButton(onClick = {
                        vm.searchUser(searchQuery) { ok, msg ->
                            showResultCard = ok
                            if (!ok) scope.launch {
                                snackbarState.showSnackbar(msg ?: "검색 실패")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            /* ─── 검색 결과 카드 ─── */
            if (showResultCard && searchedUser != null) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(searchedUser!!.nickname.ifBlank { searchedUser!!.email })
                        Spacer(Modifier.weight(1f))
                        Button(onClick = {
                            vm.sendFriendRequest(searchedUser!!.uid) { ok, msg ->
                                scope.launch {
                                    snackbarState.showSnackbar(
                                        if (ok) "친구 요청을 보냈습니다" else msg ?: "요청 실패"
                                    )
                                }
                                if (ok) {
                                    showResultCard = false
                                    searchQuery = ""
                                }
                            }
                        }) { Text("추가") }
                    }
                }
            }

            /* ─── 친구 목록 ─── */
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    friends.sortedBy { it.nickname.ifBlank { it.name } },
                    key = { it.uid }
                ) { user ->

                    /* ---------- 한 줄 카드 ---------- */
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        ListItem(
                            headlineContent   = { Text(user.nickname.ifBlank { user.name }) },
                            supportingContent = { Text(user.email) },

                            /* 오른쪽에 삭제 아이콘 */
                            trailingContent = {
                                IconButton(onClick = {
                                    /* 1차 launch 는 삭제 호출용 */
                                    scope.launch {
                                        vm.removeFriend(user.uid) { ok, _ ->
                                            /* ✅ showSnackbar 는 별도 launch 로 싸준다 */
                                            if (ok) scope.launch { snackbarState.showSnackbar("삭제되었습니다") }
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "delete")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}