package myjava.gui;

/**
 * Requires myjava.gui.common.Resources to work
 */

import java.awt.*;
import javax.swing.*;
import myjava.gui.common.Resources;

public class MyCheckBox extends JCheckBox
{
	public MyCheckBox(String str, boolean isSelected)
	{
		super(str, isSelected);
		this.setFont(Resources.f13);
		this.setFocusPainted(false);
		this.setOpaque(false);
	}
}
