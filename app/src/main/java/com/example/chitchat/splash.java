package com.example.chitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.chitchat.model.Usermodel;
import com.example.chitchat.utils.androidutils;
import com.example.chitchat.utils.firebaseutil;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(firebaseutil.isLoggedin() && getIntent().getExtras()!=null)
        {
            //from notification
            String userId = getIntent().getExtras().getString("userId");
            firebaseutil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener(task -> {
                       if (task.isSuccessful())
                       {
                           Usermodel model = task.getResult().toObject(Usermodel.class);

                           Intent mainIntent = new Intent(this, home.class);
                           mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                           startActivity(mainIntent);

                           Intent intent = new Intent(this, ChatActivity.class);
                           androidutils.passUserModelAsIntent(intent, model);
                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                           startActivity(intent);
                           finish();
                       }
                    });

        }
        else{
            new Handler().postDelayed(() -> {
                if(firebaseutil.isLoggedin())
                {
                    startActivity(new Intent(splash.this, home.class));

                }
                else {
                    startActivity(new Intent(splash.this, Login.class));

                }
                finish();
            }, 2000);
        }

    }
}