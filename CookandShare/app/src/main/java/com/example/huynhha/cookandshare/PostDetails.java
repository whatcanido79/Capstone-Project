package com.example.huynhha.cookandshare;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huynhha.cookandshare.adapter.AddCookbookAdapter;
import com.example.huynhha.cookandshare.adapter.CategoryPostAdapter;
import com.example.huynhha.cookandshare.adapter.PagerAdapter;
import com.example.huynhha.cookandshare.entity.Cookbook;
import com.example.huynhha.cookandshare.entity.Material;
import com.example.huynhha.cookandshare.entity.Post;
import com.example.huynhha.cookandshare.entity.User;
import com.example.huynhha.cookandshare.fragment.CommentFragment;
import com.example.huynhha.cookandshare.fragment.CookbookInfoFragment;
import com.example.huynhha.cookandshare.fragment.PostDetailsComment;
import com.example.huynhha.cookandshare.fragment.PostDetailsMaterialFragment;
import com.example.huynhha.cookandshare.fragment.ViewProfileFragment;
import com.example.huynhha.cookandshare.model.DBContext;
import com.example.huynhha.cookandshare.model.DBHelper;
import com.example.huynhha.cookandshare.model.FavouriteDBHelper;
import com.example.huynhha.cookandshare.model.MaterialDBHelper;
import com.example.huynhha.cookandshare.model.RateDBHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rey.material.widget.ProgressView;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class PostDetails extends AppCompatActivity {
    private static final String TAG = "Loi";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postRef = db.collection("Post");
    private ImageView btn_back;
    private ImageView btn_favourite;
    private ImageView btn_go_market;
    private ImageView btn_add_to_cookbook;
    private ImageView recipe_detail_image;
    private TextView name_of_food;
    private TextView create_by_name;
    private RatingBar ratingBar;
    public Post post = new Post();
    private ProgressDialog progressDialog;
    private List<Material> list;
    private int count = 0;
    private String postID;
    private String documentID = "";
    Context context;
    private CollectionReference cookbookRef = db.collection("Cookbook");
    private CollectionReference userRef = db.collection("User");
    private String currentUser = FirebaseAuth.getInstance().getUid().toString();
    private User user;
    private ArrayList<Cookbook> cookbooks;
    private ArrayList<String> cbName;
    private ArrayList<String> listPostID;
    private int[] tabIcons = {
            R.drawable.ic_cart,
            R.drawable.ic_categories
    };
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private PostDetailsMaterialFragment postDetailsMaterialFragment;
    private PostDetailsComment postDetailsComment;
    private String userNamePost = "";
    private ArrayList<String> postsName;
    private ArrayList<String> listRated;
    private Boolean isFavourite = false;
    private Boolean isRated = false;
    private Boolean isGoMarket = false;
    private ImageView redDot;
    private TextView numberOfMarketRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_details);
        progressDialog = new ProgressDialog(this);
        postID = getIntent().getExtras().getString("postID");
        userNamePost = getIntent().getExtras().getString("userName");
        context = this;
        cookbooks = new ArrayList<>();
        postsName = new ArrayList<>();
        listRated = new ArrayList<>();
        listPostID = new ArrayList<>();
        getData(postID);
        cbName = new ArrayList<>();
        setUp();
        getSupportActionBar().hide();
        postDetailsMaterialFragment = new PostDetailsMaterialFragment();
        postDetailsComment = new PostDetailsComment();
        getDataFromDBOffline();
        goMarketDataOffline();
        dataRated();
        isFavourite = checkFavourite();
        isRated = checkRated();
        isGoMarket = checkGoMarketList();
        System.out.println("CHEKC"+isGoMarket);
        if (listPostID != null) {
            redDot.setVisibility(View.VISIBLE);
            numberOfMarketRecipe.setVisibility(View.VISIBLE);
            numberOfMarketRecipe.setText(""+listPostID.size());
        }

        System.out.println("israted+ " + isRated.toString());
        if (isFavourite) {
            System.out.println("Check favourite: " + count);
            btn_favourite.setImageResource(R.drawable.heartactiver);
            count++;
        }
        setTabLayout();
        saveDataGoMarket();
        setFavourite();
        addToCookbook();
        setRatingBarListener();
        setBtnBackListener();
    }

    public void setBtnBackListener() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void setTabLayout() {
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(postDetailsMaterialFragment, "Nguyên liệu");
        pagerAdapter.addFragment(postDetailsComment, "Bình luận");
        viewPager.setAdapter(pagerAdapter);
        // editPostViewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }


    public void setUp() {
        tabLayout = findViewById(R.id.tab_post_details);
        viewPager = findViewById(R.id.view_pager_postDetails);
        btn_back = findViewById(R.id.btn_back);
        btn_favourite = findViewById(R.id.btn_add_favourite);
        btn_go_market = findViewById(R.id.btn_add_go_market);
        btn_add_to_cookbook = findViewById(R.id.btn_add_to_cookbook);
        recipe_detail_image = findViewById(R.id.recipe_image_details);
        name_of_food = findViewById(R.id.name_of_recipe);
        create_by_name = findViewById(R.id.create_by_name);
        ratingBar = findViewById(R.id.ratingBarSmall);
        redDot = findViewById(R.id.red_dot);
        numberOfMarketRecipe = findViewById(R.id.numberOfMarketRecipe);
        redDot.setVisibility(View.INVISIBLE);
        numberOfMarketRecipe.setVisibility(View.INVISIBLE);
    }

    public void addToCookbook() {
        btn_add_to_cookbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog;
                dialog = new Dialog(context);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_add_to_cookbook);
                Button btnAddCookbook = dialog.findViewById(R.id.btnAddNewCookbook);
                Button btnCancel = dialog.findViewById(R.id.btnAddToCookbookCancel);
                final RecyclerView rv = dialog.findViewById(R.id.rvAddToCookbookAllCookbook);

                rv.setNestedScrollingEnabled(false);
                LinearLayoutManager lln = new LinearLayoutManager(context);
                rv.setLayoutManager(lln);
                cookbookRef.whereEqualTo("userID", currentUser).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : querySnapshot) {
                                String id = queryDocumentSnapshot.getId().toString();
                                String name = queryDocumentSnapshot.get("cookbookName").toString();
                                cbName.add(name);
                                System.out.println(id + "|" + name);
                                Cookbook cookbook = new Cookbook(id, name);
                                cookbooks.add(cookbook);
                            }
                        }
                        if (cookbooks.size() > 0) {
                            AddCookbookAdapter addCookbookAdapter = new AddCookbookAdapter(context, cookbooks, postID);
                            rv.setAdapter(addCookbookAdapter);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.getMessage());
                    }
                });

                btnAddCookbook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog2;
                        dialog2 = new Dialog(context);
                        dialog2.setCancelable(false);
                        dialog2.setContentView(R.layout.dialog_new_cookbook);
                        final EditText name = dialog2.findViewById(R.id.etAddNewCookbookName);
                        final EditText des = dialog2.findViewById(R.id.etAddNewCookbookDes);
                        Button save = dialog2.findViewById(R.id.btnAddNewCookbookSave);
                        Button quit = dialog2.findViewById(R.id.btnAddNewCookbookCancel);

                        save.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                int cbNameCount = 0;
                                for (String cookbookName : cbName) {
                                    if (name.getText().toString().equals(cookbookName)) {
                                        Toast.makeText(context, "Tên Cookbook đã tồn tại", Toast.LENGTH_SHORT).show();
                                        cbNameCount = 1;
                                    }
                                }
                                if (cbNameCount == 0) {
                                    final Map<String, Object> cookbook = new HashMap<>();
                                    ArrayList<String> lp = new ArrayList<>();
                                    lp.add(postID);
                                    cookbook.put("cookbookName", name.getText().toString());
                                    cookbook.put("cookbookDescription", des.getText().toString());
                                    cookbook.put("postlist", lp);
                                    cookbook.put("userID", currentUser);
                                    db.collection("Cookbook").add(cookbook).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(context, "Thêm công thức vào Cookbook mới thành công", Toast.LENGTH_LONG).show();
                                            cookbooks.clear();
                                            dialog2.dismiss();
                                            dialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    });
                                }

                            }
                        });

                        quit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog2.dismiss();
                            }
                        });
                        dialog2.show();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cookbooks.clear();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    public void setFavourite() {
        btn_favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postID = getIntent().getExtras().getString("postID");
                FavouriteDBHelper favouriteDBHelper = new FavouriteDBHelper(getApplicationContext());
                SQLiteDatabase db1 = favouriteDBHelper.getWritableDatabase();
                SQLiteDatabase db2 = favouriteDBHelper.getReadableDatabase();

                if (count % 2 == 0) {
                    ContentValues values = new ContentValues();
                    values.put(DBContext.FavouriteDB.COLUMN_POST_ID, postID);
                    long row = db1.insert(DBContext.FavouriteDB.TABLE_NAME, null, values);
                    Toast.makeText(PostDetails.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    btn_favourite.setImageResource(R.drawable.heartactiver);
                    System.out.println("Row : " + row);
                } else {
                    btn_favourite.setImageResource(R.drawable.heartt);
                    String selection = DBContext.FavouriteDB.COLUMN_POST_ID + " LIKE ?";
                    String[] selectionArgs = {postID};
                    int deletedRows = db2.delete(DBContext.FavouriteDB.TABLE_NAME, selection, selectionArgs);
                    System.out.println("Row : " + deletedRows);
                    Toast.makeText(PostDetails.this, "Đã xoá khỏi yêu thích", Toast.LENGTH_SHORT).show();
                }

                count++;
            }
        });
    }


    public Boolean checkFavourite() {
        String postID = getIntent().getExtras().getString("postID");
        for (int i = 0; i < postsName.size(); i++) {
            if (postID.equals(postsName.get(i).toString())) {
                isFavourite = true;
                break;
            }

        }
        return isFavourite;
    }

    public Post getData(String postID) {
        postRef.whereEqualTo("postID", postID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        post.setUrlImage(document.get("urlImage").toString());
                        post.setTitle(document.get("title").toString());
                        post.setUserID(document.get("userID").toString());
                        //  post.setUserName(document.get("userName").toString());
                        post.setNumberOfPeople(document.get("numberOfPeople").toString());
                        post.setDescription(document.get("description").toString());
                        post.setDifficult(document.get("difficult").toString());
                        post.setNumberOfRate(document.get("numberOfRate").toString());
                        list = new ArrayList<>();
                        List<Map<String, Object>> list1 = (List<Map<String, Object>>) document.get("materials");
                        for (int i = 0; i < list1.size(); i++) {
                            Material material = new Material();
                            material.setMaterialName(list1.get(i).get("materialName").toString());
                            material.setQuantity(list1.get(i).get("quantity").toString());
                            list.add(material);
                        }
                        Log.d(TAG, "onComplete: " + list1.get(0).get("materialName").toString());
                        System.out.println("Quatity" + list1.get(0).toString());
                        post.setMaterials(list);
                        setData(post);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        return post;
    }

    public void setData(final Post post) {
        String str = "";
        Picasso.get().load(post.getUrlImage()).fit().centerCrop().into(recipe_detail_image);
        name_of_food.setText(post.getTitle().toString());
        create_by_name.setText(userNamePost);
        create_by_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString("userID", post.getUserID());
                ViewProfileFragment profileFragment = new ViewProfileFragment();
                profileFragment.setArguments(bundle);
                ft.replace(R.id.fl_post_details, profileFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        ratingBar.setRating(Float.parseFloat(post.getNumberOfRate()));
        progressDialog.dismiss();
    }

//    private void startLoading(Post post) {
//        progressDialog.setTitle("Đang tải ...");
//        progressDialog.show();
//
//    }

    private void saveDataGoMarket() {
        btn_go_market.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGoMarket) {
                    Toast.makeText(context, "Bạn đã thêm món ăn này vào danh sách đi chợ rồi!", Toast.LENGTH_SHORT).show();
                } else {
                    saveData();
                    goMarketDataOffline();
                    redDot.setVisibility(View.VISIBLE);
                    numberOfMarketRecipe.setVisibility(View.VISIBLE);
                    numberOfMarketRecipe.setText(""+listPostID.size());

                }

            }
        });
    }

    public void saveDataRated(String postID) {
        RateDBHelper rateDBHelper = new RateDBHelper(getApplicationContext());
        SQLiteDatabase db2 = rateDBHelper.getWritableDatabase();
        ContentValues contentValues1 = new ContentValues();
        contentValues1.put(DBContext.RateDB.COLUMN_POST_ID, postID);
        long newRowRate = db2.insert(DBContext.RateDB.TABLE_NAME, null, contentValues1);

    }

    public void saveData() {
        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        MaterialDBHelper materialDBHelper = new MaterialDBHelper(getApplicationContext());
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        SQLiteDatabase db1 = materialDBHelper.getWritableDatabase();
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DBContext.FeedEntry.COLUMN_NAME_OF_RECIPE, post.getTitle());
        values.put(DBContext.FeedEntry.COLUMN_IMG_URL, post.getUrlImage());
        values.put(DBContext.FeedEntry.COLUMN_TIME, date);
        values.put(DBContext.FeedEntry.COLUMN_USERID, post.getUserID());
        values.put(DBContext.FeedEntry.COLUMN_POST_ID, postID);
        System.out.println(values);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DBContext.FeedEntry.TABLE_NAME, null, values);
        for (int i = 0; i < list.size(); i++) {
            ContentValues contentValues = new ContentValues();
            System.out.println("RowID " + newRowId);
            contentValues.put(DBContext.MaterialDB.COLUMN_ID, String.valueOf(newRowId));
            contentValues.put(DBContext.MaterialDB.COLUMN_NAME_OF_MATERIAL, list.get(i).getMaterialName().toString());
            contentValues.put(DBContext.MaterialDB.COLUMN_QUANTITY, list.get(i).getQuantity().toString());
            contentValues.put(DBContext.MaterialDB.COLUMN_CHECK, "0");
            long rowMaterials = db1.insert(DBContext.MaterialDB.TABLE_NAME, null, contentValues);
            System.out.println("So 2 : " + rowMaterials);
            System.out.println("Ten Nguyen Lieu " + list.get(i).getMaterialName().toString());
            System.out.println(contentValues.toString());

        }
        System.out.println("So : " + newRowId);
        Toast.makeText(this, "Lưu thành công ", Toast.LENGTH_SHORT).show();

    }

    public void goMarketDataOffline() {
        listPostID.clear();
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DBContext.FeedEntry.TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String post = cursor.getString(cursor.getColumnIndex("postID"));
                listPostID.add(post);
                cursor.moveToNext();
            }
        }
        cursor.close();
    }


    @SuppressLint("ClickableViewAccessibility")
    public void setRatingBarListener() {
        ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isRated) {
                    Toast.makeText(context, "Bạn đã đánh giá công thức này rồi!", Toast.LENGTH_SHORT).show();
                } else {
                    postID = getIntent().getExtras().getString("postID");
                    final Dialog dialog;
                    dialog = new Dialog(PostDetails.this);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.rating_dialog);
                    final RatingBar rateBar = dialog.findViewById(R.id.ratingBar);
                    Button btnSendRate = dialog.findViewById(R.id.sendRating);
                    saveDataRated(postID);
                    btnSendRate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            postRef.whereEqualTo("postID", postID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                            documentID = documentSnapshot.getId();
                                            String strRate = documentSnapshot.getString("numberOfRate");
                                            System.out.println("RATE: " + strRate);
                                            System.out.println("Kaka: " + rateBar.getRating());
                                            addRate(strRate, rateBar.getRating());
                                            dialog.cancel();
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                    });
                    dialog.show();
                }
                return false;
            }
        });
    }

    public boolean checkRated() {
        String postID = getIntent().getExtras().getString("postID");
        boolean isRated = false;
        if (listRated != null) {
            for (int i = 0; i < listRated.size(); i++) {
                if (postID.equals(listRated.get(i))) {
                    System.out.println("CHeck task : " + postID + "  " + listRated.get(i));
                    isRated = true;
                    break;
                }
            }
        }

        return isRated;
    }

    public boolean checkGoMarketList() {
        String postID = getIntent().getExtras().getString("postID");
        boolean isGoMarket = false;
        if (listPostID != null) {
            for (int i = 0; i < listPostID.size(); i++) {
                if (postID.equals(listPostID.get(i))) {
                    System.out.println("CHeck task : " + postID + "  " + listPostID.get(i));
                    isGoMarket = true;
                    break;
                }
            }
        }
        System.out.println("CHeck task : "+isGoMarket);
        return isGoMarket;
    }

    public void addRate(String strRate, float newRate) {
        float oldRate = Float.parseFloat(strRate);
        float rate = (oldRate * 2 + newRate) / 3;
        System.out.println("New Rate :" + rate);
        String rateNew = String.valueOf(rate);
        postRef.document(documentID).update("numberOfRate", rateNew);

        Toast.makeText(this, "Cảm ơn bạn đã đánh giá công thức!", Toast.LENGTH_SHORT).show();
        finish();
        startActivity(getIntent());
    }

    public void getDataFromDBOffline() {
        FavouriteDBHelper favouriteDBHelper = new FavouriteDBHelper(getApplicationContext());
        SQLiteDatabase db = favouriteDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DBContext.FavouriteDB.TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String post = cursor.getString(cursor.getColumnIndex("postID"));
                postsName.add(post);
                cursor.moveToNext();
            }
        }
        cursor.close();


    }

    //get rate data
    public void dataRated() {
        RateDBHelper rateDBHelper = new RateDBHelper(getApplicationContext());
        SQLiteDatabase db1 = rateDBHelper.getReadableDatabase();
        Cursor cursor1 = db1.rawQuery("select * from " + DBContext.RateDB.TABLE_NAME, null);
        if (cursor1.moveToFirst()) {
            while (!cursor1.isAfterLast()) {
                System.out.println("test rated");
                String post = cursor1.getString(cursor1.getColumnIndex("postID"));
                System.out.println(post);
                listRated.add(post);
                cursor1.moveToNext();
            }
        }
        cursor1.close();
    }
}


