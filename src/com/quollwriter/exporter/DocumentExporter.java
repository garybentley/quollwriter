package com.quollwriter.exporter;

import java.io.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;


public interface DocumentExporter
{

    public void exportProject (File dir)
                        throws GeneralException;

    public String getStartStage ();

    public WizardStep getStage (String stage);

    public String getNextStage (String currStage);

    public String getPreviousStage (String currStage);

    public void setProject (Project p);

}
