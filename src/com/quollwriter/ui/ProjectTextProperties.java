package com.quollwriter.ui;

import java.awt.Color;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.components.TextProperties;
import com.quollwriter.ui.components.QTextEditor;

public class ProjectTextProperties extends TextProperties
{

    private AbstractProjectViewer projectViewer = null;
    private TextPropertiesSideBar sideBar = null;
    
    public ProjectTextProperties (AbstractProjectViewer pv)
    {
        
        this.projectViewer = pv;

        com.gentlyweb.properties.Properties props = Environment.getUserProperties ();
                        
        this.initInternal (props.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME),
                           props.getPropertyAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME),
                           props.getProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME),
                           props.getPropertyAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME),
                           props.getPropertyAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME),
                           Color.black,
                           UIUtils.getComponentColor (),
                           UIUtils.getColor (props.getProperty (Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME)),
                           props.getPropertyAsBoolean (Constants.EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME),
                           props.getPropertyAsInt (Constants.EDITOR_TEXT_BORDER_PROPERTY_NAME));
        
    }

    public ProjectTextProperties (AbstractProjectViewer pv,
                                  TextProperties        props)
    {
        
        this (pv);
                
        this.initInternal (props);
        
    }
    
    public void setSideBar (TextPropertiesSideBar s)
    {
        
        this.sideBar = s;
        
    }
    
    public void setBackgroundColor (Color c)
    {
        
        super.setBackgroundColor (c);
/*
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
        
        this.setProperty (new StringProperty (Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME,
                                             UIUtils.colorToHex (this.getWritingLineColor ())));        
        
    }
    
    public void setTextColor (Color c)
    {
        
        super.setTextColor (c);
/*
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
        */
/*
        this.setProperty (new StringProperty (Constants.EDITOR_FONT_COLOR_PROPERTY_NAME,
                                              UIUtils.colorToHex (this.getTextColor ())));
  */      
    }
    
    public void setLineSpacing (float v)
    {
        
        super.setLineSpacing (v);
        
        this.setProperty (new FloatProperty (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME,
                                             this.getLineSpacing ()));
        
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
        
        com.gentlyweb.properties.Properties props = Environment.getUserProperties ();

        prop.setDescription ("N/A");
        
        props.setProperty (prop.getID (),
                           prop);

        try
        {
        
            Environment.saveUserProperties (props);
        
        } catch (Exception e) {
            
            Environment.logError ("Unable to save user properties",
                                  e);            
            
        }

        this.projectViewer.reinitAllChapterEditors ();

    }
    
}
