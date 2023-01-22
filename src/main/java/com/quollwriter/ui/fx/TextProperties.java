package com.quollwriter.ui.fx;

import javafx.beans.property.*;
import javafx.scene.paint.*;

public class TextProperties implements TextStylable
{

    private TextStylable setOn = null;
    private String fontFamily = null;
    private StringProperty fontFamilyProp = null;
    private int fontSize = -1;
    private IntegerProperty fontSizeProp = null;
    private String alignment = null;
    private StringProperty alignmentProp = null;
    private boolean firstLineIndent = false;
    private BooleanProperty firstLineIndentProp = null;
    private float lineSpacing = 0;
    private FloatProperty lineSpacingProp = null;
    private Color textColor = null;
    private ObjectProperty<Color> textColorProp = null;
    private Color bgColor = null;
    private ObjectProperty<Color> bgColorProp = null;
    private Color writingLineColor = null;
    private ObjectProperty<Color> writingLineColorProp = null;
    private boolean highlightWritingLine = false;
    private BooleanProperty highlightWritingLineProp = null;
    private int textBorder = 0;
    private IntegerProperty textBorderProp = null;

    private FloatProperty paragraphSpacingProp = null;

    public TextProperties ()
    {

        this.fontFamilyProp = new SimpleStringProperty ();
        this.fontSizeProp = new SimpleIntegerProperty ();
        this.alignmentProp = new SimpleStringProperty ();
        this.firstLineIndentProp = new SimpleBooleanProperty ();
        this.lineSpacingProp = new SimpleFloatProperty ();
        this.textColorProp = new SimpleObjectProperty<> ();
        this.bgColorProp = new SimpleObjectProperty<> ();
        this.writingLineColorProp = new SimpleObjectProperty<> ();
        this.highlightWritingLineProp = new SimpleBooleanProperty ();
        this.textBorderProp = new SimpleIntegerProperty ();
        this.paragraphSpacingProp = new SimpleFloatProperty (1f);

    }
/*
    public TextProperties (TextStylable setOn)
    {

        this.setOn = setOn;

    }
*/
    public TextProperties (TextProperties props)
    {

        this (props.setOn,
              props.fontFamily,
              props.fontSize,
              props.alignment,
              props.firstLineIndent,
              props.lineSpacing,
              props.paragraphSpacingProp.getValue (),
              props.textColor,
              props.bgColor,
              props.writingLineColor,
              props.highlightWritingLine,
              props.textBorder);

    }

    public TextProperties (TextStylable setOn,
                           String       fontFamily,
                           int          fontSize,
                           String       alignment,
                           boolean      firstLineIndent,
                           float        lineSpacing,
                           float        paraSpacing,
                           Color        writingLineColor,
                           boolean      highlightWritingLine,
                           int          textBorder)
    {

        this (setOn,
              fontFamily,
              fontSize,
              alignment,
              firstLineIndent,
              lineSpacing,
              paraSpacing,
              null,
              null,
              writingLineColor,
              highlightWritingLine,
              textBorder);

    }

    public TextProperties (TextStylable setOn,
                           String       fontFamily,
                           int          fontSize,
                           String       alignment,
                           boolean      firstLineIndent,
                           float        lineSpacing,
                           float        paraSpacing,
                           Color        textColor,
                           Color        bgColor,
                           Color        writingLineColor,
                           boolean      highlightWritingLine,
                           int          textBorder)
    {

        //this (setOn);

        this ();

        this.initInternal (fontFamily,
                           fontSize,
                           alignment,
                           firstLineIndent,
                           lineSpacing,
                           paraSpacing,
                           textColor,
                           bgColor,
                           writingLineColor,
                           highlightWritingLine,
                           textBorder);

    }

    protected void initInternal (TextProperties props)
    {

        //this.setOn = props.setOn;

        this.initInternal (props.fontFamily,
                           props.fontSize,
                           props.alignment,
                           props.firstLineIndent,
                           props.lineSpacing,
                           props.paragraphSpacingProp.getValue (),
                           props.textColor,
                           props.bgColor,
                           props.writingLineColor,
                           props.highlightWritingLine,
                           props.textBorder);

    }

    protected void initInternal (String       fontFamily,
                                  int          fontSize,
                                  String       alignment,
                                  boolean      firstLineIndent,
                                  float        lineSpacing,
                                  float        paraSpacing,
                                  Color        textColor,
                                  Color        bgColor,
                                  Color        writingLineColor,
                                  boolean      highlightWritingLine,
                                  int          textBorder)
    {

        this.setFontFamily (fontFamily);
        //this.fontFamily = fontFamily;
        this.setFontSize (fontSize);
        this.setAlignment (alignment);
        this.setFirstLineIndent (firstLineIndent);
        this.setLineSpacing (lineSpacing);
        this.setParagraphSpacing (paraSpacing);
        this.setTextColor (textColor);
        this.setBackgroundColor (bgColor);
        this.setWritingLineColor (writingLineColor);
        this.setHighlightWritingLine (highlightWritingLine);
        this.setTextBorder (textBorder);

    }

