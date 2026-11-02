package com.example.al3malasquez_patitas;

import java.text.NumberFormat;
import java.util.Locale;

public final class Money {
    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(new Locale("es", "CO"));

    private Money() {
    }

    public static String format(int value) {
        return "$ " + FORMAT.format(value);
    }
}
