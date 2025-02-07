package com.quollwriter.ui.fx;

import java.io.*;
import java.nio.file.*;

import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.beans.property.*;

import com.quollwriter.*;

public class BackgroundObject
{

    public static final String BG_IMAGE_PREFIX = "bg:";

    private Color color = null;
    private Background bg = null;
    private Path userPath = null;
    private String bgImage = null;
    private ObjectProperty<Background> bgProp = null;

    public BackgroundObject ()
    {

        this.bgProp = new SimpleObjectProperty<> ();

    }

    public BackgroundObject (String id)
                      throws Exception
    {

        this ();

        if (id == null)
        {

            return;

        }

        this.update (BackgroundObject.createBackgroundObjectForId (id));

    }

    public Object getBackgroundObject ()
    {

        if (this.userPath != null)
        {

            return this.userPath;

        }

        if (this.color != null)
        {

            return this.color;

        }

        if (this.bgImage != null)
        {

            return this.bgImage;

        }

        return this.bgImage;

    }

    public Background getBackground ()
    {

        return this.bg;

    }

    public String getAsString ()
    {

        if (this.userPath != null)
        {

            return this.userPath.toString ();

        }

        if (this.color != null)
        {

            return UIUtils.colorToHex (this.color);

        }

        if (this.bgImage != null)
        {

            return BG_IMAGE_PREFIX + this.bgImage;

        }

        return null;

    }

    public ObjectProperty<Background> backgroundProperty ()
    {

        return this.bgProp;

    }

    public Path getUserPath ()
    {

        return this.userPath;

    }

    public boolean isUserPath ()
    {

        return this.userPath != null;

    }

    public boolean isImage ()
    {

        if ((this.bgImage != null)
            ||
            (this.userPath != null)
           )
        {

            return true;

        }

        return false;

    }

    public void update (Object o)
                 throws Exception
    {

        if (o == null)
        {

            this.bgImage = null;
            this.userPath = null;
            this.color = null;
            this.bg = null;
            this.bgProp.setValue (null);
            return;

        }

        if (o instanceof Color)
        {

            this.updateForColor ((Color) o);
            return;

        }

        if (o instanceof String)
        {

            this.updateForBuiltInImage ((String) o);
            return;

        }

        if (o instanceof Path)
        {

            this.updateForUserPath ((Path) o);
            return;

        }

        throw new IllegalArgumentException ("Unsupported object type: " + o.getClass ().getName ());

    }

    public void updateForUserPath (Path p)
                            throws IOException
    {

        this.color = null;
        this.bgImage = null;

        if ((Files.notExists (p))
            ||
            (Files.isDirectory (p))
           )
        {

            this.userPath = null;
            this.bg = null;
            this.bgProp.setValue (null);
            return;

        }

        this.userPath = p;

        try (InputStream is = Files.newInputStream (p))
        {

            this.bg = new Background (new BackgroundImage (new Image (is),
                                                            BackgroundRepeat.NO_REPEAT,
                                                            BackgroundRepeat.NO_REPEAT,
                                                            null,
                                                            new BackgroundSize (100, 100, true, true, true, true)));

        }

        this.bgProp.setValue (this.bg);

    }

    public void updateForBuiltInImage (String id)
    {

        if (id.startsWith (BG_IMAGE_PREFIX))
        {

            id = id.substring (BG_IMAGE_PREFIX.length ());

            // Legacy per v3
            if (id.startsWith ("1-"))
            {

                id = "_" + id;

            }

        }

        this.userPath = null;
        this.color = null;

        String name = Constants.BACKGROUND_IMGS_DIR + id;

        InputStream s = Utils.getResourceStream (name);

        if (s == null)
        {

            this.bgImage = null;
            this.bg = null;
            this.bgProp.setValue (null);
            return;

        }

        Image im = new Image (s);
        this.bgImage = id;

        // These shouldn't be stretched.
        if (id.startsWith ("_1-"))
        {

            this.bg = new Background (new BackgroundImage (im,
                                                           BackgroundRepeat.NO_REPEAT,
                                                           BackgroundRepeat.NO_REPEAT,
                                                           null,
                                                           new BackgroundSize (100, 100, true, true, true, true)));

        } else {

            this.bg = new Background (new BackgroundImage (im,
                                                           BackgroundRepeat.REPEAT,
                                                           BackgroundRepeat.REPEAT,
                                                           null,
                                                           BackgroundSize.DEFAULT));
                                                           //new BackgroundSize (100, 100, true, true, true, true)));

        }

        this.bgProp.setValue (this.bg);

    }

    public void updateForColor (Color c)
    {

        this.userPath = null;
        this.bgImage = null;
        this.color = c;
        this.bg = new Background (new BackgroundFill (c, null, null));

        this.bgProp.setValue (this.bg);

    }

    public static Object createBackgroundObjectForId (String id)
    {

        if (id == null)
        {

            return null;

        }

        if (id.startsWith ("#"))
        {

            Color col = UIUtils.hexToColor (id);

            return col;

        }

        if (id.startsWith (BG_IMAGE_PREFIX))
        {

            id = id.substring (BG_IMAGE_PREFIX.length ());

            Image im = Environment.getBackgroundImage (id, -1, -1);

            if (im != null)
            {

                return id;

            }

        }

        if (id.equals ("file:"))
        {

            return null;

        }

        if (id.startsWith ("file:"))
        {

            id = id.substring ("file:".length ());
/*
            StringBuffer _id = new StringBuffer (id);

            _id = _id.replace (0,
                             "file:C:\\".length (),
                             "file:\\\\:C:\\");

            id = _id.toString ();
*/
        }

        Path path = Paths.get (id);

        return path;

    }

}
