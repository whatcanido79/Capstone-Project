package com.example.huynhha.cookandshare.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.huynhha.cookandshare.R;
import com.example.huynhha.cookandshare.entity.Cookbook;
import com.example.huynhha.cookandshare.entity.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddCookbookAdapter extends RecyclerView.Adapter<AddCookbookAdapter.CookbookViewHolder> {
    Context context;
    ArrayList<Cookbook> cookbooks;
    Post post;
    private List<Map<String, Object>> listPost;
    private CollectionReference cookbookRef = FirebaseFirestore.getInstance().collection("Cookbook");

    public AddCookbookAdapter(Context context, ArrayList<Cookbook> cookbooks, Post post) {
        this.context = context;
        this.cookbooks = cookbooks;
        this.post = post;
    }

    @NonNull
    @Override
    public CookbookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_add_to_cookbook, parent, false);
        AddCookbookAdapter.CookbookViewHolder cpa = new AddCookbookAdapter.CookbookViewHolder(v);
        return cpa;
    }

    @Override
    public void onBindViewHolder(@NonNull CookbookViewHolder holder, int position) {
        final Cookbook cookbook = cookbooks.get(position);
        holder.tv.setText(cookbook.getCookbookName());
        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(cookbook.getCookbookID() + "|||" + cookbook.getCookbookName());
                cookbookRef.document(cookbook.getCookbookID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            listPost = (List<Map<String, Object>>) documentSnapshot.get("postlist");
                            
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return cookbooks.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class CookbookViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tv;

        public CookbookViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cvAddToCookbook);
            tv = itemView.findViewById(R.id.tvCardViewAddToCookbook);
        }
    }
}
