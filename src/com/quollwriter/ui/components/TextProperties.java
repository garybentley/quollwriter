package com.quollwriter.ui.components;

import java.awt.Color;

public class TextProperties implements TextStylable
{
    
    private TextStylable setOn = null;
    private String fontFamily = null;
    private int fontSize = -1;
    private String alignment = null;
    private boolean firstLineIndent = false;
    private float lineSpacing = 0;
    private Color textColor = null;
    private Color bgColor = null;
    private Color writingLineColor = null;
    private boolean highlightWritingLine = false;
    private int textBorder = 0;
    
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
        
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
        this.alignment = alignment;
        this.firstLineIndent = firstLineIndent;
        this.lineSpacing = lineSpacing;
        this.textColor = textColor;
        this.bgColor = bgColor;
        this.writingLineColor = writingLineColor;
        this.highlightWritingLine = highlightWritingLine;
        this.textBorder = textBorder;
        
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
    
    public void setHighlightWritingLine (boolean v)
    {
        
        this.highlightWritingLine = v;
        
        if (this.setOn != null)
        {
            
            this.setOn.setHighlightWritingLine (v);
            
        }
        
    }
    
    public int getTextBorder ()
    {
        
        return this.textBorder;
        
    }
    
    public void setTextBorder (int v)
    {
        
        this.textBorder = v;
        
        if (this.setOn != null)
        {
            
            this.setOn.setTextBorder (v);
            
        }
        
    }

    public void setWritingLineColor (Color c)
    {
        
        this.writingLineColor = c;
        
        if ((this.setOn != null)
            &&
            (this.writingLineColor != null)
           )
        {
            
            this.setOn.setWritingLineColor (c);
            
        }

    }
    
    public Color getWritingLineColor ()
    {
        
        return this.writingLineColor;
        
    }
    
    public String getFontFamily ()
    {
        
        return this.fontFamily;
        
    }
    
    public void setFontFamily (String f)
    {
        
        this.fontFamily = f;
        
        if ((this.setOn != null)
            &&
            (this.fontFamily != null)
           )
        {
            
            this.setOn.setFontFamily (f);
            
        }
        
    }

    public int getFontSize ()
    {
        
        return this.fontSize;
        
    }
    
    public void setFontSize (int v)
    {
        
        this.fontSize = v;
        
        if ((this.setOn != null)
            &&
            (this.fontSize > 0)
           )
        {
            
            this.setOn.setFontSize (v);
            
        }
        
    }

    public String getAlignment ()
    {
        
        return this.alignment;
        
    }
    
    public void setAlignment (String v)
    {
        
        this.alignment = v;
        
        if ((this.setOn != null)
            &&
            (this.alignment != null)
           )
        {
            
            this.setOn.setAlignment (v);
            
        }
        
    }

    public boolean getFirstLineIndent ()
    {
    
        return this.firstLineIndent;
        
    }
    
    public void setFirstLineIndent (boolean v)
    {
        
        this.firstLineIndent = v;
        
        if (this.setOn != null)
        {
            
            this.setOn.setFirstLineIndent (v);
            
        }
        
    }

    public float getLineSpacing ()
    {
        
        return this.lineSpacing;
        
    }
    
    public void setLineSpacing (float v)
    {
        
        this.lineSpacing = v;
        
        if ((this.setOn != null)
            &&
            (this.lineSpacing > 0)
           )
        {
            
            this.setOn.setLineSpacing (v);
            
        }
        
    }
    
    public Color getTextColor ()
    {
        
        return this.textColor;
        
    }
    
    public void setTextColor (Color c)
    {
        
        this.textColor = c;
        
        if ((this.setOn != null)
            &&
            (this.textColor != null)
           )
        {
            
            this.setOn.setTextColor (c);
            
        }
        
    }
    
    public Color getBackgroundColor ()
    {
        
        return this.bgColor;
        
    }

    public void setBackgroundColor (Color c)
    {
        
        this.bgColor = c;
        
        if ((this.setOn != null)
            &&
            (this.bgColor != null)
           )
        {
            
            this.setOn.setBackgroundColor (c);
            
        }
        
    }
 
    public void resetToDefaults ()
    {
        
        if (this.setOn != null)
        {
            
            //this.setOn.resetToDefaults ();
            
        }
        
    }
    
}