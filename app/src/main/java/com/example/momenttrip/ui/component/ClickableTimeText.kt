import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ClickableTimeText(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val context = LocalContext.current

        DisposableEffect(Unit) {
            val dialog = TimePickerDialog(
                context,
                { _: TimePicker, hour: Int, minute: Int ->
                    onTimeSelected(LocalTime.of(hour, minute))
                    showDialog = false
                },
                selectedTime.hour,
                selectedTime.minute,
                false // 12시간제
            )
            dialog.show()
            onDispose { dialog.dismiss() }
        }
    }

    val formatted = selectedTime.format(DateTimeFormatter.ofPattern("a hh:mm"))

    Text(
        text = formatted,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 8.dp)
    )
}
