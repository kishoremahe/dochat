package com.example.dochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText EmailAddress;
    private Button ResetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar toolbar=findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Forgot password");

        EmailAddress=(EditText)findViewById(R.id.email_address);
        ResetButton=(Button)findViewById(R.id.btn_reset);

        ResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email= EmailAddress.getText().toString();
                ResetPassword(email);


            }
        });
    }

    private void ResetPassword(String email) {

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(ForgotPasswordActivity.this, "Please enter the email id!", Toast.LENGTH_SHORT).show();
        }
        else{

            FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        Toast.makeText(ForgotPasswordActivity.this, "Please check your mail!", Toast.LENGTH_SHORT).show();
                        SendUserToLoginActivity();
                    }
                    else{
                        Toast.makeText(ForgotPasswordActivity.this, "Mail id doesn't exists,try agian!", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(getApplicationContext(),LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}