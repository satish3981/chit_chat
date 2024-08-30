package com.example.chitchat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.adapter.ChatRecyclerAdapter;
import com.example.chitchat.model.ChatMessageModel;
import com.example.chitchat.model.ChatroomModel;
import com.example.chitchat.model.Usermodel;
import com.example.chitchat.utils.androidutils;
import com.example.chitchat.utils.firebaseutil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    Usermodel otheruser;
    String checker="";
    String chatroomId;
    Activity activity;
    ImageButton filepicker;

    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;

    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    private Uri fileUri;
    private Uri uriFile;
    private StorageTask uploadTask;
    private String editMessageId = null;
    private boolean isEditMode = false;

private String myUrl="";
private  boolean isEditing = false;
private  String editingMessageId = null;
    StorageReference storageReference;

    RecyclerView recyclerView;
    private String chatId;
    String recieverId;
     ArrayList<ChatMessageModel> chatMessageModels;
     ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        FirebaseApp.initializeApp(this);
        //get usermodel
        otheruser =androidutils.getUserModelFromIntent(getIntent());
        if(otheruser==null)
        {
            Log.e("ChatActivity", "otheruser is null");
        }
//        recieverId= getIntent().getStringExtra("userId")
//       ;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            recieverId = currentUser.getUid();
        }else {
            throw  new IllegalArgumentException("Current user must be logged in ");
        }




        chatroomId = firebaseutil.getChatroomId(firebaseutil.currentuserid(), otheruser.getUserId());
        if(chatroomId == null || otheruser.getUserId() == null){
           throw  new IllegalArgumentException("other user model or Id must not be null");
        }
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        filepicker = findViewById(R.id.attach_file);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);

        firebaseutil. getOtherProfilePicStorageRef(otheruser.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()){
                        Uri uri = t.getResult();
                        androidutils.setProfilePic(this,uri,imageView);
                    }
                });

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );

        checkPermissions();

        filepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                    //noinspection deprecation
                startActivityForResult(Intent.createChooser(intent,"select File "),PICK_FILE_REQUEST);
            }
        });




        backBtn.setOnClickListener((v) -> {
            //noinspection deprecation
            onBackPressed();
        });
        otherUsername.setText(otheruser.getUsername());


        sendMessageBtn.setOnClickListener((v ->{
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty()){
                return;
            } else  {
                    sendMessageToUser(message);



            }


        }));
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
//            if(task.isSuccessful()){
//                String tokan = task.getResult();
//
//                FirebaseFirestore.getInstance().collection("users")
//                        .document(currentUserId)
//                        .update("fcmTokan",tokan);
//            }else{
//                Log.e("FCM Tokan","FCM Tokan Failed",task.getException());
//            }
//        });

        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }



    private  void checkPermissions()
    {
       if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
           ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},PICK_FILE_REQUEST);
       }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode==RESULT_OK && data!= null && data.getData() != null)
        {
             fileUri = data.getData();
            if(fileUri !=  null)
            {
               String fileType = getContentResolver().getType(fileUri);
               if (fileUri != null){
                   uploadFile(fileUri );
               }
            }else{
                Log.e("ChatActivity","File URI Is Null");
            }

        }
    }

    private void uploadFile(Uri fileUri) {

        if (fileUri == null){
            Log.e("ChatActivity", "Cannot upload a null URI");
            return;
        }
        String fileExtention = getFileExtention(fileUri);


        if (fileExtention==null){
            Log.e("ChatActivity", "File extention could not be determined.");
            return;
          }
             String filename = UUID.randomUUID().toString()+"."+fileExtention;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("files/" + System.currentTimeMillis() + "." + filename);

        storageReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String fileUrl= uri.toString();
                                Log.d("ChatActivity","File URL : "+fileUrl);
                                sendMessageWithFileUrl(fileUrl,fileExtention);
                            })
                            .addOnFailureListener(e -> {
                                if(e instanceof StorageException && ((StorageException)e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND){
                                    Log.e("ChatActivity"," File not Found ",e);
                                }else {
                                    Log.e("ChatActivity","Some Other Error Occurred",e);
                                }

                    });
                });



    }

    private String getFileExtention(Uri fileUri) {



        ContentResolver cr = getContentResolver();

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(cr.getType(fileUri));
    }

    void setupChatRecyclerView(){
        Query query = firebaseutil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp",Query.Direction.DESCENDING);
        query.addSnapshotListener((snapshots,e)->{
            if (e != null){
                Log.w("ChatActivity","Listen failed.",e);
                return;
            }
            if(snapshots != null){
                adapter.notifyDataSetChanged();
            }
        });

        FirestoreRecyclerOptions<ChatMessageModel> options= new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();


        adapter= new ChatRecyclerAdapter(options,getApplicationContext(),chatroomId, this,recieverId);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void sendMessageWithFileUrl(String fileUrl , String fileExtension){
        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String messageId = FirebaseDatabase.getInstance().getReference().push().getKey();


        String messageType;
        if (fileExtension != null) {
            if (fileExtension.equals("jpg") || fileExtension.equals("jpeg") || fileExtension.equals("png") ) {
                messageType = "image";
            } else if (fileExtension.equalsIgnoreCase("mp4") || fileExtension.equalsIgnoreCase("avi")) {
                messageType = "video";
            } else if (fileExtension.equalsIgnoreCase("pdf")) {
                messageType = "pdf";
            } else if (fileExtension.equalsIgnoreCase("doc") || fileExtension.equalsIgnoreCase("docx")) {
                messageType = "doc";
            } else {
                messageType = "file";
            }
        } else {
            messageType = "file";
        }
       ChatMessageModel chatMessageModel= new ChatMessageModel();

        chatMessageModel.setSenderId(senderId);
        chatMessageModel.setMassageType(messageType);
        chatMessageModel.setFileUrl(fileUrl);
        chatMessageModel.setTimestamp(Timestamp.now());

        firebaseutil.addChatMessage(chatroomId,chatMessageModel)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("ChatActivity","File Sent");
                    }else{
                        Log.e("ChatActivity","fail To sent file", task.getException());
                    }
                });

