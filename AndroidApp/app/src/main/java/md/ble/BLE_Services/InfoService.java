package md.ble.BLE_Services;

import android.bluetooth.BluetoothGattCharacteristic;

import com.chimeraiot.android.ble.sensor.Sensor;

/**
 * BLE info service.
 * @param <M> data model.
 */
public abstract class InfoService<M> extends Sensor<M> {

    /** Data value. */
    private String value;

    protected InfoService(M model) {
        super(model);
    }

    public String getValue() {
        return value;
    }

    @Override
    protected boolean apply(final BluetoothGattCharacteristic c, final M data) {
        value = c.getStringValue(0);
        return true;
    }
}
