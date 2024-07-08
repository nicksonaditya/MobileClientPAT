package com.example.mobileclientpat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EndgameActivity extends AppCompatActivity {
    private TextView tvFinalScore;
    private Button btnBackToMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endgame);

        int finalScore = getIntent().getIntExtra("finalScore", 0);

        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvFinalScore.setText("Final Score: " + finalScore);

        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(EndgameActivity.this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
