package ui.TCPScripts;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.embed.swing.JFXPanel;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import web.LocalServer;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;

public class TCPlayer {
	
	// the real challenge would be implementing the other features like Video manipulation 
	// file management , and yada yada yada alot of these are going to change if Tcplayer becomes a object

	public static void main(String[] args) {

		// Value initialization for the dimensions of the TCP! and its components
		Dimension side = new Dimension ( 300 , 400);
		Dimension tcp = new Dimension ( 800 , 400);
		Dimension bottom = new Dimension ( 300 , 400);

		LocalServer pageServer;
		try {
			pageServer = new LocalServer(8080);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		WebPlayer webplayer = new WebPlayer(pageServer.getURL());
	
		SwingUtilities.invokeLater(() -> {
			
		// The Whole Frame of the TCP
		JFrame mainframe = new JFrame();
		// Default dimensions 
		mainframe.setSize(tcp);
		mainframe.setBackground(Color.CYAN);
		
		// C1 
		
		JPanel mainpanel = new JPanel();
		mainpanel.setBackground(Color.gray);
		mainpanel.setLayout( new BorderLayout());
		
		
		// C1 - 1 Video Panel
		JPanel videopanel = new JPanel();
		videopanel.setLayout(new BorderLayout());
		videopanel.add(webplayer , BorderLayout.CENTER);
			
		
		
		// C3 - 2 - Buttons & Timeline Slider 
		TCPButton playbutton = new TCPButton("Play");

		playbutton.addActionListener ((ActionEvent e) -> {
			System.out.println("playing");
			webplayer.playVideo();
		});

		
		TCPButton pausebutton = new TCPButton("Pause");

		pausebutton.addActionListener ((ActionEvent e) -> {	
			System.out.println("paused");
			webplayer.pauseVideo();
		});
			
		
		TCPSlider videoslider = new TCPSlider();
			videoslider.addChangeListener(e -> {
    			if (!videoslider.getValueIsAdjusting()) {
        			double percentage = videoslider.getValue() / 100.0;
        			webplayer.setTimeline(percentage);
    			}
			});
		
		
		TCPButton next = new TCPButton("Next");
			
		next.addActionListener((ActionEvent e) -> {
			// playnext logic
			System.out.println("Clicked Next");
		});
		
		TCPButton back = new TCPButton("Back");
		
		back.addActionListener((ActionEvent e)-> {
			// playlast logic 
			System.out.println("Clicked Back");
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

		audioslider.addChangeListener(new ChangeListener(){
			@Override
		    public void stateChanged(ChangeEvent e) {
		    	int audiovalue = audioslider.getValue();
		    	System.out.println(audiovalue);
				
				webplayer.setVolume(audiovalue);
		    }
		});


		// C2  - Terminal and Grid
		JPanel terminal = new JPanel();
		terminal.setPreferredSize(new Dimension( 100 , 50));
		terminal.setBackground(Color.BLACK);

		JPanel grid = new JPanel();
		grid.setPreferredSize(new Dimension( 100 , 50));
		grid.setBackground(Color.PINK);
		
		
		
		// sidebar settings
		JPanel sidebar = new JPanel();
		sidebar.setBackground(Color.green);
		sidebar.setLayout(new BoxLayout(sidebar , BoxLayout.Y_AXIS));
		sidebar.add(terminal);
		sidebar.add(audioslider);
		sidebar.add(grid);


		//sidebar components 
		
				
		
		// bottom bar components like information and stuff
		
		mainpanel.add(videopanel , BorderLayout.CENTER);
		mainpanel.add(sidebar , BorderLayout.EAST);
		
		
		

		//mainframe settings
		mainframe.setLayout(new BorderLayout());
		mainframe.add(mainpanel , BorderLayout.CENTER);
		mainframe.add(utilitybar , BorderLayout.SOUTH);
		
		
		
		
		mainframe.setVisible(true);
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.addWindowListener(new java.awt.event.WindowAdapter() {
		});
		

		
		Platform.runLater(() -> {
   	
    		webplayer.loadURL(pageServer.getURL());
			
		});


		new javax.swing.Timer(250, e -> {
    		webplayer.getDuration(duration -> {
        		webplayer.getCurrentTime(currentTime -> {
            		if (duration > 0 && !videoslider.getValueIsAdjusting()) {
                		int percentage = (int) ((currentTime / duration) * 100);

              			SwingUtilities.invokeLater(() ->
                    	videoslider.setValue(percentage)
                		);
           			}
        		});
   		 	});
		}).start();



		});
	}
	
}
