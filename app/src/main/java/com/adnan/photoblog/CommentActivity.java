package com.adnan.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends AppCompatActivity {
    //Variables Declaration
    private androidx.appcompat.widget.Toolbar commentActivitytoolbar;
    private TextView textViewComment;
    private EditText editTextComment;
    private ImageView imageViewSendCommentBtn;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String blogPostId;
    private String current_user_id;
    private RecyclerView commentRecyclerView;
    private List<Comment> commentsList;
    private CommentAdapter commentAdapter;

    private static final String TAG = "CommentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        //ToolBar
        commentActivitytoolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(commentActivitytoolbar);
        getSupportActionBar().setTitle("Comments");
        //FireBase
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        //user and post id
        current_user_id=firebaseAuth.getCurrentUser().getUid();
        blogPostId=getIntent().getStringExtra("blogPostId");
        //variables initialisation
        textViewComment=findViewById(R.id.comment_activity_tv_comment);
        editTextComment=findViewById(R.id.commnt_activity_et_cooment_text);
        imageViewSendCommentBtn=findViewById(R.id.comment_activity_iv_send_comment);
        commentRecyclerView=findViewById(R.id.comment_activity_rv_comment_list);

        //RecyclerView Firebase List
        commentsList=new ArrayList<>();
        commentAdapter=new CommentAdapter(commentsList);
        commentRecyclerView.setHasFixedSize(true);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentRecyclerView.setAdapter(commentAdapter);


        firebaseFirestore.collection("posts/" +blogPostId+ "/comments")
                .addSnapshotListener(CommentActivity.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.isEmpty()) {
                            for (DocumentChange doc : value.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String commentId = doc.getDocument().getId();
                                    Comment comment=doc.getDocument().toObject(Comment.class);
                                    commentsList.add(comment);
                                    commentAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
        imageViewSendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment_message=editTextComment.getText().toString();
                if(comment_message.isEmpty()){
                    return;
                }
                Map<String,Object> commentsMap=new HashMap<>();
                commentsMap.put("message", comment_message);
                commentsMap.put("user_id",  current_user_id);
                commentsMap.put("timestamp", FieldValue.serverTimestamp());
                commentsMap.put("blogPostId", blogPostId);
             firebaseFirestore.collection("posts/" +blogPostId+ "/comments")
                     .add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                 @Override
                 public void onComplete(@NonNull Task<DocumentReference> task) {
                     if(!task.isSuccessful()){

                         Toast.makeText(CommentActivity.this, "Error Posting Comment" +task.getException().getMessage(),
                                 Toast.LENGTH_LONG).show();
                     }else{
                       editTextComment.setText("");
                     }
                 }
             });
                SharedPreferences sharedPreferences= getSharedPreferences("notification",MODE_PRIVATE);
                String userToken=sharedPreferences.getString("userToken","no token");
                String currentUserImage=sharedPreferences.getString("currentUserImage","no currentUserImage");
                String currentUsername=sharedPreferences.getString("currentUsername","no currentUsername");
                String post_userName1=sharedPreferences.getString("post_userName1","no post_userName1");
                String desc1=sharedPreferences.getString("desc1","no desc1");
                Log.i(TAG, "onComplete:desc1 " +desc1);
                Log.i(TAG, "onComplete:desc1 " +userToken);
                sendRetrofitNotification(userToken,currentUserImage,currentUsername,post_userName1,desc1);
            }
        });
    }
//this for notification with retrofit
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
                            Toast.makeText(getBaseContext(), "Notification send successful inside",
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
        //   data.addProperty("message", fromUser+" likes "+ toUser+ " post " + desc );
        data.addProperty("message", fromUser+" comments "+ toUser+" 's "+"post "+"( "+ desc1+" )" );
        data.addProperty("image", imageUrl);
        // add data payload
        payload.add("data", data);
        return payload;
    }
//
//    @Override
//    public void onBackPressed() {
//
////        SharedPreferences sp = getSharedPreferences("LoginInfos", 0);
////        SharedPreferences.Editor editor = sp.edit();
////        editor.putString("blogPostId",blogPostId);
////        editor.commit();
////        finish();
//    }

}