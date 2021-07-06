package org.terifan.apps.file_search;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicTextUI.BasicHighlighter;
import javax.swing.text.DefaultHighlighter;
import org.terifan.io.Streams;


public class FileViewer extends JEditorPane
{
	public FileViewer()
	{
	}


	public void update(File aFile, JTextField[][] aSearchFields) throws IOException
	{
		String text = new String(Streams.readAll(aFile), "utf-8");

		text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

		String s = "<html><body style='font-family:courier new;text-size:10px;'><pre>" + text + "</pre></body></html>";

		BasicHighlighter highlighter = new BasicHighlighter();

		super.setContentType("text/html");
		super.setText(s);
		super.setCaretPosition(0);
		super.setHighlighter(highlighter);
		super.invalidate();
		super.validate();
		super.repaint();

		if (aSearchFields != null)
		{
			for (JTextField[] textFields : aSearchFields)
			{
				for (JTextField textField : textFields)
				{
					String t = textField.getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

					if (!t.isEmpty())
					{
						for (int offset = 0; offset != -1; )
						{
							try
							{
								offset = s.indexOf(t, offset);
								System.out.println(offset);
								if (offset == -1)
								{
									break;
								}

								highlighter.addHighlight(offset, offset + t.length(), new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
							}
							catch (Exception e)
							{
								e.printStackTrace(System.out);
							}

							offset += t.length();
						}

						s = s.replace(t, "<span style='background-color:#ffff88;color:#444400;'>" + t + "</span>");
					}
				}
			}
		}
	}


	void clear()
	{
		super.setText("");
	}


	public static void main(String ... args)
	{
		try
		{
			FileViewer fileViewer = new FileViewer();
			fileViewer.update(new File("d:\\ID-ScanbioWinProd-59387-1601514160301-0-2023.xml"), null);

			JFrame frame = new JFrame();
			frame.add(fileViewer);
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
