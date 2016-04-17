package com.quollwriter.ui.sidebars;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.ActionAdapter;

public class ChapterInformationSideBar extends AccordionItemsSideBar<ProjectViewer>
{
    
    private Chapter chapter = null;
    
    private ChapterGoalsAccordionItem goals = null;
    private ChapterDescriptionAccordionItem desc = null;
    private ChapterPlanAccordionItem plan = null;
    private LinkedToAccordionItem linkedTo = null;
    
    public ChapterInformationSideBar (ProjectViewer v,
                                      Chapter       c)
    {
        
        super (v,
               null);
        
        this.chapter = c;
        
        this.setMinimumSize (new Dimension (300,
                                            250));
        /*
        this.setPreferredSize (new Dimension (300,
                                              250));
        */
    }

    public Dimension getMinimumSize ()
    {
        
        return this.getPreferredSize ();        
        
    }    
    
    public Dimension getPreferredSize ()
    {
        
        return new Dimension (300,
                              500);
        
    }
    
    public void init ()
               throws GeneralException
    {
        
        super.init ();
        
        this.update (this.chapter);        
        
        //this.scrollVerticalTo (0);
        
    }
    
    // TODO: Change so that each chapter has its own information sidebar
    public void panelShown (MainPanelEvent ev)
    {

        if (ev.getPanel () instanceof AbstractEditorPanel)
        {
            
            AbstractEditorPanel p = (AbstractEditorPanel) ev.getPanel ();

            this.update (p.getChapter ());
                        
        }

    }
        
    private void update (Chapter c)
    {

        this.chapter = c;
        
        this.desc.update (this.chapter);
        
        this.goals.update (this.chapter);
        
        this.plan.update (this.chapter);
        
        this.linkedTo.update (this.chapter);
                    
        this.setTitle (this.chapter.getName ());

        this.scrollVerticalTo (0);
        
    }
    
    public List<AccordionItem> getItems ()
    {
        
        List<AccordionItem> items = new ArrayList ();

        this.desc = new ChapterDescriptionAccordionItem (this.viewer,
                                                         this.chapter);
        
        this.goals = new ChapterGoalsAccordionItem (this.viewer,
                                                    this.chapter);

        this.plan = new ChapterPlanAccordionItem (this.viewer,
                                                  this.chapter);
                                
        this.linkedTo = new LinkedToAccordionItem (this.viewer,
                                                   this.chapter);
                                                   
        items.add (this.desc);
                                                   
        items.add (this.goals);
        
        items.add (this.plan);
        
        items.add (this.linkedTo);
                
        return items;
        
    }
        
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public String getTitle ()
    {
        
        return this.chapter.getName ();
        
    }
    
    public String getIconType ()
    {
        
        return Constants.CHAPTER_INFO_ICON_NAME;
        
    }
    
    @Override
    public List<JComponent> getHeaderControls ()
    {
        
        // Change chapter.        
        List<JComponent> buts = new ArrayList ();        

        return buts;        
                
    }
                
    public String getSaveState ()
    {
        
        StringBuilder ats = new StringBuilder ();

        List<String> its = new ArrayList ();

        if (this.desc.isContentVisible ())
        {
            
            its.add ("desc");
            
        }
        
        if (this.goals.isContentVisible ())
        {
            
            its.add ("goals");
            
        }

        if (this.plan.isContentVisible ())
        {
            
            its.add ("plan");
            
        }

        if (this.linkedTo.isContentVisible ())
        {
            
            its.add ("linkedto");
            
        }
        
        for (String i : its)
        {
            
            if (ats.length () > 0)
            {

                ats.append ("|");

            }

            ats.append (i);

        }

        return ats.toString ();        
        
    }
      
}