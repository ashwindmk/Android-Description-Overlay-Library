# Android Description Overlay

Android library to show element description or introduction overlay for a screen.

[![Release](https://jitpack.io/v/ashwindmk/Android-Description-Overlay-Library.svg)](https://jitpack.io/#ashwindmk/Android-Description-Overlay-Library)

### Setup

Add the jitpack.io repository in your project-level build.gradle file:
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the following dependency in your app/build.gradle file:
```gradle
compile 'com.github.ashwindmk:Android-Description-Overlay-Library:0.1'
```

### Usage

For single overlay:
```gradle
public class MainActivity extends AppCompatActivity {
    ...
    @Override
    public void onCreate() {
        super.onCreate();
        setContentView(R.layout.activity_main);

        Button mButton1 = (Button) findViewById(R.id.button1);

        new DescriptionOverlayView.Builder(this)
             .setTarget(mButton1)
             .setDismissText("GOT IT")
             .setContentText("This is some amazing feature you should know about")
             .setDelay(500)
             .singleUse("SHOWCASE_ID")  // Will be shown only first time
             .withoutShape()  // Target will not be highlighted
             .withRectangleShape()  // Target view will be highlighted in rectangle
             .withCircleShape()  // Target view will be highlighted in circle
             .setMaskColor("#888888")
             .setDismissOnTouch(true)
             .useFadeAnimation()
             .show();
    }
}
```

For sequence of overlays:
```gradle
public class MainActivity extends AppCompatActivity {
    ...
    @Override
    public void onCreate() {
        super.onCreate();
        setContentView(R.layout.activity_main);

        Button mButton1 = (Button) findViewById(R.id.button1);
        Button mButton2 = (Button) findViewById(R.id.button2);
        TextView mMainTextView = (TextView) findViewById(R.id.main_text);

        DescriptionOverlayConfig config = new DescriptionOverlayConfig();
            config.setDelay(500);  // Half second delay between each showcase view

            DescriptionOverlaySequence sequence = new DescriptionOverlaySequence(this);
            sequence.setConfig(config);
            sequence.singleUse("SHOWCASE_ID");  // Will be shown only first time
            sequence.addSequenceItem(mButton1, "Button 1", "This is button one", "GOT IT");
            sequence.addSequenceItem(mButton2, "Button 2", "This is button two", "GOT IT");
            sequence.addSequenceItem(mMainTextView, "Text view", "This is a text view", "DONE");

            sequence.start();
    }
}
```
