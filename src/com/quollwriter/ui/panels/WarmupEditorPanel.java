package com.quollwriter.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QTextEditor;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;


public class WarmupEditorPanel extends AbstractEditorPanel
{

     public static final String CONVERT_TO_PROJECT_ACTION_NAME = "convert-to-project";

    protected WarmupsViewer projectViewer = null;
    private Warmup          warmup = null;
    private JTextPane       promptPreview = null;
    private Box             header = null;

    public WarmupEditorPanel(WarmupsViewer pv,
                             Warmup        w)
                      throws GeneralException
    {

        super (pv,
               w.getChapter ());

        this.projectViewer = pv;
        this.warmup = w;

        this.actions.put (CONVERT_TO_PROJECT_ACTION_NAME,
                          this.projectViewer.getAction (WarmupsViewer.CONVERT_TO_PROJECT_ACTION,
                                                        this.warmup.getChapter ()));
    }

    public void setFontColor (Color c)
    {

        super.setFontColor (c);

        this.promptPreview.setText (UIUtils.getWithHTMLStyleSheet (this.promptPreview,
                                                                   UIUtils.formatPrompt (this.warmup.getPrompt ()),
                                                                   UIUtils.colorToHex (c),
                                                                   UIUtils.colorToHex (c)));

    }

    public Warmup getWarmup ()
    {
      
         return this.warmup;
      
    }
    
    public void restoreFontColor ()
    {

        super.restoreFontColor ();

        this.promptPreview.setText (UIUtils.getWithHTMLStyleSheet (this.promptPreview,
                                                                   UIUtils.formatPrompt (this.warmup.getPrompt ())));

    }

    public void restoreBackgroundColor ()
    {

        super.restoreBackgroundColor ();

        this.promptPreview.setBackground (Color.white);

        this.header.setBackground (Color.white);

    }

    public void setBackgroundColor (Color c)
    {

        super.setBackgroundColor (c);

        this.header.setBackground (c);
        this.promptPreview.setBackground (c);

    }

    public JComponent getEditorWrapper (final QTextEditor q)
    {

        FormLayout fl = new FormLayout ("fill:200px:grow",
                                        "fill:p:grow");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        builder.add (q,
                     cc.xy (1,
                            1));

        JPanel p = builder.getPanel ();
        p.setBorder (null);

        return p;

    }

    public void doFillToolsPopupMenu (ActionEvent ev,
                                      JPopupMenu  p)
    {

    }

    public void doFillToolBar (JToolBar acts)
    {

        final WarmupEditorPanel _this = this;
    
        acts.add (UIUtils.createToolBarButton (Constants.DOWN_ICON_NAME,
                                               "Click to view the prompt",
                                               "showprompt",
                                               new ActionAdapter ()
                                               {
                                                  
                                                   public void actionPerformed (ActionEvent ev)
                                                   {
                                                       
                                                       _this.header.setVisible (true);
                                                       
                                                   }
                                                  
                                               }));
    
        acts.add (UIUtils.createToolBarButton (Constants.CONVERT_ICON_NAME,
                                               "Click to convert this warm-up to a " + Environment.getObjectTypeName (Project.OBJECT_TYPE),
                                               "convert",
                                               this.projectViewer.getAction (WarmupsViewer.CONVERT_TO_PROJECT_ACTION,
                                                                             this.warmup.getChapter ())));

    }

    public void doFillPopupMenu (final MouseEvent ev,
                                 final JPopupMenu popup,
                                       boolean    compress)
    {

        final WarmupEditorPanel _this = this;
    
        if (compress)
        {
                         
            List<JComponent> buts = new ArrayList ();

            buts.add (this.createButton (Constants.SAVE_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Save {Warmup}",
                                         SAVE_ACTION_NAME));

            buts.add (this.createButton (Constants.CONVERT_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Convert to a {Project}",
                                         CONVERT_TO_PROJECT_ACTION_NAME));
            
            buts.add (this.createButton (Constants.FIND_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Find",
                                         Constants.SHOW_FIND_ACTION));
                                                                                        
            popup.add (UIUtils.createPopupMenuButtonBar (Environment.replaceObjectNames ("{Warmup}"),
                                                         popup,
                                                         buts));
                                            
        } else {    

          JMenuItem mi = null;
        
          mi = this.createMenuItem ("Save {Chapter}",
                                    Constants.SAVE_ICON_NAME,
                                    SAVE_ACTION_NAME,
                                    KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                                            ActionEvent.CTRL_MASK));
          mi.setMnemonic (KeyEvent.VK_S);

          popup.add (mi);
            
          popup.add (this.createMenuItem ("Convert to a {Project}",
                                          Constants.CONVERT_ICON_NAME,
                                          CONVERT_TO_PROJECT_ACTION_NAME,
                                          null));
  
        }
          
    }

    public List<Component> getTopLevelComponents ()
    {

        List<Component> l = new ArrayList ();
        l.add (this.editor);

        return l;

    }

    public void close ()
    {


    }

    public void doInit ()
                 throws GeneralException
    {

        final WarmupEditorPanel _this = this;

        this.promptPreview = UIUtils.createHelpTextPane (UIUtils.formatPrompt (this.warmup.getPrompt ()),
                                                         this.projectViewer);
        this.promptPreview.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                          Short.MAX_VALUE));
        this.promptPreview.setBorder (new EmptyBorder (0, 5, 0, 5));
        
        this.header = new Box (BoxLayout.X_AXIS);
        //this.header.add (Box.createHorizontalGlue ());
        this.header.add (this.promptPreview);
        this.header.add (Box.createHorizontalStrut (10));        
        this.header.setOpaque (true);

        final JButton upBut = UIUtils.createButton (Constants.CANCEL_ICON_NAME,
                                              Constants.ICON_PANEL_ACTION,
                                              "Click to hide the prompt",
                                              null);
        
        upBut.addActionListener (new ActionAdapter ()
                                 {
                                                
                                    public void actionPerformed (ActionEvent ev)
                                    {
                                      
                                      _this.header.setVisible (false);
                                                        
                                    }
                                  
                                });

        List<JButton> buts = new ArrayList ();

        buts.add (upBut);

         JComponent bar = UIUtils.createButtonBar (buts);
         bar.setBorder (new EmptyBorder (0, 5, 0, 5));
  
        this.header.add (bar);

        this.header.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.header.setOpaque (false);

        this.header.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.header.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getBorderColor ()),
                                                   new EmptyBorder (5, 0, 5, 0)));
        this.header.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                   this.header.getPreferredSize ().height));
        this.add (this.header,
                  0);

    }

    public void startWarmup ()
    {

        this.projectViewer.getWordCountTimer ().start ();

    }

}
