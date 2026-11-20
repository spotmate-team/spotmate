package com.example.spotmate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReactionRepositoryTest {

    private lateinit var repo: FakeReactionRepository

    private val spotId = "spot_1"
    private val me = "uid_me"
    private val author = "uid_seeun"
    private val now = 1_000_000L
    private val expiresAt = now + SPOT_LIFETIME_MILLIS

    @Before
    fun setUp() {
        repo = FakeReactionRepository()
    }

    private fun toggle(
        type: ReactionType,
        time: Long = now,
        from: String? = me,
        to: String = author,
        spot: String = spotId,
    ) = repo.toggle(spot, from, to, type, expiresAt, time)

    // ---- #1 중복 / 취소 ----

    // 처음 누르면 저장
    @Test
    fun firstClick_addsReaction() {
        assertEquals(ToggleResult.ADDED, toggle(ReactionType.GO))
        assertEquals(setOf(ReactionType.GO), repo.myReactions(spotId, me))
    }

    // 다시 누르면 취소
    @Test
    fun secondClick_removesReaction() {
        toggle(ReactionType.GO)

        assertEquals(ToggleResult.REMOVED, toggle(ReactionType.GO))
        assertTrue(repo.myReactions(spotId, me).isEmpty())
    }

    // 같은 반응은 하나만
    @Test
    fun sameReaction_isNotDuplicated() {
        toggle(ReactionType.POKE)
        toggle(ReactionType.POKE)
        toggle(ReactionType.POKE)

        // 추가 → 취소 → 추가 라서 1개만 남아야 함
        assertEquals(1, repo.receivedReactions(author).size)
    }

    // 종류가 다르면 각각 저장
    @Test
    fun differentReactions_areSavedSeparately() {
        toggle(ReactionType.GO)
        toggle(ReactionType.POKE)
        toggle(ReactionType.CURIOUS)

        assertEquals(
            setOf(ReactionType.GO, ReactionType.POKE, ReactionType.CURIOUS),
            repo.myReactions(spotId, me)
        )
    }

    // 만료된 스팟은 저장 안 됨
    @Test
    fun expiredSpot_isNotSaved() {
        val result = toggle(ReactionType.GO, time = expiresAt + 1)

        assertEquals(ToggleResult.EXPIRED, result)
        assertTrue(repo.myReactions(spotId, me).isEmpty())
    }

    // ---- #2 대상 사용자 식별 ----

    // 보낸 사람, 받는 사람 저장
    @Test
    fun reaction_savesSenderAndReceiver() {
        toggle(ReactionType.GO)

        val saved = repo.receivedReactions(author).first()
        assertEquals(me, saved.fromUid)
        assertEquals(author, saved.toUid)
    }

    // 내 스팟에는 반응 불가
    @Test
    fun mySpot_cannotReact() {
        val result = toggle(ReactionType.GO, from = author)

        assertEquals(ToggleResult.MY_SPOT, result)
        assertTrue(repo.receivedReactions(author).isEmpty())
    }

    // 로그인 안 하면 반응 불가
    @Test
    fun notLoggedIn_cannotReact() {
        val result = toggle(ReactionType.GO, from = null)

        assertEquals(ToggleResult.NOT_LOGGED_IN, result)
        assertTrue(repo.receivedReactions(author).isEmpty())
    }

    // 받은 반응은 작성자별로 조회
    @Test
    fun receivedReactions_areFilteredByReceiver() {
        toggle(ReactionType.GO)                                       // 나 → seeun
        toggle(ReactionType.POKE, from = "uid_friend")                // 친구 → seeun
        toggle(ReactionType.GO, to = "uid_other", spot = "spot_2")    // 나 → 다른 사람

        assertEquals(2, repo.receivedReactions(author).size)
        assertEquals(1, repo.receivedReactions("uid_other").size)
    }

    // 받은 반응은 최신순
    @Test
    fun receivedReactions_areSortedByLatest() {
        toggle(ReactionType.GO, time = now)
        toggle(ReactionType.POKE, time = now + 1_000, from = "uid_friend")

        val list = repo.receivedReactions(author)
        assertEquals(ReactionType.POKE, list[0].type)
        assertEquals(ReactionType.GO, list[1].type)
    }
}