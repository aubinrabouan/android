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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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


    TextView valueDisplay;
    TextView StatusBluetooth;
    EditText deviceNameDisplay;
    EditText deviceUUIDDisplay;
    TextView statusBluetooth;
    Button   connectButton;

    private BluetoothAdapter mBluetoothAdapter;
    private static final String TAG = "MyActivity";


    private static final long SCAN_PERIOD = 10000;
    public static String NAME_DEVICE = "ESP32";
    public static String SERVICE_UUID_STRING = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_UUID_STRING);
    public static String CHARACTERISTIC_UUID_STRING = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    public static UUID CHARACTERISTIC_NOTIFY_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private AbstractQueue<BluetoothDevice> mRegisteredDevices;
    private BluetoothGattCharacteristic counterCharacteristic;
    private String deviceAddress;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeScannerCompat scanner;
    private ScanSettings settings;
    private ParcelUuid mUuid;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    BluetoothDevice device;
    List<ScanFilter> filters = new ArrayList<>();
    public boolean connectionStatus = false;
    public boolean ScanStatus = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        valueDisplay = findViewById(R.id.displayValue);
        StatusBluetooth = findViewById(R.id.StatusBluetooth);
        deviceNameDisplay = findViewById(R.id.editNameBluetoothDevice);
        deviceUUIDDisplay = findViewById(R.id.editCharacteristicUUID);
        statusBluetooth = findViewById(R.id.StatusBluetooth);
        connectButton = findViewById(R.id.BluetoothButtonConnection);


        mCount = 0;
        valueDisplay.setText(String.valueOf(mCount));

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG,"BLUETOOTH NOT SUPPORTED");
        }else if (!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

//Setup and start of the bluetooth scanner.
        scanner = BluetoothLeScannerCompat.getScanner();
        settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(1000)
                .setUseHardwareBatchingIfSupported(true)
                .build();

        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());
        //valueDisplay.setText(String.valueOf(filters.size()));
        scanner.startScan(filters, settings, mScanCallback);
        ScanStatus = true;

        deviceNameDisplay.setText(NAME_DEVICE);

    }




    public void onclickConnection(View view) {
        if (connectionStatus){
            mBluetoothGatt.disconnect();

        }else{

            scanner.startScan(filters, settings, mScanCallback);
            statusBluetooth.setText(R.string.StatusBluetoothScanInProgress);
        }
    }

    public void onClickGraph(View view) {

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
                connectionStatus = true;
                connectButton.setText(getString(R.string.bluetoothDisconnect));
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT client.");
                //deviceNameDisplay.setText(getString(R.string.defaultDeviceName));
                StatusBluetooth.setText(getString(R.string.StatusBluetoothDisconnected));
                //scanner.startScan(filters, settings, mScanCallback);
                connectionStatus = false;
                connectButton.setText(getString(R.string.bluetoothConnect));
                connectButton.setEnabled(true);
                deviceUUIDDisplay.setText(getString(R.string.DefaultUUID));
                valueDisplay.setText(getString(R.string.defaultBluetoothValue));

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

                if (gattService.getUuid().equals(SERVICE_UUID)) {
                    //BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                    scanner.stopScan(mScanCallback);
                    mCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_NOTIFY_UUID);
                    gatt.setCharacteristicNotification(mCharacteristic,true);

                    List<BluetoothGattDescriptor> descriptorList = mCharacteristic.getDescriptors();
                    BluetoothGattDescriptor descriptor = null;
                    for(int i=0; i<descriptorList.size();i++){
                        Log.e(TAG, "BluetoothGattDescriptor: " + descriptorList.get(i).getUuid().toString());

                    }
                    if(descriptorList.size()>0) {
                        descriptor = descriptorList.get(0);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        gatt.writeDescriptor(descriptor);
                        deviceUUIDDisplay.setText(CHARACTERISTIC_UUID_STRING);
                        StatusBluetooth.setText(getString(R.string.StatusBluetoothConnected));

                    }
                    gatt.readCharacteristic(mCharacteristic);
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
                valueDisplay.setText(String.valueOf(characteristic.getIntValue(FORMAT_UINT8, 0)));
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
                    if( deviceName != null && deviceName.equals(NAME_DEVICE) ) {
                        mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
                        //BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                        scanner.stopScan(mScanCallback);
                        ScanStatus = false;
                        deviceNameDisplay.setText(device.getName());
                    }
                    i++;
                }
            }
            else{
                //BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                scanner.stopScan(mScanCallback);
                ScanStatus = false;
                StatusBluetooth.setText(getString(R.string.StatusScanFail));
            }
        }
        public void onScanFailed(int errorCode) {
            // Scan error
        }
    };
}
