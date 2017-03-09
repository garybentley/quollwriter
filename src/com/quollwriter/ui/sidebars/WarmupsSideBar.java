package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import java.util.List;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ScrollableBox;

public class WarmupsSideBar extends AbstractSideBar<WarmupsViewer>
{

    public static final String ID = "warmups";

    private JTree                  warmupsTree = null;
    private WarmupsAccordionItem   warmupsItem = null;
    private JTextPane prompt = null;
    private Box promptWrapper = null;
    private JComponent content = null;
    private JComponent contentBox = null;
    
    public WarmupsSideBar (WarmupsViewer v)
    {
        
        super (v);

        this.contentBox = new ScrollableBox (BoxLayout.Y_AXIS);
        
        this.contentBox.setOpaque (false);
        this.contentBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.contentBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                       Short.MAX_VALUE));
/*
        b.setBorder (new EmptyBorder (0,
                                      5,
                                      0,
                                      0));
  */
                                          
        this.content = this.wrapInScrollPane (this.contentBox);
        
        this.prompt = UIUtils.createHelpTextPane (this.viewer);
        this.prompt.setBorder (null);
        
    }

    @Override
    public String getId ()
    {
        
        return ID;
        
    }
    
    @Override
    public List<JComponent> getHeaderControls ()
    {
        
        return null;
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public boolean canClose ()
    {
        
        return false;
        
    }
    
    public String getIconType ()
    {
        
        return null;
        
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
        
        return null;
        
    }
    
    @Override
    public void init (String saveState)
               throws GeneralException
    {
        
        super.init (saveState);
        
        this.warmupsItem = new WarmupsAccordionItem (this.viewer);

        this.warmupsItem.setTitle ("Previous {Warmups}");

        this.warmupsItem.init ();
              
        this.promptWrapper = new Box (BoxLayout.Y_AXIS);
        this.promptWrapper.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        com.quollwriter.ui.components.Header h = UIUtils.createBoldSubHeader ("Prompt",
                                                null);
        h.setBorder (UIUtils.createPadding (5, 5, 0, 5));
        h.setFont (h.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));        
        this.promptWrapper.add (h);
        
        this.promptWrapper.add (this.prompt);
                           
        this.prompt.setBorder (UIUtils.createPadding (5, 10, 0, 5));
                               
        this.prompt.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                   300));
                                        
        this.contentBox.add (this.promptWrapper);
                
        this.contentBox.add (this.warmupsItem);
        this.contentBox.setBorder (UIUtils.createPadding (0, 0, 0, 5));
                                
    }

    public void reloadTree ()
    {
        
        this.warmupsItem.update ();
                
    }    
    
    public void panelShown (MainPanelEvent ev)
    {

        this.setObjectSelectedInSidebar (ev.getForObject ());
    
    }    
    
    public void setObjectSelectedInSidebar (DataObject d)
    {
                    
        this.promptWrapper.setVisible (false);

        if (d instanceof Chapter)
        {                    
        
            Warmup w = this.viewer.getWarmupForChapter ((Chapter) d);
            
            this.prompt.setText (UIUtils.formatPrompt (w.getPrompt ()));

            this.promptWrapper.setVisible (true);
            
            this.revalidate ();
            this.repaint ();
            
        }
                    
        this.warmupsItem.clearSelectedItemInTree ();
            
        this.warmupsItem.setObjectSelectedInTree (d);
        
    }
    
    public JComponent getContent ()
    {
        
        return this.content;
                
    }
    
    public JTree getWarmupsTree ()
    {
        
        return this.warmupsItem.getTree ();
        
    }
    
}