package com.example.mobileclientpat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FallingObject {
    private int x;
    private int y;
    private final int WIDTH = 60;
    private final int HEIGHT = 60;
    private final int SPEED = 18;
    private Bitmap image;

    private int screenWidth;
    private int screenHeight;
    private Context context;

    public FallingObject(Context context, int screenWidth, int screenHeight) {
        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        fetchImageFromServer();
        resetPosition();
    }

    public void draw(Canvas canvas) {
        if (image != null) {
            canvas.drawBitmap(image, x, y, null);
        }
    }

    public void fall() {
        y += SPEED;
    }

    public void resetPosition() {
        x = (int) (Math.random() * (screenWidth - WIDTH));
        y = 0;
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
        new FetchImageTask().execute("http://10.0.2.2:8000/getImageUrl?column=object&username=" + LoginActivity.username);
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
                image = BitmapFactory.decodeResource(context.getResources(), R.drawable.fallingobject);
                image = Bitmap.createScaledBitmap(image, WIDTH, HEIGHT, true);
            }
        }
    }
}
