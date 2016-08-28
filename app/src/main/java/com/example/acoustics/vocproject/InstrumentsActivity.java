package com.example.acoustics.vocproject;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Acoustics on 17/12/2015.
 */
public class InstrumentsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instruments);
        final int minRefFlute = 60;
        final int maxRefFlute = 84;
        final int minRefCralinet = 60;
        final int maxRefCralinet = 84;

        // flute button
        ImageButton fluteBtn = (ImageButton) this.findViewById(R.id.fluteButton);
        fluteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InstrumentsActivity.this, MainActivity.class);
                String name = "flute";
                intent.putExtra("minRefInstrument", minRefFlute);
                intent.putExtra("maxRefInstrument", maxRefFlute);
                intent.putExtra("instrument", name);
                InstrumentsActivity.this.startActivity(intent);
                InstrumentsActivity.this.finish();
            }
        });
        // clarinet button
        ImageButton clarinetBtn = (ImageButton) this.findViewById(R.id.clarinetButton);
        clarinetBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InstrumentsActivity.this, MainActivity.class);
                String name = "clarinet";
                intent.putExtra("minRefInstrument", minRefCralinet);
                intent.putExtra("maxRefInstrument", maxRefCralinet);
                intent.putExtra("instrument", name);
                InstrumentsActivity.this.startActivity(intent);
                InstrumentsActivity.this.finish();
            }
        });
    }
}
