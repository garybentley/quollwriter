package com.quollwriter.exporter;

import java.io.*;
import java.nio.file.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;

import com.quollwriter.ui.fx.components.*;

public interface DocumentExporter
{

    public void exportProject (Path    dir,
                               Project itemsToExport)
                        throws GeneralException;

    public String getStartStage ();

    public WizardStep getStage (String stage);

    com.quollwriter.ui.fx.components.Wizard.Step getStage2 (String stage);

    public String getNextStage (String currStage);

    public String getPreviousStage (String currStage);

    public void setProject (Project p);

}
