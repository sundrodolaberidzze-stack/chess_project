package org.example.chess;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static final Map<String, AudioClip> clips = new HashMap<>();
    private static boolean enabled = true;

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static void toggle() { enabled = !enabled; }

    public static void play(String name) {
        if (!enabled) return;
        try {
            AudioClip clip = clips.get(name);
            if (clip == null) {
                URL url = findSound(name);
                if (url == null) return;
                clip = new AudioClip(url.toExternalForm());
                clips.put(name, clip);
            }
            clip.stop();
            clip.play();
        } catch (Exception ignored) {}
    }

    private static URL findSound(String name) {
        URL url = SoundManager.class.getResource("/SoundEffects/" + name + ".wav");
        if (url != null) return url;
        url = SoundManager.class.getResource("/SoundEffects/" + name + ".mp3");
        if (url != null) return url;
        return SoundManager.class.getResource("/SoundEffects/" + name + ".ogg");
    }

    public static void button() { play("Confirmation"); }
    public static void select() { play("Select"); }
    public static void move() { play("Move"); }
    public static void capture() { play("Capture"); }
    public static void check() { play("Check"); }
    public static void checkmate() { play("Checkmate"); }
    public static void victory() { play("Victory"); }
    public static void defeat() { play("Defeat"); }
    public static void draw() { play("Draw"); }
    public static void error() { play("Error"); }
    public static void notifySound() { play("GenericNotify"); }
    public static void lowTime() { play("LowTime"); }
}
