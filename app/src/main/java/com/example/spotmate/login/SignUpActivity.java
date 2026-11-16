package com.example.spotmate.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.spotmate.MainActivity;
import com.example.spotmate.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText editNickname;
    private EditText editEmail;
    private EditText editPassword;
    private EditText editPasswordConfirm;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_signup);

        View header = findViewById(R.id.header);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // The olive header extends behind the status bar; only pad its content down.
            header.setPadding(0, systemBars.top, 0, 0);
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        // "이미 계정이 있나요? 로그인" -> go back to the login screen
        findViewById(R.id.login_link).setOnClickListener(v -> finish());

        auth = FirebaseAuth.getInstance();
        editNickname = findViewById(R.id.edit_nickname);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        editPasswordConfirm = findViewById(R.id.edit_password_confirm);
        signupButton = findViewById(R.id.signup_button);

        signupButton.setOnClickListener(v -> signUp());
    }

    private void signUp() {
        String nickname = editNickname.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();
        String passwordConfirm = editPasswordConfirm.getText().toString();

        if (nickname.isEmpty()) {
            showMessage("닉네임을 입력하세요");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("올바른 이메일 주소를 입력하세요");
            return;
        }
        if (password.length() < 6) {
            showMessage("비밀번호는 6자 이상이어야 합니다");
            return;
        }
        if (!password.equals(passwordConfirm)) {
            showMessage("비밀번호가 일치하지 않습니다");
            return;
        }

        signupButton.setEnabled(false);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        signupButton.setEnabled(true);
                        showMessage(errorMessage(task.getException()));
                        return;
                    }
                    saveNicknameAndFinish(nickname);
                });
    }

    /** Stores the nickname as the Firebase user's display name, then closes the screen. */
    private void saveNicknameAndFinish(String nickname) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            goToMain();
            return;
        }
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(nickname)
                .build();
        user.updateProfile(request).addOnCompleteListener(this, task -> {
            showMessage("회원가입이 완료되었습니다");
            goToMain();
        });
    }

    /** Sign-up also signs the user in, so continue straight to the main screen. */
    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private String errorMessage(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "이미 가입된 이메일입니다";
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "비밀번호가 너무 약합니다";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "올바른 이메일 주소를 입력하세요";
        }
        if (e instanceof FirebaseNetworkException) {
            return "네트워크 연결을 확인하세요";
        }
        return "회원가입에 실패했습니다: " + (e != null ? e.getMessage() : "알 수 없는 오류");
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
