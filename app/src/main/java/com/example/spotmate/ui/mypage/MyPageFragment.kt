package com.example.spotmate.ui.mypage

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.spotmate.R
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

class MyPageFragment : Fragment() {

    private var selectedProfileImageUri: Uri? = null

    private var editProfileImageView: ImageView? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedProfileImageUri = it
                editProfileImageView?.setImageURI(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_mypage,
            container,
            false
        )

        // 프로필 수정 버튼
        val editButton = view.findViewById<View>(R.id.btn_edit_profile)

        editButton.setOnClickListener {
            showEditProfileDialog(view)
        }

        return view
    }

    // 프로필 수정 팝업 표시
    private fun showEditProfileDialog(mainView: View) {

        val dialog = Dialog(requireContext())

        val dialogView = layoutInflater.inflate(
            R.layout.edit_profile,
            null
        )

        dialog.setContentView(dialogView)

        // 팝업의 프로필 이미지 연결
        editProfileImageView =
            dialogView.findViewById(R.id.edit_profile_image)

        // 사진 변경 버튼
        val changePhotoButton =
            dialogView.findViewById<View>(R.id.btn_change_photo)

        changePhotoButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // 현재 마이페이지의 닉네임과 자기소개 가져오기
        val currentNickname =
            mainView.findViewById<TextView>(R.id.tv_nickname).text.toString()

        val currentIntroduction =
            mainView.findViewById<TextView>(R.id.tv_introduction).text.toString()

        // 팝업 입력창에 기존 값 표시
        val nicknameInput =
            dialogView.findViewById<EditText>(R.id.edit_nickname)

        val introductionInput =
            dialogView.findViewById<EditText>(R.id.edit_introduction)

        nicknameInput.setText(currentNickname)
        introductionInput.setText(currentIntroduction)

        // 팝업 배경 투명하게 설정
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        dialog.show()

        // 팝업 화면 크기 및 위치
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setGravity(Gravity.BOTTOM)

        // 취소 버튼
        val cancelButton =
            dialogView.findViewById<View>(R.id.btn_cancel_edit)

        cancelButton.setOnClickListener {
            dialog.dismiss()



        }

        // 저장 버튼
        val saveButton =
            dialogView.findViewById<View>(R.id.btn_save_edit)

        saveButton.setOnClickListener {

            // 수정한 닉네임과 자기소개 가져오기
            val newNickname =
                dialogView.findViewById<EditText>(R.id.edit_nickname).text.toString()

            val newIntroduction =
                dialogView.findViewById<EditText>(R.id.edit_introduction).text.toString()

            // 마이페이지 화면의 텍스트 변경
            mainView.findViewById<TextView>(R.id.tv_nickname).text = newNickname
            mainView.findViewById<TextView>(R.id.tv_introduction).text = newIntroduction

            // 선택한 사진이 있다면 마이페이지 사진 변경
            selectedProfileImageUri?.let {
                mainView.findViewById<ImageView>(R.id.iv_profile).setImageURI(it)
            }

            // 팝업 닫기
            dialog.dismiss()
        }
    }
}