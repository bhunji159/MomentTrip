package com.example.momenttrip.data

import java.time.LocalDate

data class DayInfo(
    val date: LocalDate,
    val isSelected: Boolean = false
)
