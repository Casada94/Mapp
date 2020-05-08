package com.example.mapp;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


/* Class to create a dialog box */
public class DialogVerify extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button ok;
    public TextView info;
    public String text;

    public DialogVerify(Activity a, String textArg) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.text = textArg;
    }

    /* Sets up the dialog box */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.custom_dialog);
        info = (TextView) findViewById(R.id.text_dialog);
        info.setText(text);
        ok = (Button) findViewById(R.id.btn_ok);
        ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

}