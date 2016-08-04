package com.n1kko777.quickbluechat;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class About extends AppCompatActivity implements View.OnClickListener {

    private Button buyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        buyButton = (Button)findViewById(R.id.button2);
        buyButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=K8B4S76YPKGFW"));
        startActivity(browserIntent);

    }
}