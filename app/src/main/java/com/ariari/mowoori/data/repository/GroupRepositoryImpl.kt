package com.ariari.mowoori.data.repository

import com.ariari.mowoori.ui.home.entity.GroupInfo
import com.ariari.mowoori.ui.register.entity.User
import com.ariari.mowoori.ui.register.entity.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class GroupRepositoryImpl @Inject constructor(
    private val databaseReference: DatabaseReference,
    private val firebaseAuth: FirebaseAuth
) : GroupRepository {
    override suspend fun getGroupInfo(groupId: String): Result<GroupInfo> = kotlin.runCatching {
        val snapshot = databaseReference.child("groups/$groupId").get().await()
        snapshot.getValue(GroupInfo::class.java)
            ?: throw NullPointerException("getGroupInfo is null")
    }

    override fun putGroupInfo(groupInfo: GroupInfo, user: User): Result<String> =
        kotlin.runCatching {
            val newId = databaseReference.child("groups").push().key
            newId?.let {
                val tmpGroupList = user.userInfo.groupList
                    .toMutableList().apply {
                        add(newId)
                    }
                val newUserInfo = user.userInfo.copy(groupList = tmpGroupList, currentGroupId = newId)
                val childUpdates = hashMapOf(
                    "/groups/$newId" to groupInfo,
                    "/users/${user.userId}" to newUserInfo
                )
                databaseReference.updateChildren(childUpdates)
                newId
            } ?: throw NullPointerException("Couldn't get push key for posts")
        }

    override suspend fun addUserToGroup(groupId: String, user: User): Result<String> =
        kotlin.runCatching {
            val snapshot = databaseReference.child("groups/$groupId").get().await()
            val tmpGroup = snapshot.getValue(GroupInfo::class.java)
                ?: throw NullPointerException("group is Null")
            val tmpUserList = tmpGroup.userList.toMutableList().apply { add(user.userId) }
            val newGroupInfo = tmpGroup.copy(userList = tmpUserList)

            val tmpGroupList = user.userInfo.groupList.toMutableList().apply { add(groupId) }
            val newUserInfo = user.userInfo.copy(groupList = tmpGroupList, currentGroupId = groupId)
            val childUpdates = hashMapOf(
                "/groups/$groupId" to newGroupInfo,
                "/users/${user.userId}" to newUserInfo
            )
            databaseReference.updateChildren(childUpdates)
            groupId
        }

    override suspend fun getUser(): Result<User> = kotlin.runCatching {
        val uid = firebaseAuth.currentUser?.uid ?: throw NullPointerException("uid is null")
        val snapshot = databaseReference.child("users/$uid").get().await()
        val userInfo = snapshot.getValue(UserInfo::class.java)
            ?: throw NullPointerException("userInfo is null")
        User(uid, userInfo)
    }

    override suspend fun isExistGroupId(groupId: String): Boolean {
        val snapshot = databaseReference.child("groups/$groupId").get().await()
        return snapshot.exists()
    }
}
