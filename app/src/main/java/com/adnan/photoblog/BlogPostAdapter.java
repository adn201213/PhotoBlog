package com.adnan.photoblog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
public class BlogPostAdapter extends RecyclerView.Adapter<BlogPostAdapter.BlogPostViewHolder> {
    private List<BlogPost> blog_list;
    private List<User> user_list1;
    private List<User> user_list;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    SharedPreferences sharedpreferences;
    private static final String TAG = "BlogPostAdapter";
    String post_userNameId;
    public BlogPostAdapter(List<BlogPost> blog_list,List<User> user_list){
        this.blog_list=blog_list;
        this.user_list=user_list;
     }
    public void setBlog_list(List<BlogPost> blog_list) {
        this.blog_list = blog_list;
    }
    public void setUser_list(List<User> user_list) {
        this.user_list = user_list;
    }
    @NonNull
    @Override
    public BlogPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
       context=parent.getContext();
       firebaseFirestore=FirebaseFirestore.getInstance();
       firebaseAuth=FirebaseAuth.getInstance();
        return new BlogPostViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull BlogPostViewHolder holder, int position) {
            holder.setIsRecyclable(false);
      String blogPostId=blog_list.get(position).BlogPostId;
holder.updateCommentCount(blogPostId);
        String currentUserId=firebaseAuth.getCurrentUser().getUid();
        //Set Description Text
     String desc=blog_list.get(position).getDesc();
     holder.setDescText(desc);
     //Set Image
        String image_uri=blog_list.get(position).getImageOriginalUri();
        String imageThumbs_uri=blog_list.get(position).getImageThumbUri();
         post_userNameId=blog_list.get(position).getUser_id();

        holder.setBlogImage(image_uri,imageThumbs_uri);
        //set user data username and image
        String blog_user_id=blog_list.get(position).getUser_id();
        holder.blogDeleteButton.setEnabled(false);
        if(blog_user_id.equals(currentUserId)){
            holder.blogDeleteButton.setEnabled(true);
            holder.blogDeleteButton.setVisibility(View.VISIBLE);
        }
        String userName=user_list.get(position).getName();
        String userImage=user_list.get(position).getImage();
        String post_userToken=user_list.get(position).getToken();
        holder.setUserData(userName,userImage);
        //set Date
        try {
            long millisecond = blog_list.get(position).getTimestamp().getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = formatter.format(new Date(millisecond));
            holder.SetDate(dateString);
        }catch(Exception e){
            Toast.makeText(context, "Exception" +e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //get Likes Counts
        firebaseFirestore.collection("posts/"+ blogPostId + "/Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty()){
                    int count=value.size();
                    holder.updatCount(count);
                }else{
                    holder.updatCount(0);
                }
            }
        });
        //get likes
        firebaseFirestore.collection("posts/"+ blogPostId + "/Likes")
                .document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    holder.likesImageViewBtn.setImageDrawable(context.getDrawable(R.drawable.blog_list_item_accent_like_icon));
                }else{
                    holder.likesImageViewBtn.setImageDrawable(context.getDrawable(R.drawable.blog_list_item_grey_like_icon));
                }
            }
        });

    //this for handle likes
        holder.likesImageViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("posts/"+ blogPostId + "/Likes")
                        .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    if (!task.getResult().exists()) {
                        Map<String, Object> likesMap = new HashMap<>();
                        likesMap.put("timestamp", FieldValue.serverTimestamp());
                        likesMap.put("blogPostId", blogPostId);
                        firebaseFirestore.collection("posts/" + blogPostId + "/Likes")
                                .document(currentUserId).set(likesMap);
                        String userName=user_list.get(position).getName();
                        String userImage=user_list.get(position).getImage();
                        String userToken=user_list.get(position).getToken();
                        Log.i(TAG, "onComplete:userToken " +userToken);
                      //  Log.i(TAG, "onComplete:userToken "+userToken);
                        //this mrthod to send notification
                        Log.i(TAG, "onComplete:if "+post_userNameId +"  "+currentUserId );
                        if(!post_userNameId.equals(currentUserId)) {
                            holder.setFcmNotificationData(position);
                        }
                    } else {
                        firebaseFirestore.collection("posts/" + blogPostId + "/Likes")
                                .document(currentUserId).delete();
                    }
                }
                    }
                });
            }
        });
        //handel comments
