package com.adnan.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
   //Variables Declaration
    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginBtn;
    private Button loginRegBtn;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private static final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Variables Initialisation
        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        loginEmailText=(EditText) findViewById(R.id.login_et_email);
        loginPassText=(EditText) findViewById(R.id.login_et_password);
        loginBtn=(Button)findViewById(R.id.login_btn_login);
        loginRegBtn=(Button) findViewById(R.id.login_btn_register);
        progressBar=(ProgressBar) findViewById(R.id.login_progressBar);

        //for notifications for any user here when another user log in to our app, can take his id and recive his notification
     //   FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid());
        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToRegister();
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginEmail=loginEmailText.getText().toString();
                String loginPassword=loginPassText.getText().toString();
                if(loginEmail.isEmpty() || loginPassword.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please, fill data", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                updateTokenToCloude();
                                sendToMain();
                            }
                            else{
                          String errorMessage=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error" + errorMessage, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                    }
                });
            }
        });
    }
    //navigate to MainActivity if the user already login
    private void sendToMain() {
        Intent mainIntent =new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
    //navigate to MainActivity if the user already login
    private void sendToRegister() {
        Intent regIntent =new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(regIntent);
        finish();
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMain();
        }
    }


    private void updateTokenToCloude(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        String token = task.getResult();

                        DocumentReference userTokenUpdate = firebaseFirestore.collection("usersPhotoBlog").document(mAuth.getCurrentUser().getUid());

                         //update token only
                        userTokenUpdate.update("token", token)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error updating document", e);
                                    }
                                });

//                        // Get new FCM registration token
//                        String token = task.getResult();
//                        Map<String, Object> userMap = new HashMap<>();
//                        userMap.put("name", userName);
//                        userMap.put("image", uri1.toString());
//                        userMap.put("token", token);
//                        firebaseFirestore.collection("usersPhotoBlog")
//                                .document(mAuth.getCurrentUser().getUid()).update(userMap)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            Toast.makeText(LoginActivity.this, "User Setting is updated", Toast.LENGTH_SHORT).show();
//                                            sendToMain();
//
//                                        } else {
//                                            String errorMessage1 = task.getException().getMessage();
//                                            Toast.makeText(LoginActivity.this, "FireStore Error" + errorMessage1, Toast.LENGTH_SHORT).show();
//
//                                        }
//
//                                    }
//                                });

                        // Log and toast
                        // String msg = getString(R.string.msg_token_fmt, token);
                        // Log.d(TAG, msg);
                        Log.i(TAG, "onComplete:token "+token);

                    }
                });



    }

}