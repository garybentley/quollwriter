package com.quollwriter.importer;

import java.io.*;

import com.quollwriter.data.*;


public interface DocumentImporter
{

    public Project convert (InputStream in,
                            String      fileExt)
                     throws Exception;

}
