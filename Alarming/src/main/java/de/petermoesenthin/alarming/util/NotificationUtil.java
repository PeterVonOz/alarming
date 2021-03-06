/*
 * Alarming, an alarm app for the Android platform
 *
 * Copyright (C) 2014-2015 Peter Mösenthin <peter.moesenthin@gmail.com>
 *
 * Alarming is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.petermoesenthin.alarming.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.petermoesenthin.alarming.MainActivity;
import de.petermoesenthin.alarming.R;
import de.petermoesenthin.alarming.pref.PrefKey;
import de.petermoesenthin.alarming.pref.PrefUtil;

public class NotificationUtil
{

	public static final String ACTION_DISMISS_SNOOZE = "de.petermoesenthin.alarming.ACTION_DISMISS_SNOOZE";

	public static final String ACTION_DISMISS_ALARM = "de.petermoesenthin.alarming.ACTION_DISMISS_ALARM";

	public static final String DEBUG_TAG = NotificationUtil.class.getSimpleName();

	/**
	 * Shows a persistent notification indicating the alarm time if is set.
	 * @param context
	 * @param hour
	 * @param minute
	 * @param id
	 */
	public static void showAlarmSetNotification(Context context, int hour, int minute, int id)
	{
		//Early out if a notification is not wanted
		boolean showNotification = PrefUtil.getBoolean(context,
				PrefKey.SHOW_ALARM_NOTIFICATION, true);
		if (!showNotification)
		{
			Log.d(DEBUG_TAG, "Notifications disabled. Returning.");
			return;
		}
		// Build dismiss intent
		Intent intent = new Intent();
		intent.setAction(ACTION_DISMISS_ALARM);
		intent.putExtra("id", id);
		//Build and show Notification
		String alarmFormatted = StringUtil.getTimeFormattedSystem(context, hour, minute);
		Notification.Builder notBuilder = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_stat_alarmclock_light)
				.setOngoing(true)
				.setContentTitle(
						context.getResources().getString(R.string.notification_alarmActivated))
				.setContentText(context.getResources().getString(R.string.notification_timeSetTo) +
						" " + alarmFormatted);
		Intent notificationIntent = new Intent(context, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notBuilder.setContentIntent(contentIntent);
		notificationIntent.addFlags(Notification.FLAG_ONGOING_EVENT);
		getNotificationManager(context).notify(id, notBuilder.build());
	}

	public static void showSnoozeNotification(Context context, int hour, int minute, int id)
	{
		Intent intent = new Intent();
		intent.setAction(ACTION_DISMISS_SNOOZE);
		intent.putExtra("id", id);
		// Build dismiss intent
		PendingIntent pIntent = PendingIntent.getBroadcast(context, id,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		String alarmFormatted = StringUtil.getTimeFormatted(hour, minute);
		Notification.Builder notBuilder = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_stat_alarmclock_light)
				.setOngoing(true)
				.setContentTitle(
						context.getResources().getString(R.string.notification_snoozeActivated))
				.setContentText(context.getResources().getString(R.string.notification_timeSetTo) +
						" " + alarmFormatted)
				.addAction(R.drawable.ic_action_cancel,
						context.getResources().getString(R.string.notification_cancelSnooze),
						pIntent);
		getNotificationManager(context).notify(id, notBuilder.build());
	}

	public static void clearAlarmNotifcation(Context context, int id)
	{
		getNotificationManager(context).cancel(id);
	}

	public static void clearSnoozeNotification(Context context, int id)
	{
		getNotificationManager(context).cancel(id);
	}

	public static NotificationManager getNotificationManager(Context context)
	{
		return (NotificationManager) context.getSystemService(
				Activity.NOTIFICATION_SERVICE);
	}

}
