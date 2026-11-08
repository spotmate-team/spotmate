package com.example.spotmate.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.spotmate.ui.theme.SpotMateTheme

// SpotMate 디자인 색상
private val MapBg = Color(0xFFEEF1E3)
private val Olive = Color(0xFF456A0B)
private val Ink = Color(0xFF1B2410)
private val InkSub = Color(0xFF5A6648)
private val LimeLight = Color(0xFFDDE8BC)
private val White = Color.White

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MapBg)
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {

            // 상단 네비게이션
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "‹",
                    fontSize = 32.sp,
                    color = Ink,
                    modifier = Modifier.width(44.dp)
                )

                Text(
                    text = "마이페이지",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Text(
                    text = "⚙",
                    fontSize = 22.sp,
                    color = Ink,
                    modifier = Modifier.width(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 프로필 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = White
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    // 프로필 이미지 및 활동 정보
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(
                                    Color(0xFFBFE6F0),
                                    CircleShape
                                )
                                .border(
                                    3.dp,
                                    Olive,
                                    CircleShape
                                )
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "15",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = "게시글",
                                fontSize = 13.sp,
                                color = InkSub
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "2",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = "친구",
                                fontSize = 13.sp,
                                color = InkSub
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 닉네임 및 소개글
                    Text(
                        text = "me",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    Text(
                        text = "소개글을 적어보세요",
                        fontSize = 14.sp,
                        color = InkSub
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 프로필 수정 버튼
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Olive
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            Olive
                        )
                    ) {
                        Text(
                            text = "프로필 수정",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 방문 기록 제목
            Text(
                text = "내 방문 기록",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.padding(
                    start = 4.dp,
                    top = 4.dp,
                    bottom = 8.dp
                )
            )

            // 방문 기록 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = White
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {

                    HistoryItem(
                        place = "카페 온도",
                        date = "오늘",
                        description = "비 오는 날 커피 맛집 발견",
                        status = "지도에 2시간 남음",
                        isActive = true
                    )

                    HorizontalDivider(color = MapBg)

                    HistoryItem(
                        place = "합정 국숫집",
                        date = "어제",
                        description = "혼밥하기 편한 곳",
                        status = "지도에서 사라짐"
                    )

                    HorizontalDivider(color = MapBg)

                    HistoryItem(
                        place = "숭실대학교",
                        date = "3일 전",
                        description = "중간고사 끝나고 한 컷",
                        status = "지도에서 사라짐"
                    )
                }
            }
        }

        // 하단 탭 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(White)
                .border(
                    width = 2.dp,
                    color = Olive
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem("소셜", true)
            TabItem("홈", false)
            TabItem("추가", false)
        }
    }
}

@Composable
private fun HistoryItem(
    place: String,
    date: String,
    description: String,
    status: String,
    isActive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 사진 썸네일 자리
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    Color(0xFFDDE8BC),
                    RoundedCornerShape(14.dp)
                )
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = place,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = InkSub
                )
            }

            Text(
                text = description,
                fontSize = 11.sp,
                color = InkSub
            )

            if (isActive) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(100.dp)
                        .background(
                            Color(0xFFF6C90E),
                            RoundedCornerShape(11.dp)
                        )
                )
                {
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        color = Ink,
                        modifier = Modifier.offset(
                            x = 10.dp,
                            y = (-2).dp
                        )
                    )
                }
            }
            else {
                Text(
                    text = status,
                    fontSize = 10.sp,
                    color = InkSub
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    selected: Boolean
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .clickable { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(32.dp)
                .background(
                    if (selected) LimeLight else White,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (title) {
                    "소셜" -> "◎"
                    "홈" -> "⌂"
                    else -> "+"
                },
                fontSize = 22.sp,
                color = Olive
            )
        }

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            color = if (selected) Olive else InkSub
        )
    }
}
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=393dp,height=852dp,dpi=420"
)
@Composable
fun MyPageScreenPreview() {
    SpotMateTheme {
        Scaffold { innerPadding ->
            MyPageScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
@Composable
private fun StatusBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Color(0xFFF6C90E),
                RoundedCornerShape(11.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
    )
}