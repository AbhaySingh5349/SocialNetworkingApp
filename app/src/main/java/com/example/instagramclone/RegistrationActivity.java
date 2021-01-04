package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegistrationActivity extends AppCompatActivity {

    @BindView(R.id.countryCodePicker)
    CountryCodePicker countryCodePicker;
    @BindView(R.id.mobileTextInputLayout)
    TextInputLayout mobileTextInputLayout;
    @BindView(R.id.mobileTextInputEditText)
    TextInputEditText mobileTextInputEditText;
    @BindView(R.id.otpImageView)
    ImageView otpImageView;
    @BindView(R.id.otpTextInputLayout)
    TextInputLayout otpTextInputLayout;
    @BindView(R.id.otpTextInputEditText)
    TextInputEditText otpTextInputEditText;
    @BindView(R.id.resendOTPTextView)
    TextView resendOTPTextView;
    @BindView(R.id.registrationBtn)
    Button registrationBtn;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mobileNumberCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendMobileNumberToken;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String otp = "" , mobileNumber = "", mobileNumberVerificationId;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);

        countryCodePicker.registerCarrierNumberEditText(mobileTextInputEditText);

        mobileNumberVerification();

        registrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registrationBtn.getText().toString().equals("Next")) {
                    progressDialog.setTitle("Mobile Number Verification");
                    progressDialog.setMessage("Please wait, while we are verifying your contact: " + mobileNumber);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    countryCodePicker.setEnabled(false);
                    mobileTextInputEditText.setEnabled(false);
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(mobileNumber, 60, TimeUnit.SECONDS, RegistrationActivity.this, mobileNumberCallbacks);
                    startTimer();
                    resendOTPTextView.setVisibility(View.VISIBLE);
                    otpImageView.setVisibility(View.VISIBLE);
                    otpTextInputLayout.setVisibility(View.VISIBLE);
                }
                if(registrationBtn.getText().toString().equals("Proceed") || otp.equals("Code Sent")){
                    String otpCode = Objects.requireNonNull(otpTextInputEditText.getText()).toString();
                    if(otpCode.length()!=6){
                        otpTextInputLayout.setError("Enter Correct OTP");
                        otpTextInputLayout.setErrorEnabled(true);
                    }else {
                        otpTextInputLayout.setError(null);
                        otpTextInputLayout.setErrorEnabled(false);
                        progressDialog.setTitle("OTP Verification");
                        progressDialog.setMessage("Please wait, while we are verifying your OTP");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mobileNumberVerificationId,otpCode);
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }
                }
            }
        });

        mobileNumberCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(RegistrationActivity.this,"Failed to register User: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                countryCodePicker.setEnabled(true);
                mobileTextInputEditText.setEnabled(true);
                mobileTextInputEditText.setText(null);
                registrationBtn.setText("Next");
                otp = "";
                registrationBtn.setVisibility(View.GONE);
                resendOTPTextView.setVisibility(View.GONE);
                otpImageView.setVisibility(View.GONE);
                otpTextInputLayout.setVisibility(View.GONE);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                countryCodePicker.setEnabled(false);
                mobileTextInputEditText.setEnabled(false);
                registrationBtn.setText("Proceed");
                otp = "Code Sent";

                mobileNumberVerificationId = s;
                resendMobileNumberToken = forceResendingToken;
                Toast.makeText(RegistrationActivity.this,"OTP has been sent on: " + mobileNumber,Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        };

        resendOTPTextView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                resendOTP(mobileNumber,resendMobileNumberToken);
                countryCodePicker.setEnabled(false);
                mobileTextInputEditText.setEnabled(false);
                otpTextInputEditText.setText(null);
                registrationBtn.setText("Proceed");
                otp = "Code Sent";
                registrationBtn.setEnabled(true);
                Toast.makeText(RegistrationActivity.this,"OTP has been sent on: " + mobileNumber,Toast.LENGTH_SHORT).show();
                startTimer();
            }
        });
    }

    private void mobileNumberVerification() {
        mobileTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if ((TextUtils.isDigitsOnly(charSequence.toString()) && charSequence.toString().length() == 10)) {
                    mobileNumber = countryCodePicker.getFullNumberWithPlus();
                    mobileTextInputLayout.setErrorEnabled(false);
                    mobileTextInputLayout.setError(null);
                    registrationBtn.setVisibility(View.VISIBLE);
                    registrationBtn.setText("Next");
                } else if (charSequence.toString().length() == 0) {
                    mobileTextInputLayout.setErrorEnabled(false);
                    mobileTextInputLayout.setError(null);
                    registrationBtn.setVisibility(View.GONE);
                } else {
                    mobileTextInputLayout.setError("Invalid Number");
                    registrationBtn.setVisibility(View.GONE);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void startTimer(){
        mobileTextInputLayout.setEnabled(false);
        countryCodePicker.setEnabled(false);
        new CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long timeLeft) {
                resendOTPTextView.setText("ReSend OTP:" + timeLeft/1000);
                resendOTPTextView.setEnabled(false);
                registrationBtn.setEnabled(true);
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                registrationBtn.setText("Proceed");
                otp = "Code Sent";
                resendOTPTextView.setText("ReSend OTP");
                resendOTPTextView.setEnabled(true);
            }
        }.start();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();

                    Toast.makeText(RegistrationActivity.this,"OTP verified, User registered Successfully!!",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity.this,ProfileActivity.class);
                    intent.putExtra("mobile number",mobileNumber);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegistrationActivity.this,"Failed to Register User: " + Objects.requireNonNull(task.getException()).toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void resendOTP(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mobileNumberCallbacks, token);
    }

    @Override
    protected void onStart()  // automatic login functionality
    {
        super.onStart();
        if(firebaseUser!=null){
            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
            finish();
        }
    }
}