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

    private fun toggle(type: ReactionType, time: Long = now) =
        repo.toggle(spotId, me, author, type, expiresAt, time)

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
}