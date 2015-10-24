/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.granita.provider.tasks.taskhooks;

import com.granita.provider.tasks.TaskContract.Property.Relation;
import com.granita.provider.tasks.TaskContract.Tasks;
import com.granita.provider.tasks.TaskDatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * A simple hook that updates relations for new tasks.
 * <p>
 * In general there is no guarantee that a related task is already in the database when a task is inserted. In such a case we can not set the
 * {@link Relation#RELATED_ID} value. This hook updates the {@link Relation#RELATED_ID} is when a task is inserted.
 * </p>
 * <p>
 * It also updates {@link Relation#RELATED_UID} when a tasks is synced the first time.
 * </p>
 * TODO: update {@link Tasks#PARENT_ID} of related tasks.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class RelationUpdaterHook extends AbstractTaskHook
{

	@Override
	public void afterInsert(SQLiteDatabase db, long taskId, ContentValues values, boolean isSyncAdapter)
	{
		// A new task has been inserted by the sync adapter. Update all relations that point to this task.

		if (!isSyncAdapter)
		{
			// the task was created on the device, so it doesn't have a UID
			return;
		}

		String uid = values.getAsString(Tasks._UID);

		if (uid != null)
		{
			ContentValues v = new ContentValues(1);
			v.put(Relation.RELATED_ID, taskId);

			db.update(TaskDatabaseHelper.Tables.PROPERTIES, v, Relation.MIMETYPE + "= ? AND " + Relation.RELATED_UID + "=?", new String[] {
				Relation.CONTENT_ITEM_TYPE, uid });
		}
	}


	@Override
	public void afterUpdate(SQLiteDatabase db, long taskId, Cursor cursor, ContentValues values, boolean isSyncAdapter)
	{
		// A task has been updated any may have received a UID by the sync adapter. Update all by-id references to this task.

		if (!isSyncAdapter)
		{
			// only sync adapters may assign a UID
			return;
		}

		String uid = values.getAsString(Tasks._UID);

		if (uid != null)
		{
			ContentValues v = new ContentValues(1);
			v.put(Relation.RELATED_UID, uid);

			db.update(TaskDatabaseHelper.Tables.PROPERTIES, v, Relation.MIMETYPE + "= ? AND " + Relation.RELATED_ID + "=?", new String[] {
				Relation.CONTENT_ITEM_TYPE, Long.toString(taskId) });
		}
	}


	@Override
	public void afterDelete(SQLiteDatabase db, long taskId, boolean isSyncAdapter)
	{
		if (!isSyncAdapter)
		{
			// remove once the deletion is final, which is when the sync adapter removes it
			return;
		}

		db.delete(TaskDatabaseHelper.Tables.PROPERTIES, Relation.MIMETYPE + "= ? AND " + Relation.RELATED_ID + "=?", new String[] { Relation.CONTENT_ITEM_TYPE,
			Long.toString(taskId) });
	}
}
