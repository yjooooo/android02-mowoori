package com.ariari.mowoori.data.repository

import com.ariari.mowoori.ui.missions.entity.Mission
import com.ariari.mowoori.ui.missions.entity.MissionInfo
import com.ariari.mowoori.ui.register.entity.User
import com.ariari.mowoori.ui.register.entity.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class MissionsRepositoryImpl @Inject constructor(
    private val firebaseReference: DatabaseReference,
    private val firebaseAuth: FirebaseAuth
) : MissionsRepository {
    override suspend fun getMissionIdList(groupId: String): List<String> {
        val groupsRef = firebaseReference.child("groups")
            .child(groupId)
            .child("missionList")
            .get().await()

        val missionIdList = groupsRef.getValue(object : GenericTypeIndicator<List<String>>() {})
        return missionIdList ?: emptyList()
    }

    override suspend fun getMissions(userId: String): List<Mission> {
        val missionList = mutableListOf<Mission>()
        val missionsRef = firebaseReference.child("missions")
            .get().await()
        // TODO:실패할 경우 처리 필요

        missionsRef.children.forEach { dataSnapshot ->
            if (dataSnapshot.key != null) {
                val mission = Mission(
                    dataSnapshot.key as String,
                    dataSnapshot.getValue(MissionInfo::class.java) ?: MissionInfo()
                )
                if (mission.missionInfo.userId == userId) {
                    missionList.add(mission)
                }
            }
        }
        return missionList
    }

    override suspend fun getUser(): Result<User> = kotlin.runCatching {
        val uid = firebaseAuth.currentUser?.uid ?: throw NullPointerException("uid is null")
        val snapshot = firebaseReference.child("users/$uid").get().await()
        val userInfo = snapshot.getValue(UserInfo::class.java)
            ?: throw NullPointerException("userInfo is null")
        User(uid, userInfo)
    }

    override suspend fun isExistGroupId(groupId: String): Boolean {
        val snapshot = firebaseReference.child("groups/$groupId").get().await()
        return snapshot.exists()
    }

    override suspend fun postMission(missionInfo: MissionInfo, groupId:String, missionIdList: List<String>) {
        val missionId = firebaseReference.child("missions").push().key
        missionId?.let {
            val updatedMissionList = missionIdList.toMutableList().apply {
                    add(missionId)
                }

            val childUpdates = hashMapOf(
                "missions/$missionId" to missionInfo,
                "groups/$groupId/missionList" to updatedMissionList
            )
            firebaseReference.updateChildren(childUpdates)

//            firebaseReference.child("missions").child(mission.missionId)
//                .setValue(mission.missionInfo).await()

            missionId
        }
    }

    override suspend fun getUserName(userId: String): Result<String> = kotlin.runCatching {
        val snapshot = firebaseReference.child("users").child(userId).get().await()
        val userInfo = snapshot.getValue(UserInfo::class.java)
        userInfo?.nickname ?: throw NullPointerException("getUserName is null")
    }
}
