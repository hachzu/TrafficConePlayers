package com.tcp.ui;

import com.tcp.web.LocalPageServer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TrafficConePlayerWindow {
	
	// the real challenge would be implementing the other features like Video manipulation 
	// file management , and yada yada yada alot of these are going to change if TrafficConePlayerWindow becomes a object

	public static void main(String[] args) {
		LocalPageServer pageServer;
		try {
			pageServer = new LocalPageServer();
		} catch (java.io.IOException exception) {
			JOptionPane.showMessageDialog(null, "Could not start the local video page: " + exception.getMessage(),
					"Traffic Cone Player", JOptionPane.ERROR_MESSAGE);
			return;
		}

		SwingUtilities.invokeLater(() -> {
			JFrame mainframe = new JFrame();
			// Default dimensions 
			mainframe.setSize(new Dimension (800 , 400));
			mainframe.setBackground(Color.CYAN);

			JPanel mainpanel = new JPanel();
			mainpanel.setBackground(Color.gray);
			mainpanel.setLayout( new BorderLayout());

			// code about media or just video manipulation and etc..
			JPanel videopanel = new JPanel();
			videopanel.setLayout(new BorderLayout());
			JLabel mediaName = new JLabel("Loading media...");
			JLabel mediaAuthor = new JLabel("Author: Loading...");
			MediaPlayerView mediaPlayerView = new MediaPlayerView(pageServer.getUrl(), (title, artist) -> {
				mediaName.setText(title);
				mediaAuthor.setText("Author: " + artist);
			});
			videopanel.add(mediaPlayerView , BorderLayout.CENTER);

			// honestly we could either go JButton or JPanel with event listeners
			JButton playButton = new JButton("Play");
			boolean[] isPlaying = {false};
			playButton.addActionListener((ActionEvent e) -> {
				isPlaying[0] = !isPlaying[0];
				mediaPlayerView.setPlaying(isPlaying[0]);
				playButton.setText(isPlaying[0] ? "Pause" : "Play");
			});

			JSlider videoslider = new JSlider();
			videoslider.addChangeListener( new ChangeListener(){
				@Override
				public void stateChanged(ChangeEvent e) {
					// Volume Changing Logic
					int audiovalue = videoslider.getValue();
					System.out.println(audiovalue);
				}
			});

			JButton nextButton = new JButton("Next");
			nextButton.addActionListener((ActionEvent e) -> {
				// playnext logic
				System.out.println("Clicked Next");
			});

			JButton previousButton = new JButton("Previous");
			previousButton.addActionListener((ActionEvent e)-> {
				// playlast logic 
				System.out.println("Clicked Back");
			});

			JButton shuffleButton = new JButton("Shuffle");
			JButton profilesButton = new JButton("Profiles");
			JButton shopButton = new JButton("Shop");
			JButton otherButton = new JButton("Other");
			otherButton.setToolTipText("Open a local MP3 or MP4 file");
			otherButton.addActionListener(event -> {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FileNameExtensionFilter("Audio and video files (*.mp3, *.mp4)", "mp3", "mp4"));
				if (chooser.showOpenDialog(mainframe) == JFileChooser.APPROVE_OPTION) {
					isPlaying[0] = false;
					playButton.setText("Play");
					mediaPlayerView.openLocalMedia(chooser.getSelectedFile());
				}
			});

			JPanel utilitybar = new JPanel();
			utilitybar.setPreferredSize(new Dimension( 100 , 50));
			utilitybar.setLayout(new BorderLayout());

			JPanel buttonbar = new JPanel(new BorderLayout());
			JPanel playbackButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
			playbackButtons.add(previousButton);
			playbackButtons.add(playButton);
			playbackButtons.add(nextButton);
			playbackButtons.add(shuffleButton);
			JPanel extraButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			extraButtons.add(shopButton);
			extraButtons.add(profilesButton);
			extraButtons.add(otherButton);
			buttonbar.add(playbackButtons, BorderLayout.WEST);
			buttonbar.add(extraButtons, BorderLayout.EAST);
			utilitybar.add(videoslider, BorderLayout.NORTH);
			utilitybar.add(buttonbar, BorderLayout.SOUTH);

			// sidebar settings
			JPanel sidebar = new JPanel(new BorderLayout());
			sidebar.setBackground(Color.green);
			sidebar.setPreferredSize(new Dimension ( 300 , 400));
			//sidebar components 

			JPanel mediaInfo = new JPanel(new GridLayout(2, 1));
			mediaInfo.add(mediaName);
			mediaInfo.add(mediaAuthor);
			sidebar.add(mediaInfo, BorderLayout.NORTH);

			// bottom bar components like information and stuff
			mainpanel.add(videopanel , BorderLayout.CENTER);
			mainpanel.add(sidebar , BorderLayout.EAST);

			//mainframe settings
			mainframe.setLayout(new BorderLayout());
			mainframe.add(mainpanel , BorderLayout.CENTER);
			mainframe.add(utilitybar , BorderLayout.SOUTH);
			mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainframe.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent event) {
					pageServer.close();
				}
			});
			mainframe.setVisible(true);
		});
	}
}
