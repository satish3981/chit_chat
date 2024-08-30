package com.example.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chitchat.model.Usermodel;
import com.example.chitchat.utils.androidutils;
import com.example.chitchat.utils.firebaseutil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText usernameInput;
    EditText phoneInput;
   Button updateProfieBtn;

   Usermodel currentUserModel;
   ProgressBar progressBar;
   TextView logoutBtn;
   ActivityResultLauncher<Intent> imagePickLauncher;
   Uri selectedUri;

   public ProfileFragment(){

   }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
            if (result.getResultCode() == Activity.RESULT_OK){
                Intent data = result.getData();
                if(data != null && data.getData() != null){
                    selectedUri = data.getData();
                    androidutils.setProfilePic(getContext(),selectedUri,profilePic);
                }
            }
                });
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view =  inflater.inflate(R.layout.fragment_profile, container, false);
       profilePic = view.findViewById(R.id.profile_image_view);
       usernameInput = view.findViewById(R.id.profile_username);
        phoneInput= view.findViewById(R.id.profile_phone);
        updateProfieBtn = view.findViewById(R.id.profile_update_Btn);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        logoutBtn = view.findViewById(R.id.logout_btn);

        getUserData();

        updateProfieBtn.setOnClickListener(v -> {
           updateBtnClick();
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if((task.isSuccessful())){
                            firebaseutil.logout();
                            Intent intent = new Intent(getContext() , splash.class );
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                });

            }
        });
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(ProfileFragment.this).cropSquare().compress(512).maxResultSize(512,512)
                        .createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                imagePickLauncher.launch(intent);
                                return null;
                            }
                        });
            }
        });

        return view;
    }

    void updateBtnClick()
    {
        String newusername = usernameInput.getText().toString();
        if(newusername.isEmpty() || newusername.length()<2){
            usernameInput.setError("username at least 2 alphabet");
            return;
        }
        currentUserModel.setUsername(newusername);
        setInprogress(true);

        if(selectedUri != null)
        {
            firebaseutil.getCurrentProfilePicStorageRef().putFile(selectedUri)
                    .addOnCompleteListener(task -> {
                        updateToFirestore();
                    });
        }else{
            updateToFirestore();
        }




    }

    void updateToFirestore()
    {
    firebaseutil.currentuserdetails().set(currentUserModel)
            .addOnCompleteListener(task -> {
                setInprogress(false);
               if(task.isSuccessful())
               {
                   androidutils.showToast(getContext(),"UPDATED");
               }
               else {
                   androidutils.showToast(getContext(),"FAILED,RE-TRY ");
               }
            });
    }
    void getUserData() {
        setInprogress(true);
        firebaseutil.getCurrentProfilePicStorageRef().getDownloadUrl()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Uri uri = task.getResult();
                                androidutils.setProfilePic(getContext(),uri,profilePic);
                            }
                        });
        firebaseutil.currentuserdetails().get().addOnCompleteListener(task -> {
            setInprogress(false);
          currentUserModel = task.getResult().toObject(Usermodel.class);
            usernameInput.setText(currentUserModel.getUsername());
            phoneInput.setText(currentUserModel.getPhone());

        });
    }
    void setInprogress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
           updateProfieBtn.setVisibility(View.GONE);
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            updateProfieBtn.setVisibility(View.VISIBLE);
        }
    }
}