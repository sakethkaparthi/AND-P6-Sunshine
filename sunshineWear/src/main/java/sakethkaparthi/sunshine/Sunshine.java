package sakethkaparthi.sunshine;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Sunshine extends WearableActivity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final SimpleDateFormat AMBIENT_HOURS_FORMAT =
            new SimpleDateFormat("HH", Locale.US);
    private static final SimpleDateFormat AMBIENT_MINUTES_FORMAT =
            new SimpleDateFormat(":mm", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.US);
    private RelativeLayout mContainerView;
    private TextView mTextView;
    private TextView mHoursView, mMinutesView, mCountView;
    private GoogleApiClient mGoogleApiClient;
    private int count = 0;
    private static final String COUNT_KEY = "com.example.key.count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mContainerView = (RelativeLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.date);
        mHoursView = (TextView) findViewById(R.id.clock_hours);
        mMinutesView = (TextView) findViewById(R.id.clock_minutes);
        mCountView = (TextView) findViewById(R.id.count);
        mTextView.setText(DATE_FORMAT.format(new Date()));
        mHoursView.setText(AMBIENT_HOURS_FORMAT.format(new Date()));
        mMinutesView.setText(AMBIENT_MINUTES_FORMAT.format(new Date()));
        mGoogleApiClient.connect();
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
        mCountView.setText(count + "");
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            //mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mTextView.setText(DATE_FORMAT.format(new Date()));
            mHoursView.setText(AMBIENT_HOURS_FORMAT.format(new Date()));
            mMinutesView.setText(AMBIENT_MINUTES_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackgroundColor(getResources().getColor(R.color.blue_background));
            mTextView.setText(DATE_FORMAT.format(new Date()));
            mHoursView.setText(AMBIENT_HOURS_FORMAT.format(new Date()));
            mMinutesView.setText(AMBIENT_MINUTES_FORMAT.format(new Date()));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.d("Wear activity", "onConnected: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/count") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    count = dataMap.getInt(COUNT_KEY);
                    updateDisplay();
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
