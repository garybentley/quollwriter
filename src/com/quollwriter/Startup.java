package com.quollwriter;

import java.io.*;

import java.util.*;

import javax.swing.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;


public class Startup
{

    public static void main (String[] argv)
    {

        if ((argv != null) &&
            (argv.length > 0))
        {

            if (argv[0].equals ("_debugMode"))
            {

                Environment.setDebugModeEnabled (true);

            }

        }

        SplashScreen ss = null;
        
        try
        {

            ss = new SplashScreen (Environment.getLogo ().getImage (),
                                   "Starting...",
                                   com.quollwriter.ui.UIUtils.getColor ("#f9f9f9"));

            ss.setProgress (5);

            Environment.init ();

            if (!Environment.hasProjectsFile ())
            {

                ss.finish ();

                new FirstProject ().init ();

                return;

            }

            boolean showError = false;

            // See if the user property is to open the last edited project.
            if (Environment.getUserProperties ().getPropertyAsBoolean (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME))
            {
            
                try
                {

                    if (Environment.openLastEditedProject ())
                    {

                        ss.finish ();

                        return;

                    }

                    showError = true;

                } catch (Exception e)
                {

                    Environment.logError ("Unable to open last edited project",
                                          e);

                    showError = true;

                }

                ss.finish ();

            }

            FindOrOpen f = new FindOrOpen (FindOrOpen.SHOW_OPEN | FindOrOpen.SHOW_NEW);
            f.setVisible (true);

            // Need to do this here since, if there is no visible frame (somewhere) then showErrorMessage will throw an error that crashes the jvm... nice...
            if (showError)
            {

                ss.finish ();

                UIUtils.showErrorMessage (f,
                                          "Unable to open last edited project, please select another project or create one.");

            }

        } catch (Exception eee)
        {

            Environment.logError ("Unable to open Quoll Writer",
                                  eee);

            UIUtils.showErrorMessage (null,
                                      "Unable to start Quoll Writer");

        } finally
        {

            if (ss != null)
            {

                ss.finish ();

            }

        }

    }

}
