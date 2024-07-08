package com.example.mobileclientpat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class BattleEndgameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_endgame);

        int finalScore = getIntent().getIntExtra("finalScore", 0);
        int opponentScore = getIntent().getIntExtra("opponentScore", 0);

        TextView finalScoreTextView = findViewById(R.id.finalScoreTextView);
        TextView opponentScoreTextView = findViewById(R.id.opponentScoreTextView);
        TextView resultTextView = findViewById(R.id.resultTextView);

        finalScoreTextView.setText("Your Score: " + finalScore);
        opponentScoreTextView.setText("Opponent's Score: " + opponentScore);

        if (finalScore > opponentScore) {
            resultTextView.setText("You Win!");
        } else if (finalScore < opponentScore) {
            resultTextView.setText("You Lose!");
        } else {
            resultTextView.setText("It's a Tie!");
        }
    }
}
