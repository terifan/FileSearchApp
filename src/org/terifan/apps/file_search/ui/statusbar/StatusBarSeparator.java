package org.terifan.apps.file_search.ui.statusbar;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;


public class StatusBarSeparator extends JComponent
{
	public StatusBarSeparator()
	{
	}


	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		boolean isLowered = true;

		aGraphics.setColor(isLowered ? new Color(64, 64, 64) : new Color(255, 255, 255));
		aGraphics.drawLine(0, 0, 0, 0 + getHeight());
		aGraphics.setColor(!isLowered ? new Color(64, 64, 64) : new Color(255, 255, 255));
		aGraphics.drawLine(0 + 1, 0, 0 + 1, 0 + getHeight());
	}
}
