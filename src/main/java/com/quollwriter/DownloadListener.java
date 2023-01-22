package com.quollwriter;

public interface DownloadListener
{

    public void progress (int downloaded,
                          int total);

    public void finished (int total);

    public void handleError (Exception e);

    public void onStop ();

}
