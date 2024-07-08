package com.example.mobileclientpat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    private Button btnPlay, btnHighscores, btnBattleMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnPlay = findViewById(R.id.btnPlay);
        btnHighscores = findViewById(R.id.btnHighscores);
        btnBattleMode = findViewById(R.id.btnBattleMode);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, PlayActivity.class));
            }
        });

        btnHighscores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, HighscoresActivity.class));
            }
        });

        btnBattleMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, BattleModeActivity.class));
            }
        });
    }
}
