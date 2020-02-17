package com.example.mapp;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.regex.Pattern;

public class SignUpFragment extends Fragment {

    private SignUpViewModel signUpViewModel;
    private EditText email;
    private EditText password;
    private EditText rePassword;
    private Button signUp;
    private TextView emailTxt;
    private TextView passwordTxt;
    private TextView rePasswordTxt;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        signUpViewModel =
                ViewModelProviders.of(this).get(SignUpViewModel.class);

        View root = inflater.inflate(R.layout.fragment_signup, container, false);

        /* Connect the XML elements to the java code **/
        email = root.findViewById(R.id.email);
        emailTxt = root. findViewById(R.id.emailTxt);
        password = root.findViewById(R.id.PasswordSignUp);
        rePassword = root.findViewById(R.id.rePassword);
        rePasswordTxt = root.findViewById(R.id.rePasswordTxt);
        signUp = root.findViewById(R.id.loginBtn);

        /* set functionality of the sign up button **/
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Gets the text typed in the edit text fields **/
                String user = email.getText().toString();
                String pass = password.getText().toString();
                String rePass = rePassword.getText().toString();

                /* Simple regex check of the email entered by user and the passwords entered **/
                if(Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", user) && user.contains("@csulb") && pass.equals(rePass)){
                    email.clearComposingText();
                    password.clearComposingText();

                    /* closes the keyboard **/
                    password.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    Toast.makeText(getContext(), "good job", Toast.LENGTH_LONG).show();

                    /*

                      SOME DATABASE STUFF WILL HAPPEN HERE
                      TO CHECK IF EMAIL IS ALREADY IN USE
                      AND TO ADD NEW USER TO DATABASE

                      **/


                    /*Uses the activity's navigation controller to change fragments to the login fragment**/
                    NavController navController = Navigation.findNavController(((MainActivity)getActivity()).findViewById(R.id.nav_host_fragment));
                    navController.navigate(R.id.login_frag);

                }
                else{
                    Toast.makeText(getContext(),"ooo no good", Toast.LENGTH_LONG).show();
                }
            }
        });

        return root;
    }
}