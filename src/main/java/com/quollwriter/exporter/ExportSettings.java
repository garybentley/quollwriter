package com.quollwriter.exporter;

import java.io.*;

import java.util.*;

import com.quollwriter.data.*;


public class ExportSettings
{

    public static final int SINGLE_FILE = 0;
    public static final int INDIVIDUAL_FILE = 1;

    public File   outputDirectory = null;
    public int    chapterExportType = SINGLE_FILE;
    public int    otherExportType = SINGLE_FILE;
    public String fileType = null;

    public Map otherInfo = new HashMap ();

    public ExportSettings()
    {

    }

    public String toString ()
    {

        return "export-settings(output-directory: " + this.outputDirectory + ", chapter-export-type: " + this.chapterExportType + ", other-export-type: " + this.otherExportType + ", file-type: " + this.fileType + ")";

    }

}
