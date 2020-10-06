package com.example.dochat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PhoneLoginActivity extends AppCompatActivity {


    private EditText MobileNumber,VerificationCode;
    private Button VerifyBtn,VerifyCodeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        InitializeFields();

        VerifyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VerifyBtn.setVisibility(View.VISIBLE);
                VerifyCodeBtn.setVisibility(View.INVISIBLE);
                MobileNumber.setVisibility(View.INVISIBLE);
                VerificationCode.setVisibility(View.VISIBLE);

            }
        });
    }

    private void InitializeFields() {

        MobileNumber=(EditText)findViewById(R.id.mobile_number);
        VerificationCode=(EditText)findViewById(R.id.verification_code);
        VerifyBtn=(Button)findViewById(R.id.btn_verify);
        VerifyCodeBtn=(Button)findViewById(R.id.btn_verify_code);
    }
}