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
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class PWAuthActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText editEmail;
    private EditText editPassword;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_pwauth);

        View header = findViewById(R.id.header);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // The olive header extends behind the status bar; only pad its content down.
            header.setPadding(0, systemBars.top, 0, 0);
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.signup_text).setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        auth = FirebaseAuth.getInstance();
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> signIn());

        // TODO: connect google_button to Google sign-in.
    }

    private void signIn() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("올바른 이메일 주소를 입력하세요");
            return;
        }
        if (password.isEmpty()) {
            showMessage("비밀번호를 입력하세요");
            return;
        }

        loginButton.setEnabled(false);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);
                    if (!task.isSuccessful()) {
                        showMessage(errorMessage(task.getException()));
                        return;
                    }
                    FirebaseUser user = auth.getCurrentUser();
                    String name = (user != null && user.getDisplayName() != null
                            && !user.getDisplayName().isEmpty())
                            ? user.getDisplayName() : email;
                    showMessage(name + "님, 환영합니다");
                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                });
    }

    private String errorMessage(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException
                || e instanceof FirebaseAuthInvalidCredentialsException) {
            return "이메일 또는 비밀번호가 올바르지 않습니다";
        }
        if (e instanceof FirebaseTooManyRequestsException) {
            return "시도 횟수가 너무 많습니다. 잠시 후 다시 시도하세요";
        }
        if (e instanceof FirebaseNetworkException) {
            return "네트워크 연결을 확인하세요";
        }
        return "로그인에 실패했습니다: " + (e != null ? e.getMessage() : "알 수 없는 오류");
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
