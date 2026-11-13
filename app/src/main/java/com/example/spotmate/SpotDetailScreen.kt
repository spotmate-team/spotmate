package com.example.spotmate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private val SpotGreen = Color(0xFF3F6B12)
private val SpotGreenLight = Color(0xFFEAF2DC)
private val SpotYellow = Color(0xFFF7C910)
private val SpotMap = Color(0xFFCBD5C0)
private val SpotGray = Color(0xFF8A8A8A)

const val SPOT_LIFETIME_MILLIS = 6 * 60 * 60 * 1000L // 6시간
const val VIDEO_MAX_MILLIS = 3_000 // 3초

data class SpotDetail(
    val spotId: String,
    val authorUid: String,
    val authorName: String,
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val createdAtMillis: Long,
    val mediaType: MediaType,
    val comment: String, // 150자 이내
    val mood: Mood,
    val tags: List<String>,
) {
    val expiresAtMillis: Long get() = createdAtMillis + SPOT_LIFETIME_MILLIS
}

enum class MediaType { PHOTO, VIDEO }

enum class Mood(val emoji: String, val label: String) {
    GREAT("😆", "최고야"),
    GOOD("☺", "기분 좋음"),
    SOSO("😐", "그저 그래"),
    BAD("😣", "별로야"),
    TIRED("😪", "피곤해"),
}

enum class ReactionType(
    val emoji: String,
    val label: String,
    val firestoreValue: String,
    val sentMessage: String,
) {
    GO("🏃", "나도 갈래!", "go", "님에게 나도 갈래!를 보냈어요"),
    POKE("👉", "찌르기", "poke", "님을 찔렀어요"),
    CURIOUS("❓", "여기 궁금해", "curious", "님에게 여기 궁금해를 보냈어요"),
}

// TODO 현재 위치 받아오기 전까지 임시값 (숭실대 정문)
const val DUMMY_MY_LAT = 37.4963
const val DUMMY_MY_LNG = 126.9572

// 테스트용
fun dummySpot(
    authorUid: String = "uid_seeun",
    minutesAgo: Int = 48,
) = SpotDetail(
    spotId = "dummy_spot_1",
    authorUid = authorUid,
    authorName = "seeun",
    placeName = "숭실대학교",
    latitude = 37.4991,
    longitude = 126.9583,
    createdAtMillis = System.currentTimeMillis() - minutesAgo * 60 * 1000L,
    mediaType = MediaType.VIDEO,
    comment = "공강 시간에 카공 중. 콘센트 자리 아직 남아 있어요!",
    mood = Mood.GOOD,
    tags = listOf("카공하기좋은"),
)

@Composable
fun SpotDetailScreen(
    spot: SpotDetail = dummySpot(),
    myUid: String = "uid_me", // TODO 로그인 붙으면 uid로
    myLat: Double = DUMMY_MY_LAT,
    myLng: Double = DUMMY_MY_LNG,
    repository: ReactionRepository = remember { FakeReactionRepository() },
    onBack: () -> Unit = {},
    onReactionChanged: (type: ReactionType, selected: Boolean) -> Unit = { _, _ -> },
) {
    // 남은 시간 갱신용
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }

    val remainingMillis = (spot.expiresAtMillis - now).coerceAtLeast(0)
    val isExpired = remainingMillis == 0L
    val isMine = spot.authorUid == myUid
    val distance = distanceMeters(myLat, myLng, spot.latitude, spot.longitude)

    var myReactions by remember { mutableStateOf(repository.myReactions(spot.spotId, myUid)) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // 시트 드래그
    var sheetDragY by remember { mutableFloatStateOf(0f) }

    fun toggle(type: ReactionType) {
        val nowSelected = repository.toggle(spot.spotId, myUid, spot.authorUid, type)
        myReactions = repository.myReactions(spot.spotId, myUid)
        toastMessage = if (nowSelected) "${spot.authorName}${type.sentMessage}" else "${type.label} 반응을 취소했어요"
        onReactionChanged(type, nowSelected)
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2_000)
            toastMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // TODO 지도 붙이기
        MapHeader(mood = spot.mood, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp)
                .offset { IntOffset(0, sheetDragY.roundToInt()) }
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // 손잡이 내리면 닫힘
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (sheetDragY > 250f) onBack()
                                    sheetDragY = 0f
                                },
                                onDragCancel = { sheetDragY = 0f },
                            ) { _, dragAmount ->
                                sheetDragY = (sheetDragY + dragAmount).coerceAtLeast(0f)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SpotGreenLight)
                    )
                }
                Spacer(Modifier.height(4.dp))

                AuthorRow(spot = spot, distanceMeters = distance, remainingMillis = remainingMillis, now = now)
                Spacer(Modifier.height(16.dp))

                MediaBox(mediaType = spot.mediaType, autoPlay = !isExpired)
                Spacer(Modifier.height(16.dp))

                Text(spot.comment, fontSize = 15.sp, color = Color(0xFF222222))
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    spot.tags.forEach { GreenChip("#$it") }
                    GreenChip("${spot.mood.emoji} ${spot.mood.label}")
                }
                Spacer(Modifier.height(24.dp))
            }

            when {
                isExpired -> NoticeBar("6시간이 지나 지도에서 사라진 스팟이에요")
                isMine -> NoticeBar("내 스팟에는 반응할 수 없어요")
                else -> ReactionButtons(
                    selected = myReactions,
                    onClick = { toggle(it) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }

        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 140.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xDD222222))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(msg, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MapHeader(mood: Mood, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(SpotMap)
    ) {
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
        // 핀 + 기분
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-10).dp)
                .size(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .border(3.dp, SpotGreen, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFF5D35A), Color(0xFFD9A520))))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, SpotGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(mood.emoji, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AuthorRow(spot: SpotDetail, distanceMeters: Int, remainingMillis: Long, now: Long) {
    val isExpired = remainingMillis == 0L
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 프로필 (임시)
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
                "${spot.placeName} · ${formatDistance(distanceMeters)} · ${formatElapsed(now - spot.createdAtMillis)}",
                fontSize = 13.sp,
                color = SpotGray
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isExpired) Color(0xFFE0E0E0) else SpotYellow)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                if (isExpired) "만료됨" else "⏱ ${formatRemaining(remainingMillis)} 남음",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isExpired) SpotGray else Color(0xFF3A2E00)
            )
        }
    }
}

