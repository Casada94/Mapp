package com.example.mapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class ForgetPasswordFragment extends Fragment {

    private EditText email;
    private Button forgetPass;
    private static String TAG= "EmailPassword";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_forgetpassword, container, false);

        email = root.findViewById(R.id.emailForPw);
        forgetPass = root.findViewById(R.id.confirmBtn);
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        /* Sets the click functionality of the button*/
        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = email.getText().toString();
                if (Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", user) && (user.contains("@csulb.edu") || user.contains("@student.csulb.edu"))) {
                    email.clearComposingText();
                    email.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    try {
                        mAuth.sendPasswordResetEmail(user)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Email sent.");
                                            Toast.makeText(getContext(), "Email sent!", Toast.LENGTH_SHORT).show();
                                            NavController navController = Navigation.findNavController((getActivity()).findViewById(R.id.nav_host_fragment));
                                            navController.navigate(R.id.action_forgetPassword_to_forgetPasswordEmail);
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

    return root;
    }


}
