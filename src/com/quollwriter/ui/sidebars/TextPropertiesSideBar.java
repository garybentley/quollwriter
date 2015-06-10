package com.quollwriter.ui.sidebars;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.Component;
import java.awt.Image;
import java.awt.RenderingHints;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.*;

import java.awt.event.*;

import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.*;

import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.TextProperties;

public class TextPropertiesSideBar extends AbstractSideBar implements MainPanelListener
{
    
    private TextPropertiesEditPanel props = null;
    
    public TextPropertiesSideBar (AbstractProjectViewer pv,
                                  PopupsSupported       popupParent,
                                  TextProperties        props)
    {
        
        super (pv);
                     
        this.props = new TextPropertiesEditPanel (pv,
                                                  props,
                                                  null,
                                                  false,
                                                  popupParent);
                        
    }

    public Dimension getMinimumSize ()
    {
        
        return this.getPreferredSize ();        
        
    }    
    
    public List<JButton> getHeaderControls ()
    {
        
        List<JButton> buts = new ArrayList ();
        
        return buts;
        
    }
    
    public boolean removeOnClose ()
    {
        
        return true;
        
    }
    
    public boolean canClose ()
    {
        
        return true;
        
    }
    
    public String getIconType ()
    {
        
        return Constants.SETTINGS_ICON_NAME;
        
    }
    
    @Override
    public void onShow ()
    {
        
    }
    
    @Override
    public void onHide ()
    {
        
    }
    
    public void onClose ()
    {
        
    }
    
    public String getTitle ()
    {
        
        return "Text Properties";
        
    }
    
    public void init ()
               throws GeneralException
    {
        
        super.init ();
                
    }

    public void panelShown (MainPanelEvent ev)
    {
                
    }
    
    public JComponent getContent ()
    {

        this.props.init ();
    
        return this.wrapInScrollPane (this.props);
        
    }
    
}