package com.example.chitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.chitchat.R;
import com.hbb20.CountryCodePicker;

public class Login extends AppCompatActivity {

    CountryCodePicker CountryCodeid;
    EditText ed1;
    ProgressBar pronumber;
    Button b1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CountryCodeid = findViewById(R.id.CountryCodeid);
        ed1 = findViewById(R.id.Ed1);
        pronumber = findViewById(R.id.prograssbar);
        b1 = findViewById(R.id.button);

        CountryCodeid.registerCarrierNumberEditText(ed1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!CountryCodeid.isValidFullNumber()){
                    ed1.setError("Phone Number is Not Valid");
                    return;
                }
                Intent intent = new Intent(Login.this, LoginOTP.class);
                intent.putExtra("phone",CountryCodeid.getFullNumberWithPlus());
                startActivity(intent);
            }
        });



    }
}