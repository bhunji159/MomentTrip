package com.example.momenttrip.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.momenttrip.data.SchedulePlan

@Composable
fun ScheduleList(
    schedulesForDate: List<SchedulePlan>,
    firstUpcomingTime: SchedulePlan?,  // 현재 시간 이후 첫 일정
    onDetailClick: (SchedulePlan) -> Unit = {}
) {
    if (schedulesForDate.isEmpty()) {
        Text(
            text = "등록된 일정이 없습니다.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(schedulesForDate) { plan ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (plan == firstUpcomingTime)
                        androidx.compose.material3.CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    else
                        androidx.compose.material3.CardDefaults.elevatedCardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 시간 텍스트 좌측 정렬
                        Text(
                            text = "${plan.start_time} ~ ${plan.end_time}",
                            style = MaterialTheme.typography.labelSmall
                        )

                        // 제목은 가용 공간 모두 차지
                        Text(
                            text = plan.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        // 상세 텍스트 우측 정렬 + 클릭 가능
                        Text(
                            text = "상세",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .clickable { onDetailClick(plan) }
                                .padding(all = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

