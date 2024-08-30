package com.example.chitchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chitchat.model.Usermodel;
import com.example.chitchat.utils.firebaseutil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class Loginusername extends AppCompatActivity {

    Button donebtn;
    EditText usernameed;
    ProgressBar progressBar;
    String phonenumber;
    Usermodel usermodel;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginusername);

        donebtn = findViewById(R.id.donebtn);
        usernameed = findViewById(R.id.usernameed);
        progressBar = findViewById(R.id.prograssbar);

        phonenumber = getIntent().getExtras().getString("phone");
        getusername();

        donebtn.setOnClickListener((v -> {
            setUsername();
        }));


    }

    void setUsername(){

        String username = usernameed.getText().toString();
        if(username.isEmpty() || username.length()<2){
            usernameed.setError("username at least 2 alphabet");
            return;
        }
        setInprogress(true);
        if(usermodel!= null){
            usermodel.setUsername(username);
        }
        else{
//            usermodel= new Usermodel(phonenumber,username,Timestamp.now());
            com.google.firebase.Timestamp firebaseTimestamp = Timestamp.now();
            java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(firebaseTimestamp.getSeconds() * 6000);
            usermodel = new Usermodel(phonenumber,username,sqlTimestamp,firebaseutil.currentuserid());
        }
        firebaseutil.currentuserdetails().set(usermodel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(Loginusername.this, home.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    void getusername(){
        setInprogress(true);
        firebaseutil.currentuserdetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                setInprogress(false);
                if(task.isSuccessful()){
                    usermodel = task.getResult().toObject(Usermodel.class);
                    if(usermodel!=null){
                        usernameed.setText(usermodel.getUsername());
                    }
                }
            }
        });
    }

    void setInprogress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            donebtn.setVisibility(View.GONE);
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            donebtn.setVisibility(View.VISIBLE);
        }
    }
}