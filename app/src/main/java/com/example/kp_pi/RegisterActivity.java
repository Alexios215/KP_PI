package com.example.kp_pi;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText etUser, etPass;
    Button btnReg;
    Button btnBack;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().hide();

        etUser = findViewById(R.id.regUser);
        etPass = findViewById(R.id.regPass);
        btnReg = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
        db = new DBHelper(this);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        });

        btnReg.setOnClickListener(v -> {
            String u = etUser.getText().toString();
            String p = etPass.getText().toString();

            // При регистрации всегда создаем клиента (не админа)
            if (db.insertUser(u, p)) {
                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка: логин занят", Toast.LENGTH_SHORT).show();
            }
        });
    }
}