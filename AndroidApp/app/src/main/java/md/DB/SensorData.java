package md.DB;

public class SensorData{
    @com.google.gson.annotations.SerializedName("id")
    private String _id;
    @com.google.gson.annotations.SerializedName("SensorID")
    private String _sensorID;
    @com.google.gson.annotations.SerializedName("CharacteristicID")
    private String _characteristicID;
    @com.google.gson.annotations.SerializedName("Value")
    private String _value;

    public SensorData(String sensorID, String characteristicID, String value) {
        _sensorID = sensorID;
        _characteristicID = characteristicID;
        _value = value;
    }

    @Override
    public String toString() {
        return _sensorID;
    }




    @Override
    public boolean equals(Object o) {
        return o instanceof SensorData && ((SensorData) o)._id == _id;
    }
}