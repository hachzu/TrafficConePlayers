package ui.TCPScripts;

import javax.swing.*;

public class TCPButton extends JButton {
    
    public TCPButton(String text) {
        super(text);
        setPreferredSize(new java.awt.Dimension(100, 50));
        setMaximumSize(new java.awt.Dimension(100, 50));
        setBackground(new java.awt.Color(255, 102, 0));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 3, true));
    }


}
