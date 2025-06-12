package com.example.momenttrip.ui.screen.main

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.momenttrip.data.ReviewEntry
import com.example.momenttrip.ui_screen.uicomponent.ReviewAddBottomSheet
import com.example.momenttrip.viewmodel.ReviewViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    tripId: String,
    date: LocalDate
) {
    val vm: ReviewViewModel = viewModel()
    val reviews by vm.reviews.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tripId) { vm.loadReviews(tripId) }

    var showAddSheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<ReviewEntry?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = reviews.filter { review ->
                        review.date.toDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate() == date
                    },
                    key = { it.review_id!! }
                ) { entry ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(entry.content, style = MaterialTheme.typography.bodyMedium)
                            if (entry.thumbnail_url != null) {
                                Spacer(Modifier.height(8.dp))
                                AsyncImage(
                                    model = entry.thumbnail_url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                vm.deleteReview(entry.review_id!!, tripId) { ok, msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (ok) "삭제되었습니다" else msg ?: "삭제 실패"
                                        )
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "delete")
                            }
                            IconButton(onClick = {
                                editingEntry = entry
                                editTitle = entry.title
                                editContent = entry.content
                                showEditDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "edit")
                            }
                        }
                    }
                }
            } // end LazyColumn

            FloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }

            if (showAddSheet) {
                ReviewAddBottomSheet(
                    date = date,
                    onDismiss = { showAddSheet = false },
                    onSubmit = { title, content, imgs, thumb ->
                        vm.addReview(
                            writerUid = FirebaseAuth.getInstance().currentUser!!.uid,
                            tripId = tripId,
                            date = date,
                            title = title,
                            content = content,
                            thumbnailUrl = thumb,
                            imageUrls = imgs
                        ) { ok, msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (ok) "추가되었습니다" else msg ?: "추가 실패"
                                )
                            }
                            showAddSheet = false
                        }
                    }
                )
            }

            if (showEditDialog && editingEntry != null) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("리뷰 수정") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("제목") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editContent,
                                onValueChange = { editContent = it },
                                label = { Text("내용") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val entry = editingEntry!!
                            vm.updateReview(
                                reviewId = entry.review_id!!,
                                tripId = tripId,
                                newTitle = editTitle,
                                newContent = editContent,
                                newThumbnailUrl = entry.thumbnail_url,
                                newImageUrls = entry.image_urls
                            ) { ok, msg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (ok) "수정되었습니다" else msg ?: "수정 실패"
                                    )
                                }
                                showEditDialog = false
                            }
                        }) {
                            Text("확인")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("취소")
                        }
                    }
                )
            }
        }
    }
}
