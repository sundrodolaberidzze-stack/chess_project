package org.example.chess;

public class SettingsState {
    private static boolean premovesEnabled;
    private static boolean fullscreenEverywhere;
    private static String boardTheme = "Classic";

    public static boolean isPremovesEnabled() {
        return premovesEnabled;
    }

    public static void setPremovesEnabled(boolean enabled) {
        premovesEnabled = enabled;
    }

    public static boolean isFullscreenEverywhere() {
        return fullscreenEverywhere;
    }

    public static void setFullscreenEverywhere(boolean enabled) {
        fullscreenEverywhere = enabled;
    }

    public static String getBoardTheme() {
        return boardTheme;
    }

    public static void setBoardTheme(String theme) {
        boardTheme = theme == null || theme.isBlank() ? "Classic" : theme;
    }

    public static String lightSquareColor() {
        return switch (boardTheme) {
            case "Blue" -> "#D9EAF7";
            case "Brown" -> "#F0D9B5";
            case "Gray" -> "#D6D6D6";
            case "Purple" -> "#E3D5F5";
            default -> "#eeeed2";
        };
    }

    public static String darkSquareColor() {
        return switch (boardTheme) {
            case "Blue" -> "#4A7FA7";
            case "Brown" -> "#B58863";
            case "Gray" -> "#777777";
            case "Purple" -> "#7A5BA6";
            default -> "#769656";
        };
    }
}