    public void setOn (TextStylable s,
                       boolean      init)
    {

        this.setOn = s;

        if (init)
        {

            this.setFontFamily (this.fontFamily);
            this.setFontSize (this.fontSize);
            this.setAlignment (this.alignment);
            this.setFirstLineIndent (this.firstLineIndent);
            this.setLineSpacing (this.lineSpacing);
            this.setParagraphSpacing (this.paragraphSpacingProp.getValue ());
            this.setTextColor (this.textColor);
            this.setBackgroundColor (this.bgColor);
            this.setWritingLineColor (this.writingLineColor);
            this.setHighlightWritingLine (this.highlightWritingLine);
            this.setTextBorder (this.textBorder);

        }

    }

    public boolean isHighlightWritingLine ()
    {

        return this.highlightWritingLine;

    }

    public BooleanProperty highlightWritingLineProperty ()
    {

        return this.highlightWritingLineProp;

    }

    public void setHighlightWritingLine (boolean v)
    {

        this.highlightWritingLine = v;
        this.highlightWritingLineProp.setValue (v);

/*
TODO Remove?
        if (this.setOn != null)
        {

            this.setOn.setHighlightWritingLine (v);

        }
*/
    }

    public int getTextBorder ()
    {

        return this.textBorder;

    }

    public IntegerProperty textBorderProperty ()
    {

        return this.textBorderProp;

    }

    public void setTextBorder (int v)
    {

        this.textBorder = v;
        this.textBorderProp.setValue (v);

/*
TODO Remove?
        if (this.setOn != null)
        {

            this.setOn.setTextBorder (v);

        }
*/
    }

    public ObjectProperty<Color> writingLineColorProperty ()
    {

        return this.writingLineColorProp;

    }

    public void setWritingLineColor (Color c)
    {

        this.writingLineColor = c;
        this.writingLineColorProp.setValue (c);

/*
TODO Remove?
        if ((this.setOn != null)
            &&
            (this.writingLineColor != null)
           )
        {

            this.setOn.setWritingLineColor (c);

        }
*/
    }

    public Color getWritingLineColor ()
    {

        return this.writingLineColor;

    }

    public String getFontFamily ()
    {

        return this.fontFamily;

    }

    public StringProperty fontFamilyProperty ()
    {

        return this.fontFamilyProp;

    }

    public void setFontFamily (String f)
    {

        this.fontFamily = f;
        this.fontFamilyProp.setValue (f);

/*
TODO REmove?
        if ((this.setOn != null)
            &&
            (this.fontFamily != null)
           )
        {

            this.setOn.setFontFamily (f);

        }
*/
    }

    public int getFontSize ()
    {

        return this.fontSize;

    }

    public IntegerProperty fontSizeProperty ()
    {

        return this.fontSizeProp;

    }

    public void setFontSize (int v)
    {

        this.fontSize = v;
        this.fontSizeProp.setValue (v);

/*
TODO Remove
        if ((this.setOn != null)
            &&
            (this.fontSize > 0)
           )
        {

            this.setOn.setFontSize (v);

        }
*/
    }

    public String getAlignment ()
    {

        return this.alignment;

    }

    public StringProperty alignmentProperty ()
    {

        return this.alignmentProp;

    }

    public void setAlignment (String v)
    {

        this.alignment = v;
        this.alignmentProp.setValue (v);
/*
TODO Remove
        if ((this.setOn != null)
            &&
            (this.alignment != null)
           )
        {

            this.setOn.setAlignment (v);

        }
*/
    }

    public boolean getFirstLineIndent ()
    {

        return this.firstLineIndent;

    }

    public BooleanProperty firstLineIndentProperty ()
    {

        return this.firstLineIndentProp;

    }

    public void setFirstLineIndent (boolean v)
    {

        this.firstLineIndent = v;
        this.firstLineIndentProp.setValue (v);

/*
TODO Remove
        if (this.setOn != null)
        {

            this.setOn.setFirstLineIndent (v);

        }
*/
    }

    public float getLineSpacing ()
    {

        return this.lineSpacing;

    }

    public FloatProperty lineSpacingProperty ()
    {

        return this.lineSpacingProp;

    }

    public void setLineSpacing (float v)
    {

        this.lineSpacing = v;
        this.lineSpacingProp.setValue (v);
/*
TODO Remove
        if ((this.setOn != null)
            &&
            (this.lineSpacing > 0)
           )
        {

            this.setOn.setLineSpacing (v);

        }
*/
    }

    public float getParagraphSpacing ()
    {

        return this.paragraphSpacingProp.getValue ();

    }

    public FloatProperty paragraphSpacingProperty ()
    {

        return this.paragraphSpacingProp;

    }

