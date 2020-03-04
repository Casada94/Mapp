package com.example.mapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private EditText username;
    private EditText password;
    private Button login;
    private static String TAG= "EmailPassword";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        loginViewModel =
                ViewModelProviders.of(this).get(LoginViewModel.class);
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        /*Connects the buttons and edit text fields to the code**/
        username = root.findViewById(R.id.userName);
        password = root.findViewById(R.id.password);
        login = root.findViewById(R.id.loginBtn);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        final DocumentReference users_emails = database.document("users/users_emails");

        /* Sets the click functionality of the button*/
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
                                if(task.isSuccessful()){
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser curr_user = mAuth.getCurrentUser();
                                    if(curr_user != null) {
                                       /* Map<String, Object> user_email = new HashMap<>();
                                        user_email.put("email", curr_user.getEmail());
                                        // document keeps track of user emails ONLY, for verifying later if email already exists
                                        users_emails.update(user_email).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Added user name and email");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "Failed adding user name and email");
                                            }
                                        });
                                        Map<String, Object> new_user = new HashMap<>();
                                        new_user.put("email", curr_user.getEmail()); //fields each user will contain (we can add more, this was tester)
                                        // adds current user to collection
                                        database.collection("users").document(mAuth.getUid()).set(new_user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.w(TAG, "Added user to firestore.");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error adding document", e);
                                                    }
                                                });
                                    */}
                                    Intent loggedIn = new Intent(getContext(), MainActivity.class);
                                    startActivity(loggedIn);
                                    getActivity().finish();
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