package org.terifan.apps.file_search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicTextUI.BasicHighlighter;
import javax.swing.text.DefaultHighlighter;
import org.terifan.io.Streams;


public class PlainTextPane extends JPanel
{
	private JEditorPane mEditor;


	public PlainTextPane(File aFile, List<String> aHighlight) throws IOException
	{
		super(new BorderLayout());

		mEditor = new JEditorPane();

		super.add(new JScrollPane(mEditor), BorderLayout.CENTER);

		String text = new String(Streams.readAll(aFile), "utf-8");

		text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

		String s = "<html><body style='font-family:courier new;text-size:10px;'><pre>" + text + "</pre></body></html>";

		BasicHighlighter highlighter = new BasicHighlighter();

		mEditor.setContentType("text/html");
		mEditor.setText(s);
		mEditor.setCaretPosition(0);
		mEditor.setHighlighter(highlighter);
		mEditor.invalidate();
		mEditor.validate();
		mEditor.repaint();

		if (aHighlight != null)
		{
			for (String t : aHighlight)
			{
				t = t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

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


	public void close()
	{
		mEditor.setText("");
	}
}
