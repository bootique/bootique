package io.bootique.value;

import java.util.Objects;

/**
 * Represents a percent value.
 *
 * @since 0.26
 */
public class Percent {

    private double percent;

    /**
     * Creates a Percent instance from a String representation. E.g.
     *
     * @param value a String value representing percentage. Optionally followed by the percent sign. E.g. "0.5%",
     *              "100%", "-7.9%".
     */
    public Percent(String value) {
        this(parse(value));
    }

    public Percent(int value) {
        this.percent = value;
    }

    public Percent(double value) {
        this.percent = value;
    }

    static double parse(String percent) {
        Objects.requireNonNull(percent, "Null 'percent' argument");

        if (percent.length() == 0) {
            throw new IllegalArgumentException("Empty 'percent' argument");
        }

        if (percent.charAt(percent.length() - 1) == '%') {
            percent = percent.substring(0, percent.length() - 1);
        }

        if (percent.length() == 0) {
            throw new IllegalArgumentException("Non-numeric 'percent' argument: '%'");
        }

        return Double.parseDouble(percent);
    }

    /**
     * Returns a double value for this percentage that is a fraction of 1. I.e. will return 0.05 for 5%.
     *
     * @return a double value for this percentage that is a fraction of 1.
     */
    public double getValue() {
        return percent / 100.;
    }

    public double getPercent() {
        return percent;
    }

    @Override
    public String toString() {
        return percent + "%";
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Percent) {
            return percent == ((Percent) obj).percent;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(percent);
    }
}
