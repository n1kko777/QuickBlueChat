package com.n1kko777.quickbluechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;



public class About extends AppCompatActivity {

    private Button buyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        buyButton = (Button)findViewById(R.id.button2);

    }


}