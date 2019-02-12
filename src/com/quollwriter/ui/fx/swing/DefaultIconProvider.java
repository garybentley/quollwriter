package com.quollwriter.ui.fx.swing;

import java.util.*;
import java.net.*;
import javax.imageio.*;

import javax.swing.*;

import com.quollwriter.data.*;

import com.quollwriter.*;

public class DefaultIconProvider implements IconProvider
{

    private Map<String, ImageIcon> cache = new HashMap<> ();

    public static int getIconPixelWidthForType (int type)
    {

        int size = 16;

        if (type == Constants.ICON_EDITOR_MESSAGE)
        {

            size = 20;

        }

        if (type == Constants.ICON_TOOLBAR)
        {

            size = 20;

        }

        if (type == Constants.ICON_MENU_INNER)
        {

            size = 16;

        }

        if (type == Constants.ICON_PANEL_MAIN)
        {

            size = 24;

        }

        if (type == Constants.ICON_NOTIFICATION)
        {

            size = 24;

        }

        if (type == Constants.ICON_ACHIEVEMENT_HEADER)
        {

            size = 24;

        }

        if (type == Constants.ICON_PANEL_ACTION)
        {

            size = 20;

        }

        if (type == Constants.ICON_TITLE_ACTION)
        {

            size = 24;

        }

        if (type == Constants.ICON_BG_SWATCH)
        {

            size = 37;

        }

        if (type == Constants.ICON_FULL_SCREEN_ACTION)
        {

            size = 24;

        }

        if (type == Constants.ICON_TITLE)
        {

            size = 24;

        }

        if (type == Constants.ICON_EDITORS_LIST_TAB_HEADER)
        {

            size = 24;

        }

        return size;

    }

    public URL getIconURL (String  name,
                           int     type)
                                  //boolean large)
    {

        if (name == null)
        {

            return null;

        }

        if (name.indexOf ('.') == -1)
        {

            int size = this.getIconPixelWidthForType (type);

            name = Constants.IMGS_DIR + name + size + ".png";

        } else
        {

            name = Constants.IMGS_DIR + name;

        }

        return DefaultIconProvider.class.getResource (name);

    }

    public ImageIcon getIcon (String name,
                              int    type)
    {

        if (name == null)
        {

            return null;

        }

        ImageIcon ii = this.cache.get (name + "-" + type);

        if (ii == null)
        {

            if (name.equals (Constants.LOADING_GIF_NAME))
            {

                ii = this.getLoadingIcon ();

            } else {

                URL url = this.getIconURL (name,
                                           type);

                if (url == null)
                {

                    if (Environment.isDebugModeEnabled ())
                    {

                        // Can't find image, log the problem but keep going.
                        Environment.logError ("Unable to find/load image: " +
                                              name +
                                              ", type: " + type +
                                              ", check images jar to ensure file is present.",
                                              // Gives a stack trace
                                              new Exception ());

                    }

                    return null;

                }

                try
                {

                    ii = new ImageIcon (ImageIO.read (url));

                } catch (Exception e) {

                    Environment.logError ("Unable to find/load image: " +
                                          name,
                                          e);

                }

            }

            this.cache.put (name + "-" + type,
                            ii);

        }

        return ii;

    }

    public ImageIcon getLoadingIcon ()
    {

        URL url = DefaultIconProvider.class.getResource (Constants.IMGS_DIR + Constants.LOADING_GIF_NAME);

        if (url == null)
        {

            if (Environment.isDebugModeEnabled ())
            {

                // Can't find image, log the problem but keep going.
                Environment.logError ("Unable to find loading image: " +
                                      Constants.LOADING_GIF_NAME +
                                      ", check images jar to ensure file is present.",
                                      // Gives a stack trace
                                      new Exception ());

            }

            return null;

        }

        try
        {

            // Can't use ImageIO here, won't animate only reads first frame.
            return new ImageIcon (url);

        } catch (Exception e) {

            Environment.logError ("Unable to find loading image: " +
                                  Constants.LOADING_GIF_NAME,
                                  e);

        }

        return null;

    }

    public ImageIcon getIcon (DataObject d,
                              int        type)
    {

        return this.getIcon (d.getObjectType (),
                             type);

    }

    public ImageIcon getIcon (Object obj,
                              int    type)
    {

        if (obj == null)
        {

            return null;

        }

        if (obj instanceof String)
        {

            return this.getIcon (obj.toString (),
                                 type);

        }

        if (obj instanceof Note)
        {

            Note note = (Note) obj;

            if (note.isEditNeeded ())
            {

                return this.getIcon (Constants.EDIT_NEEDED_NOTE_ICON_NAME,
                                     type);

            }

        }

        if (obj instanceof DataObject)
        {

            return this.getIcon ((DataObject) obj,
                                 type);

        }

        throw new IllegalArgumentException ("Class: " +
                                            obj.getClass ().getName () +
                                            " not supported.");

    }

    public ImageIcon getObjectIcon (String ot,
                                           int    iconType)
    {

        return getIcon (ot,
                                    iconType);

    }

    public ImageIcon getObjectIcon (DataObject d,
                                           int        iconType)
    {

        String ot = d.getObjectType ();

        if (d instanceof Note)
        {

            Note n = (Note) d;

            if (n.isEditNeeded ())
            {

                ot = Constants.EDIT_NEEDED_NOTE_ICON_NAME;

            }

        }

        return getObjectIcon (ot,
                                          iconType);

    }

}
