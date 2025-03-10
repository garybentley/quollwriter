package com.quollwriter.ui.fx;

import javafx.scene.paint.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;

public class ProjectTextProperties extends TextProperties implements UserPropertySetter
{

    private boolean allowSet = true;

    public ProjectTextProperties ()
    {

        super ();

        boolean v = Environment.isNightModeEnabled ();

        this.initInternal (UserProperties.get (Constants.EDITOR_FONT_PROPERTY_NAME),
                           UserProperties.getAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME),
                           UserProperties.get (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME),
                           UserProperties.getAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME),
                           UserProperties.getAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME),
                           UserProperties.getAsFloat (Constants.EDITOR_PARAGRAPH_SPACING_PROPERTY_NAME),
                           UIUtils.hexToColor (UserProperties.get (v ? Constants.EDITOR_FONT_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.EDITOR_FONT_COLOR_PROPERTY_NAME)),
                           UIUtils.hexToColor (UserProperties.get (v ? Constants.EDITOR_BGCOLOR_NIGHT_MODE_PROPERTY_NAME : Constants.EDITOR_BGCOLOR_PROPERTY_NAME)),
                           UIUtils.hexToColor (UserProperties.get (v ? Constants.EDITOR_WRITING_LINE_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME)),
                           UserProperties.getAsBoolean (Constants.EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME),
                           UserProperties.getAsInt (Constants.EDITOR_TEXT_BORDER_PROPERTY_NAME));

        Environment.nightModeProperty ().addListener ((pr, oldv, newv) ->
        {

            this.setTextColor (UIUtils.hexToColor (UserProperties.get (newv ? Constants.EDITOR_FONT_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.EDITOR_FONT_COLOR_PROPERTY_NAME)));
            this.setBackgroundColor (UIUtils.hexToColor (UserProperties.get (newv ? Constants.EDITOR_BGCOLOR_NIGHT_MODE_PROPERTY_NAME : Constants.EDITOR_BGCOLOR_PROPERTY_NAME)));
            this.setWritingLineColor (UIUtils.hexToColor (UserProperties.get (newv ? Constants.EDITOR_WRITING_LINE_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME)));

        });

    }

    public ProjectTextProperties (TextProperties props)
    {

        this.initInternal (props);

    }

    @Override
    public void stopSetting ()
    {

        this.allowSet = false;

    }

    @Override
    public void startSetting ()
    {

        this.allowSet = true;

    }

    public void setBackgroundColor (Color c)
    {

        super.setBackgroundColor (c);

        this.setProperty (new StringProperty (Environment.isNightModeEnabled () ? Constants.EDITOR_BGCOLOR_NIGHT_MODE_PROPERTY_NAME : Constants.EDITOR_BGCOLOR_PROPERTY_NAME,
                                              UIUtils.colorToHex (this.getBackgroundColor ())));

/*
 *
 *
        if (this.sideBar != null)
        {

            this.sideBar.backgroundChanged ();

        }

        if (c.equals (this.getTextColor ()))
        {

            if (c.equals (Color.black))
            {

                // Set the background to white.
                this.setTextColor (Color.white);

            }

            if (c.equals (Color.white))
            {

                // Set the background to black.
                this.setTextColor (Color.black);

            }

        }
        */
/*
        this.setProperty (new StringProperty (Constants.EDITOR_FONT_BGCOLOR_PROPERTY_NAME,
                                              UIUtils.colorToHex (this.getBackgroundColor ())));
  */
    }

    public void setHighlightWritingLine (boolean v)
    {

        super.setHighlightWritingLine (v);

        this.setProperty (new BooleanProperty (Constants.EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME,
                                               v));

    }

    public void setWritingLineColor (Color c)
    {

        super.setWritingLineColor (c);

        this.setProperty (new StringProperty (Environment.isNightModeEnabled () ? Constants.EDITOR_WRITING_LINE_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME,
                                             UIUtils.colorToHex (this.getWritingLineColor ())));

    }

    public void setTextColor (Color c)
    {

        super.setTextColor (c);

        this.setProperty (new StringProperty (Environment.isNightModeEnabled () ? Constants.EDITOR_FONT_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.EDITOR_FONT_COLOR_PROPERTY_NAME,
                                              UIUtils.colorToHex (this.getTextColor ())));

    }

    public void setLineSpacing (float v)
    {

        super.setLineSpacing (v);

        this.setProperty (new FloatProperty (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME,
                                             this.getLineSpacing ()));

    }

    public void setParagraphSpacing (float v)
    {

        super.setParagraphSpacing (v);

        this.setProperty (new FloatProperty (Constants.EDITOR_PARAGRAPH_SPACING_PROPERTY_NAME,
                                             this.getParagraphSpacing ()));

    }

    public void setFirstLineIndent (boolean v)
    {

        super.setFirstLineIndent (v);

        this.setProperty (new BooleanProperty (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME,
                                               this.getFirstLineIndent ()));

    }

    public void setAlignment (String v)
    {

        super.setAlignment (v);

        this.setProperty (new StringProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME,
                                              this.getAlignment ()));

    }

    public void setFontSize (int v)
    {

        super.setFontSize (v);

        this.setProperty (new IntegerProperty (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME,
                                               this.getFontSize ()));

    }

    public void setFontFamily (String f)
    {

        super.setFontFamily (f);

        this.setProperty (new StringProperty (Constants.EDITOR_FONT_PROPERTY_NAME,
                                              this.getFontFamily ()));

    }

    public void setTextBorder (int v)
    {

        super.setTextBorder (v);

        this.setProperty (new IntegerProperty (Constants.EDITOR_TEXT_BORDER_PROPERTY_NAME,
                                              this.getTextBorder ()));

    }

    private void setProperty (AbstractProperty prop)
    {

        if (!this.allowSet)
        {

            return;

        }

        UserProperties.set (prop.getID (),
                            prop);

    }
/*
    public void resetToDefaults ()
    {

        this.setBackgroundColor (UIUtils.hexToColor (UserProperties.get (Constants.DEFAULT_EDITOR_BGCOLOR_PROPERTY_NAME)));
        this.setTextColor (UIUtils.hexToColor (UserProperties.get (Constants.DEFAULT_EDITOR_FONT_COLOR_PROPERTY_NAME)));
        this.setTextBorder (UserProperties.getAsInt (Constants.DEFAULT_EDITOR_TEXT_BORDER_PROPERTY_NAME));
        this.setFontFamily (UserProperties.get (Constants.DEFAULT_EDITOR_FONT_PROPERTY_NAME));
        this.setFontSize (UserProperties.getAsInt (Constants.DEFAULT_EDITOR_FONT_SIZE_PROPERTY_NAME));
        this.setAlignment (Environment.getDefaultTextAlignment ());
        this.setFirstLineIndent (UserProperties.getAsBoolean (Constants.DEFAULT_EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME));
        this.setLineSpacing (UserProperties.getAsFloat (Constants.DEFAULT_EDITOR_LINE_SPACING_PROPERTY_NAME));
        this.setParagraphSpacing (UserProperties.getAsFloat (Constants.DEFAULT_EDITOR_PARAGRAPH_SPACING_PROPERTY_NAME));
        this.setWritingLineColor (UIUtils.hexToColor (UserProperties.get (Constants.DEFAULT_EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME)));
        this.setHighlightWritingLine (UserProperties.getAsBoolean (Constants.DEFAULT_EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME));

    }
*/
}
