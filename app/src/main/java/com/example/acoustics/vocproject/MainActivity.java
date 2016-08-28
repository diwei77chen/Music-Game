package com.example.acoustics.vocproject;

import java.util.*;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends Activity {

    // the maximum number of nodes that each reference can have
    int blockSize = 512;
    // minimum reference of current instrument, default 0
    int minRef = 0;
    // maximum reference of current instrument, default 0
    int maxRef = 0;
    // current reference
    int refCurrent = 0;
    // current index of the reference, corresponding to the minimun reference
    int currIndexOfRef;
    // the name of the selected instrument
    String instrument = "";
    // play button : button pressed, game starts
    ImageButton playButton;
    // stop button : button pressed, game stops
    ImageButton stopButton;
    // back button : button pressed, the layout returns to the main
    ImageButton backButton;
    // refresh button : button pressed, the data on the row of the corresponding reference is cleared
    ImageButton refreshButton;
    // up button : button pressed, increasing the number of reference, without exceeding the upper bound
    ImageButton upButton;
    // down button : button pressed, decreasing the number of reference, without exceeding the lower bound
    ImageButton downButton;
    // reference view : shows the number of current reference
    TextView refText;
    // a 3-dimension array list to store the data of the signal input
    // 1st-dimension: reference, the initial size is 10, which will be increased by adding new elements
    // 2nd-dimension: the index of the third dimension
    // 3rd-dimension: index of 0: the pitch value, index of the rest: the amplitude values
    final ArrayList<ArrayList<ArrayList<Double>>> toTransformArrList = new ArrayList<ArrayList<ArrayList<Double>>>(10);
    // 3-dimension array list to store each color of one node(reference, x, y)
    final ArrayList<ArrayList<ArrayList<Double>>> colorArrList = new ArrayList<ArrayList<ArrayList<Double>>>(10);
    // the condition of the recorder, default false
    boolean started = false;
    // the highest pitch of the stave
    int highPitch = 96;
    // the lowest pitch of the stave
    int lowPitch = 34;
    // the range of the pitches of the stave
    int rangeOfPitch = highPitch - lowPitch;
    // the metrics to get the width of screens
    DisplayMetrics displaymetrics;
    // the width of the screen of current device
    int screenWidth;
    // the scaled width
    int bitmapScaledWidth;
    // the scaled height
    int bitmapScaledHeight;
    // handle sound input
    AudioDispatcher dispatcher;
    // an AsyncTask for drawing nodes
    RecordAudio recordTask;
    // the image view of the stave
    ImageView imageView;
    Bitmap bitmap;
    Bitmap bitmapScaled;
    Canvas canvas;
    Paint paint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // a bundle to get the values of variables from other intent
        Bundle extras = getIntent().getExtras();
        // the minimum and the default reference of the instrument
        minRef = extras.getInt("minRefInstrument");
        // the maximum reference of the instrument
        maxRef = extras.getInt("maxRefInstrument");
        // the name of the selected instrument
        instrument = extras.getString("instrument");
        // current reference of the instrument, integer number
        refCurrent = (maxRef - minRef) / 2 + minRef;
        // refCurrent = 70;
        // initialise the rest of the two dimensions of the 3-dimension array list
        for(int i = 0; i < (maxRef - minRef + 1); ++i) {
            // to each reference in the range, initialise its space
            toTransformArrList.add(new ArrayList<ArrayList<Double>>());
            colorArrList.add(new ArrayList<ArrayList<Double>>());
        }
        imageView = (ImageView) this.findViewById(R.id.ImageView01);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        // Load a bitmap from the drawable folder
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.staves);
        // the width of the current screen
        displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        // scale bitmap to the screen
        bitmapScaled = BitmapScaler.scaleToFitWidth(bitmap, screenWidth);
        bitmapScaledWidth = bitmapScaled.getWidth();
        bitmapScaledHeight = bitmapScaled.getHeight();
        // define canvas with bitmapScaled
        canvas = new Canvas(bitmapScaled);
        imageView.setImageBitmap(bitmapScaled);
        // the background color of the canvas set to be transparent
        canvas.drawColor(Color.TRANSPARENT);

        playButton = (ImageButton) this.findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (started == false) {
                    started = true;
                    if (recordTask == null) {
                        recordTask = new RecordAudio();
                        recordTask.execute();
                    }
                    else {
                        recordTask.execute();
                    }
                }
            }
        });

        refreshButton = (ImageButton) this.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int currIndexOfRef = refCurrent - minRef;
                // For current reference, clear the data stored on it
                toTransformArrList.get(currIndexOfRef).clear();
                colorArrList.get(currIndexOfRef).clear();
                // clear the nodes on the graph, which are corresponding to current reference
                Paint paint = new Paint();
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                canvas.drawPaint(paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                // reset the bitmap for canvas and imageView
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.staves);
                bitmapScaled = BitmapScaler.scaleToFitWidth(bitmap, screenWidth);
                canvas.setBitmap(bitmapScaled);
                imageView.setImageBitmap(bitmapScaled);
                canvas.drawColor(Color.TRANSPARENT);
            }
        });

        backButton = (ImageButton) this.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if the record task is running, cancel it
                if (recordTask != null) {
                    recordTask.cancel(true);
                }
                // go to the StartActivity
                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        });

        // shows current reference number
        refText = (TextView) this.findViewById(R.id.refText);
        refText.setTextSize(70);
        refText.setText(String.valueOf(refCurrent));

        upButton = (ImageButton) this.findViewById(R.id.upButton);
        upButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (refCurrent < maxRef) {
                    ++refCurrent;
                }
                else {
                    refCurrent = maxRef;
                }
                //CharSequence refCharSequence = (CharSequence) refMiddle;
                refText.setText(String.valueOf(refCurrent));
                imageView.invalidate();
            }
        });

        downButton = (ImageButton) this.findViewById(R.id.downButton);
        downButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (refCurrent > minRef) {
                    --refCurrent;
                }
                else {
                    refCurrent = minRef;
                }
                refText.setText(String.valueOf(refCurrent));
                imageView.invalidate();
            }
        });

    }

    // RecordAudio class handles signal inputs and processes the data with an output coordinate,
    // x shows the pitch of the current signal data, y shows the corresponding amplitude
    public class RecordAudio extends AsyncTask<Void, ArrayList<ArrayList<ArrayList<Double>>>, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

                // turn off the automatic gain control of the audio session used currently
