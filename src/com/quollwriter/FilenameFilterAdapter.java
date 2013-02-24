package com.quollwriter;

import java.io.*;


public class FilenameFilterAdapter implements FilenameFilter
{

    public boolean accept (File   dir,
                           String name)
    {
        return false;
    }

}
