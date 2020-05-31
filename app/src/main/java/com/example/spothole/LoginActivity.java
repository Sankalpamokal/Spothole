package com.example.spothole;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private TextView link2reg;
    EditText regEmail, regPass;
    Button btnLogin;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        regEmail = (EditText)findViewById(R.id.regEmail);
        regPass = (EditText)findViewById(R.id.regPass);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user");
        if(user != null)
        {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regEmail.getText().toString();
                String pass = regPass.getText().toString();
                if(email.length() == 0){
                    regEmail.setError("Please Enter Email");
                    regPass.requestFocus();
                    return;
                }
                else if(pass.length() == 0){
                    regPass.setError("Please Enter Password");
                    regPass.requestFocus();
                    return;
                }
                else {

                    firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login Is Successful", Toast.LENGTH_SHORT).show();
                                Intent i1 = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i1);
                                finish();

                                regEmail.setText("");
                                regPass.setText("");


                            } else {
                                Toast.makeText(LoginActivity.this, "Login Issue " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }


                    });
                }



            }
        });

        link2reg = (TextView) findViewById(R.id.movetoreg);
        link2reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegActivity();
            }
        });
    }
    public void openRegActivity()
    {
        Intent i = new Intent(this, RegistrationActivity.class);
        startActivity(i);
    }

}