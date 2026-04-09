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

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextPseudo;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonSignup;
    private TextView textViewLogin;

    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Log.d("SignupActivity", "onCreate appelé");

        // Utilisation du singleton au lieu de new DataManager()
        dataManager = DataManager.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        editTextPseudo = findViewById(R.id.editTextPseudo);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        textViewLogin = findViewById(R.id.textViewLogin);
    }

    private void setupListeners() {
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inscrireUtilisateur();
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retour à l'écran de connexion
                finish();
            }
        });
    }

    private void inscrireUtilisateur() {
        String pseudo = editTextPseudo.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validations
        if (pseudo.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un nom d'utilisateur", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pseudo.length() < 3) {
            Toast.makeText(this, "Le nom d'utilisateur doit contenir au moins 3 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(email)) {
            Toast.makeText(this, "Email invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un mot de passe", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 4 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier si l'utilisateur existe déjà
        if (dataManager.utilisateurExiste(pseudo)) {
            Toast.makeText(this, "Ce nom d'utilisateur existe déjà", Toast.LENGTH_LONG).show();
            return;
        }

        // Inscription
        boolean success = dataManager.inscrireUtilisateur(pseudo, email, password);

        if (success) {
            Toast.makeText(this, "Inscription réussie ! Veuillez vous connecter", Toast.LENGTH_LONG).show();
            Log.d("SignupActivity", "Nouvel utilisateur inscrit: " + pseudo);

            // Retourner à l'écran de connexion
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("new_username", pseudo);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) {
            dataManager.close();
        }
        Log.d("SignupActivity", "onDestroy");
    }
}