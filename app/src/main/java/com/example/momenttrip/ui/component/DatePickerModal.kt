package com.example.momenttrip.ui.component

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.util.*

@Composable
fun DatePickerModal(
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val today = Calendar.getInstance()

    DisposableEffect(Unit) {
        val dialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selected = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(selected)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )

        // ✅ 범위 설정
        minDate?.let {
            dialog.datePicker.minDate = it.toEpochDay() * 24 * 60 * 60 * 1000
        }
        maxDate?.let {
            dialog.datePicker.maxDate = it.toEpochDay() * 24 * 60 * 60 * 1000
        }

        dialog.show()

        onDispose {
            dialog.dismiss()
        }
    }
}

