package com.example.mapp;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private EditText username;
    private EditText password;
    private Button login;
    private TextView forgotPass;
    private static String TAG= "EmailPassword";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_login, container, false);

        /*Connects the buttons and edit text fields to the code**/
        username = root.findViewById(R.id.userName);
        password = root.findViewById(R.id.password);
        login = root.findViewById(R.id.loginBtn);
        forgotPass = root.findViewById(R.id.forgotPassClickTxt);

        /* Hides search button in action bar */
        Toolbar toolbar = ((MainActivity) getActivity()).findViewById(R.id.toolBar);
        MenuItem menuItem = toolbar.getMenu().getItem(0);
        menuItem.setVisible(false);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();


        /* Sets the click functionality of the forgot password text*/
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                final String user = username.getText().toString();
                if (Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", user) && (user.contains("@csulb.edu") || user.contains("@student.csulb.edu"))) {
                    try {
                        mAuth.fetchSignInMethodsForEmail(user).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                                if (isNewUser) {
                                    Log.e("TAG", "Is New User!");
                                    Toast.makeText(getContext(), "Account does not exist", Toast.LENGTH_LONG).show();
                                } else {
                                    Log.e("TAG", "Is existing User!");
                                    mAuth.sendPasswordResetEmail(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Email sent.");
                                                DialogVerify forgotPwDialog = new DialogVerify(getActivity(), "Email sent to change password! Please check your inbox and junk/spam folders before making another request.");
                                                forgotPwDialog.show();
                                            } else {
                                                Log.d(TAG, "Sending email failed.");
                                                Toast.makeText(getContext(), "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter correct user in the email field", Toast.LENGTH_LONG).show();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = username.getText().toString();
                String pass = password.getText().toString();

                /* Simple regex check for the formatting of the email**/
                if(Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", user) && (user.contains("@csulb.edu" )|| user.contains("@student.csulb.edu"))){
                    username.clearComposingText();
                    password.clearComposingText();
                    username.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    password.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    try {
                        mAuth.signInWithEmailAndPassword(user,pass).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser curr_user = mAuth.getCurrentUser();

                                    if (curr_user.isEmailVerified())
                                    {
                                        // user is verified, send to main activity
                                        Intent loggedIn = new Intent(getContext(), MainActivity.class);
                                        startActivity(loggedIn);
                                        getActivity().finish();
                                    }
                                    else {
                                        // email is not verified, so just prompt the message to the user
                                        Toast.makeText(getContext(),"Email not verified", Toast.LENGTH_LONG).show();
                                        password.clearComposingText();
                                    }
                                }else{
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(getActivity().getApplicationContext(), "Username or Password are incorrect", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    
                }
                else{
                    Toast.makeText(getContext(),"Username incorrect format", Toast.LENGTH_LONG).show();
                    password.clearComposingText();
                }
            }
        });

        return root;
    }


}