// TODO 사진은 Coil, 영상은 ExoPlayer로 바꾸기
@Composable
private fun MediaBox(mediaType: MediaType, autoPlay: Boolean = true) {
    val progress = remember { Animatable(0f) }
    var isPlaying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun play() {
        if (isPlaying) return
        scope.launch {
            isPlaying = true
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(VIDEO_MAX_MILLIS, easing = LinearEasing))
            isPlaying = false
        }
    }

    // 들어오면 한 번 자동재생
    LaunchedEffect(Unit) {
        if (autoPlay && mediaType == MediaType.VIDEO) play()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF6B5B4B))
            .clickable(enabled = mediaType == MediaType.VIDEO) { play() },
        contentAlignment = Alignment.Center
    ) {
        when (mediaType) {
            MediaType.VIDEO -> {
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
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000)),
                    contentAlignment = Alignment.Center
                ) {
                    val secLeft = ((1f - progress.value) * VIDEO_MAX_MILLIS / 1000f)
                    Text(
                        if (isPlaying) String.format("%.1f", secLeft) else "▶",
                        color = Color.White,
                        fontSize = if (isPlaying) 15.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // 진행바
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x55FFFFFF))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress.value)
                            .height(3.dp)
                            .background(Color.White)
                    )
                }
            }
            MediaType.PHOTO -> {
                Text("📷 사진", color = Color.White, fontSize = 18.sp)
            }
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
private fun NoticeBar(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF2F2F2))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = SpotGray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReactionButtons(
    selected: Set<ReactionType>,
    onClick: (ReactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

// 하버사인
fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Int {
    val r = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2) * sin(dLng / 2)
    return (r * 2 * atan2(sqrt(a), sqrt(1 - a))).toInt()
}

private fun formatDistance(meters: Int): String =
    if (meters < 1000) "${meters}m" else String.format("%.1fkm", meters / 1000.0)

private fun formatElapsed(elapsedMillis: Long): String {
    val min = elapsedMillis / 60_000
    return when {
        min < 1 -> "방금 전"
        min < 60 -> "${min}분 전"
        else -> "${min / 60}시간 전"
    }
}

private fun formatRemaining(remainingMillis: Long): String {
    val totalMin = remainingMillis / 60_000
    val h = totalMin / 60
    val m = totalMin % 60
    return when {
        h > 0 -> "${h}시간 ${m}분"
        m > 0 -> "${m}분"
        else -> "1분 미만"
    }
}

@Preview(name = "친구 스팟", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun SpotDetailScreenPreview() {
    SpotDetailScreen()
}

@Preview(name = "내 스팟", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun SpotDetailMinePreview() {
    SpotDetailScreen(spot = dummySpot(authorUid = "uid_me"))
}

@Preview(name = "만료된 스팟", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun SpotDetailExpiredPreview() {
    SpotDetailScreen(spot = dummySpot(minutesAgo = 400))
}