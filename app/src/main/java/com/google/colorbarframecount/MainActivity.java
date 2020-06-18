/*
 * Copyright (C) 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.colorbarframecount;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Choreographer;
import android.widget.TextView;

/*
 * Main Activity class that loads colorbar.
 */
public class MainActivity extends Activity {

    private static final String TAG = "colorbar";
    private static final boolean DEBUG = false;
    volatile boolean shutdown = false;
    private TextView frameCountTextView;
    private TextView timestampTextView;
    private TextView presentationTimestampTextView;
    private static int frameCount = 0;
    private static final int[] binaryColor = { // ARGB format
            0xff000000, // black
            0xffffffff, // white
    };
    // Hex code maps to a color
    private static final int[] cbColor = { // ARGB format
            0xff000000, // black
            0xff00ffff,
            0xffff00ff,
            0xff0000ff,
            0xffffff00,
            0xff00aa00,
            0xffff0000,
            0xffffc0c0,
            0xff808080,
            0xff4080a0,
            0xff603040,
            0xffc0c080,
            0xffd2691e,
            0xffccffcc,
            0xff765432,
            0xffffffff, // white
    };
    private TextView[] fcBinaryBars;
    private TextView[] fcColorBars;
    private TextView[] tsColorBars;
    private TextView[] ptsColorBars;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameCountTextView = (TextView) findViewById(R.id.frameCount);
        timestampTextView = (TextView) findViewById(R.id.timeStamp);
        presentationTimestampTextView = (TextView) findViewById(R.id.presentationTimeStamp);

        fcBinaryBars = new TextView[32];
        setTextViewArray(fcBinaryBars, "b");

        fcColorBars = new TextView[8];
        setTextViewArray(fcColorBars, "fc");

        // A 64 bit integer timestamp will have 16 hex digits.
        // Each hex digit maps to a a view where background color is set based on hex value.
        tsColorBars = new TextView[16];
        setTextViewArray(tsColorBars, "ts");

        ptsColorBars = new TextView[16];
        setTextViewArray(ptsColorBars, "pts");
    }

    private void setTextViewArray(TextView[] textViewArray, String idPrefix) {
        for (int i = 0; i < textViewArray.length; i++) {
            String id = String.format(idPrefix + "%02d", i);
            int resource = getResources().getIdentifier(id, "id", getPackageName());
            if (resource == 0) {
                Log.e(TAG, "Unable to find resource for i: " + i + "  id: " + id);
            }
            textViewArray[i] = findViewById(resource);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        float refreshRate = getWindowManager().getDefaultDisplay().getRefreshRate();
        Log.d(TAG, "Refresh rate: " + refreshRate);

        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (!shutdown) {
                    Choreographer.getInstance().postFrameCallback(this);
                }
                if (frameCount % 128 == 0) {
                    Log.d(TAG, "frameCount: " + frameCount);
                }
                frameCountTextView.setText("" + frameCount);

                setBarcode(fcBinaryBars, frameCount); // Set binary frame count bars

                drawColorbars(fcColorBars, frameCount, 32);

                drawColorbars(tsColorBars, System.currentTimeMillis(), 64); // Set current timestamp

                drawColorbars(ptsColorBars, frameTimeNanos, 64);  // Set presentation timestamp

                frameCount++;
            }
        });

    }

    // Draws black and white barcodes.
    private void setBarcode(TextView[] textView, int frameCount) {
        int shiftFrame = frameCount;
        for (int i = 0; i < 32; i++) {
            int color = 0x01 & shiftFrame;
            shiftFrame = shiftFrame >>> 1;
            textView[i].setBackgroundColor(binaryColor[color]);
        }
    }

    // Draws a number encoded as colorbars
    private void drawColorbars(TextView[] textView, long numberToEncode, int bitsize) {
        long timestamp = numberToEncode;
        int numLoops = bitsize / 4;
        for (int i = 0; i < numLoops; i++) {
            int colorIndex = (int) (0x0f & timestamp);
            timestamp = timestamp >>> 4;
            textView[i].setBackgroundColor(cbColor[colorIndex]);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        shutdown = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        shutdown = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shutdown = true;
    }
}
