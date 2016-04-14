package com.quollwriter.ui;

import java.awt.Color;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.TextProperties;
import com.quollwriter.ui.components.QTextEditor;

public class FullScreenTextProperties extends TextProperties
{

    private FullScreenFrame fsf = null;
    private FullScreenPropertiesSideBar sideBar = null;
    
    public FullScreenTextProperties (FullScreenFrame fsf)
    {
        
        this.fsf = fsf;
        
        AbstractProjectViewer pv = fsf.getProjectViewer ();
        
        Project proj = pv.getProject ();
        
        this.initInternal (proj.getProperty (Constants.FULL_SCREEN_EDITOR_FONT_PROPERTY_NAME,
                                             Constants.EDITOR_FONT_PROPERTY_NAME),
                           proj.getPropertyAsInt (Constants.FULL_SCREEN_EDITOR_FONT_SIZE_PROPERTY_NAME,
                                                  Constants.EDITOR_FONT_SIZE_PROPERTY_NAME),
                           proj.getProperty (Constants.FULL_SCREEN_EDITOR_ALIGNMENT_PROPERTY_NAME,
                                             Constants.EDITOR_ALIGNMENT_PROPERTY_NAME),
                           proj.getPropertyAsBoolean (Constants.FULL_SCREEN_EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME,
                                                      Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME),
                           proj.getPropertyAsFloat (Constants.FULL_SCREEN_EDITOR_LINE_SPACING_PROPERTY_NAME,
                                                    Constants.EDITOR_LINE_SPACING_PROPERTY_NAME),
                           UIUtils.getColor (proj.getProperty (Constants.FULL_SCREEN_EDITOR_FONT_COLOR_PROPERTY_NAME)),
                           UIUtils.getColor (proj.getProperty (Constants.FULL_SCREEN_EDITOR_FONT_BGCOLOR_PROPERTY_NAME)),
                           UIUtils.getColor (proj.getProperty (Constants.FULL_SCREEN_EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME,
                                                               Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME)),
                           proj.getPropertyAsBoolean (Constants.FULL_SCREEN_EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME,
                                                      Constants.EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME),
                           proj.getPropertyAsInt (Constants.FULL_SCREEN_EDITOR_TEXT_BORDER_PROPERTY_NAME,
                                                  Constants.EDITOR_TEXT_BORDER_PROPERTY_NAME));

    }

    public FullScreenTextProperties (FullScreenFrame fsf,
                                     TextProperties  props)
    {
        
        this (fsf);
                
        this.initInternal (props);
        
    }
    
    public void setSideBar (FullScreenPropertiesSideBar s)
    {
        
        this.sideBar = s;
        
    }
    
    public void setBackgroundColor (Color c)
    {
        
        super.setBackgroundColor (c);

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
        
        this.setProperty (new StringProperty (Constants.FULL_SCREEN_EDITOR_FONT_BGCOLOR_PROPERTY_NAME,
                                              UIUtils.colorToHex (this.getBackgroundColor ())));
        
    }
    
    public void setTextColor (Color c)
    {
        
        super.setTextColor (c);

        AbstractEditorPanel aep = (AbstractEditorPanel) this.fsf.getPanel ().getChild ();

        QTextEditor editor = aep.getEditor ();
        
        if (c.equals (this.getBackgroundColor ()))
        {

            if (c.equals (Color.black))
            {

                // Set the background to white.
                aep.restoreBackgroundColor ();

                this.setBackgroundColor (Color.white);
                
            }

            if (c.equals (Color.white))
            {

                // Set the background to black.
                this.setBackgroundColor (Color.black);

            }

        }
        
        UserProperties.set (Constants.FULL_SCREEN_EDITOR_FONT_COLOR_PROPERTY_NAME,
                            UIUtils.colorToHex (this.getTextColor ()));
        
    }
    
    public void setWritingLineColor (Color c)
    {

        super.setWritingLineColor (c);

        UserProperties.set (Constants.FULL_SCREEN_EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME,
                            UIUtils.colorToHex (this.getWritingLineColor ()));    
    
    }    
 
    public void setHighlightWritingLine (boolean v)
    {

        super.setHighlightWritingLine (v);

        UserProperties.set (Constants.FULL_SCREEN_EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME,
                            this.isHighlightWritingLine ());    
    
    }
    
    public void setLineSpacing (float v)
    {
        
        super.setLineSpacing (v);
        
        UserProperties.set (Constants.FULL_SCREEN_EDITOR_LINE_SPACING_PROPERTY_NAME,
                            this.getLineSpacing ());
            
    }
    
    public void setFirstLineIndent (boolean v)
    {
        
        super.setFirstLineIndent (v);
        
        UserProperties.set (Constants.FULL_SCREEN_EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME,
                            this.getFirstLineIndent ());
        
    }
    
    public void setAlignment (String v)
    {
        
        super.setAlignment (v);

        UserProperties.set (Constants.FULL_SCREEN_EDITOR_ALIGNMENT_PROPERTY_NAME,
                            this.getAlignment ());
        
    }
    
    public void setFontSize (int v)
    {
        
        super.setFontSize (v);
        
        UserProperties.set (Constants.FULL_SCREEN_EDITOR_FONT_SIZE_PROPERTY_NAME,
                            this.getFontSize ());
                
    }
    
    public void setTextBorder (int v)
    {
        
        super.setTextBorder (v);
        
        UserProperties.set (Constants.FULL_SCREEN_EDITOR_TEXT_BORDER_PROPERTY_NAME,
                            this.getTextBorder ());
                
    }

    public void setFontFamily (String f)
    {
    
        super.setFontFamily (f);
        
        UserProperties.set (Constants.FULL_SCREEN_EDITOR_FONT_PROPERTY_NAME,
                            this.getFontFamily ());
        
    }
    
    private void setProperty (AbstractProperty prop)
    {

        UserProperties.set (prop.getID (),
                            prop);
    
    }

    public void resetToDefaults ()
    {

        this.fsf.resetPropertiesToDefaults ();
            
    }
    
}
