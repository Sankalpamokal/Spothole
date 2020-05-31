package com.example.spothole;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    private TextView link2login;
    EditText regName,regEmail,regPass;
    Button regButton;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private ProgressDialog PD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        PD = new ProgressDialog(this);
        PD.setMessage("Loading...");
        PD.setCancelable(true);
        PD.setCanceledOnTouchOutside(false);

        regName = findViewById(R.id.regName);
        regEmail = findViewById(R.id.regEmail);
        regPass = findViewById(R.id.regPass);
        regButton = findViewById(R.id.regButton);
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user");
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
            finish();
        }
        link2login = findViewById(R.id.move2login);
        link2login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = regName.getText().toString();
                final String email = regEmail.getText().toString();
                final String pass = regPass.getText().toString();
                if (pass.length() > 0 && email.length() > 0) {
                    PD.show();
                    //authenticate user
                    firebaseAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthWeakPasswordException e) {
                                            regPass.setError("weak_password");
                                            regPass.requestFocus();
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            regEmail.setError("invalid_email");
                                            regEmail.requestFocus();
                                        } catch (FirebaseAuthUserCollisionException e) {
                                            regEmail.setError("user_exists");
                                            regEmail.requestFocus();
                                        } catch (Exception e) {
                                            //Log.e(TAG, e.getMessage());
                                        }
                                    } else {
                                        new user(name, email, pass);
                                        Intent intent = new Intent(RegistrationActivity.this, SplashScreen.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    PD.dismiss();
                                }
                            });
                } else {
                    Toast.makeText(
                            RegistrationActivity.this,
                            "Fill All Fields",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }
    public void openLoginActivity()
    {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}