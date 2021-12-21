package org.terifan.apps.file_search.ui.statusbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import org.terifan.ui.NinePatchImage;


public class StatusBar extends JComponent implements LayoutManager, Iterable<StatusBarField>
{
	private static final long serialVersionUID = 1L;

	private int mSpacing;
	private Insets mMargin;
	private NinePatchImage mBackgroundImage;


	public StatusBar()
	{
		setLayout(this);

		mSpacing = 4;
		mMargin = new Insets(2, 0, 2, 0);
	}


	public NinePatchImage getBackgroundImage()
	{
		return mBackgroundImage;
	}


	public StatusBar setBackgroundImage(NinePatchImage aBackgroundImage)
	{
		mBackgroundImage = aBackgroundImage;
		return this;
	}


	public Insets getMargin()
	{
		return mMargin;
	}


	public StatusBar setMargin(Insets aMargin)
	{
		mMargin = aMargin;
		return this;
	}


	public int getSpacing()
	{
		return mSpacing;
	}


	public StatusBar setSpacing(int aSpacing)
	{
		mSpacing = aSpacing;
		return this;
	}


	@Override
	protected void paintComponent(Graphics g)
	{
		if (mBackgroundImage != null)
		{
			mBackgroundImage.paintImage(g, 0, 0, getWidth(), getHeight());
		}
		else
		{
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}


	@Override
	public void addLayoutComponent(String name, Component comp)
	{
	}


	@Override
	public void removeLayoutComponent(Component comp)
	{
	}


	@Override
	public Dimension minimumLayoutSize(Container aParent)
	{
		int w = 0;
		int h = 0;
		int n = aParent.getComponentCount();

		for (int i = 0; i < n; i++)
		{
			Component comp = aParent.getComponent(i);
			Dimension d = comp.getMinimumSize();
			if (comp instanceof StatusBarField)
			{
				StatusBarField field = (StatusBarField)comp;
				if (field.getFixedSize() != null)
				{
					w += field.getFixedSize();
				}
				else
				{
					w += d.width;
				}
			}
			else
			{
				w += d.width;
			}
			h = Math.max(h, d.height);
		}

		return new Dimension(w + mSpacing * (n - 1) + mMargin.left + mMargin.right, h + mMargin.top + mMargin.bottom);
	}


	@Override
	public Dimension preferredLayoutSize(Container aParent)
	{
		int w = 0;
		int h = 0;
		int n = aParent.getComponentCount();

		for (int i = 0; i < n; i++)
		{
			Component comp = aParent.getComponent(i);
			Dimension d = comp.getPreferredSize();
			if (comp instanceof StatusBarField)
			{
				StatusBarField field = (StatusBarField)comp;
				if (field.getFixedSize() != null)
				{
					w += field.getFixedSize();
				}
				else
				{
					w += d.width;
				}
			}
			else
			{
				w += d.width;
			}
			h = Math.max(h, d.height);
		}

		return new Dimension(w + mSpacing * (n - 1) + mMargin.left + mMargin.right, h + mMargin.top + mMargin.bottom);
	}


	@Override
	public void layoutContainer(Container aParent)
	{
		int x = mMargin.left;
		int y = mMargin.top;
		int n = aParent.getComponentCount();
		int h = aParent.getHeight() - mMargin.top - mMargin.bottom;

		int parentWidth = aParent.getWidth();
		int width = preferredLayoutSize(aParent).width;

		for (int i = 0; i < n; i++)
		{
			Component comp = aParent.getComponent(i);
			int cw = comp.getPreferredSize().width;
			int w = cw;

			if (comp instanceof StatusBarField)
			{
				StatusBarField field = (StatusBarField)comp;
				if (field.getFixedSize() != null)
				{
					w = Math.max(cw, field.getFixedSize());
				}
				else if (field.getResize() == StatusBarField.Resize.SPRING)
				{
					w = Math.max(cw, parentWidth - width + w);
				}
				if (width > parentWidth)
				{
					int corr = w - Math.max(cw, w - (width - parentWidth));
					w -= corr;
					width -= corr;
				}
			}

			comp.setBounds(x, y, w, h);

			x += w + mSpacing;
		}
	}


	public StatusBarField getField(int aIndex)
	{
		return (StatusBarField)getComponent(aIndex);
	}


	@Override
	public Iterator<StatusBarField> iterator()
	{
		ArrayList<StatusBarField> fields = new ArrayList<>();
		for (int i = 0; i < getComponentCount(); i++)
		{
			Component comp = getComponent(i);
			if (comp instanceof StatusBarField)
			{
				fields.add((StatusBarField)comp);
			}
		}
		return fields.iterator();
	}


	public static void main(String... args)
	{
		try
		{
			StatusBar sb = new StatusBar();
			sb.setBackground(new Color(255,0,0));
			sb.add(new StatusBarField("test1", SwingConstants.LEFT, 100));
			sb.add(new StatusBarField("test2", SwingConstants.CENTER, 100));
			sb.add(new StatusBarField("test3", SwingConstants.RIGHT, 100));
			sb.add(new StatusBarField("test7", SwingConstants.RIGHT, StatusBarField.Resize.SPRING).setBorderStyle(StatusBarField.NONE));
			sb.add(new StatusBarSeparator());
			sb.add(new StatusBarField("test4").setBorderStyle(StatusBarField.NONE));
			sb.add(new StatusBarSeparator());
			sb.add(new StatusBarField("test5").setBorderStyle(StatusBarField.NONE));
			sb.add(new StatusBarSeparator());
			sb.add(new StatusBarField("test6").setBorderStyle(StatusBarField.NONE));

			JFrame frame = new JFrame();
			frame.add(sb, BorderLayout.SOUTH);
			frame.setSize(1024, 768);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
