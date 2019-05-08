package com.quollwriter;

import java.io.*;
import java.nio.channels.*;

import java.util.*;

import javax.swing.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import javafx.stage.*;
import javafx.application.*;
import javafx.beans.property.*;

public class Startup_fx extends Application
{

    @Override
    public void init ()
    {

    }

    @Override
    public void start (Stage s)
    {

        //Splashscreen ss = null;
        Splashscreen ss = null;

        try
        {

            ss = Splashscreen.builder ().build ();

            ss.show ();
            javafx.geometry.Rectangle2D rb = Screen.getPrimary ().getBounds ();
            ss.setX (((rb.getWidth () - ss.getWidth ()) / 2));
            ss.setY (((rb.getHeight () - ss.getHeight ()) / 2));

            //DoubleProperty progress = ss.progressProperty ();

            ss.updateProgress (0.5f);
/*
            ss = new SplashScreen (Environment.getLogo ().getImage (),
                                   "Starting...",
                                   com.quollwriter.ui.UIUtils.getColor ("#f9f9f9"));
*/
            //ss.setProgress (5);

            Environment.init ();

            Environment.showAllProjectsViewer ();

            ss.updateProgress (0.6f);

System.out.println ("X: " + Platform.isImplicitExit ());

            /*
            if (Environment.isFirstUse ())
            {

                new FirstUseWizard ().init ();

                return;

            }

            if (Environment.getAllProjectInfos ().size () == 0)
            {

                ss.finish ();

                Environment.showLanding ();

                return;

            }

            boolean showError = false;

            if (Environment.getUserProperties ().getPropertyAsBoolean (Constants.SHOW_LANDING_ON_START_PROPERY_NAME))
            {

                Environment.showLanding ();

            }

            // See if the user property is to open the last edited project.
            if (Environment.getUserProperties ().getPropertyAsBoolean (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME))
            {

                try
                {

                    if (!Environment.openLastEditedProject ())
                    {

                        showError = true;

                    }

                } catch (Exception e)
                {

                    showError = true;

                }

            }

            // Need to do this here since, if there is no visible frame (somewhere) then showErrorMessage will throw an error that crashes the jvm... nice...
            if (showError)
            {

                Environment.showLanding ();

                UIUtils.showMessage ((java.awt.Component) null,
                                     "Unable to open last {project}",
                                     "Unable to open last edited {project}, please select another {project} or create a new one.");

            }

            */
        } catch (Exception eee)
        {

            if (eee instanceof OverlappingFileLockException)
            {

                ComponentUtils.showErrorMessage (null,
                                                 new SimpleStringProperty ("It appears that Quoll Writer is already running.  Please close the other instance before starting Quoll Writer again."));


            } else {

                Environment.logError ("Unable to open Quoll Writer",
                                      eee);

                ComponentUtils.showErrorMessage (null,
                                                 new SimpleStringProperty ("Unable to start Quoll Writer"));

            }

        } catch (Error err) {

            err.printStackTrace ();

        } finally
        {

            if (ss != null)
            {

                ss.finish ();

            }

            Environment.startupComplete ();

        }

    }

    public static void main (String[] argv)
    {

        Startup_fx.launch (argv);

    }

}
