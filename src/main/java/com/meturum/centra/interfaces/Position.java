package com.meturum.centra.interfaces;

import org.jetbrains.annotations.NotNull;

public record Position(int x, int y) {

    public Position {
        if (x < 0 || y < 0)
            throw new IllegalArgumentException("Cannot create a position with negative coordinates.");

        if (x > 9 || y > 11)
            throw new IllegalArgumentException("Cannot create a position with coordinates greater than x>9 or y>11.");

    }

    /**
     * @return The x coordinate of this position.
     */
    @Override
    public int x() {
        return x;
    }

    /**
     * @return The y coordinate of this position.
     */
    @Override
    public int y() {
        return y;
    }

    /**
     * Converts this position to an integer.
     *
     * @return The integer representation of this position.
     */
    public int toInt() {
        return y * 9 + x;
    }

    /**
     * Converts an integer to a position.
     *
     * @param position The integer to convert.
     * @return The position representation of the integer.
     * @throws IllegalArgumentException If the position could not be instantiated.
     */
    public static @NotNull Position of(int position) throws IllegalArgumentException {
        int y = position / 9;
        int x = position - (y * 9);

        return new Position(x, y);
    }

}
