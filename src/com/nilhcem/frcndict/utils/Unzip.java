package com.nilhcem.frcndict.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.nilhcem.frcndict.core.AbstractCancellableObservable;

public final class Unzip extends AbstractCancellableObservable {
	private final File mLocation;
	private final File mZipFile;

	public Unzip(File zipFile, File location) {
		super();
		mZipFile = zipFile;
		mLocation = location;
		dirChecker("");
	}

	private void dirChecker(String dir) {
		File curDir = new File(mLocation.getAbsolutePath() + File.separator + dir);
		if (!curDir.isDirectory()) {
			curDir.mkdirs();
		}
	}

	private long getTotalSize() throws IOException {
		long totalSize = 0l;

		ZipFile zf = new ZipFile(mZipFile);
		Enumeration<? extends ZipEntry> e = zf.entries();
		while (!mCancelled && e.hasMoreElements()) {
			ZipEntry ze = (ZipEntry) e.nextElement();
			if (ze.getSize() > 0) {
				totalSize += ze.getSize();
			}
		}

		return totalSize;
	}

	@Override
	public void start() throws IOException {
		long totalSize = 0l;

		if (countObservers() > 0) {
			totalSize = getTotalSize();
		}

		long curSize = 0;
		byte[] buffer = new byte[1024];

		ZipInputStream zip = new ZipInputStream(new FileInputStream(mZipFile));
		BufferedInputStream in = new BufferedInputStream(zip, buffer.length);

		ZipEntry entry;
		while ((!mCancelled && (entry = zip.getNextEntry()) != null)) {
			if (entry.isDirectory()) {
				dirChecker(entry.getName());
			} else {
				FileOutputStream fos = new FileOutputStream(mLocation.getAbsolutePath() + File.separator + entry.getName());
				BufferedOutputStream out = new BufferedOutputStream(fos, buffer.length);

				int read;
				while ((read = in.read(buffer, 0, buffer.length)) != -1) {
					out.write(buffer, 0, read);

					// Notify percentage to observers
					if (countObservers() > 0 && totalSize != 0) {
						curSize += read;
						updateProgress((int) ((curSize * 100) / totalSize));
					}
				}
				out.flush();
				out.close();
				fos.close();
			}
			zip.closeEntry();
		}
		zip.close();
		in.close();
	}
}
