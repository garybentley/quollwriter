package com.quollwriter.importer;

import java.net.*;

import com.quollwriter.data.*;


public interface ImportCallback
{

    public void projectCreated (Project p,
                                URI     u);

    public void exceptionOccurred (Exception e,
                                   URI       u);

}
