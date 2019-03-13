/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2018 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.catroid.io.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import org.catrobat.catroid.io.StorageOperations;
import org.catrobat.catroid.io.XstreamSerializer;
import org.catrobat.catroid.utils.FileMetaDataExtractor;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static org.catrobat.catroid.common.Constants.CODE_XML_FILE_NAME;
import static org.catrobat.catroid.common.FlavoredConstants.DEFAULT_ROOT_DIRECTORY;

public class ProjectCopyTask extends AsyncTask<String, Void, Boolean> {

	public static final String TAG = ProjectCopyTask.class.getSimpleName();

	private WeakReference<ProjectCopyListener> weakListenerReference;

	public ProjectCopyTask(ProjectCopyListener listener) {
		weakListenerReference = new WeakReference<>(listener);
	}

	public static boolean task(String srcName, String dstName) {
		if (!FileMetaDataExtractor.getProjectNames(DEFAULT_ROOT_DIRECTORY).contains(srcName)) {
			Log.e(TAG, "Cannot copy. Project " + srcName + " does not exist");
			return false;
		}
		if (FileMetaDataExtractor.getProjectNames(DEFAULT_ROOT_DIRECTORY).contains(dstName)) {
			Log.e(TAG, "Cannot copy. Project " + dstName + " already exist");
			return false;
		}

		File srcDir = new File(DEFAULT_ROOT_DIRECTORY, srcName);
		File dstDir = new File(DEFAULT_ROOT_DIRECTORY, dstName);

		try {
			StorageOperations.copyDir(srcDir, dstDir);
			XstreamSerializer.renameProject(new File(dstDir, CODE_XML_FILE_NAME), dstName);
			return true;
		} catch (IOException e) {
			Log.e(TAG, "Something went wrong while copying project " + srcName + " to " + dstName, e);
			if (dstDir.isDirectory()) {
				Log.e(TAG, "Folder exists, trying to delete folder.");
				try {
					StorageOperations.deleteDir(dstDir);
				} catch (IOException deleteException) {
					Log.e(TAG, "Cannot delete folder " + dstName, deleteException);
				}
			}
			return false;
		}
	}

	@Override
	protected Boolean doInBackground(String... strings) {
		return task(strings[0], strings[1]);
	}

	@Override
	protected void onPostExecute(Boolean success) {
		ProjectCopyListener listener = weakListenerReference.get();
		if (listener != null) {
			listener.onCopyFinished(success);
		}
	}

	public interface ProjectCopyListener {

		void onCopyFinished(boolean success);
	}
}
