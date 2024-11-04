package org.terifan.apps.file_search;

import java.io.File;
import java.io.IOException;
import javax.swing.JTextField;
import org.terifan.concurrent.AsyncTask;
import org.terifan.io.Streams;


public class SearchWorker extends AsyncTask<File, File, File>
{
	private File mDirectory;
	private JTextField[][] mSearchFields;
	private String mFilter;
	private long mMinFileLength;
	private long mMaxFileLength;


	public SearchWorker(File aDirectory, String aFilter, JTextField[][] aSearchFields, long aMinFileLength, long aMaxFileLength)
	{
		mDirectory = aDirectory;
		mFilter = aFilter;
		mSearchFields = aSearchFields;
		mMinFileLength = aMinFileLength;
		mMaxFileLength = aMaxFileLength;
	}


	@Override
	protected File doInBackground(File aParam) throws Throwable
	{
		searchDir(mDirectory, mFilter);
		return null;
	}


	private void searchDir(File aDirectory, String aFilter) throws IOException
	{
		System.out.println("Visting directory: " + aDirectory);

		long start = System.currentTimeMillis();

		File[] listFiles = aDirectory.listFiles();

		System.out.println(listFiles.length + " loaded in " + (System.currentTimeMillis() - start));

		for (File file : listFiles)
		{
			if (isCancelled())
			{
				return;
			}
			if (file.isFile())
			{
				long len = file.length();

				if (len >= mMinFileLength && len < mMaxFileLength)
				{
					String name = file.getName().toLowerCase();

					if (aFilter.isEmpty() || name.matches(aFilter.replace(".", "\\.").replace("*", ".*")))
					{
						searchFile(file);
					}
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
		byte[] buffer;

		try
		{
			buffer = Streams.readAll(aFile);
		}
		catch (Exception e)
		{
			// ignore, file is locked or deleted
			return;
		}

		String src = new String(buffer).toLowerCase();
		boolean rowFound = false;

		onProgressUpdate(aFile);

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
			onResultUpdate(aFile);
		}
	}
}
