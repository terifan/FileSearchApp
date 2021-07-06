package org.terifan.apps.file_search;

import java.awt.BorderLayout;
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
import org.terifan.ui.DragAndDrop;
import static org.terifan.ui.DragAndDrop.FILE_FLAVOR;
import org.terifan.ui.Utilities;
import org.terifan.ui.statusbar.StatusBar;
import org.terifan.ui.statusbar.StatusBarField;
import org.terifan.util.AsyncTask;
import org.terifan.util.log.Log;


public class FileSearch
{
	private JTextField mPath;
	private JTextField mFilter;
	private JTextField[][] mSearchFields;
	private DefaultListModel<File> mResultListModel;
	private JList<File> mResultList;
	private JButton mSearchButton;
	private JButton mCancelButton;
	private StatusBar mStatusBar;
	private StatusBarField mStatusCounter;
	private StatusBarField mStatusResultCount;
	private StatusBarField mStatusFile;
	private FileViewer mFileViewer;
	private AsyncTask mSearchWorker;


	public FileSearch()
	{
		Utilities.setSystemLookAndFeel();

		mPath = new JTextField();
		mFilter = new JTextField();
		mSearchFields = new JTextField[5][3];
		mResultListModel = new DefaultListModel<>();

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

		mFileViewer = new FileViewer();

		DragAndDrop.register(mResultList, pt -> FILE_FLAVOR, pt ->
		{
			System.out.println(mResultList.getSelectedValuesList().size());
			return mResultList.getSelectedValuesList().toArray(new File[mResultList.getSelectedValuesList().size()]);
		}, null);

		mSearchButton = new JButton(mSearchAction);
		mCancelButton = new JButton(mCancelAction);
		mCancelButton.setVisible(false);

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
		c.gridx = 3;
		inputPanel.add(mCancelButton, c);

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

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(mResultList), new JScrollPane(mFileViewer));

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
					mFileViewer.update(mResultListModel.get(mResultList.getSelectedIndex()), mSearchFields);
				}
				catch (IOException e)
				{
					e.printStackTrace(Log.out);
				}
			});
		}
	};

	private AbstractAction mCancelAction = new AbstractAction("Cancel")
	{
		@Override
		public void actionPerformed(ActionEvent aE)
		{
			mSearchWorker.cancel();
		}
	};

	private AbstractAction mSearchAction = new AbstractAction("Search files")
	{
		@Override
		public void actionPerformed(ActionEvent aE)
		{
			mSearchWorker = new SearchWorker(new File(mPath.getText()), mFilter.getText(), mSearchFields)
			{
				int fileCount;

				@Override
				protected void onPreExecute()
				{
					mResultList.clearSelection();
					mResultListModel.clear();
					mFileViewer.clear();
					mStatusCounter.setText("");
					mStatusFile.setText("");
					mStatusResultCount.setText("No results");
					mSearchButton.setVisible(false);
					mCancelButton.setVisible(true);
				}


				@Override
				protected void onProgressUpdate(File aFile)
				{
					mStatusCounter.setText(++fileCount + " files searched");
					mStatusFile.setText("Reading " + aFile.getAbsolutePath());
					mStatusBar.repaint();
				}


				@Override
				protected void onPostExecute(File aResult) throws Throwable
				{
					onCancelled(aResult);
				}


				@Override
				protected void onCancelled(File aResult) throws Throwable
				{
					mStatusFile.setText("Done");
					mStatusBar.repaint();
					mResultList.invalidate();
					mResultList.revalidate();
					mSearchButton.setVisible(true);
					mCancelButton.setVisible(false);
				}


				@Override
				protected void onResultUpdate(File aFile)
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
							// ignore
						}
					});
				}
			}.execute();
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
