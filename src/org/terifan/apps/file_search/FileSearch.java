package org.terifan.apps.file_search;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
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
	private JTextField[][] mSearchFields;
	private DefaultListModel<File> mResultListModel;
	private JList<File> mResultList;
	private JButton mSearchButton;
	private JTextArea mOutputField;
	private StatusBar mStatusBar;
	private StatusBarField mStatusCount;
	private StatusBarField mStatusResultCount;
	private StatusBarField mStatusFile;


	public FileSearch()
	{
		Utilities.setSystemLookAndFeel();

		mPath = new JTextField();
		mSearchFields = new JTextField[5][3];
		mResultListModel = new DefaultListModel<>();
		mOutputField = new JTextArea();

		mStatusCount = new StatusBarField(" ");
		mStatusResultCount = new StatusBarField(" ");
		mStatusFile = new StatusBarField(" ").setResize(StatusBarField.Resize.SPRING);
		mStatusBar = new StatusBar();
		mStatusBar.add(mStatusCount.setBorderStyle(StatusBarField.LOWERED));
		mStatusBar.add(mStatusResultCount.setBorderStyle(StatusBarField.LOWERED));
		mStatusBar.add(mStatusFile.setBorderStyle(StatusBarField.LOWERED));

		mResultList = new JList<>(mResultListModel);
		mResultList.addListSelectionListener(mListSelectionListener);

		DragAndDrop.register(mResultList, pt->FILE_FLAVOR, pt->mResultList.getSelectedValuesList(), null);

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
		c.insets = new Insets(0, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		inputPanel.add(new JLabel("Path"), c);
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		inputPanel.add(mPath, c);
		c.gridx = 1;
		c.weightx = 0;
		inputPanel.add(mSearchButton, c);
		c.gridx = 0;
		c.gridy = 2;
		inputPanel.add(new JLabel("Search"), c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
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
		frame.setSize(1024, 768);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private ListSelectionListener mListSelectionListener = aEvent ->
	{
		if (!aEvent.getValueIsAdjusting() && mResultList.getSelectedIndex() != -1)
		{
			SwingUtilities.invokeLater(()->
			{
				try
				{
					String s = new String(Streams.readAll(mResultListModel.get(mResultList.getSelectedIndex())));

					mOutputField.setLineWrap(!s.contains("\n"));
					mOutputField.setText(s);
					mOutputField.setCaretPosition(0);
					mOutputField.invalidate();
					mOutputField.validate();
					mOutputField.repaint();
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

						searchDir(new File(mPath.getText()));

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
					mFileSize += buffer.length;

					mStatusCount.setText(mFileCount + " files searched");
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
						SwingUtilities.invokeLater(()->
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
