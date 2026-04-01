package progression;

import data.progression.AttributesData;

public record EquipmentStatProfile(
        AttributesData attributes,
        int weaponPower,
        int armor,
        double movementSpeed) {
    public static final EquipmentStatProfile NONE = new EquipmentStatProfile(
            new AttributesData(0, 0, 0, 0, 0),
            0,
            0,
            0.0);
}
