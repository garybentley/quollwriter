package com.quollwriter;

import java.io.*;
import java.nio.channels.*;

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

Environment.setDebugModeEnabled (true);
/*
new FirstUseWizard ().init ();

if (true)
{
    return;
}
*/
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

            if (UserProperties.getAsBoolean (Constants.SHOW_LANDING_ON_START_PROPERY_NAME))
            {

                Environment.showLanding ();

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

                Environment.showLanding ();

                UIUtils.showMessage ((java.awt.Component) null,
                                     Environment.getUIString (LanguageStrings.startup,
                                                              LanguageStrings.cantopenlastprojecterror,
                                                              LanguageStrings.title),
                                     //"Unable to open last {project}",
                                     Environment.getUIString (LanguageStrings.startup,
                                                              LanguageStrings.cantopenlastprojecterror,
                                                              LanguageStrings.text));
                                    //"Unable to open last edited {project}, please select another {project} or create a new one.");

            }

        } catch (Exception eee)
        {

            if (eee instanceof OverlappingFileLockException)
            {

                UIUtils.showErrorMessage (null,
                                          Environment.getUIString (LanguageStrings.startup,
                                                                   LanguageStrings.alreadyrunningerror));
                                          //"It appears that Quoll Writer is already running.  Please close the other instance before starting Quoll Writer again.");


            } else {

                Environment.logError ("Unable to open Quoll Writer",
                                      eee);

                UIUtils.showErrorMessage (null,
                                          Environment.getUIString (LanguageStrings.startup,
                                                                   LanguageStrings.unabletostarterror));
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
