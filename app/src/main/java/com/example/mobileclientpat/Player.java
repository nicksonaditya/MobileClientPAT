package com.example.mobileclientpat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.MotionEvent;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Player {
    private int x;
    private int y;
    private int dx;
    private final int WIDTH = 200;
    private final int HEIGHT = 120;
    private final int SPEED = 10;
    private Bitmap image;

    private int screenWidth;
    private int screenHeight;
    private Context context;

    public Player(Context context, int screenWidth, int screenHeight) {
        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        fetchImageFromServer();
        x = screenWidth / 2 - WIDTH / 2;
        y = screenHeight - HEIGHT - 50;
    }

    public void draw(Canvas canvas) {
        if (image != null) {
            canvas.drawBitmap(image, x, y, null);
        }
    }

    public void update() {
        x += dx;

        if (x < 0) {
            x = 0;
        }

        if (x > screenWidth - WIDTH) {
            x = screenWidth - WIDTH;
        }
    }

    public void handleTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                x = (int) event.getX() - WIDTH / 2;
                break;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public Rect getBounds() {
        return new Rect(x, y, x + WIDTH, y + HEIGHT);
    }

    private void fetchImageFromServer() {
        new FetchImageTask().execute("http://10.0.2.2:8000/getImageUrl?column=basket&username=" + LoginActivity.username);
    }

    private class FetchImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlString = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                InputStream inputStream = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                image = Bitmap.createScaledBitmap(result, WIDTH, HEIGHT, true);
            } else {
                // Fallback to a default image if the fetch fails
                image = BitmapFactory.decodeResource(context.getResources(), R.drawable.basketbaru);
                image = Bitmap.createScaledBitmap(image, WIDTH, HEIGHT, true);
            }
        }
    }
}
