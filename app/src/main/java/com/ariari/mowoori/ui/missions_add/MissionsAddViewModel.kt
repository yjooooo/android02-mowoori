package com.ariari.mowoori.ui.missions_add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariari.mowoori.data.repository.MissionsRepository
import com.ariari.mowoori.ui.missions.entity.Mission
import com.ariari.mowoori.ui.missions.entity.MissionInfo
import com.ariari.mowoori.util.Event
import com.ariari.mowoori.util.TimberUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MissionsAddViewModel @Inject constructor(
    private val missionsRepository: MissionsRepository
) : ViewModel() {
    private val _backBtnClick = MutableLiveData<Event<Boolean>>()
    val backBtnClick: LiveData<Event<Boolean>> = _backBtnClick

    private val _isPostMission = MutableLiveData<Event<Boolean>>()
    val isPostMission: LiveData<Event<Boolean>> = _isPostMission

    // 테스트를 위한 객체
    var groupId: String = "testGroupId"
    val mission = Mission("mission74", MissionInfo("미완료 미션1", "user1", 30, 10, 211101, 211201))

    fun setBackBtnClick() {
        _backBtnClick.value = Event(true)
    }

    fun getGroupId() {
        groupId = "testGroupId"
    }

    fun createNewMission() {
        Timber.d("createMission")
        viewModelScope.launch {
            // 해당 group에 missionId 추가
            var missionIdList = missionsRepository.getMissionIdList(groupId)
            if (missionIdList.isEmpty()) missionIdList = mutableListOf()
            (missionIdList as MutableList).add(mission.missionId)
            missionsRepository.postMissionIdList(groupId, missionIdList)

            // missions에 mission 추가
            postMission()

            // 화면 종료 Event 실행
            _isPostMission.value = Event(true)
        }
    }

    private suspend fun postMission() {
        Timber.d("startPostMission")
        missionsRepository.postMission(mission)
        Timber.d("finishPostMission")
    }
}
