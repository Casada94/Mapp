package com.example.mapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mapp.R;

public class MailSenderActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); //edit and delete main.xml

        final Button send = this.findViewById(R.id.send); //edit and clean
        send.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                try {
                    GMailSender sender = new GMailSender("username@gmail.com", "password");
                    sender.sendMail("This is Subject",
                            "This is Body",
                            "user@gmail.com",
                            "user@yahoo.com");
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }

            }
        });

    }
}