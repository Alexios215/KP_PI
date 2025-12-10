package com.example.kp_pi;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class RegisterActivity extends AppCompatActivity {


    EditText etUser, etPass;
    Button btnReg;
    DBHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        etUser = findViewById(R.id.regUser);
        etPass = findViewById(R.id.regPass);
        btnReg = findViewById(R.id.btnRegister);
        db = new DBHelper(this);


        btnReg.setOnClickListener(v -> {
            String u = etUser.getText().toString();
            String p = etPass.getText().toString();


            if (db.insertUser(u, p)) {
                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Ошибка: логин занят", Toast.LENGTH_SHORT).show();
            }
        });
    }
}