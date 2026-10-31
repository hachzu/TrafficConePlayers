package ui;

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

public class Tcplayer {
	
	// the real challenge would be implementing the other features like Video manipulation 
	// file management , and yada yada yada alot of these are going to change if Tcplayer becomes a object

	public static void main(String[] args) {
		LocalServer pageServer;
		try {
			pageServer = new LocalServer(8080);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		WebPlayer webplayer = new WebPlayer(pageServer.getURL());
	
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
			
			
			videopanel.add(webplayer , BorderLayout.CENTER);
			
		
		// honestly we could either go JButton or JPanel with event listeners
		
		JButton playbutton = new JButton();
		playbutton.setText("play");

		playbutton.addActionListener ((ActionEvent e) -> {
			System.out.println("playing");
			webplayer.playVideo();
		});

		
		JButton pausebutton = new JButton();
		pausebutton.setText("pause");

		pausebutton.addActionListener ((ActionEvent e) -> {	
			System.out.println("paused");
			webplayer.pauseVideo();
		});
			
		
		
		JSlider videoslider = new JSlider();
		
		videoslider.addChangeListener( new ChangeListener(){
		    @Override
		    public void stateChanged(ChangeEvent e) {
		    	int audiovalue = videoslider.getValue();
		    	System.out.println(audiovalue);

				webplayer.setVolume(audiovalue);
		    }
		});
		
		JButton next = new JButton();
			
		next.addActionListener((ActionEvent e) -> {
			// playnext logic
			System.out.println("Clicked Next");
		});
		
		next.setText("next");
		
		JButton back = new JButton();
		
		back.addActionListener((ActionEvent e)-> {
			// playlast logic 
			System.out.println("Clicked Back");
		});
		
		back.setText("back");
		
		
		JPanel utilitybar = new JPanel();
		utilitybar.setPreferredSize(new Dimension( 100 , 50));
		utilitybar.setLayout( new BoxLayout(utilitybar , BoxLayout.Y_AXIS));
		

		JPanel buttonbar = new JPanel();
		buttonbar.setLayout( new BoxLayout(buttonbar , BoxLayout.X_AXIS));
		buttonbar.add(back);
		buttonbar.add(playbutton);
		buttonbar.add(pausebutton);
		buttonbar.add(next);
		
		
		utilitybar.add(videoslider);
		utilitybar.add(buttonbar);
		
		// sidebar settings
		JPanel sidebar = new JPanel();
		sidebar.setBackground(Color.green);
		sidebar.setPreferredSize(new Dimension ( 300 , 400));
		
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

		});

	}

	
	
	

}
