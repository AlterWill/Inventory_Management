package com.app;
import javax.swing.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.*;


public class UI extends JFrame {
		UI() {
			 try {
		     UIManager.setLookAndFeel(new FlatDarkLaf());
			 } catch (Exception e) {
			     e.printStackTrace();	
			 }
			   setLayout(new BorderLayout());

			  setTitle("Inventory Management System");
			   setSize(400, 300);
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				setVisible(true);

				JPanel nav=new JPanel();
				nav.setPreferredSize(new Dimension(100,40));
				nav.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().setBackground(new Color(34,34,34));	
				JButton button = new JButton("login");
				JButton butt1= new JButton("signup");
				nav.add(button);
				nav.add(butt1);
				
				add(nav,BorderLayout.NORTH);
		}
}


