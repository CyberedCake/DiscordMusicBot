package net.cybercake.discordmusicbot.generalutils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberUtils {

    /**
     * Formats a double decimal a certain number of places and with a certain {@link RoundingMode}
     * @param value the value to format
     * @param places the amount of places to show. for example, "3" would be "3.141" and "5" would be "3.14159"
     * @param roundingMode the {@link RoundingMode} to use for the {@link DecimalFormat}
     * @return the decimal format in {@link String} form
     * @since 15
     */
    public static String formatDecimal(double value, int places, RoundingMode roundingMode) {
        DecimalFormat decimalFormat = new DecimalFormat("0." + "#".repeat(Math.max(0, places)));
        decimalFormat.setRoundingMode(roundingMode);
        return decimalFormat.format(value);
    }

    /**
     * Formats a double decimal a certain number of places and with an assumed rounding mode: {@link RoundingMode#HALF_EVEN}
     * @param value the value to format
     * @param places the amount of places to show. for example, "3" would be "3.141" and "5" would be "3.14159"
     * @return the decimal format in {@link String} form
     * @since 15
     */
    public static String formatDecimal(double value, int places) {
        return formatDecimal(value, places, RoundingMode.HALF_EVEN);
    }

}