//                int id = dispatcher.getIdOfAudioSession();
//                if (AutomaticGainControl.isAvailable()) {
//                    AutomaticGainControl agc = AutomaticGainControl.create(id);
//                    //agc.g
//                    System.out.println("AudioRecord: " + "AGC1111111 is " + (agc.getEnabled() ? "enabled" : "disabled"));
//                    agc.setEnabled(false);
//                    System.out.println("AudioRecord: " + "AGC2222222 is " + (agc.getEnabled() ? "enabled" : "disabled" + " after trying to disable"));
//                }
//                else {
//                    System.out.println("AudioRecord: " + "AGC is unavailable");
//                }
                // this handler will be called recursively
                PitchDetectionHandler pdh = new PitchDetectionHandler() {
                    @Override
                    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                        // when backButton is pressed this task will be cancelled, then the dispatcher will stop
                        if (isCancelled()) {
                            dispatcher.stop();
                        }
                        // current index of reference
                        currIndexOfRef = refCurrent - minRef;
                        // x axis : the values of amplitude
                        Double toTransformX = audioEvent.getRMS();
                        // scaling function for x axis
                        toTransformX = screenWidth * (Math.log(toTransformX.doubleValue() * 100) / Math.log(10)) * 2 / 3;
                        // y axis : the values of pitch
                        float pitchInHz = pitchDetectionResult.getPitch();
                        double y = pitchInHz;
                        Double toTransformY = (Double)y;

                        // for the 2-dimension graph, we store the value of x-axis into
                        // the first position of the third dimension of the array list
                        boolean xExist = false;

                        // to help resetting the data of the 3-dimension array list
                        int count = 0;
                        // each time gets the third dimension of the 3-dimension array list
                        for (ArrayList<Double> tmp : toTransformArrList.get(currIndexOfRef)) {
                            if (tmp.get(0).intValue() == toTransformX.intValue()) {
                                xExist = true;
                                toTransformArrList.get(currIndexOfRef).get(count).set(1, toTransformY);
                                colorArrList.get(currIndexOfRef).get(count).set(1, toTransformY);
                                break;
                            }
                            ++count;
                        }
                        // if the 3-dimension array list does not store any data of the x value
                        // then create one
                        if (xExist == false) {
                            toTransformArrList.get(currIndexOfRef).add(new ArrayList<Double>(2));
                            toTransformArrList.get(currIndexOfRef).get(toTransformArrList.get(currIndexOfRef).size() - 1).add(toTransformX);
                            toTransformArrList.get(currIndexOfRef).get(toTransformArrList.get(currIndexOfRef).size() - 1).add(toTransformY);
                            colorArrList.get(currIndexOfRef).add(new ArrayList<Double>(2));
                            colorArrList.get(currIndexOfRef).get(colorArrList.get(currIndexOfRef).size() - 1).add(toTransformX);
                            colorArrList.get(currIndexOfRef).get(colorArrList.get(currIndexOfRef).size() - 1).add(toTransformY);
                        }

                        // restrict the number of nodes that a reference can have
                        if (toTransformArrList.get(currIndexOfRef).size() <= blockSize) {
                            publishProgress(toTransformArrList);
                        }
                        else {
                            toTransformArrList.get(currIndexOfRef).clear();
                            colorArrList.get(currIndexOfRef).clear();
                            System.out.println("Clearing the array lists.");
                        }
                    }
                };
                dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh));
                dispatcher.run();
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(ArrayList<ArrayList<ArrayList<Double>>>... toTransform) {
            // to store the hsv values of a color
            float[] hsv = new float[3];
            // the value of the node of y-axis
            double playPlot = 0;

            for (int i = 0; i < toTransform[0].size(); ++i) {
                if (currIndexOfRef == i ) {
                    for (int j = 0; j < toTransform[0].get(i).size(); ++j) {
                        // get the x value stored at the first position
                        int x = toTransform[0].get(i).get(j).get(0).intValue();
                        double y = toTransform[0].get(i).get(j).get(1);
                        double yScale = (12 * Math.log( y / 440) / Math.log(2) + 69);
                        double play = yScale - refCurrent;
                        // if the selected instrument is flute
                        if (instrument.equals("flute")) {
                                if (play > -2 && play < 2) {
                                    hsv[0] = (float) ((play * 128) + 128);
                                    hsv[1] = 255;
                                    hsv[2] = 255;
                                    Double color = (double)Color.HSVToColor(hsv);
                                    colorArrList.get(i).get(j).set(1, color);
                                    // the plot is in the range of minimum reference and maximum reference
                                    playPlot = (((double)bitmapScaledHeight / rangeOfPitch) * (yScale - lowPitch));
                                    toTransform[0].get(i).get(j).set(1, playPlot);
                                }
                        }
                        else if (instrument.equals("clarinet")) { // the selected instrument is clarinet
                            if (play > -.5 && play < .5) {
                                    hsv[0] = (float) ((play * 512) + 128);
                                    hsv[1] = 255;
                                    hsv[2] = 255;
                                    Double color = (double)Color.HSVToColor(hsv);
                                    paint.setColor(color.intValue());
                                    colorArrList.get(i).get(j).set(1, color);
                                    playPlot = (((double)bitmapScaledHeight / rangeOfPitch) * (yScale - lowPitch));
                                    toTransform[0].get(i).get(j).set(1, playPlot);
                            }
                        }
                    }
                }

                // draw the nodes separately
                for (int j = 0; j < toTransform[0].get(i).size(); ++j) {
                    int x = toTransform[0].get(i).get(j).get(0).intValue();
                    int y = toTransform[0].get(i).get(j).get(1).intValue();
                    // set the paint color from the color array list
                    paint.setColor(colorArrList.get(i).get(j).get(1).intValue());
                    if (x > 0 && y > 0) {
                        canvas.drawCircle(x, bitmapScaledHeight - y, 5, paint);
                    }
                }
                imageView.invalidate();
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
