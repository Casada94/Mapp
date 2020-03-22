package com.example.mapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.regex.Pattern;

public class ForgetPasswordEmailFragment extends Fragment {

    private Button okayBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_forgetpasswordemail, container, false);

        okayBtn = root.findViewById(R.id.okayBtn);

        okayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController((getActivity()).findViewById(R.id.nav_host_fragment));
                navController.navigate(R.id.action_forgetPasswordEmail_to_login);
            }
        });
        return root;
    }
}
