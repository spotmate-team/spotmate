package com.example.spotmate

data class Reaction(
    val spotId: String,
    val fromUid: String,          // 반응을 누른 사람
    val toUid: String,            // 반응을 받는 사람 (스팟 작성자)
    val type: ReactionType,
    val createdAt: Long = System.currentTimeMillis(),
) {
    /** 문서 ID = 사용자ID_반응종류 → 같은 사람이 같은 반응을 두 번 저장할 수 없음 (중복 방지) */
    val docId: String get() = "${fromUid}_${type.firestoreValue}"
}

/**
 * 반응 저장소 인터페이스
 * 지금은 FakeReactionRepository(메모리 저장)를 쓰고,
 * 나중에 같은 함수들을 Firestore로 구현한 클래스로 교체하면 화면 코드는 그대로 쓸 수 있음
 */
interface ReactionRepository {
    /** 반응 토글: 없으면 저장, 있으면 삭제(취소). 저장 후 선택 상태를 반환 */
    fun toggle(spotId: String, fromUid: String, toUid: String, type: ReactionType): Boolean

    /** 내가 이 스팟에 누른 반응 목록 */
    fun myReactions(spotId: String, fromUid: String): Set<ReactionType>

    /** 스팟 작성자가 받은 반응 목록 (마이페이지 '받은 반응' 화면용) */
    fun receivedReactions(toUid: String): List<Reaction>
}

/** 테스트용 메모리 저장소 (앱을 끄면 사라짐) */
class FakeReactionRepository : ReactionRepository {

    // key: "spotId/docId"
    private val store = mutableMapOf<String, Reaction>()

    override fun toggle(spotId: String, fromUid: String, toUid: String, type: ReactionType): Boolean {
        // 반응 대상 사용자 식별: 내 스팟에는 반응할 수 없음
        if (fromUid == toUid) return false

        val reaction = Reaction(spotId, fromUid, toUid, type)
        val key = "$spotId/${reaction.docId}"

        return if (store.containsKey(key)) {
            store.remove(key)      // 이미 있으면 취소
            false
        } else {
            store[key] = reaction  // 없으면 저장
            true
        }
    }

    override fun myReactions(spotId: String, fromUid: String): Set<ReactionType> =
        store.values
            .filter { it.spotId == spotId && it.fromUid == fromUid }
            .map { it.type }
            .toSet()

    override fun receivedReactions(toUid: String): List<Reaction> =
        store.values
            .filter { it.toUid == toUid }
            .sortedByDescending { it.createdAt }
}