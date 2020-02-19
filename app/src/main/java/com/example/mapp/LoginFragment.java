package com.example.mapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private EditText username;
    private EditText password;
    private Button login;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        loginViewModel =
                ViewModelProviders.of(this).get(LoginViewModel.class);
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        /*Connects the buttons and edit text fields to the code**/
        username = root.findViewById(R.id.userName);
        password = root.findViewById(R.id.password);
        login = root.findViewById(R.id.loginBtn);


        /* Sets the click functionality of the button*/
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString();
                String pass = password.getText().toString();

                /* Simple regex check for the formatting of the email**/
                if(Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", user) && (user.contains("@csulb.edu" )|| user.contains("@csulb.student.edu"))){
                    username.clearComposingText();
                    password.clearComposingText();
                    username.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    password.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    if(checkCredentials(user,pass)) {
                        Intent loggingIn = new Intent(getContext(), MainActivity.class);
                        loggingIn.putExtra("loggedIn", true);
                        startActivity(loggingIn);
                        getActivity().finish();
                    }
                }
                else{

                    Toast.makeText(getContext(),"Username/Password are incorrect", Toast.LENGTH_LONG).show();
                }
            }
        });



        return root;
    }



    public boolean checkCredentials(String username, String password){
        return true;
    }

}