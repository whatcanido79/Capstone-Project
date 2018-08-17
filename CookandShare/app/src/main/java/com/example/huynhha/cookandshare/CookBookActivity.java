package com.example.huynhha.cookandshare;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.example.huynhha.cookandshare.adapter.ListCategoriesAdapter;
import com.example.huynhha.cookandshare.adapter.ListCookbookAdapter;
import com.example.huynhha.cookandshare.entity.Category;
import com.example.huynhha.cookandshare.entity.Cookbook;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CookBookActivity extends AppCompatActivity {
    private String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
    private ListCookbookAdapter listCookbookAdapter;
    @BindView(R.id.rvCookbook)
    RecyclerView rv;
    @BindView(R.id.btnCookbookClose)
    Button btnClose;
    private Context context;
    private ArrayList<Cookbook> cookbooks;
    private ArrayList<String> listpost;
    private ArrayList<String> listImg;
    private int count;
    private int count1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_book);
        getSupportActionBar().hide();
        ButterKnife.bind(this);
        context = this;
        count = 0;
        count1 = 0;
        System.out.println("SSSS+" + count);
        cookbooks = new ArrayList<>();
        listImg = new ArrayList<>();
        close();
        importListCookbook();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void importListCookbook() {
        final GridLayoutManager gln = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        rv.setLayoutManager(gln);

        MainActivity.db.collection("Cookbook").whereEqualTo("userID", currentUser).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            if (task.getResult().size() > 0) {
                                final String id = documentSnapshot.getId().toString();
                                listpost = (ArrayList<String>) documentSnapshot.get("postlist");
                                final int numberpost = listpost.size();
                                final String[] image = new String[1];
                                final String cookbookName = documentSnapshot.get("cookbookName").toString();
                                System.out.println(listpost.get(0).toString() + "AAAAAAAAA");
                                MainActivity.db.collection("Post").whereEqualTo("postID", listpost.get(0).toString()).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                        count++;
                                        if (task1.isSuccessful()) {
                                            Cookbook cookbook;
                                            QuerySnapshot querySnapshot = task1.getResult();
                                            if (task1.getResult() != null) {
                                                for (QueryDocumentSnapshot queryDocumentSnapshot : querySnapshot) {
                                                    System.out.println("ASS: " + queryDocumentSnapshot.getString("urlImage"));
                                                    image[0] = queryDocumentSnapshot.getString("urlImage");

                                                }
                                                cookbook = new Cookbook(id, cookbookName, image[0], numberpost + "");
                                                cookbooks.add(cookbook);
                                            }
                                            if(cookbooks.size()==count){
                                                listCookbookAdapter = new ListCookbookAdapter(context, cookbooks);
                                                rv.setAdapter(listCookbookAdapter);
                                            }
                                        }
                                        System.out.println("AAAA+" + count);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                });
                            }
                        }
                        System.out.println("ASS:1");
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    public void close() {
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
