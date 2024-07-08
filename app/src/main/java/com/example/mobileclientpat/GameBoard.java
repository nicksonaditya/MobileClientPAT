package com.example.mobileclientpat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameBoard extends FrameLayout {
    private static final String TAG = "GameBoard";

    private Player player;
    private List<FallingObject> fallingObjects;
    private boolean inGame;
    private int score;
    private int timeLeft;
    private final int GAME_DURATION = 30; // Game duration in seconds
    private final int FRAME_RATE = 30; // Frame rate in frames per second
    private final long FRAME_PERIOD = 1000 / FRAME_RATE; // Frame period in milliseconds
    private Handler handler;
    private Button backButton;
    private Context context;

    private int screenWidth;
    private int screenHeight;

    public GameBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        Log.d(TAG, "GameBoard initialized");
        initBoard();
    }

    private void initBoard() {
        try {
            setBackgroundColor(Color.BLACK); // Set the background to black
            handler = new Handler();
            initBackButton();
            getScreenDimensions();

            player = new Player(context, screenWidth, screenHeight);
            fallingObjects = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                fallingObjects.add(new FallingObject(context, screenWidth, screenHeight));
            }
            inGame = true;
            score = 0;
            timeLeft = GAME_DURATION;
            startGame();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing board: " + e.getMessage());
        }
    }

    private void getScreenDimensions() {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
            Log.d(TAG, "Screen dimensions: width=" + screenWidth + ", height=" + screenHeight);
        } catch (Exception e) {
            Log.e(TAG, "Error getting screen dimensions: " + e.getMessage());
        }
    }

    private void initBackButton() {
        try {
            backButton = new Button(context);
            backButton.setText("Back to Main Menu");
            backButton.setTextColor(Color.WHITE); // Set the text color to white
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });

            // Create layout parameters for the button
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE); // Align to the bottom
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE); // Center horizontally
            params.setMargins(0, 30, 0, 30); // Add some margin

            // Add the button directly to the GameBoard and set it to invisible initially
            addView(backButton, params);
            backButton.setVisibility(INVISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing back button: " + e.getMessage());
        }
    }

    private void startGame() {
        try {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (inGame) {
                        updateGame();
                        postInvalidate();
                    }
                }
            }, 0, FRAME_PERIOD);

            new CountDownTimer(GAME_DURATION * 1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    timeLeft = (int) (millisUntilFinished / 1000);
                }

                public void onFinish() {
                    inGame = false;
                    postInvalidate(); // Force a redraw to show the game over message
                    saveScoreToServer(score);
                    navigateToEndgameActivity();
                }
            }.start();
        } catch (Exception e) {
            Log.e(TAG, "Error starting game: " + e.getMessage());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw called");

        if (inGame) {
            drawObjects(canvas);
        }
    }

    private void drawObjects(Canvas canvas) {
        try {
            player.draw(canvas);

            for (FallingObject obj : fallingObjects) {
                obj.draw(canvas);
            }

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            canvas.drawText("Score: " + score, 10, 40, paint);
            canvas.drawText("Time: " + timeLeft + " seconds", getWidth() - 300, 40, paint);
        } catch (Exception e) {
            Log.e(TAG, "Error drawing objects: " + e.getMessage());
        }
    }

    private void updateGame() {
        try {
            player.update();
            for (FallingObject obj : fallingObjects) {
                obj.fall();
                if (obj.getY() > getHeight()) {
                    obj.resetPosition(); // Call resetPosition() without arguments
                }
                if (Rect.intersects(player.getBounds(), obj.getBounds())) {
                    score++;
                    obj.resetPosition(); // Call resetPosition() without arguments
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating game: " + e.getMessage());
        }
    }

    private void navigateToEndgameActivity() {
        Intent intent = new Intent(context, EndgameActivity.class);
        intent.putExtra("finalScore", score);
        context.startActivity(intent);
    }

    private void saveScoreToServer(int score) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:8000/score"); // Adjust to your server endpoint
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // Create JSON object with username and score
                    JSONObject jsonScore = new JSONObject();
                    jsonScore.put("username", LoginActivity.username); // Assuming LoginActivity.username is accessible and contains the username
                    jsonScore.put("score", score);

                    // Write JSON payload to the connection
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonScore.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "Score saved successfully");
                    } else {
                        Log.e(TAG, "Failed to save score. Response code: " + responseCode);
                    }

                    conn.disconnect();
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Error saving score to server: " + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            player.handleTouchEvent(event);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error handling touch event: " + e.getMessage());
            return false;
        }
    }
}