package com.quollwriter.events;

import java.util.EventListener;

public interface UpdateEventListener<E extends UpdateEvent> extends EventListener
{

    public void valueUpdated (E ev);

}
