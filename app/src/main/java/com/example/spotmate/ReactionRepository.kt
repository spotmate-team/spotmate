package com.example.spotmate

// spots/{spotId}/reactions/{docId}
data class Reaction(
    val spotId: String,
    val fromUid: String,
    val toUid: String, // 스팟 작성자
    val type: ReactionType,
    val createdAt: Long = System.currentTimeMillis(),
) {
    // uid_type 으로 해서 중복 안 생기게
    val docId: String get() = "${fromUid}_${type.firestoreValue}"
}

enum class ToggleResult {
    ADDED,    // 저장됨
    REMOVED,  // 취소됨
    EXPIRED,  // 만료된 스팟
    MY_SPOT,  // 내 스팟
}

interface ReactionRepository {
    // 있으면 삭제, 없으면 추가
    fun toggle(
        spotId: String,
        fromUid: String,
        toUid: String,
        type: ReactionType,
        expiresAtMillis: Long,
        now: Long = System.currentTimeMillis(),
    ): ToggleResult

    fun myReactions(spotId: String, fromUid: String): Set<ReactionType>

    fun receivedReactions(toUid: String): List<Reaction>
}

// TODO Firestore 연결 전까지 임시로 메모리에 저장
class FakeReactionRepository : ReactionRepository {

    private val store = mutableMapOf<String, Reaction>()

    override fun toggle(
        spotId: String,
        fromUid: String,
        toUid: String,
        type: ReactionType,
        expiresAtMillis: Long,
        now: Long,
    ): ToggleResult {
        if (fromUid == toUid) return ToggleResult.MY_SPOT
        if (now >= expiresAtMillis) return ToggleResult.EXPIRED

        val reaction = Reaction(spotId, fromUid, toUid, type, createdAt = now)
        val key = "$spotId/${reaction.docId}"

        return if (store.containsKey(key)) {
            store.remove(key)
            ToggleResult.REMOVED
        } else {
            store[key] = reaction
            ToggleResult.ADDED
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