    public void setParagraphSpacing (float v)
    {

        this.paragraphSpacingProp.setValue (v);

    }

    public Color getTextColor ()
    {

        return this.textColor;

    }

    public ObjectProperty<Color> textColorProperty ()
    {

        return this.textColorProp;

    }

    public void setTextColor (Color c)
    {

        this.textColor = c;
        this.textColorProp.setValue (c);
/*
TODO REmove
        if ((this.setOn != null)
            &&
            (this.textColor != null)
           )
        {

            this.setOn.setTextColor (c);

        }
*/
    }

    public Color getBackgroundColor ()
    {

        return this.bgColor;

    }

    public ObjectProperty<Color> backgroundColorProperty ()
    {

        return this.bgColorProp;

    }

    public void setBackgroundColor (Color c)
    {

        this.bgColor = c;
        this.bgColorProp.setValue (c);

/*
TODO Remove
        if ((this.setOn != null)
            &&
            (this.bgColor != null)
           )
        {

            this.setOn.setBackgroundColor (c);

        }
*/
    }
/*
    public void resetToDefaults ()
    {

        if (this.setOn != null)
        {

            //this.setOn.resetToDefaults ();

        }

    }
*/
    public void unbindAll ()
    {

        this.fontFamilyProp.unbind ();
        this.fontSizeProp.unbind ();
        this.alignmentProp.unbind ();
        this.firstLineIndentProp.unbind ();
        this.lineSpacingProp.unbind ();
        this.textColorProp.unbind ();
        this.bgColorProp.unbind ();
        this.writingLineColorProp.unbind ();
        this.highlightWritingLineProp.unbind ();
        this.textBorderProp.unbind ();
        this.paragraphSpacingProp.unbind ();

    }

    /**
     * Binds the properties in these properties to the properties in the passed in value.  The bind is one way, "this" properties do not
     * update the the passed in properties.
     *
     * So: this.lineSpacing gets its value from props.lineSpacing.
     */
    public void bindTo (TextProperties props)
    {

        this.unbindAll ();
        this.fontFamilyProp.bind (props.fontFamilyProperty ());

        this.fontFamilyProp.addListener ((pr, oldv, newv) ->
        {

            this.fontFamily = newv;

        });

        this.fontFamily = props.getFontFamily ();

        this.fontSizeProp.bind (props.fontSizeProperty ());

        this.fontSizeProp.addListener ((pr, oldv, newv) ->
        {

            this.fontSize = newv.intValue ();

        });

        this.fontSize = props.getFontSize ();

        this.alignmentProp.bind (props.alignmentProperty ());

        this.alignmentProp.addListener ((pr, oldv, newv) ->
        {

            this.alignment = newv;

        });
        this.alignment = props.getAlignment ();

        this.firstLineIndentProp.bind (props.firstLineIndentProperty ());

        this.firstLineIndentProp.addListener ((pr, oldv, newv) ->
        {

            this.firstLineIndent = newv;

        });
        this.firstLineIndent = props.getFirstLineIndent ();

        this.lineSpacingProp.bind (props.lineSpacingProperty ());

        this.lineSpacingProp.addListener ((pr, oldv, newv) ->
        {

            this.lineSpacing = newv.floatValue ();

        });
        this.lineSpacing = props.getLineSpacing ();

        this.paragraphSpacingProp.bind (props.paragraphSpacingProperty ());
/*
        this.pargraphSpacingProp.addListener ((pr, oldv, newv) ->
        {

            this.lineSpacing = newv.floatValue ();

        });
        this.lineSpacing = props.getLineSpacing ();
*/
        this.textColorProp.bind (props.textColorProperty ());

        this.textColorProp.addListener ((pr, oldv, newv) ->
        {

            this.textColor = newv;

        });
        this.textColor = props.getTextColor ();

        this.bgColorProp.bind (props.backgroundColorProperty ());

        this.bgColorProp.addListener ((pr, oldv, newv) ->
        {

            this.bgColor = newv;

        });
        this.bgColor = props.getBackgroundColor ();

        this.writingLineColorProp.bind (props.writingLineColorProperty ());

        this.writingLineColorProp.addListener ((pr, oldv, newv) ->
        {

            this.writingLineColor = newv;

        });
        this.writingLineColor = props.getWritingLineColor ();

        this.highlightWritingLineProp.bind (props.highlightWritingLineProperty ());

        this.highlightWritingLineProp.addListener ((pr, oldv, newv) ->
        {

            this.highlightWritingLine = newv;

        });
        this.highlightWritingLine = props.isHighlightWritingLine ();

        this.textBorderProp.bind (props.textBorderProperty ());

        this.textBorderProp.addListener ((pr, oldv, newv) ->
        {

            this.textBorder = newv.intValue ();

        });
        this.textBorder = props.getTextBorder ();

    }

}
