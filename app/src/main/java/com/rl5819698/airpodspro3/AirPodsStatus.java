package com.rl5819698.airpodspro3;

public final class AirPodsStatus {
    public final String mac, model;
    public final int left, right, caseBattery, rssi;
    public final boolean leftCharging, rightCharging, caseCharging, leftInEar, rightInEar;
    public final long timestamp;
    public AirPodsStatus(String mac, String model, int left, int right, int caseBattery,
                         boolean leftCharging, boolean rightCharging, boolean caseCharging,
                         boolean leftInEar, boolean rightInEar, int rssi) {
        this.mac = mac; this.model = model; this.left = left; this.right = right;
        this.caseBattery = caseBattery; this.leftCharging = leftCharging;
        this.rightCharging = rightCharging; this.caseCharging = caseCharging;
        this.leftInEar = leftInEar; this.rightInEar = rightInEar; this.rssi = rssi;
        this.timestamp = System.currentTimeMillis();
    }
    public static int battery(int nibble) {
        if (nibble >= 0 && nibble <= 10) return nibble == 10 ? 100 : nibble * 10 + 5;
        return -1;
    }
}
