package com.tcp.ui;

import com.tcp.web.LocalPageServer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
			YouTubeWebView jfxpanel = new YouTubeWebView(pageServer.getUrl());
			videopanel.add(jfxpanel , BorderLayout.CENTER);

			// honestly we could either go JButton or JPanel with event listeners
			JButton playbutton = new JButton();
			playbutton.addActionListener ((ActionEvent e) -> {
				// code to manipulate timeline / pause or play videos
				// code to change icon
				// etc..
				if (playbutton.getText().equals("pause")){
					playbutton.setText("play");
					System.out.println("playing");
				}else {
					playbutton.setText("pause");
					System.out.println("paused");
				}
			});
			playbutton.setText("play");

			JSlider videoslider = new JSlider();
			videoslider.addChangeListener( new ChangeListener(){
				@Override
				public void stateChanged(ChangeEvent e) {
					// Volume Changing Logic
					int audiovalue = videoslider.getValue();
					System.out.println(audiovalue);
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
			buttonbar.add(playbutton);
			buttonbar.add(back);
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
