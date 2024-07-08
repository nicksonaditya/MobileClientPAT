package com.example.mobileclientpat;

import android.app.Activity;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class BattleMode extends FrameLayout {
    private static final String TAG = "BattleMode";
    private Player player;
    private List<FallingObject> fallingObjects;
    private boolean inGame;
    private int score;
    private int opponentScore;
    private int timeLeft;
    private final int GAME_DURATION = 30;
    private final int FRAME_RATE = 30;
    private final long FRAME_PERIOD = 1000 / FRAME_RATE;
    private Handler handler;
    private Button backButton;
    private Context context;
    private int screenWidth;
    private int screenHeight;
    private WebSocket webSocket;
    private OkHttpClient client;
    private boolean enemyReady = false;
    private Handler checkOpponentHandler;
    private Runnable checkOpponentRunnable;

    public BattleMode(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        Log.d(TAG, "BattleMode initialized");
        initBoard();
        initWebSocket();
        startCheckOpponentTimer();
    }

    private void initBoard() {
        setBackgroundColor(Color.BLACK);
        handler = new Handler();
        getScreenDimensions();
        player = new Player(context, screenWidth, screenHeight);
        fallingObjects = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            fallingObjects.add(new FallingObject(context, screenWidth, screenHeight));
        }
        inGame = false;
        score = 0;
        opponentScore = 0;
        timeLeft = GAME_DURATION;

        // Initialize the back button
        backButton = ((Activity) context).findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
            backButton.setVisibility(INVISIBLE);
        }
    }

    private void getScreenDimensions() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        Log.d(TAG, "Screen dimensions: width=" + screenWidth + ", height=" + screenHeight);
    }

    private void initWebSocket() {
        client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://10.0.2.2:8000/websocket").build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "WebSocket opened");
                webSocket.send("{\"type\": \"ready\", \"username\": \"" + LoginActivity.username + "\"}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Receiving : " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    String type = json.getString("type");
                    if (type.equals("start")) {
                        enemyReady = true;
                        startGame();
                    } else if (type.equals("score")) {
                        opponentScore = json.getInt("opponentScore");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.d(TAG, "Receiving bytes : " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d(TAG, "Closing : " + code + " / " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                t.printStackTrace();
            }
        });
        client.dispatcher().executorService().shutdown();
    }

    private void startCheckOpponentTimer() {
        checkOpponentHandler = new Handler();
        checkOpponentRunnable = new Runnable() {
            @Override
            public void run() {
                checkOpponentReady();
                checkOpponentHandler.postDelayed(this, 3000);
            }
        };
        checkOpponentHandler.post(checkOpponentRunnable);
    }

    private void checkOpponentReady() {
        Request request = new Request.Builder().url("http://10.0.2.2:8000/checkEnemyReady").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        enemyReady = json.getBoolean("ready");
                        if (enemyReady) {
                            startGame();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void startGame() {
        checkOpponentHandler.removeCallbacks(checkOpponentRunnable);
        inGame = true;
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
                postInvalidate();
                webSocket.send("{\"type\": \"score\", \"username\": \"" + LoginActivity.username + "\", \"score\": " + score + "}");
                navigateToEndgameActivity();
            }
        }.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw called");

        if (inGame) {
            drawObjects(canvas);
        } else {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            canvas.drawText("Waiting for enemy...", getWidth() / 2 - 150, getHeight() / 2, paint);
        }
    }

    private void drawObjects(Canvas canvas) {
        player.draw(canvas);
        for (FallingObject obj : fallingObjects) {
            obj.draw(canvas);
        }

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        canvas.drawText("Score: " + score, 10, 40, paint);
        canvas.drawText("Time: " + timeLeft + " seconds", getWidth() - 300, 40, paint);
        canvas.drawText("Opponent Score: " + opponentScore, 10, 80, paint);
    }

    private void updateGame() {
        player.update();
        for (FallingObject obj : fallingObjects) {
            obj.fall();
            if (obj.getY() > getHeight()) {
                obj.resetPosition();
            }
            if (Rect.intersects(player.getBounds(), obj.getBounds())) {
                score++;
                obj.resetPosition();
            }
        }
    }

    private void navigateToEndgameActivity() {
        Intent intent = new Intent(context, BattleEndgameActivity.class);
        intent.putExtra("finalScore", score);
        intent.putExtra("opponentScore", opponentScore);
        context.startActivity(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        player.handleTouchEvent(event);
        return true;
    }
}
