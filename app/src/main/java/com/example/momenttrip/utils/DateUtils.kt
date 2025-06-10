package com.example.momenttrip.utils

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.util.Date


fun getWeekDates(baseDate: LocalDate): List<LocalDate> {
    val startOfWeek = baseDate.with(ChronoField.DAY_OF_WEEK, 1) // 월요일 기준
    return (0..6).map { startOfWeek.plusDays(it.toLong()) }
}

fun Date.toLocalDate(): LocalDate {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}