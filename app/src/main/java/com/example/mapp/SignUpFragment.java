package com.example.mapp;

import android.app.Activity;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import java.util.regex.Pattern;

public class SignUpFragment extends Fragment {

    private SignUpViewModel signUpViewModel;
    EditText email;
    EditText password;
    EditText rePassword;
    Button signUp;
    TextView emailTxt;
    TextView passwordTxt;
    TextView rePasswordTxt;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        signUpViewModel =
                ViewModelProviders.of(this).get(SignUpViewModel.class);
        View root = inflater.inflate(R.layout.fragment_signup, container, false);

        email = root.findViewById(R.id.email);
        emailTxt = root. findViewById(R.id.emailTxt);
        password = root.findViewById(R.id.PasswordSignUp);
        rePassword = root.findViewById(R.id.rePassword);
        rePasswordTxt = root.findViewById(R.id.rePasswordTxt);
        signUp = root.findViewById(R.id.loginBtn);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = email.getText().toString();
                String pass = password.getText().toString();
                String rePass = rePassword.getText().toString();


                if(Pattern.matches("[\\w | \\. ]+\\@[\\w | \\. ]+", user) && user.contains("@csulb") && pass.equals(rePass)){
                    email.clearComposingText();
                    password.clearComposingText();

                    password.onEditorAction(EditorInfo.IME_ACTION_DONE);
                    Toast.makeText(getContext(), "good job", Toast.LENGTH_LONG).show();

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