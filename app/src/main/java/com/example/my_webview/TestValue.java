package com.example.my_webview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class TestValue extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_value);
        EditText testedit = findViewById(R.id.testedit);
        Button textbut = findViewById(R.id.textbut);
    }

}