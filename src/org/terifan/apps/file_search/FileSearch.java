package org.terifan.apps.file_search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.DefaultHighlighter;
import org.terifan.io.Streams;
import org.terifan.ui.DragAndDrop;
import static org.terifan.ui.DragAndDrop.FILE_FLAVOR;
import org.terifan.ui.Utilities;
import org.terifan.ui.statusbar.StatusBar;
import org.terifan.ui.statusbar.StatusBarField;
import org.terifan.util.ErrorReportWindow;
import org.terifan.util.log.Log;


public class FileSearch
{
	private JTextField mPath;
	private JTextField mFilter;
	private JTextField[][] mSearchFields;
	private DefaultListModel<File> mResultListModel;
	private JList<File> mResultList;
	private JButton mSearchButton;
	private JEditorPane mOutputField;
	private StatusBar mStatusBar;
	private StatusBarField mStatusCounter;
	private StatusBarField mStatusResultCount;
	private StatusBarField mStatusFile;


	public FileSearch()
	{
		Utilities.setSystemLookAndFeel();

		mPath = new JTextField();
		mFilter = new JTextField();
		mSearchFields = new JTextField[5][3];
		mResultListModel = new DefaultListModel<>();
		mOutputField = new JEditorPane();

		mStatusCounter = new StatusBarField(" ");
		mStatusResultCount = new StatusBarField(" ");
		mStatusFile = new StatusBarField(" ").setResize(StatusBarField.Resize.SPRING);
		mStatusBar = new StatusBar();
		mStatusBar.add(mStatusCounter.setBorderStyle(StatusBarField.LOWERED));
		mStatusBar.add(mStatusResultCount.setBorderStyle(StatusBarField.LOWERED));
		mStatusBar.add(mStatusFile.setBorderStyle(StatusBarField.LOWERED));

		mResultList = new JList<>(mResultListModel);
		mResultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mResultList.addListSelectionListener(mListSelectionListener);

		DragAndDrop.register(mResultList, pt -> FILE_FLAVOR, pt ->
		{
			System.out.println(mResultList.getSelectedValuesList().size());
			return mResultList.getSelectedValuesList().toArray(new File[mResultList.getSelectedValuesList().size()]);
		}, null);

		mSearchButton = new JButton(mSearchAction);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 2, 2, 2);

		JPanel searchPanel = new JPanel(new GridBagLayout());
		for (int i = 0; i < mSearchFields.length; i++)
		{
			for (int j = 0, x = 0; j < mSearchFields[i].length; j++)
			{
				mSearchFields[i][j] = new JTextField();

				c.gridx = x++;
				c.gridy = i;
				c.weightx = 0;
				c.anchor = GridBagConstraints.EAST;
				searchPanel.add(new JLabel(j == 0 ? i == 0 ? "" : "or" : "and"), c);
				c.gridx = x++;
				c.gridy = i;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1;
				searchPanel.add(mSearchFields[i][j], c);
			}
		}

