package md.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.chimeraiot.android.ble.BleManager;
import com.chimeraiot.android.ble.BleUtils;
import com.chimeraiot.android.ble.sensor.Sensor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import md.App;
import md.AppConfig;
import md.DB.SensorData;
import md.apk.R;
import md.ble.BLE_Services.InfoService;
import md.ble.BLE_Services.MiBand2.HeartRateService;
import md.ble.BLE_Services.MyCustomService;

public class BleManagerService extends com.chimeraiot.android.ble.BleService
        implements ScanProcessor.ScanProcessorListener {
    public enum bleState {
        connected,
        disconnected,
        connectionFailed
    }

    public interface BleStateListener {
        void onBleStateChange(bleState state);
    }

    private class FoundDeviceEntity {
        private final String _adress;
        private final String _deviceName;
        private int _rssi;

        //private bleState _state = bleState.disconnected;
        //private final BluetoothDevice _device;
        public FoundDeviceEntity(final BluetoothDevice device, final int rssi) {
            //    _device = device;
            _adress = device.getAddress();
            _deviceName = device.getName();
            _rssi = rssi;
        }

        public String getAdress() {
            return _adress;
        }

        public String getDeviceName() {
            return _deviceName;
        }

        public int getRssi() {
            return _rssi;
        }

        public void setRssi(final int rssi) {
            _rssi = rssi;
        }
    }

    private static final String TAG = BleManagerService.class.getSimpleName();
    private static final Set<String> RECORD_DEVICE_NAME = new HashSet<>(Arrays.asList(
            new String[]{"eBike MC", "MI Band 2"}
    ));
    //private static Set<FoundDeviceEntity> _foundDevices = new HashSet<>();

    private ScanProcessor scanner = null;
    //private static final String SENSOR_TO_READ = MyCustomService.UUID_SERVICE;
    private static BleManagerService INSTANCE = null;

    private BleStateListener _bleStateListener = null;

    public static BleManagerService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BleManagerService();
        }
        return (INSTANCE);
    }

    public void setBleStateListener(BleStateListener listener) {
        _bleStateListener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (INSTANCE == null)
            INSTANCE = this;
        final int bleStatus = BleUtils.getBleStatus(getBaseContext());
        switch (bleStatus) {
            case BleUtils.STATUS_BLE_NOT_AVAILABLE:
                Toast.makeText(getApplicationContext(), R.string.dialog_error_no_ble, Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            case BleUtils.STATUS_BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(getApplicationContext(), R.string.dialog_error_no_bluetooth, Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            default:
                break;
        }
        getBleManager().initialize(getBaseContext());
        final BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(getBaseContext());
        scanner = new ScanProcessor(this, bluetoothAdapter);
    }

    @Override
    protected BleManager createBleManager() {
        return new BleManager(App.DEVICE_DEF_COLLECTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (scanner == null)
            return super.onStartCommand(intent, flags, startId);
        scanner.FindDevice(RECORD_DEVICE_NAME);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service stopped");
        setServiceListener(null);
        if (scanner != null)
            scanner.Stop();
    }

    @Override
    public void onConnected(final String name, final String address) {
        super.onConnected(name, address);
        if (_bleStateListener != null)
            _bleStateListener.onBleStateChange(bleState.connected);
    }

    @Override
    public void onDisconnected(final String name, final String address) {
        super.onDisconnected(name, address);
        if (_bleStateListener != null)
            _bleStateListener.onBleStateChange(bleState.disconnected);
        scanner.FindDevice(RECORD_DEVICE_NAME);
    }

    @Override
    public void onConnectionFailed(String name, String address, int status, int state) {
        super.onConnectionFailed(name, address, status, state);
        if (_bleStateListener != null)
            _bleStateListener.onBleStateChange(bleState.connectionFailed);
        //getBleManager().reset(address);
        scanner.FindDevice(RECORD_DEVICE_NAME);
    }

    @Override
    public void onServiceDiscovered(final String name, final String address) {
        super.onServiceDiscovered(name, address);
        Log.d(TAG, "Service discovered_______");

        for ( BluetoothGattService service : getBleManager().getSupportedGattServices(address)){
            if (service.getUuid().toString().equals(MyCustomService.UUID_SERVICE))
            {
                final InfoService<?> sensor = (InfoService<?>) getBleManager().getDeviceDefCollection()
                        .get(name, address).getSensor(MyCustomService.UUID_SERVICE);
                if (sensor != null) {
                    getBleManager().listen(address, sensor, MyCustomService.UUID_BIKE_BATTERY_LEVEL_ID);
                    getBleManager().listen(address, sensor, MyCustomService.UUID_CURRENT_ID);
                    getBleManager().listen(address, sensor, MyCustomService.UUID_BIKE_SPEED_ID);
                    getBleManager().listen(address, sensor, MyCustomService.UUID_BIKE_FLAGS_ID);
                }
            }if (service.getUuid().toString().equals(HeartRateService.UUID_SERVICE)) {
                final InfoService<?> sensor = (InfoService<?>) getBleManager().getDeviceDefCollection()
                        .get(name, address).getSensor(HeartRateService.UUID_SERVICE);

                if (sensor != null) {
                    getBleManager().listen(address, sensor, HeartRateService.UUID_HEARTRATE_MEASURE_ID);
                }
            }
        }

        /*final InfoService<?> sensor = (InfoService<?>) getBleManager().getDeviceDefCollection()
                .get(name, address).getSensor(SENSOR_TO_READ);
        if (sensor instanceof MyCustomService) {
            getBleManager().listen(address, sensor, MyCustomService.UUID_BIKE_BATTERY_LEVEL_ID);
            getBleManager().listen(address, sensor, MyCustomService.UUID_CURRENT_ID);
            getBleManager().listen(address, sensor, MyCustomService.UUID_BIKE_SPEED_ID);
            getBleManager().listen(address, sensor, MyCustomService.UUID_BIKE_FLAGS_ID);
        }*/
    }

    @Override
    public void onCharacteristicChanged(final String name, final String address,
                                        final String serviceUuid,
                                        final String characteristicUuid) {
        super.onCharacteristicChanged(name, address, serviceUuid, characteristicUuid);
        Log.d(TAG, "Service='" + serviceUuid + " characteristic=" + characteristicUuid);


        final InfoService<?> sensor = (InfoService<?>) getBleManager().getDeviceDefCollection()
                .get(name, address).getSensor(MyCustomService.UUID_SERVICE);
        if (sensor instanceof MyCustomService) {

            try {
                App.sensorDataController.addItemInTable(
                        new SensorData(serviceUuid,
                                characteristicUuid,
                                Integer.toString((((MyCustomService) sensor).getIntValue()))));//WTF is getIntValue??? TODO: depending on characteristicUuid get last cached value
            } catch (Exception e) {

            }
        }

        final InfoService<?> sensor2 = (InfoService<?>) getBleManager().getDeviceDefCollection()
                .get(name, address).getSensor(HeartRateService.UUID_SERVICE);
        if (sensor2 instanceof HeartRateService) {

            try {
                App.sensorDataController.addItemInTable(
                        new SensorData(serviceUuid,
                                characteristicUuid,
                                Integer.toString((((HeartRateService) sensor2).getMeasureValue()))));
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onAskedDeviceFound(final BluetoothDevice device, final int rssi) {
        final FoundDeviceEntity foundDevice = new FoundDeviceEntity(device, rssi);
        //_foundDevices.add(foundDevice);
            getBleManager().connect(getBaseContext(), foundDevice.getAdress());
    }

    public void update(Sensor<?> sensor, String uuid, Bundle data) {
        for ( BluetoothDevice device : getBleManager().getConnectedDevices()){
            if (sensor instanceof HeartRateService && device.getName().equals(AppConfig.DEF_MI_BAND2_DEVICE_NAME))
            {
                getBleManager().update(device.getAddress(), sensor, uuid, data);
            }
            else if  (sensor instanceof MyCustomService && device.getName().equals(AppConfig.DEF_DEVICE_NAME))
            {
                getBleManager().update(device.getAddress(), sensor, uuid, data);
            }
        }
    }

}
