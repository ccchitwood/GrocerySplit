package edu.uga.cs.grocerysplit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        EditText email = findViewById(R.id.editTextTextEmailAddress2);
        EditText password = findViewById(R.id.editTextTextPassword2);
        EditText confirmedPassword = findViewById(R.id.editTextTextPassword3);
        Button registerButton = findViewById(R.id.button6);

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailInput = email.getText().toString().trim();
                String passwordInput = password.getText().toString().trim();
                String confirmationPasswordInput = confirmedPassword.getText().toString().trim();

                if (emailInput.isEmpty() || passwordInput.isEmpty() || confirmationPasswordInput.isEmpty()) {
                    Toast.makeText(Register.this, "Please enter email, password, and password confirmation.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!passwordInput.equals(confirmationPasswordInput)) {
                    Toast.makeText(Register.this, "Passwords do not match.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                        .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(Register.this, "Registration successful.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Register.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    if (task.getException() != null) {
                                        Toast.makeText(Register.this, "Error: " + task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });
    }
}