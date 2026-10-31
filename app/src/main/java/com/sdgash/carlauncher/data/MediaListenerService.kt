package com.sdgash.carlauncher.data

import android.service.notification.NotificationListenerService

/**
 * Empty notification listener. Android only lets an app read other apps'
 * media sessions (now playing + controls) while the user has granted it
 * "notification access", which is tied to a service like this one.
 */
class MediaListenerService : NotificationListenerService()
