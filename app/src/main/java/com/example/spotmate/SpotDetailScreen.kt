package com.example.spotmate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────────────────── 색상 (PPT 시안 기준) ─────────────────────────
private val SpotGreen = Color(0xFF3F6B12)       // 메인 진한 초록 (버튼)
private val SpotGreenLight = Color(0xFFEAF2DC)  // 태그 칩 배경
private val SpotYellow = Color(0xFFF7C910)      // 남은 시간 칩
private val SpotMap = Color(0xFFCBD5C0)         // 지도 자리 배경
private val SpotGray = Color(0xFF8A8A8A)

// ───────────────────────── 데이터 ─────────────────────────

/** 스팟 상세에 필요한 데이터 (나중에 Firestore 스팟 문서에서 채움) */
data class SpotDetail(
    val spotId: String,
    val authorUid: String,        // 반응 대상 사용자 식별용
    val authorName: String,
    val placeName: String,
    val distanceMeters: Int,
    val expiresAtMillis: Long,    // 작성 시간 + 6시간
    val isVideo: Boolean,         // true면 3초 영상, false면 사진
    val comment: String,          // 한마디 (최대 150자)
    val mood: Mood,
    val tags: List<String>,
)

/** 기분 5종 */
enum class Mood(val emoji: String, val label: String) {
    GREAT("😆", "최고야"),
    GOOD("☺", "기분 좋음"),
    SOSO("😐", "그냥 그래"),
    BAD("😞", "별로야"),
    TIRED("😪", "피곤해"),
}

/** 반응 3종 — firestoreValue는 Firestore의 type 필드 값 */
enum class ReactionType(val emoji: String, val label: String, val firestoreValue: String) {
    GO("🏃", "나도 갈래!", "go"),
    POKE("👉", "찌르기", "poke"),
    CURIOUS("❓", "여기 궁금해", "curious"),
}

/** 테스트용 더미 데이터 (PPT 시안 내용 그대로) */
fun dummySpot() = SpotDetail(
    spotId = "dummy_spot_1",
    authorUid = "uid_seeun",
    authorName = "seeun",
    placeName = "숭실대학교",
    distanceMeters = 320,
    expiresAtMillis = System.currentTimeMillis() + (5 * 60 + 12) * 60 * 1000L,
    isVideo = true,
    comment = "공강 시간에 카공 중. 콘센트 자리 아직 남아 있어요!",
    mood = Mood.GOOD,
    tags = listOf("카공하기좋은"),
)

// ───────────────────────── 화면 ─────────────────────────

/**
 * 스팟 상세 화면
 * @param onBack 뒤로가기 (Navigation 연결 시 사용)
 * @param onReactionChanged 반응을 누르거나 취소했을 때 호출 → 여기서 Firestore 저장/삭제 연결 예정
 */
@Composable
fun SpotDetailScreen(
    spot: SpotDetail = dummySpot(),
    onBack: () -> Unit = {},
    onReactionChanged: (type: ReactionType, selected: Boolean) -> Unit = { _, _ -> },
) {
    // 내가 누른 반응 (같은 버튼을 다시 누르면 취소)
    var myReactions by remember { mutableStateOf(setOf<ReactionType>()) }

    fun toggle(type: ReactionType) {
        val nowSelected = type !in myReactions
        myReactions = if (nowSelected) myReactions + type else myReactions - type
        onReactionChanged(type, nowSelected)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // 1. 상단 지도 영역 (지도 담당 팀원의 Google Map으로 교체 예정)
        MapHeader(onBack = onBack)

        // 2. 아래에서 올라온 시트
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // 드래그 손잡이
                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SpotGreenLight)
                    )
                }
                Spacer(Modifier.height(16.dp))

                AuthorRow(spot)
                Spacer(Modifier.height(16.dp))

                MediaBox(isVideo = spot.isVideo)
                Spacer(Modifier.height(16.dp))

                // 한마디
                Text(spot.comment, fontSize = 15.sp, color = Color(0xFF222222))
                Spacer(Modifier.height(12.dp))

                // 태그 + 기분
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    spot.tags.forEach { GreenChip("#$it") }
                    GreenChip("${spot.mood.emoji} ${spot.mood.label}")
                }
                Spacer(Modifier.height(24.dp))
            }

            // 3. 반응 버튼 (하단 고정)
            ReactionButtons(
                selected = myReactions,
                onClick = { toggle(it) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun MapHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(SpotMap)
    ) {
        // 뒤로가기 버튼
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("‹", fontSize = 26.sp, color = Color.Black)
        }
        // 스팟 버블 핀
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-10).dp)
                .size(56.dp)
                .border(3.dp, SpotGreen, CircleShape)
                .padding(3.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFF5D35A), Color(0xFFD9A520))))
        )
    }
}

@Composable
private fun AuthorRow(spot: SpotDetail) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 프로필 이미지 자리
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFF5D35A), Color(0xFFD9A520))))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(spot.authorName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "${spot.placeName} · ${formatDistance(spot.distanceMeters)}",
                fontSize = 13.sp,
                color = SpotGray
            )
        }
        // 남은 시간 칩
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(SpotYellow)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                "⏱ ${formatRemaining(spot.expiresAtMillis)} 남음",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3A2E00)
            )
        }
    }
}

/** 사진 / 3초 영상 영역 — 나중에 Coil(사진), ExoPlayer(영상)로 교체 */
@Composable
private fun MediaBox(isVideo: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF6B5B4B)),
        contentAlignment = Alignment.Center
    ) {
        if (isVideo) {
            // "3초 영상" 배지
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x99000000))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("3초 영상", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            // 재생 버튼
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", color = Color.White, fontSize = 20.sp)
            }
            // 재생 진행 바
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x88FFFFFF))
            )
        } else {
            Text("📷 사진", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
private fun GreenChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SpotGreenLight)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SpotGreen)
    }
}

@Composable
private fun ReactionButtons(
    selected: Set<ReactionType>,
    onClick: (ReactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 나도 갈래! (큰 버튼)
        val goSelected = ReactionType.GO in selected
        Button(
            onClick = { onClick(ReactionType.GO) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (goSelected) SpotGreenLight else SpotGreen,
                contentColor = if (goSelected) SpotGreen else Color.White
            ),
            border = if (goSelected) BorderStroke(1.5.dp, SpotGreen) else null
        ) {
            Text(
                if (goSelected) "✓ 나도 갈래! 보냈어요" else "${ReactionType.GO.emoji} ${ReactionType.GO.label}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // 찌르기 · 여기 궁금해
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(ReactionType.POKE, ReactionType.CURIOUS).forEach { type ->
                val isOn = type in selected
                OutlinedButton(
                    onClick = { onClick(type) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, SpotGreen),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isOn) SpotGreen else Color.White,
                        contentColor = if (isOn) Color.White else SpotGreen
                    )
                ) {
                    Text(
                        if (isOn) "✓ ${type.label}" else "${type.emoji} ${type.label}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ───────────────────────── 유틸 ─────────────────────────

private fun formatDistance(meters: Int): String =
    if (meters < 1000) "${meters}m" else String.format("%.1fkm", meters / 1000.0)

/** 만료 시각까지 남은 시간 → "5시간 12분" */
private fun formatRemaining(expiresAtMillis: Long): String {
    val totalMin = ((expiresAtMillis - System.currentTimeMillis()) / 60_000).coerceAtLeast(0)
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "${h}시간 ${m}분" else "${m}분"
}

// ───────────────────────── 미리보기 ─────────────────────────

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun SpotDetailScreenPreview() {
    SpotDetailScreen()
}