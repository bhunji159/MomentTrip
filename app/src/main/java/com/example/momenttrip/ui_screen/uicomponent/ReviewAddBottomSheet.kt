package com.example.momenttrip.ui_screen.uicomponent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewAddBottomSheet(
    date: LocalDate,
    onDismiss: () -> Unit,
    onSubmit: (title: String, content: String,
               imageUrls: List<String>, thumbnailUrl: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var pickedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val pickerLauncher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { pickedImages = pickedImages + it }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text("여행 일지 작성 (${date})", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(pickedImages) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { pickerLauncher.launch("image/*") }) {
                    Text("이미지 추가")
                }
                Button(
                    enabled = title.isNotBlank(),
                    onClick = {
                        // 🔽 Firebase Storage 업로드 & Firestore 등록
                        scope.launch {
                            val storage = Firebase.storage
                            val urls = pickedImages.map { uri ->
                                val path = "images/reviews/${System.currentTimeMillis()}_${uri.lastPathSegment}"
                                val ref = storage.reference.child(path)
                                ref.putFile(uri).await()             // kotlinx-coroutines-play-services 필요
                                ref.downloadUrl.await().toString()
                            }
                            val thumbnail = urls.firstOrNull()
                            onSubmit(title, content, urls, thumbnail)
                        }
                    }
                ) { Text("확인") }
            }
        }
    }
}