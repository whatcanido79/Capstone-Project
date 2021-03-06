package com.example.huynhha.cookandshare.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.huynhha.cookandshare.MainActivity;
import com.example.huynhha.cookandshare.R;
import com.example.huynhha.cookandshare.adapter.CommentAdapter;
import com.example.huynhha.cookandshare.entity.Comment;
import com.example.huynhha.cookandshare.entity.NotificationDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostDetailsComment extends Fragment {
    private RecyclerView rcPostDetailsComment;
    private EditText edtPostDetailsComment;
    private Button btnSendComment;
    private String postID = "";
    private String documentID = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Map<String, Object>> list1;
    private List<Map<String, Object>> listNoti;
    private ArrayList<NotificationDetails> listNotiDetails;
    private CollectionReference postRef = db.collection("Comment");
    private CollectionReference notiRef = db.collection("Notification");
    private CollectionReference userRef = db.collection("User");
    public CommentFragment commentFragment;
    private FirebaseAuth firebaseAuth;
    private String documentNoti = "";
    private int count = 0;
    private String userID = "";
    private ArrayList<Comment> list;
    private CommentAdapter commentAdapter;
    @ServerTimestamp
    Date date;


    public PostDetailsComment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_post_details_comment, container, false);
        setUp(v);
        list = new ArrayList<>();
        list1 = new ArrayList<>();
        listNoti = new ArrayList<>();
        listNotiDetails = new ArrayList<>();
        postID = getActivity().getIntent().getExtras().getString("postID");
        userID = getActivity().getIntent().getExtras().getString("userID");
        date = new Date();
        loadComment(postID);
        addComment();
        return v;

    }

    public void setUp(View v) {
        rcPostDetailsComment = v.findViewById(R.id.rc_post_comment);
        edtPostDetailsComment = v.findViewById(R.id.edt_post_comment);
        btnSendComment = v.findViewById(R.id.btn_send_post_comment);
    }


    public void loadComment(final String postID) {
        LinearLayoutManager lln = new LinearLayoutManager(this.getActivity());
        rcPostDetailsComment.setLayoutManager(lln);
        MainActivity.db.collection("Comment").whereEqualTo("postID", postID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        System.out.println("ID :" + document.getId());
                        documentID = document.getId();
                        try {
                            list1 = (List<Map<String, Object>>) document.get("comment");
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        if (list1 == null) {
                            System.out.println("Chưa có bình luận nào.");
                        } else {
                            for (int i = 0; i < list1.size(); i++) {
                                Comment comment = new Comment();
                                comment.setUserID(list1.get(i).get("userID").toString());
                                comment.setUserImgUrl(list1.get(i).get("userImgUrl").toString());
                                comment.setCommentContent(list1.get(i).get("commentContent").toString());

                                list.add(comment);
                            }
                            System.out.println(list.toString());
                            commentAdapter = new CommentAdapter(list, getContext(), postID, rcPostDetailsComment);

                        }
                        rcPostDetailsComment.setAdapter(commentAdapter);

                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public void addComment() {
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = edtPostDetailsComment.getText().toString();
                Comment comment1 = new Comment();
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                comment1.setUserName(currentFirebaseUser.getDisplayName().toString());
                comment1.setUserImgUrl(currentFirebaseUser.getPhotoUrl().toString());
                comment1.setUserID(currentFirebaseUser.getUid().toString());
                comment1.setCommentContent(comment);
                list.add(comment1);
                //list1.add((Map<String, Object>) comment1);
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("userImgUrl", currentFirebaseUser.getPhotoUrl().toString());
                updateMap.put("userID", currentFirebaseUser.getUid().toString());
                updateMap.put("commentContent", comment);
                updateMap.put("time",date);
                if (list1 != null) {
                    list1.add(updateMap);
                } else {
                    list1 = new ArrayList<>();
                    list1.add(updateMap);
                }
                db.collection("Comment").document(documentID).update("comment", list1);
                edtPostDetailsComment.setText("");
                commentAdapter = new CommentAdapter(list, getContext(), postID, rcPostDetailsComment);
                rcPostDetailsComment.setAdapter(commentAdapter);
                loadNotification();
            }

        });


    }

    public void loadNotification() {
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUser = firebaseAuth.getUid().toString();
        if (currentUser.equals(userID)) {
            System.out.println("Nothing");
        } else {
            System.out.println("UserID " + userID);
            notiRef.whereEqualTo("userID", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            documentNoti = document.getId();
                            System.out.println("documentNoti: " + documentID);
                            try {
                                listNoti = (List<Map<String, Object>>) document.get("notification");
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            if (listNoti == null) {
                                System.out.println("Khong co noti");
                            } else {
                                for (int i = 0; i < listNoti.size(); i++) {
                                    NotificationDetails notificationDetails = new NotificationDetails();
                                    notificationDetails.setType(listNoti.get(i).get("type").toString());
                                    notificationDetails.setUserUrlImage(listNoti.get(i).get("userUrlImage").toString());
                                    notificationDetails.setPostID(listNoti.get(i).get("postID").toString());
                                    notificationDetails.setTime(listNoti.get(i).get("time").toString());
                                    notificationDetails.setContent(listNoti.get(i).get("content").toString());
                                    notificationDetails.setUserID(listNoti.get(i).get("userID").toString());
                                    listNotiDetails.add(notificationDetails);
                                    count++;
                                }
                            }

                            if (listNoti == null) {
                                addNoti(documentNoti);
                            } else if (count == listNoti.size()) {
                                addNoti(documentNoti);
                                count = 0;
                            }

                        }

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    public void addNoti(final String documentID) {
        firebaseAuth = FirebaseAuth.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        final String date = df.format(Calendar.getInstance().getTime());
        Map<String, Object> updateNoti = new HashMap<>();
        updateNoti.put("postID", postID);
        updateNoti.put("time", date);
        updateNoti.put("type", 1);
        updateNoti.put("userID", firebaseAuth.getCurrentUser().getUid().toString());
        updateNoti.put("userUrlImage", firebaseAuth.getCurrentUser().getPhotoUrl().toString());
        updateNoti.put("content", firebaseAuth.getCurrentUser().getDisplayName().toString() + " đã bình luận vào bài viết của bạn");
        if (listNoti == null) {
            listNoti = new ArrayList<>();
            listNoti.add(updateNoti);
        } else {
            listNoti.add(updateNoti);
        }
        System.out.println("check noti comment");
        notiRef.document(documentID).update("notification", listNoti);
        Toast.makeText(getContext(), "Add Noti Success", Toast.LENGTH_SHORT).show();
    }

}
