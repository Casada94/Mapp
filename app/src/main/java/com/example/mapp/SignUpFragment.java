package com.example.mapp;

import android.graphics.Color;
import android.os.Bundle;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.regex.Pattern;

public class SignUpFragment extends Fragment {

    /* Variables needed for the Ui elements of the sign up page */
    private EditText email;
    private EditText password;
    private EditText rePassword;
    private Button signUp;
    private TextView emailTxt;
    private TextView passwordTxt;
    private TextView rePasswordTxt;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_signup, container, false);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        /* Connect the XML elements to the java code **/
        email = root.findViewById(R.id.email);
        emailTxt = root.findViewById(R.id.emailTxt);
        password = root.findViewById(R.id.PasswordSignUp);
        passwordTxt = root.findViewById(R.id.passwordTxt);
        rePassword = root.findViewById(R.id.rePassword);
        rePasswordTxt = root.findViewById(R.id.rePasswordTxt);
        signUp = root.findViewById(R.id.loginBtn);

        /* Hides search button in action bar */
        Toolbar toolbar = ((MainActivity) getActivity()).findViewById(R.id.toolBar);
        MenuItem menuItem = toolbar.getMenu().getItem(0);
        menuItem.setVisible(false);


        /* set functionality of the sign up button */
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean goodToSubmit = true;
                String error = new String();
                int errorCount = 0;

                /* Gets the text typed in the edit text fields **/
                String user = email.getText().toString();
                String pass = password.getText().toString();
                String rePass = rePassword.getText().toString();

                emailTxt.setTextColor(Color.BLACK);
                passwordTxt.setTextColor(Color.BLACK);
                rePasswordTxt.setTextColor(Color.BLACK);

                /* Simple regex check of the email entered by user and the passwords entered **/
                if(!Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", user)) {
                    emailTxt.setTextColor(Color.RED);
                    goodToSubmit = false;
                    errorCount++;
                    error = "Email format incorrect";
                }
                if(!(user.contains("@csulb.edu") || user.contains("@student.csulb.edu"))) {
                    emailTxt.setTextColor(Color.RED);
                    goodToSubmit = false;
                    errorCount++;
                    error = "Email must be a CSULB email";
                }
                if(!pass.equals(rePass)){
                    passwordTxt.setTextColor(Color.RED);
                    goodToSubmit = false;
                    errorCount++;
                    error = "Passwords do not match";
                }

                if(errorCount == 1) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();

                }else if(errorCount > 1) {
                    Toast.makeText(getContext(), "Correct red fields", Toast.LENGTH_LONG).show();
                }

                if(goodToSubmit){
                    /* closes the keyboard **/
                    password.onEditorAction(EditorInfo.IME_ACTION_DONE);

                    /* Logic to send verification email to new user */
                    mAuth.createUserWithEmailAndPassword(user,pass).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseAuth.getInstance().signOut();
                                            email.clearComposingText();
                                            password.clearComposingText();

                                            /* Shows a dialog box to user to inform that sign up was successful */
                                            DialogVerify signUpDialog = new DialogVerify(getActivity(), "Email sent to confirm registration! Thanks for being part of the Mapp community! Please check your inbox and junk/spam folders before making another request.");
                                            signUpDialog.show();

                                            /*Uses the activity's navigation controller to change fragments to the login fragment*/
                                            NavController navController = Navigation.findNavController((getActivity()).findViewById(R.id.nav_host_fragment));
                                            navController.navigate(R.id.action_signUp_to_login);

                                        } else {
                                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                if(task.getException() instanceof FirebaseAuthUserCollisionException)
                                    Toast.makeText(getContext(), "Email Already In Use", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getContext(), "Sign Up failed, Try Again!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        return root;
    }

}