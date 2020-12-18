package com.quollwriter;

import java.io.*;
import java.nio.channels.*;

import java.util.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;
import javafx.stage.*;
import javafx.application.*;
import javafx.beans.property.*;

import javafx.scene.*;
import javafx.scene.control.*;

//import de.codecentric.centerdevice.javafxsvg.*;
//import de.codecentric.centerdevice.javafxsvg.dimension.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class Startup extends Application
{

    private Splashscreen ss = null;

    @Override
    public void init ()
    {

    }

    public void updateProgress (double v)
    {

        UIUtils.runLater (() ->
        {

            if (ss != null)
            {

                //ss.updateProgress (v);

            }

        });

    }

    public void finish ()
    {

        UIUtils.runLater (() ->
        {

            if (ss != null)
            {

                ss.finish ();

            }

        });

    }

    @Override
    public void start (Stage s)
    {

        //Platform.setImplicitExit (false);

        try
        {
/*
            SvgImageLoaderFactory.install (new PrimitiveDimensionProvider ()
            {

                @Override
                public Dimension getDimension (org.w3c.dom.Document d)
                {
System.out.println ("CALLED: " + d);
                    return new Dimension (64, 64);

                }

            });
*/
            this.ss = Splashscreen.builder ().build ();

            this.ss.show ();

            javafx.geometry.Rectangle2D rb = Screen.getPrimary ().getBounds ();

            this.ss.setX (((rb.getWidth () - this.ss.getWidth ()) / 2));
            this.ss.setY (((rb.getHeight () - this.ss.getHeight ()) / 2));

            this.updateProgress (0.2d);

        } catch (Exception e) {

            //e.printStackTrace ();

        }

        Thread t = new Thread (() ->
        {

            try
            {

                Environment.init (this.ss.progressProperty (),
                                  () ->
                {

                    try
                    {

                        Environment.setHostServices (this.getHostServices ());

                        Environment.incrStartupProgress ();

                        if (Environment.isFirstUse ())
                        {

                            new FirstUseWizard ().show ();

                            this.finish ();

                            return;

                        }

                        if (Environment.getAllProjectInfos ().size () == 0)
                        {

                            this.finish ();

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

                            ComponentUtils.showErrorMessage (getUILanguageStringProperty (startup,cantopenlastprojecterror,text));

                        }

                    } catch (Exception e) {

                        Environment.logError ("Unable to start up: " +
                                              e);

                        ComponentUtils.showErrorMessage (getUILanguageStringProperty (startup,unabletostarterror));

                    }

                });

            } catch (Exception eee)
            {

                if (eee instanceof OverlappingFileLockException)
                {

                    // We need to show english here since we don't have the ui strings set up yet.
                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (startup,alreadyrunningerror));

                } else {

                    Environment.logError ("Unable to open Quoll Writer",
                                          eee);

                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (startup,unabletostarterror));

                }

            } catch (Error err) {

                err.printStackTrace ();

            } finally
            {

                this.finish ();

                Environment.startupComplete ();

            }

        });

        t.start ();

    }

    public static void main (String[] argv)
    {

        Startup.launch (argv);

    }

}
