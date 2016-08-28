package com.example.acoustics.vocproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;

/**
 * Created by Acoustics on 8/12/2015.
 */
public class StartActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        final int minRefDefault = 60;
        final int maxRefDefault = 84;

        ImageButton startBtn = (ImageButton) this.findViewById(R.id.startButton);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                String name = "flute";
                intent.putExtra("minRefInstrument", minRefDefault);
                intent.putExtra("maxRefInstrument", maxRefDefault);
                intent.putExtra("instrument", name);
                StartActivity.this.startActivity(intent);
                StartActivity.this.finish();
            }
        });

        ImageButton instrumentsBtn = (ImageButton) this.findViewById(R.id.instrumentsButton);
        instrumentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, InstrumentsActivity.class);
                StartActivity.this.startActivity(intent);
                StartActivity.this.finish();
            }
        });

        ImageButton settingBtn = (ImageButton) this.findViewById(R.id.settingButton);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater
                        = (LayoutInflater)getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.around, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
                // exit popup window
                ImageButton exit2Btn = (ImageButton) popupView.findViewById(R.id.exit2Button);
                exit2Btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }
        });

        ImageButton exitButton = (ImageButton) this.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
    }


}
