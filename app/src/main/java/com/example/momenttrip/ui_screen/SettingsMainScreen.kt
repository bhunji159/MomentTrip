import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.momenttrip.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(), onNavigateToLogin: () -> Unit) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("설정", style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = { isEditing = !isEditing; editedName = user?.nickname ?: "" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("계정 설정")
        }

        Button(
            onClick = {
                viewModel.logout()
                onNavigateToLogin()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그아웃")
        }

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://your.terms.url"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("이용약관 및 개인정보처리방침")
        }

        if (isEditing) {
            Divider()

            Text("프로필 수정", style = MaterialTheme.typography.titleMedium)

            user?.profile_url?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            }

            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("닉네임") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.updateUserName(editedName) { success, error ->
                        if (success) isEditing = false
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("저장")
            }
        }
    }
}
