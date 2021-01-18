package com.adnan.photoblog;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
public List<Notifications> NotificationList;
    //private List<BlogPost> blog_list;
private Context context;
private FirebaseFirestore firebaseFirestore;
private FirebaseAuth firebaseAuth;
    private static final String TAG = "CommentAdapter";
    String currentUserId;
public NotificationAdapter(List<Notifications> NotificationList){
    this. NotificationList=NotificationList;
   // this.blog_list=blog_list;
}

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notifications_list, parent, false);
context=parent.getContext();
firebaseFirestore=FirebaseFirestore.getInstance();

        return new NotificationViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
//holder.setIsRecyclable(false);
        firebaseAuth=FirebaseAuth.getInstance();
      currentUserId=firebaseAuth.getCurrentUser().getUid();
        // prepare the values to send the to cardview
        String toUserName=NotificationList.get(position).getToUserName();//user who recive likes or comment

        String fromUserName=NotificationList.get(position).getFromUserName();//user who does like or comment
        holder.setfromUsername(fromUserName);
        String type1=NotificationList.get(position).getType();
        String desc1=NotificationList.get(position).getDesc1();
        String NotificationMessage;
        if(type1.equals("likes")){
            type1="likes";
        }else if(type1.equals("comments")){
            type1="comments on";
        }
         NotificationMessage=fromUserName+" "+type1+" your post ("+desc1+" )";
        holder.setnotificationMessage(NotificationMessage);
        String fromUserImageUri=NotificationList.get(position).getFromUserNameImageUri();
        holder.setfromUserImageView(fromUserImageUri);



        //get the data from firebase to display userName snd User Image
//        firebaseFirestore.collection("usersPhotoBlog").document(currentUserId).get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if(task.isSuccessful()){
//                            String userName=task.getResult().getString("name");
//                            String userNameimage=task.getResult().getString("image");
//                    //        holder.setUserName(userName);
//                      //      holder.SetUserNameCommentImage(userNameimage);
//                        }else{
//                            String errorMessage=task.getException().getMessage();
//                            Log.i(TAG, "onComplete: Error" +errorMessage);
//                        }
//                    }
//                });

    }

    @Override
    public int getItemCount() {
       if(NotificationList!=null){
           return NotificationList.size();
       }
       else {
           return 0;
       }
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
    private View mView;

        private CircleImageView fromUserImageView;
        private TextView fromUsernameTextView;
        private TextView notificationMessageTextView;
        private TextView postDescriptionTextView;
        private TextView notificationDate;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setnotificationMessage(String notificationMessage){
            notificationMessageTextView=mView.findViewById(R.id.notification_list_tv_message);
            notificationMessageTextView.setText(notificationMessage);
        }
        public void setfromUsername(String fromUsername){
            fromUsernameTextView=mView.findViewById(R.id.notification_list_tv_from_user_name);
            fromUsernameTextView.setText(fromUsername);
        }
        public void setfromUserImageView(String imageUri){
            fromUserImageView=mView.findViewById(R.id.notification_list_circleImageView_from_user_image);
            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.default_image);
            Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(imageUri).into(fromUserImageView);
        }
        public void SetDate(String date){
            notificationDate=mView.findViewById(R.id.notification_list_tv_date);
            notificationDate.setText(date);
        }

        public void setDescText(String desc){
            postDescriptionTextView=mView.findViewById(R.id.notification_list_tv_post_desc);
            postDescriptionTextView.setText(desc);
        }
    }
}
