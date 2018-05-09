package md.ble.BLE_Services;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.util.Log;

import com.chimeraiot.android.ble.BleGattExecutor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import Loging.FileLog;


/** BLE device info services. */
public class MyCustomService<T> extends InfoService<T> {
    public interface MyCustomServiceListener {
        void OnCharacteristicChanged(String characteristic, int value);
        void OnCharacteristicChanged(String characteristic, float value);
    }

    private static final String TAG = MyCustomService.class.getSimpleName();
    /**
     * Service UUID.
     */
    public static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";

    public static final String UUID_MODE_ID = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static final String UUID_PWM_DUTY_CYCLE_ID = "0000fff2-0000-1000-8000-00805f9b34fb";
    public static final String UUID_V_THRESHOLD_ID = "0000fff3-0000-1000-8000-00805f9b34fb";
    public static final String UUID_BIKE_BATTERY_LEVEL_ID = "0000fff4-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CURRENT_ID = "0000fff5-0000-1000-8000-00805f9b34fb";
    public static final String UUID_BIKE_SPEED_ID = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static final String UUID_BIKE_FLAGS_ID = "0000fff7-0000-1000-8000-00805f9b34fb";

    /**
     * Characteristics.
     */
    private static final HashMap<String, String> CHARACTERISTIC_MAP = new HashMap<>();

    static {
        CHARACTERISTIC_MAP.put(UUID_MODE_ID, "Working mode");
        CHARACTERISTIC_MAP.put(UUID_PWM_DUTY_CYCLE_ID, "PWM duty cycle");
        CHARACTERISTIC_MAP.put(UUID_V_THRESHOLD_ID, "V THRESHOLD");
        CHARACTERISTIC_MAP.put(UUID_BIKE_BATTERY_LEVEL_ID, "Bike battery level");
        CHARACTERISTIC_MAP.put(UUID_CURRENT_ID, "Electric current level");
        CHARACTERISTIC_MAP.put(UUID_BIKE_SPEED_ID, "Bike speed");
        CHARACTERISTIC_MAP.put(UUID_BIKE_FLAGS_ID, "Bike flags");
    }

    private int batValue;

    public int getBatValue() {
        return batValue;
    }
    private int currentValue;

    public int getCurrentValue() {
        return currentValue;
    }

    private int dutyCycleValue;

    public int getDutyCycleValue() {
        return dutyCycleValue;
    }

    private float rpmValue;
    public float getRPMValue() {
        return rpmValue;
    }
    private MyCustomServiceListener _listener = null;

    public void setServiceListener(MyCustomServiceListener listener) {
        _listener = listener;
    }

    protected MyCustomService(T model) {
        super(model);
    }

    @Override
    public String getServiceUUID() {
        return UUID_SERVICE;
    }

    @Override
    public String getName() {
        return "My custom service";
    }

    @Override
    public String getCharacteristicName(String uuid) {
        if (!CHARACTERISTIC_MAP.containsKey(uuid)) {
            return "Unknown";
        }
        return CHARACTERISTIC_MAP.get(uuid);
    }

    @Override
    public BleGattExecutor.ServiceAction[] update(final String uuid, final Bundle data) {
        byte[] bytes = {0, 0, 0, 0};

        try {
            bytes = ByteBuffer.allocate(4)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(Integer.parseInt(data.get(BLEConst.DATA).toString()))
                    .array();
        } catch (Exception ex) {
        }
        FileLog.Write("log_CustomService"+ uuid.substring(4,8),  "\t"+ data.get(BLEConst.DATA).toString());
        switch (uuid) {
            case UUID_PWM_DUTY_CYCLE_ID:
                return new BleGattExecutor.ServiceAction[]{
                        write(UUID_PWM_DUTY_CYCLE_ID, new byte[]{bytes[0]})
                };
            case UUID_MODE_ID:
                return new BleGattExecutor.ServiceAction[]{
                        write(UUID_MODE_ID, new byte[]{bytes[0]})
                };
            case UUID_V_THRESHOLD_ID:
                return new BleGattExecutor.ServiceAction[]{
                        write(UUID_V_THRESHOLD_ID, bytes)
                };
            default:
                return super.update(uuid, data);
        }
    }


    @Override
    protected boolean apply(final BluetoothGattCharacteristic c, final T data) {
        int intValue = 0;
        float floatValue = 0;
        switch (c.getUuid().toString()) {
            case UUID_BIKE_BATTERY_LEVEL_ID:
                batValue = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                if (batValue > 4096) batValue = 0;
                intValue = batValue;
                break;
            case UUID_CURRENT_ID:
                currentValue = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                if (currentValue > 4096) currentValue = 0;
                intValue = currentValue;
                break;
            case UUID_BIKE_SPEED_ID:
                byte bytes[] = c.getValue();
                rpmValue = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                //floatValue = c.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0);
                if (rpmValue > 4096) rpmValue = 0;
                if (rpmValue < 1) rpmValue = 0;
                floatValue = rpmValue;
                break;
            case UUID_PWM_DUTY_CYCLE_ID:
                dutyCycleValue = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                intValue = dutyCycleValue;
                break;
            case UUID_BIKE_FLAGS_ID:
                intValue = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                read(UUID_PWM_DUTY_CYCLE_ID);
                break;
        }

        if (_listener != null) {
            if (!c.getUuid().toString().equals( UUID_BIKE_SPEED_ID))
                _listener.OnCharacteristicChanged(c.getUuid().toString(), intValue);
            else
                _listener.OnCharacteristicChanged(c.getUuid().toString(), floatValue);
        }
       // Log.d(TAG, "MyCustomService.Value ='" + value);
        return true;
    }
}





















