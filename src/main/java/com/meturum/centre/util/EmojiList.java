package com.meturum.centre.util;

import org.jetbrains.annotations.NotNull;

public enum EmojiList {

    ERROR_ICON('⚠', "error"),
    WARNING_ICON('⧮', "warning"),
    SUCCESS_ICON('✔', "success");

    private final char emoji;
    private String name;

    EmojiList(char emoji, @NotNull String name) {
        this.emoji = emoji;
        this.name = name;
    }

    /**
     * @return The emoji character.
     */
    public char getEmoji() {
        return emoji;
    }

    /**
     * @return The name of the emoji.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The emoji as a string. (e.g. :success:)
     */
    public String getChatFormat() {
        return ":"+name+":";
    }

    /**
     * Sets the emoji's name.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The spacing character.
     *
     * @apiNote  I WOULD GIVE ANYTHING TO MAKE THIS BETTER. (I'm mainly too lazy to make it better ;) .)
     * TODO: Refactor this method by using HashMaps. (HashMap<Integer, String>)
     */
    public static String getSpacingCharacter(int space) {
        switch (space) {
            case 1 -> {
                return "\uF821";
            }
            case 2 -> {
                return "\uF822";
            }
            case 3 -> {
                return "\uF823";
            }
            case 4 -> {
                return "\uF824";
            }
            case 5 -> {
                return "\uF825";
            }
            case 6 -> {
                return "\uF826";
            }
            case 7 -> {
                return "\uF827";
            }
            case 8 -> {
                return "\uF828";
            }
            case 16 -> {
                return "\uF829";
            }
            case 32 -> {
                return "\uF82A";
            }
            case 64 -> {
                return "\uF82B";
            }
            case 128 -> {
                return "\uF82C";
            }
            case 512 -> {
                return "\uF82D";
            }
            case 1024 -> {
                return "\uF82E";
            }
        }

        switch (space) {
            case -1 -> {
                return "\uF801";
            }
            case -2 -> {
                return "\uF802";
            }
            case -3 -> {
                return "\uF803";
            }
            case -4 -> {
                return "\uF804";
            }
            case -5 -> {
                return "\uF805";
            }
            case -6 -> {
                return "\uF806";
            }
            case -7 -> {
                return "\uF807";
            }
            case -8 -> {
                return "\uF808";
            }
            case -16 -> {
                return "\uF809";
            }
            case -32 -> {
                return "\uF80A";
            }
            case -64 -> {
                return "\uF80B";
            }
            case -128 -> {
                return "\uF80C";
            }
            case -512 -> {
                return "\uF80D";
            }
            case -1024 -> {
                return "\uF80E";
            }
        }

        return null;
    }

    public static String read(String string) {
        for(EmojiList emoji : EmojiList.values()) {
            string = string.replace(":"+emoji.getName()+":", String.valueOf(emoji.getEmoji()));
        }

        return string;
    }

    @Override
    public String toString() {
        return String.valueOf(emoji);
    }
}