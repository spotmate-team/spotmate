package com.example.spotmate.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.spotmate.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CloudFirestoreActivity extends AppCompatActivity {

    private static final String COLLECTION = "messages";
    private static final String DOCUMENT = "memo";

    private FirebaseFirestore db;
    private EditText editMessage;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_cloud_firestore);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left + 16, systemBars.top + 16, systemBars.right + 16, systemBars.bottom + 16);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        editMessage = findViewById(R.id.edit_message);
        resultText = findViewById(R.id.result_text);

        Button saveButton = findViewById(R.id.save_button);
        Button loadButton = findViewById(R.id.load_button);

        saveButton.setOnClickListener(v -> saveMessage());
        loadButton.setOnClickListener(v -> loadMessage());
    }

    private void saveMessage() {
        String text = editMessage.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "내용을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("timestamp", System.currentTimeMillis());

        db.collection(COLLECTION).document(DOCUMENT)
                .set(data)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadMessage() {
        db.collection(COLLECTION).document(DOCUMENT)
                .get()
                .addOnSuccessListener(this::showDocument)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "불러오기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showDocument(DocumentSnapshot snapshot) {
        if (snapshot.exists()) {
            resultText.setText(snapshot.getString("text"));
        } else {
            resultText.setText("저장된 데이터가 없습니다.");
        }
    }
}
