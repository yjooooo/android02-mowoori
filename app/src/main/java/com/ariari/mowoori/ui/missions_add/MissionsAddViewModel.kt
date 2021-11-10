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
import com.ariari.mowoori.util.getCurrentDate
import com.ariari.mowoori.util.getCurrentDatePlusMonths
import com.ariari.mowoori.util.getMissionIntFormatDate
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _isCreateMission = MutableLiveData<Event<Boolean>>()
    val isCreateMission: LiveData<Event<Boolean>> = _isCreateMission

    private val _numberCountClick = MutableLiveData<Event<Unit>>()
    val numberCountClick: LiveData<Event<Unit>> = _numberCountClick

    private val _missionCount = MutableLiveData<Int>()
    val missionCount: LiveData<Int> = _missionCount

    private val _missionStartDate = MutableLiveData<Int>()
    val missionStartDate: LiveData<Int> = _missionStartDate

    private val _missionEndDate = MutableLiveData<Int>()
    val missionEndDate: LiveData<Int> = _missionEndDate

    private val _inValidMissionNameEvent = MutableLiveData<Event<Unit>>()
    val inValidMissionNameEvent: LiveData<Event<Unit>> = _inValidMissionNameEvent

    private val _inValidMissionDateEvent = MutableLiveData<Event<Boolean>>()
    val inValidMissionDateEvent: LiveData<Event<Boolean>> = _inValidMissionDateEvent

    // 테스트를 위한 객체
    var groupId: String = "testGroupId"
    val mission = Mission("mission74", MissionInfo("미완료 미션1", "user1", 30, 10, 211101, 211201))

    init {
        _missionStartDate.value = getCurrentDate()
        _missionEndDate.value = getCurrentDatePlusMonths(1)
        updateMissionCount(10)
    }

    fun setBackBtnClick() {
        _backBtnClick.value = Event(true)
    }

    fun getGroupId() {
        groupId = "testGroupId"
    }

    fun postMission() {
        Timber.d("createMission")
        viewModelScope.launch {
            // 해당 group에 missionId 추가
            var missionIdList = missionsRepository.getMissionIdList(groupId)
            if (missionIdList.isEmpty()) missionIdList = mutableListOf()
            (missionIdList as MutableList).add(mission.missionId)
            missionsRepository.postMissionIdList(groupId, missionIdList)

            // missions에 mission 추가
            missionsRepository.postMission(mission)

            // 화면 종료 Event 실행
            _isPostMission.value = Event(true)
        }
    }

    private suspend fun createMission() {

    }

    fun showNumberPicker() {
        _numberCountClick.postValue(Event(Unit))
    }

    fun updateMissionCount(count: Int) {
        _missionCount.value = count
    }

    fun updateMissionStartDate(year: Int, month: Int, date: Int) {
        _missionStartDate.value = getMissionIntFormatDate(year, month, date)
        TimberUtil.timber("update", _missionStartDate.value.toString())
    }

    fun updateMissionEndDate(year: Int, month: Int, date: Int) {
        _missionEndDate.value = getMissionIntFormatDate(year, month, date)
    }

    fun updateMissionInvalid() {
        checkMissionDateInvalid()
        checkMissionNameInvalid()
    }

    private fun checkMissionNameInvalid() {
        _inValidMissionNameEvent.postValue(Event(Unit))
    }

    private fun checkMissionDateInvalid() {
        if (missionStartDate.value!! > missionEndDate.value!!) {
            _inValidMissionDateEvent.postValue(Event(false))
        } else {
            _inValidMissionDateEvent.postValue(Event(true))
        }
    }
}
