package com.example.dochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {


    private static final String TAG = "kishoremahe";
    private Toolbar toolbar;
    private EditText Email,Password1,Password2;
    private Button SignUp;
    private TextView Login;
    private String DeviceToken;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        toolbar=(Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chatty");


        InitializeFields();

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createNewAccount();

            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToLoginActivity();

            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(getApplicationContext(),LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void InitializeFields() {
        Email=(EditText)findViewById(R.id.email);
        Password1=(EditText)findViewById(R.id.password1);
        Password2=(EditText)findViewById(R.id.password2);
        SignUp=(Button)findViewById(R.id.btn_register);
        Login=(TextView)findViewById(R.id.login);
        loadingBar=new ProgressDialog(RegistrationActivity.this);
    }

    private void createNewAccount(){

        final String email=Email.getText().toString();
        String password1=Password1.getText().toString();
        String password2=Password2.getText().toString();

        if(email.equals("") || password1.equals("") || password2.equals("")){
            Toast.makeText(getApplicationContext(),"Please fill required fields",Toast.LENGTH_SHORT).show();
        }
        else{

            if(password1.equals(password2)){

                if(password1.length() >= 8){

                    loadingBar.setTitle("Creating new account..");
                    loadingBar.setMessage("Please wait when new account is being created.");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

                    firebaseAuth.createUserWithEmailAndPassword(email,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                DataRegistration();

                                SendUserToMainActivity();
                                Toast.makeText(getApplicationContext(),"Registered successfully",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();



                            }
                            else if(task.isCanceled()){

                                Toast.makeText(getApplicationContext(),"Registration failed",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }

                        }
                    });

                }
                else{
                    Toast.makeText(getApplicationContext(),"password must contains atleast 8 characters",Toast.LENGTH_SHORT).show();
                }

            }
            else{
                Toast.makeText(getApplicationContext(),"Password mismatch",Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void DataRegistration() {

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId=firebaseUser.getUid();
        String devicetoken= FirebaseInstanceId.getInstance().getToken();

        DatabaseReference RootReference=FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid","");
        hashMap.put("username","");
        hashMap.put("about","i am using chatty");
        hashMap.put("imageurl","default");
        hashMap.put("status","offline");
        hashMap.put("devicetoken",devicetoken);

        RootReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    SendUserToMainActivity();
                    Toast.makeText(getApplicationContext(),"Registered successfully",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                }
                else{
                    Toast.makeText(RegistrationActivity.this, "Not registered successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void SendUserToMainActivity() {

        Intent mainIntent=new Intent(getApplicationContext(),MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}