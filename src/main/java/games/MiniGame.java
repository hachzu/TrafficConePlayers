package games;

import java.awt.Window;

import ui.DefaultUI;

@FunctionalInterface
public interface MiniGame {
    // A completed round returns its reward. Closing a round early returns zero.
    int play(Window parent, DefaultUI player);
}