//        DatabaseReference chatRef = FirebaseDatabase.getInstance()
//                .getReference("chats")
//                .child(chatroomId)
//                .child("messages");
//
//        chatRef.child(UUID.randomUUID().toString()).setValue(chatMessageModel)
//                .addOnSuccessListener(aVoid ->
//                {
//                    Log.d("ChatActivity","message sent Successfully");
//                })
//                .addOnFailureListener(e ->
//                {
//                    Log.e("ChatActivity","Failed Message sending ",e);
//                });
    }
     void sendMessageToUser(String message) {

        if (isEditing &&editingMessageId != null){
            firebaseutil.getChatroomMessageReference(chatroomId)
                    .document(editingMessageId)
                    .update("message",message)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            messageInput.setText("");

                            isEditing=false;
                            editingMessageId =null;
                            Toast.makeText(this, "Message Updated", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(this, "Not Updated", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            chatroomModel.setLastMessageTimestamp(Timestamp.now());
            chatroomModel.setLastMassageSenderId(firebaseutil.currentuserid());
            chatroomModel.setLastMessage(message);
            firebaseutil.getChatroomReference(chatroomId).set(chatroomModel);

//            String fileUrl= fileUri!=null ?fileUri.toString():"";
            String messageType= fileUri!=null ?"file":"text";

            ChatMessageModel chatMessageModel= new ChatMessageModel(
                    message.isEmpty()?"Send a File" : message,
                    firebaseutil.currentuserid(),
                    Timestamp.now(),
                    messageType
            );
            firebaseutil.addChatMessage(chatroomId,chatMessageModel)
                    .addOnCompleteListener(new OnCompleteListener<Void>(){

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                messageInput.setText("");
                                sendNotification(message);

//                                fileUri=null;
                            }
                        }
                    });
        }




    }


    void getOrCreateChatroomModel() {

        firebaseutil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null) {
                    // first timeout
                    chatroomModel = new ChatroomModel(
                            chatroomId,
                            Arrays.asList(firebaseutil.currentuserid(), otheruser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    firebaseutil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }

    void sendNotification(String message){

        firebaseutil.currentuserdetails().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Usermodel currentUser = task.getResult().toObject(Usermodel.class);
                try{
//                    JSONObject messagetokan = new JSONObject();
//                    messagetokan.put("token",currentUser.getFcmToken());

                    JSONObject jsonObject= new JSONObject();
                    jsonObject.put("to",otheruser.getFcmToken());

;
                    JSONObject notificationObj= new JSONObject();
                    notificationObj.put("title",currentUser.getUsername());
                    notificationObj.put("body",message);


//                    JSONObject dataObj= new JSONObject();
//                    dataObj.put("userId",currentUser.getUserId());

                    jsonObject.put("notification",notificationObj);
                  //  jsonObject.put("data",dataObj);


                    callApi(jsonObject);

                }catch (Exception e){

                }
            }
        });

    }
    void callApi(JSONObject jsonObject){
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/v1/projects/chitchat-back/messages:send";
       // String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        String accessToken = "ya29.c.c0ASRK0GaJ2tYLclrNe45t4waukrzkuhzigveC3YiO4JqjpIu3leD5_4owuMEYv-bcHIsp6vw8RZtNBdK4BsLQggMCv_lISSCPTGmjLxXBc6kCXnuouzsg8YJ7mwcch9zhMvE3G5taoS8pWmBTs6zeHCaak4DWdVKCfsSqXnbS1URFTw9JIn0jabDbX-ZMPv7pozCygLrUXS5XiMgTrACXsmULfdUokYd4KXa5pQ08iYFbOVosxYhaL9eQZ2tlG2r6nE9cUtBmCWAznsaHF_ROmSujRHEkVg47q18fj9Jq71_yrjB80xurWO6k1BdzexTEEOveNjP6RhCvFVFRE8vhzzAuBRtq5zmWQVUs4YGb9agAfu3it3NtnuIE384Csk1FwaSMMkQZm3e4_uOMrv5BucRr5We0Szcna73wscnrtvabpa7mn7txF8cjrn5tuRq6MzuzhU2BRs79b0dbU1-bbm7ZlxbSog9Fya_XvXqVgbyx0nwaWS3z08aouMmRIusUW09qinqOfXdqOMigo83rr_0YJOOgBwUj82b2r7Oa6ySxQsZ7wdvrFVetSbOqgjswW-Rv9nr19RRUvRpdmbJ6ZpBtrQsaUiWwpMJe-4ZMzq8Jop3FYoFjZapboSjWgcJjO4rq0wa_Jxc6p5inUeVMYq-FbJ4ba1rXeuk2c5pqadWnsasypFe3tnFYyvzb7gZSXp2wVUheuZvu0bs3W_O3B2Mpz1ak2px1RzzMJg_p-fB9FcVp5VlbddvdnOJn8bOhydg-F61y_7JcJR7U2jtMJed4JMFboeucmsj8qdt5787_efRSoabcgrgX88j7WRS9xQ0w0yI-a6O_V3q1zcWRWyIVpQovBfQYfuXvcpXiIfrF5dca6wFSiYcUssx3blx7bMWkSacttx2X5JeUc8Vz0ztRFqkzw0e-vljRvOdYsRntoQ5b37QgOz947fwi53pk0ixOpFoMkl99Whccm1n3wguzuOQS41Y0FOIrV53M3miM1MX3v4tu3mB";

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer "+ accessToken)
                .header("content-Type","application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if(!response.isSuccessful()){
                    System.out.println("ERROR:" + response.body().toString());
                }else{
                    System.out.println("SUCCESS:" + response.body().toString());
                }
            }
        });
    }

}