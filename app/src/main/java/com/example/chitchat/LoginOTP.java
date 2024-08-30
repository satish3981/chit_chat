package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chitchat.utils.androidutils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOTP extends AppCompatActivity {
    String phoneNumber;

    EditText ed2;
    Button button;
    ProgressBar progressBar;
    TextView textView;
    TextView resendtxt;
    FirebaseAuth mAUTH = FirebaseAuth.getInstance();
    Long timeoutsec = 30L;
    String verificationcode;
    PhoneAuthProvider.ForceResendingToken resendingToken;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_otp);

        ed2 = findViewById(R.id.Ed2);
        button = findViewById(R.id.button);
        progressBar = findViewById(R.id.prograssbar);
        textView = findViewById(R.id.loginText);
        resendtxt = findViewById(R.id.resendtxt);

        button.setOnClickListener(v -> {
            String enterOTP = ed2.getText().toString();
            PhoneAuthCredential credential =PhoneAuthProvider.getCredential(verificationcode,enterOTP);
            singIn(credential);
            setInprogress(true);
        });

        resendtxt.setOnClickListener((v) -> {
            sendOtp(phoneNumber, true);
        });


        phoneNumber = getIntent().getExtras().getString("phone");
        sendOtp(phoneNumber , false);

    }
    void sendOtp(String phoneNumber,boolean isResend){
        startresendtimer();
        setInprogress(true);
        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(mAUTH)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(timeoutsec, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        singIn(phoneAuthCredential);
                        setInprogress(false);}
                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        androidutils.showToast(getApplicationContext(),"Wrong OTP");
                        setInprogress(false);}
                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationcode = s;
                        resendingToken = forceResendingToken;
                        androidutils.showToast(getApplicationContext(),"Successfully OTP send");
                        setInprogress(false);}});
        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        }
        else{
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }


    }
    void setInprogress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }
    void singIn(PhoneAuthCredential phoneAuthCredential)
    {
        setInprogress(true);
        mAUTH.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInprogress(false);
                if(task.isSuccessful()){
                    Intent intent=new Intent(LoginOTP.this, Loginusername.class);
                    intent.putExtra("phone",phoneNumber);
                    startActivity(intent);

                }else {
                     androidutils.showToast(getApplicationContext(),"OTP Faild ");
                }
            }
        });
    }

    void startresendtimer(){
        resendtxt.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutsec--;
                resendtxt.setText("Resend OTP in "+ timeoutsec +" seconds");
                if(timeoutsec<=0)
                {
                    timeoutsec=60L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        resendtxt.setEnabled(true);
                    });
                }
            }
        },0,1000);

    }

}