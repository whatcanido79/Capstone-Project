package com.example.huynhha.cookandshare.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.huynhha.cookandshare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateProfileFragment extends Fragment {
    @BindView(R.id.drop_down_list)
    Spinner sex_drop_down;
    @BindView(R.id.edt_date_of_birth)
    EditText edt_date_of_birth;
    @BindView(R.id.edt_phone_number_update)
    EditText edt_phone_number;
    @BindView(R.id.edt_email)
    EditText edt_email;
    @BindView(R.id.edt_first_name)
    EditText edt_first_name;
    @BindView(R.id.edt_second_name)
    EditText edt_second_name;
    private FirebaseAuth mAuth;
    private static final String[] paths = {"Nam", "Nữ", "Giới tính thứ 3"};

    public UpdateProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_update_profile, container, false);
        ButterKnife.bind(this, v);
        mAuth = FirebaseAuth.getInstance();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, paths);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sex_drop_down.setAdapter(adapter);
        getInfo();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void getInfo() {
        for (UserInfo user : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("facebook.com")) {
                System.out.println("User is signed in with Facebook");
            }
            if (user.getProviderId().equals("google.com")) {
                edt_email.setText(mAuth.getCurrentUser().getEmail());
                edt_first_name.setText(mAuth.getCurrentUser().getDisplayName());
                edt_phone_number.setText(mAuth.getCurrentUser().getPhoneNumber());
               
            }
        }
    }
}