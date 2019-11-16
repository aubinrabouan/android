package com.example.helloworld;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private int mCount;
    TextView mDisplay;
    TextView Name;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "MyActivity";


    private static final long SCAN_PERIOD = 10000;
    public static String ServUUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    public static UUID SERVICE_UUID = UUID.fromString(ServUUID);
    public static UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    public static UUID CHARACTERISTIC_NOTIFY_UUID = UUID.fromString("BEB5483E-36E1-4688-B7F5-EA07361b26A8");
    public static UUID DESCRIPTOR_CONFIG_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private int currentCounterValue;
    private AbstractQueue<BluetoothDevice> mRegisteredDevices;
    private BluetoothGattCharacteristic counterCharacteristic;
    private String deviceAddress;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeScannerCompat scanner;
    private ScanSettings settings;
    private ScanFilter scanFilter;
    private ParcelUuid mUuid;
    private GoogleMap mMap;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    BluetoothDevice device;
    List<ScanFilter> filters = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDisplay = findViewById(R.id.mDisplay);
        Name = findViewById(R.id.Name);
        mCount = 0;
        mDisplay.setText(String.valueOf(mCount));


        LocationManager locationManager;
        String provider;


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);


            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {

            Log.i("Location Info", "Location achieved!");

        } else {

            Log.i("Location Info", "No location :(");

        }




        scanner = BluetoothLeScannerCompat.getScanner();
        settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(1000)
                .setUseHardwareBatchingIfSupported(true)
                .build();

        List<ScanFilter> filters = new ArrayList<>();



        //Log.w(TAG, String.valueOf(filters.size()));
        //Log.w(TAG, String.valueOf(filters.get(0)));
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(String.valueOf(SERVICE_UUID)))
                .build();
        filters.add(scanFilter);
        filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());
        mDisplay.setText(String.valueOf(filters.size()));
        scanner.startScan(filters, settings, mScanCallback);
        scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(SERVICE_UUID)).build();
        //scanner.startScan(Arrays.asList(scanFilter), settings, mScanCallback);




    }

    public void testonclick(View view) {
        mCount++;

        mDisplay.setText(String.valueOf(mCharacteristic.getIntValue(FORMAT_UINT8,0)));
        //mBluetoothGatt.getConnectionState()


    }


    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.i(TAG, "---CONTENTCAHNGE---");
    }

    private android.bluetooth.BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG, "onConnectionStateChange: ");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT client. Attempting to start service discovery.");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT client.");
                Name.setText("Disconnected");
                scanner.startScan(filters, settings, mScanCallback);

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG, "---onServicesDiscovered---");

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered: discovering services failed.");
                return;
            }

            for (BluetoothGattService gattService : gatt.getServices()) {
                Log.i(TAG, "" + gatt.getDevice().getName() + "=" + gattService.getUuid().toString());

                if (gattService.getUuid().toString().equals(ServUUID)) {
                    BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                    scanner.stopScan(mScanCallback);


                    //Name.setText(device.getName());
                    Log.i(TAG, "LED found...");
                    //mCharacteristic = gattService.getCharacteristic(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
                    mCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_NOTIFY_UUID);
                    gatt.setCharacteristicNotification(mCharacteristic,true);
                    BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(CHARACTERISTIC_NOTIFY_UUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                    gatt.readCharacteristic(mCharacteristic);
                    //Name.setText(device.getName());
                    if(mCharacteristic!=null){
                        //mDisplay.setText(String.valueOf(mCharacteristic.getIntValue(FORMAT_UINT8,4)));
                    }



                    //mCharacteristic.setValue(b);
                    //mBluetoothGatt.writeCharacteristic(mCharacteristic);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "onCharacteristicRead: ");
            
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "onCharacteristicWrite: " + Utils.byteToHexStr(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "onCharacteristicChanged: ");
            if(characteristic.getIntValue(FORMAT_UINT8,0)!=null) {
                mDisplay.setText(String.valueOf(characteristic.getIntValue(FORMAT_UINT8, 0)));
            }else{
                Log.w(TAG,"RECEIVE NULL VALUE");
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.i(TAG, "onReliableWriteCompleted: ");
        }



    };


    private final ScanCallback mScanCallback = new ScanCallback() {

        public void onScanResult(int callbackType, ScanResult result) {
            // We scan with report delay > 0. This will never be called.
        }


        public void onBatchScanResults(List<ScanResult> results) {

            if (!results.isEmpty()) {
                int i = 0;
                while (i<results.size()){
                    ScanResult result = results.get(i);
                    device = result.getDevice();
                    deviceAddress = device.getAddress();
                    String deviceName = device.getName();
                    if( deviceName != null && deviceName.equals("ESP32") ) {
                        mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
                        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                        scanner.stopScan(mScanCallback);
                        Name.setText(device.getName());
                    }
                    i++;
                }

            }
/*
            if (!results.isEmpty()) {
                int i = 0;
                while (results.get(i)!=null){
                ScanResult result = results.get(i);
                device = result.getDevice();
                deviceAddress = device.getAddress();
                mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
                BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                scanner.stopScan(mScanCallback);
                Name.setText(device.getName());
                i++;
                }

            }

*/
            else{
                Name.setText("SCANNING");
            }
        }


        public void onScanFailed(int errorCode) {
            // Scan error
        }
    };




}
