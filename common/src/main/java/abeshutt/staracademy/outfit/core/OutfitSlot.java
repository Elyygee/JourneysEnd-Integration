package abeshutt.staracademy.outfit.core;

import net.minecraft.entity.EquipmentSlot;

// TODO: Integrate later
public enum OutfitSlot {

    HEAD(EquipmentSlot.HEAD),
    CHEST(EquipmentSlot.CHEST),
//    BELT, // Theoretical outfit slot, that isn't associated with any of the existing slots
    LEGS(EquipmentSlot.LEGS),
    FEET(EquipmentSlot.FEET),
    ;

    EquipmentSlot overwrittenSlot = null;

    OutfitSlot() {}

    OutfitSlot(EquipmentSlot negatedSlot) {
        this.overwrittenSlot = negatedSlot;
    }

    public EquipmentSlot getOverwrittenSlot() {
        return overwrittenSlot;
    }

}
