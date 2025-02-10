package com.quollwriter.exporter;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.fx.components.*;

public interface DocumentExporter
{

    public void exportProject (Path             dir,
                               Set<NamedObject> itemsToExport)
                        throws GeneralException;

    public String getStartStage ();

    Wizard.Step getStage (String stage);

    public String getNextStage (String currStage);

    public String getPreviousStage (String currStage);

    public void setProject (Project p);

}
