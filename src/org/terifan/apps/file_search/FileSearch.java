package org.terifan.apps.file_search;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.terifan.io.Streams;
import org.terifan.ui.Utilities;
import org.terifan.util.ErrorReportWindow;
import org.terifan.util.log.Log;


public class FileSearch
{
	public static void main(String ... args)
	{
		try
		{
			Utilities.setSystemLookAndFeel();

			JTextField path = new JTextField();
			JTextField[][] search = new JTextField[3][3];
			DefaultListModel<File> resultListModel = new DefaultListModel<>();
			JList<File> resultList = new JList<>(resultListModel);
			JButton button = new JButton();
			JTextArea fileOutput = new JTextArea();

			for (int i = 0; i < search.length; i++)
			{
				for (int j = 0; j < search[i].length; j++)
				{
					search[i][j] = new JTextField();
				}
			}

			resultList.addListSelectionListener(aEvent ->
			{
				if (!aEvent.getValueIsAdjusting())
				{
					try
					{
						fileOutput.setText(new String(Streams.readAll(resultListModel.get(aEvent.getFirstIndex()))));
						fileOutput.setCaretPosition(0);
					}
					catch (IOException e)
					{
						e.printStackTrace(Log.out);
					}
				}
			});

			AbstractAction action = new AbstractAction("Search")
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
								button.setEnabled(false);

								resultListModel.clear();
								fileOutput.setText("");

								searchDir(new File(path.getText()));

								fileOutput.setText("Finished searching " + mFileCount + " files (" + mFileSize/1024/1024 + " MiB)");
								resultList.invalidate();
								resultList.revalidate();
							}
							catch (Exception e)
							{
								ErrorReportWindow.show(e);
							}
							finally
							{
								button.setEnabled(true);
							}
						}

						private void searchDir(File aDirectory) throws IOException
						{
							for (File file : aDirectory.listFiles())
							{
								if (file.isFile())
								{
									searchFile(file);
								}
								else
								{
									searchDir(file);
								}
							}
						}


						private void searchFile(File aFile) throws IOException
						{
							byte[] buffer = Streams.readAll(aFile);
							String src = new String(buffer).toLowerCase();
							boolean rowFound = false;

							mFileCount++;
							mFileSize+=buffer.length;

							for (JTextField[] tfs : search)
							{
								boolean elementFound = true;
								boolean hasValue = false;
								for (JTextField tf : tfs)
								{
									if (!tf.getText().isEmpty())
									{
										elementFound &= src.contains(tf.getText().toLowerCase());
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
								resultListModel.addElement(aFile);
							}
						}
					}.start();
				}
			};

			button.setAction(action);

			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(0, 2, 2, 2);

			JPanel searchPanel = new JPanel(new GridBagLayout());
			for (int i = 0; i < search.length; i++)
			{
				for (int j = 0, x = 0; j < search[i].length; j++)
				{
					c.gridx = x++;
					c.gridy = i;
					c.weightx = 0;
					c.anchor = GridBagConstraints.EAST;
					searchPanel.add(new JLabel(j == 0 ? i == 0 ? "" : "or" : "and"), c);
					c.gridx = x++;
					c.gridy = i;
					c.fill = GridBagConstraints.HORIZONTAL;
					c.weightx = 1;
					searchPanel.add(search[i][j], c);
				}
			}

			JPanel inputPanel = new JPanel(new GridBagLayout());
			c.insets = new Insets(0, 2, 2, 2);
			c.gridx = 0;
			c.gridy = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			inputPanel.add(new JLabel("Path"), c);
			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 1;
			inputPanel.add(path, c);
			c.gridx = 1;
			c.weightx = 0;
			inputPanel.add(button, c);
			c.gridx = 0;
			c.gridy = 2;
			inputPanel.add(new JLabel("Search"), c);
			c.gridx = 0;
			c.gridy = 3;
			c.gridwidth = 2;
			c.insets = new Insets(0, 0, 4, 0);
			inputPanel.add(searchPanel, c);

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(resultList), new JScrollPane(fileOutput));

			JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.add(inputPanel, BorderLayout.NORTH);
			mainPanel.add(splitPane, BorderLayout.CENTER);

			JFrame frame = new JFrame("FileSearch");
			frame.add(mainPanel);
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
