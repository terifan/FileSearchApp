package org.terifan.apps.file_search.ui.statusbar;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import org.terifan.ui.NinePatchImage;


public class StatusBarField extends JLabel
{
	private static final long serialVersionUID = 1L;

	public final static Border LOWERED = BorderFactory.createLoweredBevelBorder();
	public final static Border RAISED = BorderFactory.createRaisedBevelBorder();
	public final static Border NONE = null;

	public enum Resize
	{
		//STATIC,
		CONTENT,
		SPRING
	}

	private Integer mFixedSize;
	private Resize mResize;
	private NinePatchImage mBackgroundImage;


	public StatusBarField(String aText)
	{
		this(aText, SwingConstants.LEFT, Resize.CONTENT);
	}


	public StatusBarField(String aText, int aHorizontalAlignment, Resize aResize)
	{
		this(aText, aHorizontalAlignment, aResize, null);
	}


	public StatusBarField(String aText, int aHorizontalAlignment, int aFixedSize)
	{
		this(aText, aHorizontalAlignment, Resize.CONTENT, aFixedSize);
	}


	public StatusBarField(String aText, int aHorizontalAlignment, Resize aResize, Integer aFixedSize)
	{
		super(aText, aHorizontalAlignment);

		mResize = aResize;
		mFixedSize = aFixedSize;

		super.setOpaque(true);
		super.setBorder(LOWERED);
	}


	public Integer getFixedSize()
	{
		return mFixedSize;
	}


	public StatusBarField setFixedSize(Integer aFixedSize)
	{
		mFixedSize = aFixedSize;
		return this;
	}


	public Resize getResize()
	{
		return mResize;
	}


	public StatusBarField setResize(Resize aFixedSize)
	{
		mResize = aFixedSize;
		return this;
	}


	/**
	 * Same as setBorder(Border)
	 */
	public StatusBarField setBorderStyle(Border aBorder)
	{
		super.setBorder(aBorder);
		return this;
	}


	public NinePatchImage getBackgroundImage()
	{
		return mBackgroundImage;
	}


	public StatusBarField setBackgroundImage(NinePatchImage aBackgroundImage)
	{
		mBackgroundImage = aBackgroundImage;
		super.setOpaque(mBackgroundImage != null);
		return this;
	}


	/**
	 * Same as setForeground(Color)
	 */
	public StatusBarField setTextColor(Color aColor)
	{
		super.setForeground(aColor);
		return this;
	}


	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		if (mBackgroundImage != null)
		{
			mBackgroundImage.paintImage(aGraphics, 0, 0, getWidth(), getHeight());
		}

		super.paintComponent(aGraphics);
	}
}
