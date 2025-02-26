package com.quollwriter.ui.fx.components;

public interface NotificationViewer
{

    void removeAllNotifications ();

    void removeNotification (Notification n);

    void addNotification (Notification n);

    Notification getNotificationById (String id);
}
