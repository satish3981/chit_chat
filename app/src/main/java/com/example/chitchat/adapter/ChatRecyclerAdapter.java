package com.example.chitchat.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chitchat.R;
import com.example.chitchat.model.ChatMessageModel;
import com.example.chitchat.utils.firebaseutil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    Context context;
    String recieverId;
    private String chatroomId;
    private Activity activity;
    ArrayList<ChatMessageModel> chatMessageModels;
    String recId;
    private List<ChatMessageModel> msgDtoList;


    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context, String chatroomId, Activity activity, String recId) {
        super(options);
        this.context = context;
        this.chatroomId=chatroomId;
        this.activity = activity;
        this.recId=recId;

    }



    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {

        Log.d("ChatRecyclerAdapter","Binding message: "+model.getMessage()+" with URL: "+model.getFileUrl()+" Type: "+ model.getMessageType());


        String messageId = getSnapshots().getSnapshot(position).getId();
        String messageType = model.getMessageType();
        String fileUrl = model.getFileUrl();
       // String message = messageList.get(position);

        if (messageId == null){
            holder.itemView.setVisibility(View.GONE);
            return;
        }
        boolean isCurrentUser = model.getSenderId() != null && model.getSenderId().equals(firebaseutil.currentuserid());



        if(isCurrentUser){
           holder.leftChatLayout.setVisibility(View.GONE);
           holder.rightChatLayout.setVisibility(View.VISIBLE);


            if ("image".equalsIgnoreCase(messageType)) {
                holder.rightChatImageView.setVisibility(View.VISIBLE);
                holder.rightChatTextview.setVisibility(View.GONE);
                Glide.with(context).load(fileUrl).into(holder.rightChatImageView);

            } else {
                holder.rightChatImageView.setVisibility(View.GONE);
                holder.rightChatTextview.setVisibility(View.VISIBLE);
                holder.rightChatTextview.setText( model.getMessage());
            }

       }
       else{
           holder.rightChatLayout.setVisibility(View.GONE);
           holder.leftChatLayout.setVisibility(View.VISIBLE);


            if ("image".equalsIgnoreCase(messageType)) {
                holder.leftChatImageView.setVisibility(View.VISIBLE);
                holder.leftChatTextView.setVisibility(View.GONE);
                Glide.with(context).load(fileUrl).into(holder.leftChatImageView);
            } else {
                holder.leftChatTextView.setVisibility(View.VISIBLE);
                holder.leftChatImageView.setVisibility(View.GONE);
                holder.leftChatTextView.setText( model.getMessage());
            }
        }


       View.OnLongClickListener longClickListener = v ->{
           FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

           if (currentUser != null) {
               recieverId = currentUser.getUid();
           }
           String senderRoom = FirebaseAuth.getInstance().getCurrentUser().getUid()+recieverId;
           showDeleteConfirmationDialog(messageId,chatroomId , model.getMessage());
           return true;
       };

       if(isCurrentUser)
       {
           holder.rightChatTextview.setOnLongClickListener(longClickListener);
           holder.rightChatImageView.setOnLongClickListener(longClickListener);
       }else {
           holder.leftChatTextView.setOnLongClickListener(longClickListener);
           holder.leftChatImageView.setOnLongClickListener(longClickListener);
       }
    }
    private void showDeleteConfirmationDialog(String messageId,String chatroomId ,String currentMessage ){

        if (activity != null && !activity.isFinishing()) {
            if (messageId == null || chatroomId == null){
                Log.e("ChatRecyclerAdapter","MessageId Or ChatRoomId Is Null");
                Toast.makeText(activity, "Invalid", Toast.LENGTH_SHORT).show();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Do you want to delete this message?");
            builder.setPositiveButton("Delete", (dialog, which) ->{
                deleteMessage(messageId,chatroomId);
            });
            builder.setNeutralButton("Edit", (dialog, which) -> {
                editMessage(messageId,chatroomId,currentMessage);

            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        }


    }


private void editMessage(String messageId , String chatroomId , String crrentmessage){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Edit Messsage");

        final EditText input = new EditText(activity);
        input.setText(crrentmessage);
        builder.setView(input);

        builder.setPositiveButton("save",(dialog, which) -> {
            String editMessage = input.getText().toString().trim();
            if (!editMessage.isEmpty()){
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("chatrooms")
                        .document(chatroomId)
                        .collection("chats")
                        .document(messageId)
                        .update("message",editMessage)
                        .addOnSuccessListener(aVoid ->{
                            Toast.makeText(activity, "Message Edited", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(activity, "Fail", Toast.LENGTH_SHORT).show();
                        });
            }else{
                Toast.makeText(activity, "Empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNeutralButton("cancel",(dialog, which) -> {dialog.dismiss();});
        builder.show();

}
    private void deleteMessage(String messageId,String chatroomId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Log.e("ChatRecyclerAdapter", "Deleteing Message With Id :" + messageId + " From Chatroom Id " + chatroomId);
        if (messageId != null && chatroomId != null) {
            firestore.collection("chatrooms")
                    .document(chatroomId)
                    .collection("chats")
                    .document(messageId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(activity, "Message Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ChatRecyclerAdapter", "Failed To delete message", e);
                        Toast.makeText(activity, "Failed to delete message ", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(activity, "Message Id wrong", Toast.LENGTH_SHORT).show();
        }
    }
    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row,parent,false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder{

        LinearLayout leftChatLayout,rightChatLayout , rightFileLayout , leftFileLayout;
        TextView leftChatTextView,rightChatTextview ,rightFileNaame ,leftFileName;
        ImageView leftChatImageView,rightChatImageView , rightFileIcon , leftFileIcon ;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextView = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            leftChatImageView= itemView.findViewById(R.id.left_chat_imageview);
            rightChatImageView= itemView.findViewById(R.id.right_chat_imageview);






        }
    }
}
