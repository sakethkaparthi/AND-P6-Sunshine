package sakethkaparthi.sunshine;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.US);
    private RelativeLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (RelativeLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.date);
        mClockView = (TextView) findViewById(R.id.clock);
        mTextView.setText(DATE_FORMAT.format(new Date()));
        mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            //mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mTextView.setText(DATE_FORMAT.format(new Date()));
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackgroundColor(getResources().getColor(R.color.blue_background));
            mTextView.setText(DATE_FORMAT.format(new Date()));
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        }
    }
}
