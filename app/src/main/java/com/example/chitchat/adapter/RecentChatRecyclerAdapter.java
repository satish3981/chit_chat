package com.example.chitchat.adapter;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.ChatActivity;
import com.example.chitchat.R;
import com.example.chitchat.model.ChatroomModel;
import com.example.chitchat.model.Usermodel;
import com.example.chitchat.utils.androidutils;
import com.example.chitchat.utils.firebaseutil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
              firebaseutil.getOtherUserFromChatroom(model.getUserIds())
                      .get().addOnCompleteListener(task -> {
                         if (task.isSuccessful()){
                             boolean lastMessageSentByMe = model.getLastMassageSenderId().equals(firebaseutil.currentuserid());


                             Usermodel otherUserModel = task.getResult().toObject(Usermodel.class);

                             firebaseutil. getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                     .addOnCompleteListener(t -> {
                                         if (t.isSuccessful()){
                                             Uri uri = t.getResult();
                                             androidutils.setProfilePic(context,uri,holder.profilepic);
                                         }
                                     });

                             holder.usernameText.setText(otherUserModel.getUsername());

                             if (lastMessageSentByMe){
                               holder.lastMessageText.setText("You : "+model.getLastMessage());
                             }
                             else
                                 holder.lastMessageText.setText(model.getLastMessage());
                             holder.lastMessageTime.setText(firebaseutil.timestampToString(model.getLastMessageTimestamp()));

                             holder.itemView.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     //chat activity
//                Log.d("RecyclerView","User clicked"+model.getUsername());
//                Intent intent= new Intent(context, ChatActivity.class);
//                androidutils.passUserModelAsIntent(intent,model);
//                context.startActivity(intent);

                                     Intent intent = new Intent(context, ChatActivity.class);
                                     androidutils.passUserModelAsIntent(intent,otherUserModel);
                                     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     context.startActivity(intent);
                                 }
                             });

                         }
                      });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row,parent,false);
        return new ChatroomModelViewHolder(view);
    }

    static class ChatroomModelViewHolder extends RecyclerView.ViewHolder{

        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilepic;
        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilepic = itemView.findViewById(R.id.profile_pic_image_view);

        }
    }
}
