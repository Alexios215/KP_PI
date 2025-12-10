package com.example.kp_pi;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {


    EditText etUser, etPass;
    Button btnLogin, btnToReg;
    DBHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        etUser = findViewById(R.id.loginUser);
        etPass = findViewById(R.id.loginPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnToReg = findViewById(R.id.btnToRegister);
        db = new DBHelper(this);


        btnLogin.setOnClickListener(v -> {
            String u = etUser.getText().toString();
            String p = etPass.getText().toString();


            if (db.checkLogin(u, p)) {
                Toast.makeText(this, "Успешный вход", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, CatalogActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Неверные данные", Toast.LENGTH_SHORT).show();
            }
        });


        btnToReg.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }
}