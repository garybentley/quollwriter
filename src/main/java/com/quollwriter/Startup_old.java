package com.quollwriter;

import java.io.*;
import java.nio.channels.*;

import java.util.*;

import javax.swing.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class Startup_old
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

        Splashscreen ss = null;

        try
        {

            ss = Splashscreen.builder ()
                .build ();

            ss.updateProgress (5);

            Environment.init (null, null);

            final Splashscreen _ss = ss;
/*
            Environment.startupProgressProperty ().addListener ((p, oldv, newv) ->
            {

                _ss.updateProgress (5);

            });
*/
            if (Environment.isFirstUse ())
            {

                // TODO new FirstUseWizard ().init ();

                return;

            }

            if (Environment.getAllProjects ().size () == 0)
            {

                ss.finish ();

                Environment.showAllProjectsViewer ();

                return;

            }

            boolean showError = false;

            if (UserProperties.getAsBoolean (Constants.SHOW_LANDING_ON_START_PROPERY_NAME))
            {

                Environment.showAllProjectsViewer ();

            }

            // See if the user property is to open the last edited project.
            if (UserProperties.getAsBoolean (Constants.OPEN_LAST_EDITED_PROJECT_PROPERTY_NAME))
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

                Environment.showAllProjectsViewer ();

                QuollPopup.messageBuilder ()
                    .withViewer (Environment.getFocusedViewer ())
                    .title (startup,cantopenlastprojecterror,title)
                    .message (startup,cantopenlastprojecterror,text)
                    .build ();

            }

        } catch (Exception eee)
        {

            if (eee instanceof OverlappingFileLockException)
            {

                ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                 getUILanguageStringProperty (startup,alreadyrunningerror));
                                          //"It appears that Quoll Writer is already running.  Please close the other instance before starting Quoll Writer again.");


            } else {

                Environment.logError ("Unable to open Quoll Writer",
                                      eee);

                ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                 getUILanguageStringProperty (startup,unabletostarterror));
                                          //"Unable to start Quoll Writer");

            }

        } finally
        {

            if (ss != null)
            {

                ss.finish ();

            }

            Environment.startupComplete ();

        }

    }

}
