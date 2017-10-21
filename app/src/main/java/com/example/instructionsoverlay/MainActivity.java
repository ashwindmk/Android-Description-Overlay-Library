package com.example.instructionsoverlay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ashwin.descriptionoverlay.DescriptionOverlayConfig;
import com.ashwin.descriptionoverlay.DescriptionOverlaySequence;
import com.ashwin.descriptionoverlay.DescriptionOverlayView;

public class MainActivity extends AppCompatActivity {

    private static final String SHOWCASE_ID = "Showcase ID";

    private Button mButton1, mButton2, mButton3;
    private TextView mMainTextView;
    private ImageView mShareImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        showHelp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_help) {
            showHelp();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mButton3 = (Button) findViewById(R.id.button3);
        mMainTextView = (TextView) findViewById(R.id.main_text);
        mShareImageView = (ImageView) findViewById(R.id.image_share);
    }

    private void showHelp() {
        // Single example
        /*new DescriptionOverlayView.Builder(this)
                .setTarget(mButton3)
                .setDismissText("GOT IT")
                .setContentText("This is some amazing feature you should know about")
                .setDelay(500)
                //.singleUse(SHOWCASE_ID)  // Will be shown only first time
                //.withoutShape()  // Target will not be highlighted
                //.withRectangleShape()  // Target view will be highlighted in rectangle
                .withCircleShape()  // Target view will be highlighted in circle
                .setMaskColor("#888888")
                .setDismissOnTouch(true)
                .useFadeAnimation()
                .show();*/

        // Sequence example
        DescriptionOverlayConfig config = new DescriptionOverlayConfig();
        config.setDelay(500);  // Half second between each showcase view

        DescriptionOverlaySequence sequence = new DescriptionOverlaySequence(this);
        sequence.setConfig(config);
        //sequence.singleUse(SHOWCASE_ID);  // Will be shown only first time
        sequence.addSequenceItem(mButton1, "Button 1", "This is button one", "GOT IT");
        sequence.addSequenceItem(mButton2, "Button 2", "This is button two", "GOT IT");
        sequence.addSequenceItem(mMainTextView, "Text view", "This is a text view", "GOT IT");
        sequence.addSequenceItem(mShareImageView, "Share", "This enables you to share app", "DONE");

        sequence.start();
    }
}