holder.imageViewCommentsBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //this line to send notification data to comment activity
       holder.senNotificationDataToComment(position);
        Intent commentIntent=new Intent(context,CommentActivity.class);
        commentIntent.putExtra("blogPostId", blogPostId);
        Log.i(TAG, "onClick:blogPostId "+blogPostId);
        context.startActivity(commentIntent);
    }
});
//delete button
        holder.blogDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Do your Yes progress
                                holder.deleteSubCollection(blogPostId, "comments");
                                holder.deleteSubCollection(blogPostId, "Likes");
                                firebaseFirestore.collection("posts")
                                        .document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        blog_list.remove(position);
                                        user_list.remove(position);
                                        setBlog_list(blog_list);
                                        setUser_list(user_list);
                                        notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //Do your No progress
                                break;
                        }
                    }
                };
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setMessage("Are you sure to delete?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }
    @Override
    public int getItemCount() {
        return blog_list.size();
    }
    public class BlogPostViewHolder extends RecyclerView.ViewHolder{
     private View mView;
     private TextView textViewDesc;
        private TextView textViewUserName;
        private TextView blogDate;
    private ImageView imageViewPostImage;
     private CircleImageView blogImageView;
        private ImageView testImage;
private Context context;
     //likes and text view
        private ImageView likesImageViewBtn;
        private TextView textViewLikesCount;
        //comments
        private ImageView imageViewCommentsBtn;
        private  TextView textViewComment;
        private TextView textViewCommentsCount;
        //delete button
        private ImageView blogDeleteButton;
        public  static final String CHANNEL_ID="blog1_channelId";
       public BlogPostViewHolder(@NonNull View itemView) {
           super(itemView);
           context=itemView.getContext();
        mView=itemView;
           likesImageViewBtn=mView.findViewById(R.id.blog_list_item_iv_blog_like);
         //  textViewLikesCount =mView.findViewById(R.id.blog_list_item_tv_blog_like_count);
           imageViewCommentsBtn=mView.findViewById(R.id.blog_list_item_iv_comment);
           textViewComment=mView.findViewById(R.id.blog_list_item_tv_comment_count);
           blogDeleteButton=mView.findViewById(R.id.blog_list_item_iv_delete_btn1);
           testImage=mView.findViewById(R.id.blog_list_item_iv_comment);
       }
       public void setDescText(String desc){
           textViewDesc=mView.findViewById(R.id.blog_list_item_tv_post_text);
           textViewDesc.setText(desc);
       }
        public void setBlogImage(String downloadUri,String imageThumbs_uri){
            imageViewPostImage=mView.findViewById(R.id.blog_list_item_iv_postImage);
            Glide.with(context).load(downloadUri).thumbnail( Glide.with(context).load(imageThumbs_uri)).into(imageViewPostImage);
        }
        public void setUserData(String userName,String image){
            textViewUserName=mView.findViewById(R.id.blog_list_item_tv_username);
            textViewUserName.setText(userName);
            blogImageView=mView.findViewById(R.id.blog_list_item_circleImageView);
            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.default_image);
           Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(image).into(blogImageView);
        }
        public void SetDate(String date){
            blogDate=mView.findViewById(R.id.blog_list_item_tv_post_date);
            blogDate.setText(date);
        }
        public void updatCount(int count){
            textViewLikesCount=mView.findViewById(R.id.blog_list_item_tv_blog_like_count);
            textViewLikesCount.setText(count +"likes");
        }
        public void updateCommentCount(String blogPostId){
            textViewCommentsCount=mView.findViewById(R.id.blog_list_item_tv_comment_count);
            firebaseFirestore.collection("posts" ).document(blogPostId).collection("comments")
                    .whereEqualTo("blogPostId", blogPostId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                  //  WriteBatch batch=FirebaseFirestore.getInstance().batch();
                    List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                   int count=documentSnapshots.size();
                    textViewCommentsCount.setText(count +"comments");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String errorMessgae=e.getMessage();
                    Log.i(TAG, "onFailure: "+errorMessgae);
                }
            });
        }
        //this method to delete subcollection like comments and likes
        public void deleteSubCollection(String documentPath,String subCollection){
            firebaseFirestore.collection("posts" ).document(documentPath).collection(subCollection)
                    .whereEqualTo("blogPostId", documentPath).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    WriteBatch batch=FirebaseFirestore.getInstance().batch();
                    List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot snapshot:documentSnapshots){
                        batch.delete(snapshot.getReference());
                    }
                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "All recorded Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String errorMessgae=e.getMessage();
                            Log.i(TAG, "onFailure: "+errorMessgae);
                        }
                    });;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String errorMessage=e.getMessage();
                    Log.i(TAG, "onFailure: "+errorMessage);
                }
            });
        }

