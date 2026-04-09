package com.example.deadlinebudget.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.deadlinebudget.R;
import com.example.deadlinebudget.data.DataManager;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextPseudo;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignup;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("LoginActivity", "onCreate appelé");

        dataManager = DataManager.getInstance();

        initViews();
        setupListeners();

        String newUsername = getIntent().getStringExtra("new_username");
        if (newUsername != null && !newUsername.isEmpty()) {
            editTextPseudo.setText(newUsername);
            editTextPassword.requestFocus();
        }
    }

    private void initViews() {
        editTextPseudo = findViewById(R.id.editTextPseudo);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignup = findViewById(R.id.textViewSignup);
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(v -> verifierConnexion());
        textViewSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void verifierConnexion() {
        String pseudo = editTextPseudo.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (pseudo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dataManager.verifierConnexion(pseudo, password)) {
            Toast.makeText(this, "Bienvenue " + pseudo + " !", Toast.LENGTH_SHORT).show();
            Log.d("LoginActivity", "Connexion réussie: " + pseudo);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("username", pseudo);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
            Log.d("LoginActivity", "Échec connexion pour: " + pseudo);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.close();
        Log.d("LoginActivity", "onDestroy");
    }
}