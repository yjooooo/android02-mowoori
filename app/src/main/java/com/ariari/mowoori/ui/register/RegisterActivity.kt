package com.ariari.mowoori.ui.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.ariari.mowoori.R
import com.ariari.mowoori.data.preference.MoWooriPreference
import com.ariari.mowoori.databinding.ActivityRegisterBinding
import com.ariari.mowoori.ui.main.MainActivity
import com.ariari.mowoori.util.EventObserver
import com.ariari.mowoori.util.toastMessage
import com.ariari.mowoori.widget.BaseDialogFragment
import com.ariari.mowoori.widget.ConfirmDialogFragment
import com.ariari.mowoori.widget.ProgressDialogManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val viewModel: RegisterViewModel by viewModels()
    private val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let {
            viewModel.setProfileImage(it)
        }
    }
    @Inject
    lateinit var preference: MoWooriPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setObservers()
        setRootClick()
        setCompleteClick()
        viewModel.createNickName()
    }

    private fun setObservers() {
        setInvalidNickNameObserver()
        setRegisterSuccessObserver()
        setProfileClickObserver()
        setLoadingEventObserver()
    }

    private fun setRootClick() {
        binding.root.setOnClickListener {
            hideKeyboard(it)
            currentFocus?.clearFocus()
        }
    }

    private fun hideKeyboard(v: View) {
        // InputMethodManager 를 통해 가상 키보드를 숨길 수 있다.
        // 현재 focus 되어있는 뷰의 windowToken 을 hideSoftInputFromWindow 메서드의 매개변수로 넘겨준다.
        val inputMethodManager =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun setCompleteClick() {
        binding.btnRegisterComplete.setOnClickListener {
            ConfirmDialogFragment(object : BaseDialogFragment.NoticeDialogListener {
                override fun onDialogPositiveClick(dialog: DialogFragment) {
                    viewModel.registerUserInfo()
                }

                override fun onDialogNegativeClick(dialog: DialogFragment) {
                    dialog.dismiss()
                }
            }).show(supportFragmentManager, "ConfirmFragment")
        }
    }

    private fun setInvalidNickNameObserver() {
        viewModel.invalidNicknameEvent.observe(this, EventObserver {
            ProgressDialogManager.instance.clear()
            toastMessage(getString(R.string.register_nickname_error_msg))
        })
    }

    private fun setRegisterSuccessObserver() {
        viewModel.registerSuccessEvent.observe(this, EventObserver {
            ProgressDialogManager.instance.clear()
            if (it) {
                preference.setUserRegistered(true)
                moveToMain()
            } else {
                toastMessage(getString(R.string.register_fail_msg))
            }
        })
    }

    private fun setProfileClickObserver() {
        viewModel.profileImageClickEvent.observe(this, EventObserver {
            getContent.launch("image/*")
        })
    }

    private fun setLoadingEventObserver() {
        viewModel.loadingEvent.observe(this, EventObserver {
            if (it) {
                ProgressDialogManager.instance.show(this)
            } else {
                ProgressDialogManager.instance.clear()
            }
        })
    }

    private fun moveToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }
}
