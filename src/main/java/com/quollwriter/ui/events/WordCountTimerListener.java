package com.quollwriter.ui.events;

public interface WordCountTimerListener extends java.util.EventListener
{

    public void timerFinished (WordCountTimerEvent ev);

    public void timerStarted (WordCountTimerEvent ev);
    
    public void timerUpdated (WordCountTimerEvent ev);

}
