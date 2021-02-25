package com.quollwriter.ui.fx;

import javafx.scene.paint.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.data.*;

public class FullScreenTextProperties extends TextProperties implements UserPropertySetter
{

    private boolean allowSet = true;

    public FullScreenTextProperties ()
    {

        boolean v = Environment.isNightModeEnabled ();

        String fontColor = null;
        String bgColor = null;
        String lineColor = null;

        if (v)
        {

            fontColor = UserProperties.get (Constants.FULL_SCREEN_EDITOR_FONT_COLOR_NIGHT_MODE_PROPERTY_NAME);

            if (fontColor == null)
            {

                fontColor = UserProperties.get (Constants.EDITOR_FONT_COLOR_NIGHT_MODE_PROPERTY_NAME);

            }

            bgColor = UserProperties.get (Constants.FULL_SCREEN_EDITOR_BGCOLOR_NIGHT_MODE_PROPERTY_NAME);

            if (bgColor == null)
            {

                bgColor = UserProperties.get (Constants.EDITOR_BGCOLOR_NIGHT_MODE_PROPERTY_NAME);

            }

            lineColor = UserProperties.get (Constants.FULL_SCREEN_EDITOR_WRITING_LINE_COLOR_NIGHT_MODE_PROPERTY_NAME);

            if (lineColor == null)
            {

                lineColor = UserProperties.get (Constants.EDITOR_WRITING_LINE_COLOR_NIGHT_MODE_PROPERTY_NAME);

            }

        }

        if (fontColor == null)
        {

            fontColor = UserProperties.get (Constants.FULL_SCREEN_EDITOR_FONT_COLOR_PROPERTY_NAME,
                                            Constants.EDITOR_FONT_COLOR_PROPERTY_NAME);

        }

        if (bgColor == null)
        {

            bgColor = UserProperties.get (Constants.FULL_SCREEN_EDITOR_BGCOLOR_PROPERTY_NAME,
                                          Constants.EDITOR_BGCOLOR_PROPERTY_NAME);

        }

        if (lineColor == null)
        {

            lineColor = UserProperties.get (Constants.FULL_SCREEN_EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME,
                                            Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME);

        }

        this.initInternal (UserProperties.get (Constants.FULL_SCREEN_EDITOR_FONT_PROPERTY_NAME,
                                               Constants.EDITOR_FONT_PROPERTY_NAME),
                           UserProperties.getAsInt (Constants.FULL_SCREEN_EDITOR_FONT_SIZE_PROPERTY_NAME,
                                                    Constants.EDITOR_FONT_SIZE_PROPERTY_NAME),
                           UserProperties.get (Constants.FULL_SCREEN_EDITOR_ALIGNMENT_PROPERTY_NAME,
                                               Constants.EDITOR_ALIGNMENT_PROPERTY_NAME),
                           UserProperties.getAsBoolean (Constants.FULL_SCREEN_EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME,
                                                        Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME),
                           UserProperties.getAsFloat (Constants.FULL_SCREEN_EDITOR_LINE_SPACING_PROPERTY_NAME,
                                                      Constants.EDITOR_LINE_SPACING_PROPERTY_NAME),
                           UserProperties.getAsFloat (Constants.FULL_SCREEN_EDITOR_PARAGRAPH_SPACING_PROPERTY_NAME,
                                                      Constants.EDITOR_PARAGRAPH_SPACING_PROPERTY_NAME),
                           UIUtils.hexToColor (fontColor),
                           UIUtils.hexToColor (bgColor),
                           UIUtils.hexToColor (lineColor),
                           UserProperties.getAsBoolean (Constants.FULL_SCREEN_EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME,
                                                        Constants.EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME),
                           UserProperties.getAsInt (Constants.FULL_SCREEN_EDITOR_TEXT_BORDER_PROPERTY_NAME,
                                                    Constants.EDITOR_TEXT_BORDER_PROPERTY_NAME)
                          );

        Environment.nightModeProperty ().addListener ((pr, oldv, newv) ->
        {

            String c = UserProperties.get (newv ? Constants.FULL_SCREEN_EDITOR_FONT_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.FULL_SCREEN_EDITOR_FONT_COLOR_PROPERTY_NAME);

            if (c == null)
            {

                c = UserProperties.get (Constants.FULL_SCREEN_EDITOR_FONT_COLOR_PROPERTY_NAME,
                                        Constants.EDITOR_FONT_COLOR_PROPERTY_NAME);

            }

            this.setTextColor (UIUtils.hexToColor (c));

            c = UserProperties.get (newv ? Constants.FULL_SCREEN_EDITOR_BGCOLOR_NIGHT_MODE_PROPERTY_NAME : Constants.FULL_SCREEN_EDITOR_BGCOLOR_PROPERTY_NAME);

            if (c == null)
            {

                c = UserProperties.get (Constants.FULL_SCREEN_EDITOR_BGCOLOR_PROPERTY_NAME,
                                        Constants.EDITOR_BGCOLOR_PROPERTY_NAME);

            }

            this.setBackgroundColor (UIUtils.hexToColor (c));

            c = UserProperties.get (newv ? Constants.FULL_SCREEN_EDITOR_WRITING_LINE_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.FULL_SCREEN_EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME);

            if (c == null)
            {

                c = UserProperties.get (Constants.FULL_SCREEN_EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME,
                                        Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME);

            }

            this.setWritingLineColor (UIUtils.hexToColor (c));

        });

    }

