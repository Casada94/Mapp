package com.example.mapp;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.WriteResult;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
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

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        final DocumentReference users_emails = database.document("users/users_emails");

        /* Sets the click functionality of the text*/
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                final String userPasser = username.getText().toString();
                SharedPreferences.Editor editor = getContext().getSharedPreferences("emailForForgotPassword",  getContext().MODE_PRIVATE).edit();
                if(Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", userPasser) && (userPasser.contains("@csulb.edu" )|| userPasser.contains("@student.csulb.edu"))){
                    editor.putString("key", userPasser);
                    editor.apply();
                }
                else {
                    editor.putString("key", "");
                    editor.apply();
                }
                username.clearComposingText();
                password.clearComposingText();
                //NavController navController = Navigation.findNavController((getActivity()).findViewById(R.id.nav_host_fragment));
                //navController.navigate(R.id.action_login_to_forgotPassword);

                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View popupView = layoutInflater.inflate(R.layout.fragment_forgotpassword, null);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
                popupWindow.setTouchable(true);
                popupWindow.setElevation(40);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, -600);

                SharedPreferences prefs = getContext().getSharedPreferences("emailForForgotPassword", getContext().MODE_PRIVATE);
                String loadedString = prefs.getString("key", null);
                EditText email = null;
                email = (EditText) popupView.findViewById(R.id.emailForPw);
                email.setText(loadedString);

                final EditText finalEmail = email;

                ((Button) popupView.findViewById(R.id.confirmBtn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String user = finalEmail.getText().toString();
                        if (Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", user) && (user.contains("@csulb.edu") || user.contains("@student.csulb.edu"))) {
                            finalEmail.clearComposingText();
                            finalEmail.onEditorAction(EditorInfo.IME_ACTION_DONE);
                            try {
                                mAuth.sendPasswordResetEmail(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Email sent.");
                                                    Toast.makeText(getContext(), "Email sent!", Toast.LENGTH_SHORT).show();
                                                    //NavController navController = Navigation.findNavController((getActivity()).findViewById(R.id.nav_host_fragment));
                                                    //navController.navigate(R.id.action_forgotPassword_to_forgotPasswordEmail);
                                                    popupWindow.dismiss();
                                                } else {
                                                    Log.d(TAG, "Sending email failed.");
                                                    Toast.makeText(getContext(), "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getContext(), "Username incorrect format", Toast.LENGTH_LONG).show();
                        }
                    }
                });

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



    /* Checks the user input against DB login info */
    private boolean checkCredentials(String username, String password){
        return true;
    }

}