		JPanel inputPanel = new JPanel(new GridBagLayout());
		inputPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));

		c.insets = new Insets(0, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		inputPanel.add(new JLabel("Path"), c);
		c.gridx = 1;
		c.weightx = 1;
		inputPanel.add(mPath, c);
		c.gridx = 2;
		c.weightx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 2;
		inputPanel.add(mSearchButton, c);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0;
		c.gridheight = 1;
		inputPanel.add(new JLabel("Filter"), c);
		c.gridx = 1;
		c.weightx = 1;
		inputPanel.add(mFilter, c);

		c.gridx = 1;
		c.gridy = 2;
		inputPanel.add(new JLabel("Use asterix for unknown characters, e.g. *.xml"), c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		inputPanel.add(new JLabel("Search"), c);
		c.gridx = 0;
		c.gridy = 4;
		c.insets = new Insets(0, 0, 4, 0);

		inputPanel.add(searchPanel, c);

		JPanel progressPanel = new JPanel(new BorderLayout());
		progressPanel.add(mStatusBar, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(mResultList), new JScrollPane(mOutputField));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(inputPanel, BorderLayout.NORTH);
		mainPanel.add(splitPane, BorderLayout.CENTER);
		mainPanel.add(progressPanel, BorderLayout.SOUTH);

		JFrame frame = new JFrame("FileSearch");
		frame.add(mainPanel);
		frame.setSize(1400, 900);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private ListSelectionListener mListSelectionListener = aEvent ->
	{
		if (!aEvent.getValueIsAdjusting() && mResultList.getSelectedIndex() != -1)
		{
			SwingUtilities.invokeLater(() ->
			{
				try
				{
					String s = "<html><body style='font-family:courier new;text-size:10px;'>" + new String(Streams.readAll(mResultListModel.get(mResultList.getSelectedIndex()))).replace("\n", "<br/>") + "</body></html>";

					BasicTextUI.BasicHighlighter highlighter = new BasicTextUI.BasicHighlighter();

					mOutputField.setContentType("text/html");
					mOutputField.setText(s);
					mOutputField.setCaretPosition(0);
					mOutputField.setHighlighter(highlighter);
					mOutputField.invalidate();
					mOutputField.validate();
					mOutputField.repaint();

					for (JTextField[] textFields : mSearchFields)
					{
						for (JTextField textField : textFields)
						{
							String t = textField.getText();

							if (!t.isEmpty())
							{
//								for (int offset = 0; offset != -1; )
//								{
//									try
//									{
//										offset = s.indexOf(t, offset);
//										System.out.println(offset);
//										if (offset == -1)
//										{
//											break;
//										}
//
//										highlighter.addHighlight(offset, offset + t.length(), new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
//									}
//									catch (Exception e)
//									{
//										e.printStackTrace(System.out);
//									}
//
//									offset += t.length();
//								}

								s = s.replace(t, "<span style='background-color:#ffff88;color:#444400;'>" + t + "</span>");
							}
						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace(Log.out);
				}
			});
		}
	};

	private AbstractAction mSearchAction = new AbstractAction("Search files")
	{
		@Override
		public void actionPerformed(ActionEvent aE)
		{
			new Thread()
			{
				int mFileCount;
				int mFileSize;


				@Override
				public void run()
				{
					try
					{
						mSearchButton.setEnabled(false);

						mResultList.clearSelection();
						mResultListModel.clear();
						mOutputField.setText("");
						mStatusCounter.setText("");
						mStatusFile.setText("");
						mStatusResultCount.setText("No results");

						searchDir(new File(mPath.getText()), mFilter.getText());

						mStatusFile.setText("Done");
						mStatusBar.repaint();

						mResultList.invalidate();
						mResultList.revalidate();
					}
					catch (Exception e)
					{
						ErrorReportWindow.show(e);
					}
					finally
					{
						mSearchButton.setEnabled(true);
					}
				}


				private void searchDir(File aDirectory, String aFilter) throws IOException
				{
					System.out.println("Visting directory: " + aDirectory);

					long start = System.currentTimeMillis();

					File[] listFiles = aDirectory.listFiles();

					System.out.println(listFiles.length + " loaded in " + (System.currentTimeMillis() - start));

					for (File file : listFiles)
					{
						if (file.isFile())
						{
							String name = file.getName().toLowerCase();

							if (aFilter.isEmpty() || name.matches(aFilter.replace(".", "\\.").replace("*", ".*")))
							{
								searchFile(file);
							}
						}
						else
						{
							searchDir(file, aFilter);
						}
					}
				}


				private void searchFile(File aFile) throws IOException
				{
					byte[] buffer = Streams.readAll(aFile);
					String src = new String(buffer).toLowerCase();
					boolean rowFound = false;

					mFileCount++;
					mFileSize += buffer.length;

					mStatusCounter.setText(mFileCount + " files searched");
					mStatusFile.setText("Reading " + aFile.getAbsolutePath());
					mStatusBar.repaint();

					for (JTextField[] tfs : mSearchFields)
					{
						boolean elementFound = true;
						boolean hasValue = false;
						for (JTextField tf : tfs)
						{
							if (!tf.getText().trim().isEmpty())
							{
								elementFound &= src.contains(tf.getText().trim().toLowerCase());
								hasValue = true;
							}
						}
						if (hasValue)
						{
							rowFound |= elementFound;
						}
					}
					if (rowFound)
					{
						SwingUtilities.invokeLater(() ->
						{
							try
							{
								mResultListModel.addElement(aFile);
								mStatusResultCount.setText(mResultListModel.size() + " files found");
								mStatusBar.repaint();
							}
							catch (Exception e)
							{
							}
						});
					}
				}
			}.start();
		}
	};


	public static void main(String... args)
	{
		try
		{
			new FileSearch();
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
