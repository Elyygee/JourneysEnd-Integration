package abeshutt.staracademy.card;

import abeshutt.staracademy.attribute.again.*;
import abeshutt.staracademy.world.roll.NumberRoll;
import abeshutt.staracademy.world.roll.UniformNumberRoll;

/**
 * Utility class for extracting display information from attribute trees
 */
public final class AttributeUtils {
    private AttributeUtils() {}
    
    public static NumberRoll findFirstNumberRoll(Attribute<?> a) {
        if (a instanceof NumberConstantAttribute n) return n.getConfig();
        if (a instanceof AssignAttribute<?> asg)    return findFirstNumberRoll(asg.getValue());
        if (a instanceof AddAttribute<?> add)       return findFirstNumberRoll(add.getValue());
        if (a instanceof MultiplyAttribute<?> mul)  return findFirstNumberRoll(mul.getValue());
        if (a instanceof NodeAttribute<?> node) {
            for (var m : node.getModifiers()) {
                var r = findFirstNumberRoll(m.getAttribute());
                if (r != null) return r;
            }
        }
        return null;
    }

    /**
     * Find the first UniformNumberRoll in an attribute tree
     */
    public static UniformNumberRoll findUniform(abeshutt.staracademy.attribute.again.Attribute<?> attribute) {
        if (attribute instanceof AddAttribute<?> add) {
            var value = add.getValue();
            if (value instanceof NodeAttribute<?> node) {
                for (var modifier : node.getModifiers()) {
                    var modAttr = modifier.getAttribute();
                    if (modAttr instanceof AssignAttribute<?> assign) {
                        var assignValue = assign.getValue();
                        if (assignValue instanceof NumberConstantAttribute constant) {
                            var roll = constant.getConfig();
                            if (roll instanceof UniformNumberRoll uniform) {
                                return uniform;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find the first constant value in an attribute tree
     */
    public static NumberConstantAttribute findConstant(abeshutt.staracademy.attribute.again.Attribute<?> attribute) {
        if (attribute instanceof AddAttribute<?> add) {
            var value = add.getValue();
            if (value instanceof NodeAttribute<?> node) {
                for (var modifier : node.getModifiers()) {
                    var modAttr = modifier.getAttribute();
                    if (modAttr instanceof AssignAttribute<?> assign) {
                        var assignValue = assign.getValue();
                        if (assignValue instanceof NumberConstantAttribute constant) {
                            return constant;
                        }
                    }
                }
            }
        }
        return null;
    }
}
