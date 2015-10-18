package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Font;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.jdom.*;

import com.gentlyweb.xml.*;
import com.gentlyweb.properties.*;

//import wordcram.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.whatsnewcomps.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ActionAdapter;

public class WordCloudPanel extends QuollPanel
{
    
    public static final String PANEL_ID = "wordcloud";
    
    public WordCloudPanel (AbstractProjectViewer pv)
                           throws                Exception
    {
        
        super (pv,
               null);
/*
 *For a future version
        Header h = UIUtils.createHeader ("Your Word Cloud",
                                         Constants.PANEL_TITLE,
                                         Constants.INFO_ICON_NAME,
                                         null);

        this.add (h);

        QuollPanel qp = pv.getCurrentlyVisibleTab ();
        
        String text = null;
        
        if (qp instanceof QuollEditorPanel)
        {
            
            text = ((QuollEditorPanel) qp).getEditor ().getText ();
            
        }        
        
        final WordCram wc = new WordCram ();
        
        wc.withDimensions (new Dimension (750, 750));
        wc.fromText (text);
        wc.sizedByWeight(30, 120);
        wc.allowWordsWithinWords (false);
        wc.withWordPadding (new Insets (1, 1, 1, 1));
        wc.withFont(new Font ("Futura",Font.BOLD, 1));

        WordsList words = wc.layout ();

        final WordCramPanel wcp = new WordCramPanel (words,
                                                     new DefaultRenderer (Painters.alwaysUse (UIUtils.getColor ("#DBC900"))));
        this.add (wcp);
  */                                      
    }

    public String getPanelId ()
    {

        return PANEL_ID;
    
    }
    
    @Override
    public void close ()
    {
        
    }

    public void init ()
    {

    }

    public void getState (Map<String, Object> s)
    {
        
    }

    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {

        this.setReadyForUse (true);
        
    }

    public boolean saveUnsavedChanges ()
                                       throws Exception
    {
        
        return false;
        
    }

    public String getIconType ()
    {
        
        return Constants.INFO_ICON_NAME;
        
    }

    public String getTitle ()
    {
        
        return "Word Cloud";
        
    }
    
    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {
                
    }
    
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {
        
    }

    public List<Component> getTopLevelComponents ()
    {
        
        return new ArrayList ();
        
    }

    public <T extends NamedObject> void refresh (T n)
    {
        
    }
        
}