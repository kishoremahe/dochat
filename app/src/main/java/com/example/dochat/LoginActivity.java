package com.example.dochat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
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
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText Email,Password;
    private Button LoginBtn,PhoneBtn,SignupBtn;
    private TextView ForgotPassword,SignUp;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingBar;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitializeFields();

//        PhoneBtn.setOnClickListener((new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SendUserToPhoneLoginActivity();
//            }
//        }));

        SignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToRegistrationActivity();

            }
        });

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               checkLogin();
            }
        });

        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),ForgotPasswordActivity.class));

            }
        });

    }

    private void SendUserToPhoneLoginActivity()
    {
        Intent phoneLoginIntent=new Intent(getApplicationContext(),PhoneLoginActivity.class);
//        phoneLoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneLoginIntent);
//        finish();
    }

    private void InitializeFields() {

        Email=(EditText)findViewById(R.id.email);
        Password=(EditText)findViewById(R.id.password);
        LoginBtn=(Button)findViewById(R.id.btn_login);
//        PhoneBtn=(Button)findViewById(R.id.btn_phone);
        ForgotPassword=(TextView)findViewById(R.id.forgot_password);
        SignupBtn=(Button)findViewById(R.id.btn_signup);
        toolbar=(Toolbar)findViewById(R.id.app_bar);
        loadingBar=new ProgressDialog(LoginActivity.this);
        firebaseAuth=FirebaseAuth.getInstance();


    }

    private void checkLogin() {

        String email=Email.getText().toString();
        String password=Password.getText().toString();

        if(email.equals("") || password.equals("")){
            Toast.makeText(getApplicationContext(),"Please fill the required fields",Toast.LENGTH_SHORT).show();
        }
        else{

            loadingBar.setTitle("Checking....");
            loadingBar.setMessage("Please wait while we are checking.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    if(task.isSuccessful()){

                        String devicetoken= FirebaseInstanceId.getInstance().getToken();
                        HashMap<String,Object> hashMap =new HashMap<>();
                        hashMap.put("devicetoken",devicetoken);
                        hashMap.put("login_status","loggedin");

                        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        databaseReference.updateChildren(hashMap);
                        SendUserToMainActivity();
                        Toast.makeText(getApplicationContext(),"Authentication success",Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }

    }

    private void SendUserToRegistrationActivity(){

        Intent registrationIntent=new Intent(getApplicationContext(),RegistrationActivity.class);
//        registrationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(registrationIntent);
//        finish();

    }

    private void SendUserToMainActivity(){
        Intent mainIntent=new Intent(getApplicationContext(),MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}