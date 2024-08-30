package com.example.chitchat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.adapter.SearchUserRecyclerAdapter;
import com.example.chitchat.model.Usermodel;
import com.example.chitchat.utils.firebaseutil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;


public class SearchUserActivity extends AppCompatActivity {

    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;
    TextView txt;

    SearchUserRecyclerAdapter adapter;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);


        txt= findViewById(R.id.txtname);
        searchInput = findViewById(R.id.search_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycler_view);


        searchInput.requestFocus();


        backButton.setOnClickListener(v -> {
            //noinspection deprecation
            onBackPressed();
        });


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchInput.getText().toString();
                if(searchTerm.isEmpty() || searchTerm.length()<2)
                {
                    searchInput.setError("Invalid Username");

                }
                else
                {
                    setupSearchRecyclerView(searchTerm);
                    searchInput.setText(" ");
                }

            }
        });


    }
    void setupSearchRecyclerView(String searchTerm) {
        firebaseutil.allUserCollectionReference().get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                for(DocumentSnapshot document : task.getResult()) {
                    String username = document.getString("username");
                    if(username != null){
                        String lowercaseUsername = username.toLowerCase();

                        document.getReference().update("username_lowercase",lowercaseUsername);
                    }
                }
            }
        });
//        String capitalizedSearchTerm = searchTerm.substring(0,1).toUpperCase()+searchTerm.substring(1).toLowerCase();
//
        String lowercaseSearchTerm = searchTerm.toLowerCase();
        Query query = firebaseutil.allUserCollectionReference()
//                .whereGreaterThanOrEqualTo("username",searchTerm);
                .orderBy("username_lowercase")
                .startAt(lowercaseSearchTerm)
                .endAt(lowercaseSearchTerm + "\uf8ff");



        FirestoreRecyclerOptions<Usermodel> options = new FirestoreRecyclerOptions.Builder<Usermodel>()
                .setQuery(query,Usermodel.class)
                .build();




            adapter = new SearchUserRecyclerAdapter(options, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            adapter.startListening();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(adapter!=null)
        {
            adapter.startListening();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
        {
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
        {
            adapter.startListening();
        }

    }
}

