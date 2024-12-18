package com.hafizeogut.b210109058;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText loginEmail, loginPassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Authentication başlatılıyor
        auth = FirebaseAuth.getInstance();

        // Arayüz bileşenlerini tanımla
        loginEmail = findViewById(R.id.email);
        loginPassword = findViewById(R.id.password);

        // Oturum açık kullanıcıyı kontrol et
        checkExistingSession();
    }

    private void checkExistingSession() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d("FirebaseAuth", "Oturum açık: " + currentUser.getEmail());
            // Kullanıcı oturum açmışsa direkt MainActivity2'ye yönlendir
            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
            startActivity(intent);
            finish();
        }
    }

    private Boolean validateEmail() {
        String email = loginEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            loginEmail.setError("Email address cannot be empty!");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Invalid email format!");
            return false;
        }

        loginEmail.setError(null);
        return true;
    }

    private Boolean validatePassword() {
        String password = loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            loginPassword.setError("Password cannot be empty!");
            return false;
        }

        if (password.length() < 6) {
            loginPassword.setError("Password must be at least 6 characters!");
            return false;
        }

        loginPassword.setError(null);
        return true;
    }

    private void loginUser() {
        String userEmail = loginEmail.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        // Firebase Authentication ile giriş yap
        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d("FirebaseAuth", "Giriş başarılı.");
                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Log.e("FirebaseAuth", "Giriş hatası: ", task.getException());
                        Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void login(View view) {
        if (!validateEmail() || !validatePassword()) {
            return;
        }
        loginUser();
    }

    public void register(View view) {
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void home2(View view) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
