package kr.maden.watson_iot.ui.fragment;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import kr.maden.watson_iot.R;
import kr.maden.watson_iot.service.SensorTagService;
import kr.maden.watson_iot.ui.DeviceSearchActivity;

public abstract class SectionFragment extends Fragment {
    protected boolean mConnected = false;

    protected TextView mNotConnectedTextView;

    protected void setConnected(boolean connected) {
        setConnected(connected, false);
    }

    protected void setConnected(boolean connected, boolean force) {
        if (force || mConnected != connected) {
            mConnected = connected;
            if (mConnected) {
                mNotConnectedTextView.setVisibility(View.GONE);
                getSectionLayout().setVisibility(View.VISIBLE);
            } else {
                mNotConnectedTextView.setVisibility(View.VISIBLE);
                getSectionLayout().setVisibility(View.GONE);
            }
        }
    }

    private void registerReceiver() {
        IntentFilter temperatureFilter = new IntentFilter(SensorTagService.ACTION_GATT_CONNECTED);
        temperatureFilter.addAction(SensorTagService.ACTION_GATT_DISCONNECTED);
        registerSectionReceiver(temperatureFilter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DeviceSearchActivity.BUNDLE_KEY_IS_CONNECTED, mConnected);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mConnected = savedInstanceState.getBoolean(DeviceSearchActivity.BUNDLE_KEY_IS_CONNECTED, false);
        }

        registerReceiver();
        View rootView = inflater.inflate(getLayoutResource(), container, false);
        mNotConnectedTextView = (TextView) rootView.findViewById(R.id.section_not_connected);

        onCreateViewHook(rootView);

        setConnected(mConnected, true);

        return rootView;
    }

    abstract protected int getLayoutResource();
    abstract protected void onCreateViewHook(View rootView);
    abstract protected LinearLayout getSectionLayout();
    abstract protected void registerSectionReceiver(IntentFilter filter);
    abstract protected void unregisterSectionReceiver();
}