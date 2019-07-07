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

    protected TextProperties ()
    {

    }

    public TextProperties (TextStylable setOn)
    {

        this.setOn = setOn;

    }

    public TextProperties (TextProperties props)
    {

        this (props.setOn,
              props.fontFamily,
              props.fontSize,
              props.alignment,
              props.firstLineIndent,
              props.lineSpacing,
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
                           Color        textColor,
                           Color        bgColor,
                           Color        writingLineColor,
                           boolean      highlightWritingLine,
                           int          textBorder)
    {

        this (setOn);

        this.initInternal (fontFamily,
                           fontSize,
                           alignment,
                           firstLineIndent,
                           lineSpacing,
                           textColor,
                           bgColor,
                           writingLineColor,
                           highlightWritingLine,
                           textBorder);

    }

    protected void initInternal (TextProperties props)
    {

        this.setOn = props.setOn;

        this.initInternal (props.fontFamily,
                           props.fontSize,
                           props.alignment,
                           props.firstLineIndent,
                           props.lineSpacing,
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
                                  Color        textColor,
                                  Color        bgColor,
                                  Color        writingLineColor,
                                  boolean      highlightWritingLine,
                                  int          textBorder)
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

        this.setFontFamily (fontFamily);
        //this.fontFamily = fontFamily;
        this.setFontSize (fontSize);
        this.setAlignment (alignment);
        this.setFirstLineIndent (firstLineIndent);
        this.setLineSpacing (lineSpacing);
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

    public void resetToDefaults ()
    {

        if (this.setOn != null)
        {

            //this.setOn.resetToDefaults ();

        }

    }

}
