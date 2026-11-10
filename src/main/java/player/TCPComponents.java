package player;

import java.util.function.Consumer;

import javax.swing.JComponent;

public interface TCPComponents {
    JComponent getView();
    void play();
    void pause();
    void seekTo(double seconds);
    void setVolume(double volume);
    void getDuration(Consumer<Double> callback);
    void getCurrentTime(Consumer<Double> callback);
    void nextTrack();
    void previousTrack();
    void close();
    void viewplaylist();
}