    public FullScreenTextProperties (TextProperties  props)
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
/*
        if (c.equals (this.getTextColor ()))
        {

            if (c.equals (Color.BLACK))
            {

                // Set the background to white.
                this.setTextColor (Color.WHITE);

            }

            if (c.equals (Color.WHITE))
            {

                // Set the background to black.
                this.setTextColor (Color.BLACK);

            }

        }
*/
        this.setProperty (new StringProperty (Environment.isNightModeEnabled () ? Constants.FULL_SCREEN_EDITOR_BGCOLOR_NIGHT_MODE_PROPERTY_NAME : Constants.FULL_SCREEN_EDITOR_BGCOLOR_PROPERTY_NAME,
                                              UIUtils.colorToHex (this.getBackgroundColor ())));

    }

    public void setTextColor (Color c)
    {

        super.setTextColor (c);
/*
        if (c.equals (this.getBackgroundColor ()))
        {

            if (c.equals (Color.BLACK))
            {

                this.setBackgroundColor (Color.WHITE);

            }

            if (c.equals (Color.WHITE))
            {

                // Set the background to black.
                this.setBackgroundColor (Color.BLACK);

            }

        }
*/
        this.setProperty (new StringProperty (Environment.isNightModeEnabled () ? Constants.FULL_SCREEN_EDITOR_FONT_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.FULL_SCREEN_EDITOR_FONT_COLOR_PROPERTY_NAME,
                                              UIUtils.colorToHex (this.getTextColor ())));

    }

    public void setWritingLineColor (Color c)
    {

        super.setWritingLineColor (c);

        this.setProperty (new StringProperty (Environment.isNightModeEnabled () ? Constants.FULL_SCREEN_EDITOR_WRITING_LINE_COLOR_NIGHT_MODE_PROPERTY_NAME : Constants.FULL_SCREEN_EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME,
                                             UIUtils.colorToHex (this.getWritingLineColor ())));

    }

    public void setHighlightWritingLine (boolean v)
    {

        super.setHighlightWritingLine (v);

        this.setProperty (new BooleanProperty (Constants.FULL_SCREEN_EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME,
                                               v));

    }

    public void setLineSpacing (float v)
    {

        super.setLineSpacing (v);

        this.setProperty (new FloatProperty (Constants.FULL_SCREEN_EDITOR_LINE_SPACING_PROPERTY_NAME,
                                             this.getLineSpacing ()));

    }

    public void setParagraphSpacing (float v)
    {

        super.setParagraphSpacing (v);

        this.setProperty (new FloatProperty (Constants.FULL_SCREEN_EDITOR_PARAGRAPH_SPACING_PROPERTY_NAME,
                                             this.getLineSpacing ()));

    }

    public void setFirstLineIndent (boolean v)
    {

        super.setFirstLineIndent (v);

        this.setProperty (new BooleanProperty (Constants.FULL_SCREEN_EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME,
                                               this.getFirstLineIndent ()));

    }

    public void setAlignment (String v)
    {

        super.setAlignment (v);

        this.setProperty (new StringProperty (Constants.FULL_SCREEN_EDITOR_ALIGNMENT_PROPERTY_NAME,
                                              this.getAlignment ()));

    }

    public void setFontSize (int v)
    {

        super.setFontSize (v);

        this.setProperty (new IntegerProperty (Constants.FULL_SCREEN_EDITOR_FONT_SIZE_PROPERTY_NAME,
                                               this.getFontSize ()));

    }

    public void setTextBorder (int v)
    {

        super.setTextBorder (v);

        this.setProperty (new IntegerProperty (Constants.FULL_SCREEN_EDITOR_TEXT_BORDER_PROPERTY_NAME,
                                              this.getTextBorder ()));

    }

    public void setFontFamily (String f)
    {

        super.setFontFamily (f);

        this.setProperty (new StringProperty (Constants.FULL_SCREEN_EDITOR_FONT_PROPERTY_NAME,
                                              this.getFontFamily ()));

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
        this.setWritingLineColor (UIUtils.hexToColor (UserProperties.get (Constants.DEFAULT_EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME)));
        this.setHighlightWritingLine (UserProperties.getAsBoolean (Constants.DEFAULT_EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME));

    }
*/
}
