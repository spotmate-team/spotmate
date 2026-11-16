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
import android.content.Context
import java.io.File

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

        // 저장된 프로필 정보 불러오기
        val prefs = requireContext().getSharedPreferences(
            "spotmate_profile",
            Context.MODE_PRIVATE
        )

        // 닉네임과 자기소개 불러오기
        val savedNickname = prefs.getString("nickname", null)
        val savedIntroduction = prefs.getString("introduction", null)

        if (savedNickname != null) {
            view.findViewById<TextView>(R.id.tv_nickname).text = savedNickname
        }

        if (savedIntroduction != null) {
            view.findViewById<TextView>(R.id.tv_introduction).text = savedIntroduction
        }

        // 저장된 프로필 사진 불러오기
        val savedImagePath = prefs.getString("profile_image_path", null)

        if (savedImagePath != null) {
            val imageFile = File(savedImagePath)

            if (imageFile.exists()) {
                view.findViewById<ImageView>(R.id.iv_profile)
                    .setImageURI(Uri.fromFile(imageFile))
            }
        }

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
        selectedProfileImageUri = null

        val dialogView = layoutInflater.inflate(
            R.layout.edit_profile,
            null
        )

        dialog.setContentView(dialogView)

        // 팝업의 프로필 이미지 연결
        editProfileImageView =
            dialogView.findViewById(R.id.edit_profile_image)

        // 이전에 저장한 프로필 사진 불러오기
        val prefs = requireContext().getSharedPreferences(
            "spotmate_profile",
            Context.MODE_PRIVATE
        )

        val savedImagePath = prefs.getString("profile_image_path", null)

        if (savedImagePath != null) {
            val imageFile = File(savedImagePath)

            if (imageFile.exists()) {
                editProfileImageView?.setImageURI(Uri.fromFile(imageFile))
            }
        }

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
            val newNickname = nicknameInput.text.toString()

            val newIntroduction = introductionInput.text.toString()

            // 마이페이지 화면에 반영
            mainView.findViewById<TextView>(R.id.tv_nickname).text = newNickname
            mainView.findViewById<TextView>(R.id.tv_introduction).text = newIntroduction

            // 닉네임과 자기소개 저장
            val prefs = requireContext().getSharedPreferences(
                "spotmate_profile",
                Context.MODE_PRIVATE
            )

            prefs.edit()
                .putString("nickname", newNickname)
                .putString("introduction", newIntroduction)
                .apply()

            // 선택한 사진이 있다면 내부 저장소에 복사
            selectedProfileImageUri?.let { uri ->

                val imageFile = File(
                    requireContext().filesDir,
                    "profile_image.jpg"
                )

                val inputStream =
                    requireContext().contentResolver.openInputStream(uri)

                inputStream?.use { input ->
                    imageFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                //사진 경로 저장
                if (imageFile.exists()) {
                    prefs.edit()
                        .putString("profile_image_path", imageFile.absolutePath)
                        .apply()

                    mainView.findViewById<ImageView>(R.id.iv_profile)
                        .setImageURI(Uri.fromFile(imageFile))
                }
            }

            // 팝업 닫기
            dialog.dismiss()
        }
    }
}