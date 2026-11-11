package player;

import java.awt.CardLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class TCPLocalPlayer extends JPanel implements TCPComponents {
    private static final String VIDEO_CARD = "video";
    private static final String VINYL_CARD = "vinyl";

    private final List <String> PLAYLIST;
    private final JFXPanel VIDEOSURFACE = new JFXPanel();
    private final TCPVinyl VINYLPANEL = new TCPVinyl();
    private final Path TRACKSFOLDER = Path.of("src", "main", "java", "player" , "Tracks");

    private volatile int currentIndex;
    private volatile int playlistSize;

    private volatile boolean audioOnly;
    private volatile boolean videoMode;

    private volatile String currentTrackTitle = "";
    private MediaPlayer mediaPlayer;

    private volatile Consumer<Boolean> audioTrackListener = audioOnly -> { };
    
    private JFrame playlistFrame;
    private JPanel playlistRows;

    public TCPLocalPlayer() {

        // Layout stuff yipee!!

       super(new CardLayout());
       add(VIDEOSURFACE, VIDEO_CARD);
       add(VINYLPANEL, VINYL_CARD);
       ((CardLayout) getLayout()).show(this, VINYL_CARD);
       VINYLPANEL.setPlaying(true);

       PLAYLIST = new ArrayList<>();
       try {
           loadPlayListFromFolder();
       } catch (IOException error) {
           System.err.println("Could not load tracks from " + TRACKSFOLDER + ": " + error.getMessage());
       }
    }

    public JComponent getView() {
        return this;
    }

    private void loadPlayListFromFolder() throws IOException{
        PLAYLIST.clear(); // Clears Playlist before loading new files

            /*
            1 Java infers that "files" is. a stream path from var keyword + its try with rescources 
            2. Opens Folder and returns a sort of list called a STREAM filled with path objects 
            3. Sorted in Natural order - the order you see the tracks listed as in the folder 
            4. Maps each Path Object to String
            5. add Each String to Playlist
            6. if playlist NOT empty play - loadtrack(PLAYLIST(index) , Boolean AutoPlay)
            */
            
            //1
            try(var files = Files.list(TRACKSFOLDER)) {
                //2
                files.filter(Files::isRegularFile)
                    .filter(TCPLocalPlayer::isSupportedMedia)
                    //3
                    .sorted()
                    //4
                    .map(Path::toString)
                    //5
                    .forEach(PLAYLIST::add);
            }

        playlistSize = PLAYLIST.size();
        currentIndex = 0;
        
        //6
        if(!PLAYLIST.isEmpty()) {
            Platform.runLater(() -> { 
                loadTrack(currentIndex , false);
            });
        }
    }

    public void loadTrack(int index, boolean autoplay) {
        if (PLAYLIST.isEmpty()) return;
        if (mediaPlayer != null) {
           mediaPlayer.dispose();
           mediaPlayer = null;
       }
       

            currentIndex = Math.floorMod(index, PLAYLIST.size());
            
            String directory = PLAYLIST.get(currentIndex);

            Path mediaPath = Path.of(directory).toAbsolutePath().normalize();


            // Path Checks 
            if (!Files.isRegularFile(mediaPath)) {
                System.out.println("Media file is missing or unsupported: " + mediaPath);
                return;
            }


            // Misc Data
            currentTrackTitle = mediaPath.getFileName().toString();
            playlistSize = PLAYLIST.size();

            audioOnly = isAudioResource(directory);
                if (audioOnly) setVideoMode(false);
                    audioTrackListener.accept(audioOnly);


            Media media = new Media(mediaPath.toUri().toString());
            MediaPlayer newPlayer = new MediaPlayer(media);
            mediaPlayer = newPlayer;

            //Stylistic Choices 

            MediaView mediaView = new MediaView(newPlayer);
            mediaView.setPreserveRatio(true);

            StackPane videoRoot = new StackPane(mediaView);
            videoRoot.setStyle("-fx-background-color: black;");

            mediaView.fitWidthProperty().bind(videoRoot.widthProperty());
            mediaView.fitHeightProperty().bind(videoRoot.heightProperty());


            // Finally it sets the scene
            VIDEOSURFACE.setScene(new Scene(videoRoot));


            newPlayer.setOnReady(() -> {
            if (autoplay && mediaPlayer == newPlayer) newPlayer.play();
             });

            newPlayer.setOnEndOfMedia(this::nextTrack);


            // on the off chance of an error, print trace
            newPlayer.setOnError(() -> {
            Throwable error = newPlayer.getError();
            if (error != null) error.printStackTrace();
            });





    }

    public void importMedia() {
        SwingUtilities.invokeLater(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Video and audio files", "mp4", "m4v", "mov", "mp3", "m4a", "wav"));
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

            File selectedFile = chooser.getSelectedFile();
            if (!isSupportedMedia(selectedFile.toPath())) return;
            try {
                Files.createDirectories(TRACKSFOLDER);
                String filename = selectedFile.getName();
                int dot = filename.lastIndexOf('.');
                String extension = dot >= 0 ? filename.substring(dot) : "";
                Path destination = TRACKSFOLDER.resolve(UUID.randomUUID() + extension);
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.COPY_ATTRIBUTES);
                PLAYLIST.add(destination.toString());
                playlistSize = PLAYLIST.size();
            } catch (IOException error) {
                JOptionPane.showMessageDialog(this,
                        "Could not copy the media file:\n" + error.getMessage(),
                        "Import failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void setVideoMode(boolean videoMode){

         this.videoMode = videoMode && !audioOnly;

         SwingUtilities.invokeLater(() -> {
            CardLayout cards = (CardLayout) getLayout();
                cards.show(this, this.videoMode ? VIDEO_CARD : VINYL_CARD);
                    VINYLPANEL.setPlaying(!this.videoMode);
            });
        
    }
    
    private static boolean isSupportedMedia(Path path) {
    String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
    return name.endsWith(".mp4")
            || name.endsWith(".m4v")
            || name.endsWith(".mov")
            || name.endsWith(".mp3")
            || name.endsWith(".m4a")
            || name.endsWith(".wav");
    }
    
    private static boolean isAudioResource(String resource){
       String lower = resource.toLowerCase();
       int queryStart = lower.indexOf('?');
       if (queryStart >= 0) lower = lower.substring(0, queryStart);
       return lower.endsWith(".m4a") || lower.endsWith(".mp3") || lower.endsWith(".wav");
    }   

    
    public void setAudioTrackListener(Consumer<Boolean> listener) {
       audioTrackListener = listener;
       if (!PLAYLIST.isEmpty()) {
       listener.accept(isAudioResource(PLAYLIST.get(currentIndex)));
       }
   }

    @Override
    public void play() {
         Platform.runLater(() -> {
           if (mediaPlayer != null) mediaPlayer.play();
       });

    }

    @Override
    public void pause() {
        Platform.runLater(() -> {
           if (mediaPlayer != null) mediaPlayer.pause();
       });
    }

    @Override
    public void getDuration(Consumer<Double> callback) {
         Platform.runLater(() -> {
           Duration duration = mediaPlayer == null ? null : mediaPlayer.getTotalDuration();
           double seconds = duration == null ? 0.0 : duration.toSeconds();
           callback.accept(Double.isFinite(seconds) ? seconds : 0.0);
       });

    }

    public void getTrackDetails(Consumer<TrackDetails> callback) {
        Platform.runLater(() -> {
            double duration = mediaPlayer == null || mediaPlayer.getTotalDuration() == null
                    ? 0.0 : mediaPlayer.getTotalDuration().toSeconds();
            double currentTime = mediaPlayer == null
                    ? 0.0 : mediaPlayer.getCurrentTime().toSeconds();
            if (!Double.isFinite(duration) || duration < 0) duration = 0.0;
            if (!Double.isFinite(currentTime) || currentTime < 0) currentTime = 0.0;
            int trackNumber = PLAYLIST.isEmpty() ? 0 : currentIndex + 1;
            callback.accept(new TrackDetails(currentTrackTitle, trackNumber,
                    PLAYLIST.size(), currentTime, duration));
        });
    }

    public record TrackDetails(String title, int trackNumber, int trackCount,
            double currentTime, double duration) { }

    @Override
    public void getCurrentTime(Consumer<Double> callback) {
        Platform.runLater(() -> {
           double seconds = mediaPlayer == null
                   ? 0.0
                   : mediaPlayer.getCurrentTime().toSeconds();
           callback.accept(Double.isFinite(seconds) ? seconds : 0.0);
       });
    }

    @Override
    public void nextTrack() {
        Platform.runLater(() -> {
           if (!PLAYLIST.isEmpty()) loadTrack(currentIndex + 1, true);
       });

    }

    @Override
    public void previousTrack() {
        Platform.runLater(() -> {
           if (!PLAYLIST.isEmpty()) loadTrack(currentIndex - 1, true);
       });

    }

    @Override
    public void close() {
        Platform.runLater(() -> {
           if (mediaPlayer != null) {
               mediaPlayer.dispose();
               mediaPlayer = null;
           }
       });
       VINYLPANEL.close();
    }

    @Override
    public void viewplaylist() {
    }

    @Override 
    public void seekTo(double seconds) {
          if (!Double.isFinite(seconds) || seconds < 0) return;
       Platform.runLater(() -> {
           if (mediaPlayer != null) mediaPlayer.seek(Duration.seconds(seconds));
       });

    }

    @Override
    public void setVolume(double volume) {
        double safeVolume = Math.max(0.0, Math.min(1.0, volume));
           VINYLPANEL.setVolume(safeVolume);
           Platform.runLater(() -> {
           if (mediaPlayer != null) mediaPlayer.setVolume(safeVolume);
       });

    }


}

    
