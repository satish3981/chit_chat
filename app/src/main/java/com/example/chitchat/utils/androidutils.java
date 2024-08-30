package com.example.chitchat.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chitchat.model.Usermodel;

public class androidutils {
    public static void showToast(Context context,String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    public static void passUserModelAsIntent(Intent intent, Usermodel model){
        intent.putExtra("username",model.getUsername());
        intent.putExtra("phone",model.getPhone());
        intent.putExtra("userId",model.getUserId());
    }
    public static Usermodel getUserModelFromIntent(Intent intent){
        Usermodel usermodel = new Usermodel();
        usermodel.setUsername(intent.getStringExtra("username"));
        usermodel.setPhone(intent.getStringExtra("phone"));
        usermodel.setUserId(intent.getStringExtra("userId"));
        usermodel.setFcmToken(intent.getStringExtra("fcmToken"));

        return usermodel;
    }
    public static void setProfilePic(Context context, Uri imageUri ,ImageView imageView)
    {
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

}
