package com.hypnotriod.beatsqueezereditor.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 *
 * @author Ilya Pikin
 */
public class TooltipHelper {

    public static Tooltip getTooltip1(String str) {
        return getTooltip(str, 800, 60000, 200);
    }

    public static Tooltip getTooltip(String str, double openMillis, double visibleMillis, double closeMillis) {
        Tooltip tooltip = new Tooltip(str);
        tooltip.setStyle("-fx-font-size: 12");
        try {
            Class<?> clazz = tooltip.getClass().getDeclaredClasses()[0];
            Constructor<?> constructor = clazz.getDeclaredConstructor(
                    Duration.class,
                    Duration.class,
                    Duration.class,
                    boolean.class);
            constructor.setAccessible(true);
            Object tooltipBehavior = constructor.newInstance(
                    new Duration(openMillis), //open
                    new Duration(visibleMillis), //visible
                    new Duration(closeMillis), //close
                    false);
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            fieldBehavior.set(tooltip, tooltipBehavior);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return tooltip;
    }

}
