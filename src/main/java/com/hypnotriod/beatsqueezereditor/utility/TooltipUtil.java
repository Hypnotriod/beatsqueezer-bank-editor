package com.hypnotriod.beatsqueezereditor.utility;

import com.hypnotriod.beatsqueezereditor.constants.Styles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 *
 * @author Ilya Pikin
 */
public class TooltipUtil {

    public static Tooltip getTooltipDefault(String str) {
        return getTooltip(str, Styles.TOOLTIP1_STYLE, 600, 60000, 200);
    }

    /* Java 11 */
    public static Tooltip getTooltip(String str, String style, double openMillis, double visibleMillis, double closeMillis) {
        Tooltip tooltip = new Tooltip(str);
        tooltip.setStyle(style);
        tooltip.setShowDelay(new Duration(openMillis));
        tooltip.setShowDuration(new Duration(visibleMillis));
        tooltip.setHideDelay(new Duration(closeMillis));
        return tooltip;
    }
    /* End of Java 11 */

    /* Java 8 */
    /*
    public static Tooltip getTooltip(String str, String style, double openMillis, double visibleMillis, double closeMillis) {
        Tooltip tooltip = new Tooltip(str);
        tooltip.setStyle(style);
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
    */
    /* End of Java 8 */
}
