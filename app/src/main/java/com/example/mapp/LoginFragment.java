package com.example.mapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private TextView username;
    private TextView password;
    private Button login;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        loginViewModel =
                ViewModelProviders.of(this).get(LoginViewModel.class);
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        username = root.findViewById(R.id.userName);
        password = root.findViewById(R.id.password);
        login = root.findViewById(R.id.loginBtn);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString();
                String pass = password.getText().toString();


                if(Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", user) && user.contains("@csulb")){
                    username.clearComposingText();
                    password.clearComposingText();
                    username.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    password.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    Toast.makeText(getContext(), "good job", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getContext(),"ooo no good", Toast.LENGTH_LONG).show();
                }
            }
        });



        return root;
    }




}