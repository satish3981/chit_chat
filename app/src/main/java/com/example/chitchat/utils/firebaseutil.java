package com.example.chitchat.utils;

import com.example.chitchat.model.ChatMessageModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class firebaseutil {



    public static String currentuserid(){
        return FirebaseAuth.getInstance().getUid();
    }
    public static boolean isLoggedin(){
        if (currentuserid()!=null){
            return  true;
        }
        return false;
    }
    public static DocumentReference currentuserdetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentuserid());
    }
    public static CollectionReference allUserCollectionReference()
    {
        return FirebaseFirestore.getInstance().collection("users");
    }
    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }
    public  static CollectionReference getChatroomMessageReference(String chatroomId)
    {
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static String getChatroomId(String userId1,String userId2)
    {
        if(userId1==null||userId2==null)
        {
            throw new IllegalArgumentException("User ID Must Not Be Null");
        }
        return userId1.hashCode()<=userId2.hashCode()?
                userId1+"_"+userId2:
                userId2+"_"+userId1;
//        if(userId1.hashCode()<userId2.hashCode()){
//            return userId1+"_"+userId2;
//        }
//        else {
//            return userId2+"_"+userId1;
//        }

    }



    public static CollectionReference allChatroomCollectionReference(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }
    public static DocumentReference getOtherUserFromChatroom(List<String>userIds){
        if (userIds.get(0).equals(firebaseutil.currentuserid())){
           return allUserCollectionReference().document(userIds.get(1));
        }
        else {
           return allUserCollectionReference().document(userIds.get(0));
        }
    }
    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH.MM").format(timestamp.toDate());
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }
    public static Task<Void> addChatMessage(String chatroomId , ChatMessageModel chatMessageModel)
    {
        DocumentReference docRef = getChatroomMessageReference(chatroomId).document();
        chatMessageModel.setMessageId(docRef.getId());
//        docRef.set(chatMessageModel)
//                .addOnSuccessListener(aVoid -> Log.d("firebaseutil","Message successfully Added!"))
//                .addOnFailureListener(e-> Log.e("firebaseutil","Error adding message",e));
        return docRef.set(chatMessageModel);
    }
    public static StorageReference getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(firebaseutil.currentuserid());
    }

    public static StorageReference getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(otherUserId);
    }
    public static  CollectionReference allUserCollectionRef(){
        return FirebaseFirestore.getInstance().collection("users");
    }
}
