package sakethkaparthi.sunshine;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Sunshine extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_HOURS_FORMAT =
            new SimpleDateFormat("HH", Locale.US);
    private static final SimpleDateFormat AMBIENT_MINUTES_FORMAT =
            new SimpleDateFormat(":mm", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.US);
    private static final String TAG = Sunshine.class.getSimpleName();
    private RelativeLayout mContainerView;
    private TextView mTextView;
    private TextView mHoursView, mMinutesView, mTempView;
    private GoogleApiClient mGoogleApiClient;
    final String WEATHER_PATH = "/weather";
    final String WEATHER_TEMP_HIGH_KEY = "weather_temp_high_key";
    final String WEATHER_TEMP_LOW_KEY = "weather_temp_low_key";
    final String WEATHER_TEMP_ICON_KEY = "weather_temp_icon_key";
    String weatherTempHigh;
    String weatherTempLow;
    DataApi.DataListener dataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            Log.e(TAG, "onDataChanged(): " + dataEvents);

            for (DataEvent event : dataEvents) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    String path = event.getDataItem().getUri().getPath();
                    if (WEATHER_PATH.equals(path)) {
                        Log.e(TAG, "Data Changed for " + WEATHER_PATH);
                        try {
                            DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                            weatherTempHigh = dataMapItem.getDataMap().getString(WEATHER_TEMP_HIGH_KEY);
                            weatherTempLow = dataMapItem.getDataMap().getString(WEATHER_TEMP_LOW_KEY);
                            updateDisplay();
                            /*final Asset photo = dataMapItem.getDataMap().getAsset(WEATHER_TEMP_ICON_KEY);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    weatherTempIcon = bitmapFromAsset(mGoogleApiClient, photo);
                                }
                            }).start();*/

                        } catch (Exception e) {
                            Log.e(TAG, "Exception   ", e);
                            //weatherTempIcon = null;
                        }

                    } else {

                        Log.e(TAG, "Unrecognized path:  \"" + path + "\"  \"" + WEATHER_PATH + "\"");
                    }

                } else {
                    Log.e(TAG, "Unknown data event type   " + event.getType());
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.e(TAG, "onConnected: Successfully connected to Google API client");
                        Wearable.DataApi.addListener(mGoogleApiClient, dataListener);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                                for (final Node node : nodes.getNodes()) {
                                    MessageApi.SendMessageResult messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/run_sync", null).await();
                                    if (messageResult.getStatus().isSuccess()) {
                                        Log.d("Sent to", node.getDisplayName());
                                    }
                                }
                            }
                        }).start();

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.e(TAG, "onConnectionSuspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result : " + connectionResult);
                    }
                })
                .build();
        mGoogleApiClient.connect();
        mContainerView = (RelativeLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.date);
        mHoursView = (TextView) findViewById(R.id.clock_hours);
        mMinutesView = (TextView) findViewById(R.id.clock_minutes);
        mTempView = (TextView) findViewById(R.id.temperature);
        mTextView.setText(DATE_FORMAT.format(new Date()));
        mHoursView.setText(AMBIENT_HOURS_FORMAT.format(new Date()));
        mMinutesView.setText(AMBIENT_MINUTES_FORMAT.format(new Date()));
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
        if (weatherTempHigh != null && !weatherTempHigh.isEmpty())
            mTempView.setText(weatherTempHigh + " " + weatherTempLow);
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
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

}