private void sendRetrofitNotification(String token1,String imageUrl,String fromUser,String toUser,String desc1) {
    Log.i(TAG, "onResponse: " + "Notification send successful outside");
    JsonObject payload = buildNotificationPayload(token1, imageUrl,fromUser,toUser,desc1);
    Log.i(TAG, "sendRetrofitNotification: " + payload);
    // send notification to receiver ID
    NotificationApiService notificationApiService = ApiClient.getApiService();
    Call<JsonObject> call = notificationApiService.sendFcmNotification(payload);
    call.enqueue(
            new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Notification send successful inside",
                                Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onResponse5: " + "Notification send successful1");
                        Log.i(TAG, "onResponse10: " + response.body());
                    } else {
                        Log.i(TAG, "onResponse:notfound " + response.code());
                        Log.i(TAG, "onResponse:notfound " + "notfound");
                    }
                }
                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.i(TAG, "onFailure:retrofit " + t.getMessage());
                }
            });
}
        private JsonObject buildNotificationPayload(String token1,String imageUrl,String fromUser,String toUser,String desc1) {
            // compose notification json payload
            JsonObject payload = new JsonObject();
            payload.addProperty("to", token1);
            // compose data payload here
            JsonObject data = new JsonObject();
            data.addProperty("title", fromUser);
                data.addProperty("message", fromUser + " likes " + toUser + " 's " + "post " + "( " + desc1 + " )");
            data.addProperty("image", imageUrl);
            // add data payload
            payload.add("data", data);
            return payload;
        }

        //this method to save notification data in shared prefrencs and get data anywhere
        public void setFcmNotificationData(int position){
            String currentUserId=firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("usersPhotoBlog").document(currentUserId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().exists()) {
                                    //current user name who will send notification
                                    String currentUsername = task.getResult().getString("name");
                                    String currentUserImage=task.getResult().getString("image");
                                    //token +user name + post description of receiver who owner the post
                                    String post_userId=blog_list.get(position).getUser_id();
                                    firebaseFirestore.collection("usersPhotoBlog")
                                            .document(post_userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (task.getResult().exists()) {
                                                    //token +user name + post description of receiver who owner the post
                                                    String post_userName1=user_list.get(position).name;
                                                    String desc1=blog_list.get(position).getDesc();
                                                   String  post_userToken=task.getResult().getString("token");
                                                    Log.i(TAG, "onComplete:post_userTokennew String post_userName1 "+ post_userName1 +" "+post_userToken);

                                                    Log.i(TAG, "onComplete:post_userTokennew " +desc1);
                                                    sendRetrofitNotification(post_userToken,currentUserImage,currentUsername ,post_userName1, desc1);

                                                    }
                                                }
                                        }
                                    });

                                }
                            }
                        }
                    });
        }
        public void senNotificationDataToComment(int position){
            String currentUserId=firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("usersPhotoBlog").document(currentUserId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().exists()) {
                                    //current user name who will send notification
                                    String currentUsername = task.getResult().getString("name");
                                    String currentUserImage=task.getResult().getString("image");
                                    //token +user name + post description of receiver who owner the post
                                    String post_userId=blog_list.get(position).getUser_id();
                                    firebaseFirestore.collection("usersPhotoBlog")
                                            .document(post_userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (task.getResult().exists()) {
                                                    //token +user name + post description of receiver who owner the post
                                                    String post_userName1=user_list.get(position).name;
                                                    String desc1=blog_list.get(position).getDesc();
                                                    String  post_userToken=task.getResult().getString("token");
                                                    String  post_userId=blog_list.get(position).getUser_id();
                                                    Log.i(TAG, "onComplete:post_userTokennew String post_userName1 "+ post_userName1 +" "+post_userToken);
                                                    Log.i(TAG, "onComplete:post_userTokennew " +desc1);
                                                    //Share Preferences to save the notification data, so I can send and use them in Comment Activity
                                                    sharedpreferences= context.getSharedPreferences("notification",context.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                                    editor.putString("userToken", post_userToken);
                                                    editor.putString("currentUserImage", currentUserImage);
                                                    editor.putString("currentUsername", currentUsername);
                                                    editor.putString("post_userName1", post_userName1);
                                                    editor.putString("desc1", desc1);
                                                    editor.putString("currentUserId", currentUserId);
                                                    editor.putString("post_userId", post_userId);
                                                    editor.commit();

                                                }
                                            }
                                        }
                                    });

                                }
                            }
                        }
                    });

        }

}

    private void getListUser() {

        firebaseFirestore.collection("usersPhotoBlog").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                            return;
                        } else {

                            // Convert the whole Query Snapshot to a list
                            // of objects directly! No need to fetch each
                            // document.
                            List<User> userss = documentSnapshots.toObjects(User.class);
                            Log.i(TAG, "onSuccess:size "+userss.size());
                            Log.i(TAG, "onSuccess:array "+userss);

                            user_list1.addAll(userss);
                            Log.i(TAG, "onSuccess:arrray1 "+ user_list1.get(1));
                        }
                        }
                    }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure::user_list " +e.getMessage());
            }
        });
    }


}
