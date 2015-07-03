package myjava.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Component;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class MyFileChooser extends JFileChooser
{
	private static final Font FONT = new Font("Microsoft Jhenghei", Font.PLAIN, 13); 
	public MyFileChooser(String str)
	{
		this.setPreferredSize(new Dimension(500,420));
		this.setDialogTitle(str);
		this.setFileHidingEnabled(false);
		this.setDialogType(JFileChooser.OPEN_DIALOG);
		this.setApproveButtonText("Open");
		setStylePrivate((Container)this, Color.WHITE, Color.BLACK);
	}
	
	public void setStyle(Color bg, Color fg)
	{
		setStylePrivate(this, bg, fg);
	}
	
	private void setStylePrivate(Container c, Color bg, Color fg)
    {
		Component[] comp = c.getComponents();
        for (int i=0; i<comp.length; i++)
        {
            if (comp[i] instanceof Container)
            {
                setStylePrivate((Container)comp[i], bg, fg);
            }
            if (comp[i] instanceof JButton)
            {
                comp[i].setBackground(bg);
            }
            else if (comp[i] instanceof JList)
            {
                comp[i].setBackground(bg);
            }
            else if (comp[i] instanceof JComboBox)
            {
                comp[i].setBackground(bg);
            }
            else if (comp[i] instanceof JTextField)
            {
                comp[i].setBackground(bg);
            }
            else if (comp[i] instanceof JToggleButton)
            {
                comp[i].setBackground(bg);
            }
            else if (comp[i] instanceof JLabel)
            {
                comp[i].setBackground(bg);
            }	 
            comp[i].setForeground(fg);
            comp[i].setFont(FONT);
        }
        
        UIManager.put("OptionPane.buttonFont", FONT);
        UIManager.put("OptionPane.messageFont", FONT);
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("Button.background", bg);
 
        UIManager.put("PopupMenu.background", bg);
        UIManager.put("MenuItem.background", bg);
        UIManager.put("RadioButtonMenuItem.background", bg);
 
        UIManager.put("PopupMenu.font", FONT);
        UIManager.put("MenuItem.font", FONT);
        UIManager.put("Menu.font", FONT);
        UIManager.put("RadioButtonMenuItem.font", FONT);
        UIManager.put("TextField.font", FONT);
 
        UIManager.put("ToolTip.font", FONT);
        UIManager.put("ToolTip.border", new LineBorder(fg, 1));
        UIManager.put("ToolTip.background", bg);
    }
}
