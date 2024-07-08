package com.example.mobileclientpat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();
    private EditText etUsername, etPassword;
    private Button btnSignupSubmit, btnGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsernameSignup);
        etPassword = findViewById(R.id.etPasswordSignup);
        btnSignupSubmit = findViewById(R.id.btnSignupSubmit);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnSignupSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (!username.isEmpty() && !password.isEmpty()) {
                    signup(username, password);
                } else {
                    Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void signup(String username, String password) {
        String url = "http://10.0.2.2:8000/signup";

        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON creation error", e);
            Toast.makeText(SignupActivity.this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Signup failed", e);
                runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response data: " + responseData);

                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject jsonObject = new JSONObject(responseData);
                            if (jsonObject.has("message")) {
                                String message = jsonObject.getString("message");
                                showSignupCompletedDialog(message);
                            } else if (jsonObject.has("error")) {
                                String error = jsonObject.getString("error");
                                Toast.makeText(SignupActivity.this, "Signup failed: " + error, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignupActivity.this, "Signup failed: Unknown response format", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String errorMessage = "Error " + response.code() + ": " + response.message();
                            Log.e(TAG, errorMessage);
                            Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
                        Toast.makeText(SignupActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showSignupCompletedDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle("Signup Successful")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
