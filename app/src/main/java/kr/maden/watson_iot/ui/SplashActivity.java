package kr.maden.watson_iot.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.maden.watson_iot.R;

public class SplashActivity extends AppCompatActivity {
    private final String TAG = SplashActivity.class.getName();
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 100;
    private static final int REQUEST_ENABLE_BLUETOOTH = 99;
    private View decorView;
    private int uiOption;
    private BluetoothAdapter bluetoothAdapter;
    private Button button_click;
    private ImageView splashLogo;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        decorView = getWindow().getDecorView();
        uiOption = getWindow().getDecorView().getSystemUiVisibility();
        decorView.setSystemUiVisibility(uiOption);

        button_click = (Button) findViewById(R.id.move_to_btn);

        button_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                checkAndRequestPermissions();

            }
        });

        final TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                getResources().getDrawable(R.drawable.splash_logo),
                getResources().getDrawable(R.drawable.splash_logo2)
        });
        splashLogo = (ImageView) findViewById(R.id.splash_icon);
        splashLogo.setImageDrawable(td);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                td.startTransition(1500);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        checkAndRequestPermissions();
                    }
                }, 2000);

            }
        }, 1000);

    }

    private void checkBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Can not run because you have no bluetooth module", Toast.LENGTH_LONG).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
            } else {
                Intent intent = new Intent(this, DeviceSearchActivity.class);
//                Intent intent = new Intent(this, TodayisActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Can not run because it does not have permission.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
                } else {
                    checkAndRequestPermissions();
                }
                break;
        }
    }

    private void checkAndRequestPermissions() {
        List<String> listPermissionsCheck = new ArrayList<>();
        List<String> listPermissionsNeeded = new ArrayList<>();

        listPermissionsCheck.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        listPermissionsCheck.add(Manifest.permission.WAKE_LOCK);
        listPermissionsCheck.add(Manifest.permission.ACCESS_FINE_LOCATION);
        listPermissionsCheck.add(Manifest.permission.CAMERA);
        listPermissionsCheck.add(Manifest.permission.READ_PHONE_STATE);
        listPermissionsCheck.add(Manifest.permission.ACCESS_FINE_LOCATION);

        for (String permission : listPermissionsCheck) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return;
        }
        checkBluetooth();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                int grantResult = 0, shouldRequest = 0;
                Map<String, Integer> perms = new HashMap<>();
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        grantResult += grantResults[i];
                        shouldRequest += ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]) ? 1 : 0;
                        perms.put(permissions[i], grantResults[i]);
                    }
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "All services permission granted");
                        checkAndRequestPermissions();
                        return;
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        if (shouldRequest > 0) {
                            showDialogOK("Permission is required to enable BLE and send sensor information to server.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    Toast.makeText(getApplicationContext(), "Can not run because it does not have permission.", Toast.LENGTH_LONG).show();
                                                    break;
                                            }
                                        }
                                    });
                            return;
                        } else {
//                            Toast.makeText(this, "Can not run because it does not have permission.", Toast.LENGTH_LONG).show();
                            checkAndRequestPermissions();
                            return;
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                //  .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
}
