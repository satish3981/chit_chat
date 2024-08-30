package com.example.chitchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chitchat.utils.firebaseutil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;

public class home extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageButton searchbtn;

    Button aiBtn;
    ChatFragment chatFragment;
    ProfileFragment profileFragment;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();

//        aiBtn = findViewById(R.id.aibtn);
//        aiBtn.setOnClickListener(v -> {
//         Intent i = new Intent(home.this,aichat.class);
//         startActivity(i);
//        });

        bottomNavigationView = findViewById(R.id.bottomnavigate);
        searchbtn = findViewById(R.id.main_search_btn);
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(home.this,SearchUserActivity.class));
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.menu_chart){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout,chatFragment).commit();
                }
                if(item.getItemId()==R.id.menu_profile){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout,profileFragment).commit();
                }
                if(item.getItemId()==R.id.menu_ai){
                    Intent i = new Intent(home.this,aichat.class);
                    startActivity(i);

                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.menu_chart);

        getFCMToken();
    }


    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
           if (task.isSuccessful())
           {
               String token = task.getResult();
               firebaseutil.currentuserdetails().update("fcmToken",token);
           }
        });
    }
}