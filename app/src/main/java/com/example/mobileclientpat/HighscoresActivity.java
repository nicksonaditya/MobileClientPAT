package com.example.mobileclientpat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HighscoresActivity extends AppCompatActivity {

    private static final String HIGH_SCORE_URL = "http://10.0.2.2:8000/high_scores"; // Replace with your actual API endpoint
    private RecyclerView recyclerView;
    private HighscoresAdapter adapter;
    private List<Highscore> highscoreList;
    private Button backToMenuButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);

        recyclerView = findViewById(R.id.highscoresRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        highscoreList = new ArrayList<>();
        adapter = new HighscoresAdapter(highscoreList);
        recyclerView.setAdapter(adapter);

        backToMenuButton = findViewById(R.id.backToMenuButton);
        backToMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MenuActivity
                Intent intent = new Intent(HighscoresActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });

        fetchAndDisplayHighScores();
    }

    private void fetchAndDisplayHighScores() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, HIGH_SCORE_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String username = jsonObject.getString("username");
                                int score = jsonObject.getInt("score");
                                highscoreList.add(new Highscore(i + 1, username, score));
                            }
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(HighscoresActivity.this, "Error parsing high scores", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HighscoresActivity", "Error fetching high scores", error);
                Toast.makeText(HighscoresActivity.this, "Error fetching high scores", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonArrayRequest);
    }
}
