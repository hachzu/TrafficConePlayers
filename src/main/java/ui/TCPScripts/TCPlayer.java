package ui.TCPScripts;

import java.util.function.Consumer;

import javax.swing.*;

import java.awt.*;

public class TCPlayer extends JPanel{

    public interface VideoBackend {
    JComponent getView();
    void play();
    void pause();
    void seekTo(double seconds);
    void setVolume(double volume); // 0.0 to 1.0
    void getDuration(Consumer<Double> callback);
    void getCurrentTime(Consumer<Double> callback);
    void nextTrack();
    void previousTrack();
    void close();
    void viewplaylist();
    }

    public void setVideoBackend(VideoBackend next) {
    if (videoBackend != null) {
        videoPanel.remove(videoBackend.getView());
        videoBackend.close();
    }

    videoBackend = next;
    if (next != null) {
        videoPanel.add(next.getView(), BorderLayout.CENTER);
    }

    videoPanel.revalidate();
    videoPanel.repaint();
}

    // C1 
    private VideoBackend videoBackend;
    private final JPanel mainpanel = new JPanel(new BorderLayout());
    private final JPanel videoPanel = new JPanel(new BorderLayout());


    // TCPlayer constructor
    public TCPlayer() {
        setLayout(new BorderLayout());
        add(mainpanel, BorderLayout.CENTER);

        // C1 - 1 Video Panel
        mainpanel.add(videoPanel, BorderLayout.CENTER);
        
        // C3 - 2 - Buttons & Timeline Slider 
		TCPButton playbutton = new TCPButton("Play");

		playbutton.addActionListener(e -> {
            if (videoBackend != null) videoBackend.play();
        });

		
		TCPButton pausebutton = new TCPButton("Pause");

		pausebutton.addActionListener(e -> {
            if (videoBackend != null) videoBackend.pause();
        });
			
		
		TCPSlider videoslider = new TCPSlider();
			videoslider.addChangeListener(e -> {
				if (!videoslider.getValueIsAdjusting() && videoBackend != null) {
                    double percentage = videoslider.getValue() / 100.0;
                    videoBackend.getDuration(duration -> {
                        if (duration > 0) videoBackend.seekTo(duration * percentage);
                    });
				}
			});
		
		
		TCPButton next = new TCPButton("Next");
			
		next.addActionListener(e -> {
            if (videoBackend != null) videoBackend.nextTrack();
        });
		
		TCPButton back = new TCPButton("Back");
		
		back.addActionListener(e -> {
            if (videoBackend != null) videoBackend.previousTrack();
        });

        // C3 - Buttons & Timeline Panel
		JPanel utilitybar = new JPanel();
		utilitybar.setPreferredSize(new Dimension( 100 , 50));
		utilitybar.setLayout( new BoxLayout(utilitybar , BoxLayout.Y_AXIS));
		utilitybar.setBackground(Color.CYAN);

		// Buttonbar is set as opaque so as to not bother the background of utilitybar 
		JPanel buttonbar = new JPanel();
		buttonbar.setLayout( new BoxLayout(buttonbar , BoxLayout.X_AXIS));
		buttonbar.setOpaque(true);
		buttonbar.add(back);
		buttonbar.add(playbutton);
		buttonbar.add(pausebutton);
		buttonbar.add(next);
		
		// C3 - 1 - Timeline
		
		utilitybar.add(videoslider);

		// C3 - 2 - Buttons
		utilitybar.add(buttonbar);

        TCPSlider audioslider = new TCPSlider();

		audioslider.addChangeListener(e -> {
            if (videoBackend != null) {
                videoBackend.setVolume(audioslider.getValue() / 100.0);
            }
        });

        // C2  - Terminal and Grid - Swap PlayLogic Button


		JPanel terminal = new JPanel();
		terminal.setPreferredSize(new Dimension( 100 , 50));
		terminal.setBackground(Color.BLACK);

		JPanel grid = new JPanel();
		grid.setPreferredSize(new Dimension( 100 , 50));
		grid.setBackground(Color.PINK);
		
        JButton swap = new JButton("Swap");
		swap.addActionListener(e -> {
            if (videoBackend instanceof WebPlayer) {
                setVideoBackend(new LocalPlayer());
            } else {
                try {
                    setVideoBackend(new WebPlayer());
                }catch (java.io.IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Could not start the web player: " + ex.getMessage(),
                            "Player switch failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton playlistButton = new JButton("Videos");
        playlistButton.addActionListener(e -> {
            if (videoBackend != null) videoBackend.viewplaylist();
        });
		
		
		// sidebar settings
		JPanel sidebar = new JPanel();
		sidebar.setBackground(Color.green);
		sidebar.setLayout(new BoxLayout(sidebar , BoxLayout.Y_AXIS));
		sidebar.add(terminal);
		sidebar.add(audioslider);
        sidebar.add(swap);
        sidebar.add(playlistButton);
		sidebar.add(grid);

        // bottom bar components like information and stuff
		
		mainpanel.add(sidebar , BorderLayout.EAST);
        mainpanel.add(utilitybar , BorderLayout.SOUTH);
		

    }

	public static void main(String[] args) {
		// Value initialization for the dimensions of the TCP! and its components
		Dimension side = new Dimension ( 300 , 400);
		Dimension tcp = new Dimension ( 800 , 400);
		Dimension bottom = new Dimension ( 300 , 400);
	
		SwingUtilities.invokeLater(() -> {
			
		// The Whole Frame of the TCP
		JFrame mainframe = new JFrame("TCP!");
        TCPlayer player = new TCPlayer();
        try {
            player.setVideoBackend(new WebPlayer());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not start the local WebPlayer server", e);
        }

        //mainframe settings
		mainframe.setLayout(new BorderLayout());
		mainframe.setContentPane(player);

		mainframe.setSize(tcp);
		mainframe.setBackground(Color.CYAN);	
		mainframe.setVisible(true);
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainframe.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                player.setVideoBackend(null);
            }
		});



		});
	}
	
}
