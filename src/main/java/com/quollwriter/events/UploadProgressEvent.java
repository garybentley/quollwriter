package com.quollwriter.events;

import java.util.EventObject;

public class UploadProgressEvent extends UpdateEvent
{

    private int percent = 0;
    private long sent = 0;
    private long total = 0;

    public UploadProgressEvent (Object source,
                                long    sent,
                                long   total)
    {

        super (source);

        this.sent = sent;
        this.total = total;

    }

    public int getPercent ()
    {

        return (int) (this.sent * 100.0 / this.total + 0.5);

    }

    public long getSent ()
    {

        return this.sent;

    }

    public long getTotal ()
    {

        return this.total;

    }

}
