package com.quollwriter.ui.events;

import java.util.EventListener;


public interface DictionaryChangedListener extends EventListener
{

    public void dictionaryChanged (DictionaryChangedEvent ev);

}
