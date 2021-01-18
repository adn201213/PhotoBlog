package com.adnan.photoblog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment {
    private RecyclerView notificationt_recyclerView;
    private List<Notifications> notificationa_list;
    private FirebaseFirestore firebaseFirestore;
    private NotificationAdapter notificationAdapter;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private static final String TAG = "NotificationFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NotificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_notification, container, false);

        //setting up recyclerview
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        currentUserId=firebaseAuth.getCurrentUser().getUid();
        notificationa_list=new ArrayList<>();
        getNotifications();
       // getCurrentUserNotifications(currentUserId);
//        Notifications notifications=new Notifications();
//        notifications.setFromUser("adnan");
//       // notifications.setTimestamp(2038-01-19 03:14:07.999999);
//
//        notifications.setMessage("hello");
//        notifications.setUserImageUri("https://firebasestorage.googleapis.com/v0/b/class16firebase.appspot.com/o/profile_images%2F4JAiyozdPfUhGTqO91suU8SHoe83.jpg?alt=media&token=8bf76c63-38c2-4f0e-ad00-6cb125a35e1d");
//        notificationa_list.add(notifications);

        notificationAdapter=new NotificationAdapter(notificationa_list);
        notificationt_recyclerView=view.findViewById(R.id.notification_fragment_recyclerview);
        notificationt_recyclerView.setHasFixedSize(true);
        notificationt_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationt_recyclerView.setAdapter(notificationAdapter);
        return view;
    }
    public void getCurrentUserNotifications(String currentUserId){

        firebaseFirestore.collection("notifications")
                .whereEqualTo("post_userId", currentUserId)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()){
                    for (QueryDocumentSnapshot notification: queryDocumentSnapshots){
                        Notifications notifications1= notification.toObject(Notifications.class);
                        notificationa_list.add(notifications1);

                        Log.i(TAG, "onSuccess:arraylist "+"nothing");
                        Log.i(TAG, "onSuccess:arraylist "+notifications1.getFromUserNameImageUri());
                        Log.i(TAG, "onSuccess:arraylist "+notifications1.getDesc1());
                        Log.i(TAG, "onSuccess:arraylistgetToUserName() "+notifications1.getToUserName());
                        Log.i(TAG, "onSuccess:arraylistgetFromUserName()) "+notifications1.getFromUserName());
                        Log.i(TAG, "onSuccess:arraylistlikes "+notifications1.getType());
                    }

                }else{
                    Log.i(TAG, "onSuccess: "+"notification were not founded");
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorMessgae=e.getMessage();
                Log.i(TAG, "onFailure: "+errorMessgae);
            }
        });

    }
    public void getNotifications(){
        firebaseFirestore.collection("notifications")
                .whereEqualTo("post_userId", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.isEmpty()) {
                            for (DocumentChange doc : value.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String commentId = doc.getDocument().getId();
                                    Notifications notifications=doc.getDocument().toObject(Notifications.class);
                                    notificationa_list.add(notifications);
                                    notificationAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });

    }
}
