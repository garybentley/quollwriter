package com.quollwriter.ui.components;

import java.awt.Color;

public interface TextStylable
{

    public void setFontFamily (String f);

    public void setFontSize (int v);

    public void setAlignment (String v);

    public void setFirstLineIndent (boolean v);

    public void setLineSpacing (float v);
    
    public void setTextColor (Color c);
    
    public void setBackgroundColor (Color c);
    
    public void setWritingLineColor (Color c);
    
    public void setHighlightWritingLine (boolean v);
    
    public void setTextBorder (int v);
    
    //public void resetToDefaults ();
    
}
