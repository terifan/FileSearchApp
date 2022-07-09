package org.terifan.apps.file_search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
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

		String body = "<html><body style='font-family:courier new;text-size:10px;'><pre>" + text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</pre></body></html>";

		mEditor.setContentType("text/html");
		mEditor.setText(body);

		try
		{
			body = mEditor.getDocument().getText(0, mEditor.getDocument().getLength()).toLowerCase();
		}
		catch (BadLocationException e)
		{
		}

		Highlighter highlighter = mEditor.getHighlighter();
		DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

		if (aHighlight != null)
		{
			for (String pattern : aHighlight)
			{
				pattern = pattern.toLowerCase();

				try
				{
					for (int pos = 0; (pos = body.indexOf(pattern, pos)) > -1;)
					{
						System.out.println("highlight: " + pos);

						highlighter.addHighlight(pos, pos + pattern.length(), highlightPainter);
						pos += pattern.length();
					}
				}
				catch (BadLocationException e)
				{
				}
			}
		}

		mEditor.setCaretPosition(0);
		mEditor.invalidate();
		mEditor.validate();
		mEditor.repaint();
	}


	public void close()
	{
		mEditor.setText("");
	}
}
