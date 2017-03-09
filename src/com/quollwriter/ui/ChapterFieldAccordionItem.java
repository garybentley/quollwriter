package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.Markup;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.text.*;

public abstract class ChapterFieldAccordionItem extends AccordionItem
{
        
    private ProjectViewer projectViewer = null;
    private Chapter chapter = null;
    private Box content = null;
    private Box view = null;
    private Box edit = null;
    private TextArea editText = null;
    private boolean typed = false;
        
    public ChapterFieldAccordionItem (ProjectViewer pv,
                                      Chapter       c)
    {
        
        super ("");
        
        this.setTitle (this.getFieldNamePlural ());
        this.setIconType (this.getFieldIconType ());

        this.chapter = c;
        this.projectViewer = pv;

        final ChapterFieldAccordionItem _this = this;
        
        this.content = new Box (BoxLayout.Y_AXIS);
        
        this.view = new Box (BoxLayout.Y_AXIS);
        
        this.edit = new Box (BoxLayout.Y_AXIS);
        
        this.content.add (this.view);
        this.content.add (this.edit);
        
        this.edit.setVisible (false);

        String help = "Separate each " + this.getFieldName () + " with a newline.  To save press Ctrl+Enter or use the buttons below.";
        
        if (!this.isBulleted ())
        {
            
            help = "Enter the " + this.getFieldName () + ", to save press Ctrl+Enter or use the buttons below.";
            
        }
            
        this.editText = UIUtils.createTextArea (pv,
                                                help,
                                                20,
                                                -1);
        this.editText.setCanFormat (true);
        this.editText.setBorder (null);
                    
        this.edit.add (this.editText);
        
        JButton save = UIUtils.createButton (Constants.SAVE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Click to save the " + this.getFieldNamePlural (),
                                             new ActionAdapter ()
                                             {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                                                                        
                                                    _this.save ();
                                                    
                                                }
                                                
                                             });

        JButton cancel = UIUtils.createButton (Constants.CANCEL_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to cancel",
                                               new ActionAdapter ()
                                               {
                                                
                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                    _this.update (_this.chapter);
                                                    
                                                  }
                                                  
                                               });

        List<JButton> buts = new ArrayList ();

        buts.add (save);
        buts.add (cancel);

        JToolBar tb = UIUtils.createButtonBar (buts);

        tb.setAlignmentX (Component.LEFT_ALIGNMENT);        
        
        Box buttons = new Box (BoxLayout.X_AXIS);
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        buttons.add (Box.createHorizontalGlue ());
        buttons.add (tb);

        buttons.setBorder (new CompoundBorder (new MatteBorder (1,
                                                                0,
                                                                0,
                                                                0,
                                                                UIUtils.getColor ("#dddddd")),
                                               new EmptyBorder (3,
                                                                3,
                                                                3,
                                                                3)));
                                                                
                                                                
        this.edit.add (buttons);
        /*
        this.editText.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {

                _this.editText.grabFocus ();

            }
        });

        this.editText.addKeyListener (new KeyAdapter ()
        {

            private boolean typed = false;
        
            public void keyPressed (KeyEvent ev)
            {
                        
                if (!_this.typed)
                {                        
 
                    _this.typed = true;
                
                    if (!_this.hasField ())
                    {
                        
                        _this.editText.setText ("");
                        _this.editText.setForeground (Color.BLACK);
                            
                    }

                }
                
                if ((ev.getKeyCode () == KeyEvent.VK_ENTER) &&
                    ((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK))
                {

                    _this.save ();

                }

            }

        });        
          */
        
        UIUtils.addDoActionOnReturnPressed (this.editText,
                                            new ActionListener ()
                                            {
                                            
                                                @Override
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.save ();
                                                    
                                                }
                                                
                                             });
        this.edit.setBorder (UIUtils.createLineBorderWithPadding (3, 0, 5, 0));
        /*
        this.edit.setBorder (new CompoundBorder (new EmptyBorder (3, 5, 5, 0),
                                                 UIUtils.createLineBorder ()));
                                                 */
        /*
        this.editText.setBorder (new EmptyBorder (3,
                                                  3,
                                                  3,
                                                  3));
*/
        Header h = this.getHeader ();
                                        
        h.setBorder (new CompoundBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getBorderColor ()),
                                                             new EmptyBorder (0, 0, 3, 0)),
                                         h.getBorder ()));

        this.content.setBorder (new EmptyBorder (5, 0, 10, 0));
        
    }

    public abstract boolean isBulleted ();
    
    public abstract StringWithMarkup getFieldValue (Chapter c);
    
    public abstract String getFieldName ();
    
    public abstract String getFieldNamePlural ();
    
    public abstract void setFieldValue (StringWithMarkup v,
                                        Chapter          c);
    
    public abstract String getFieldIconType ();
        
    private boolean hasField ()
    {
 
        StringWithMarkup v = this.getFieldValue (this.chapter);
        
        return (v != null && v.hasText ());
        
    }
    
    private void initEditText ()
    {
        
        if (!this.hasField ())
        {
/*
            final Color lightGrey = UIUtils.getColor ("#aaaaaa");

            this.editText.setForeground (lightGrey);
            
            String help = "Separate each " + this.getFieldName () + " with a newline.  To save press Ctrl+Enter or use the buttons below.";
            
            if (!this.isBulleted ())
            {
                
                help = "Enter the " + this.getFieldName () + ", to save press Ctrl+Enter or use the buttons below.";
                
            }
            
            this.editText.setText (help);

            this.editText.getCaret ().setDot (0);
*/
            //this.editText.grabFocus ();

        } else
        {

            StringWithMarkup v = this.getFieldValue (this.chapter);
                    
            if ((v != null)
                &&
                (v.hasText ())
               )
            {                    
            
                this.editText.setText (v);
                
            }

        }
        
    }
    
    public void update (Chapter c)
    {
                
        this.chapter = c;
        
        this.getHeaderControls ().setVisible (true);        
        
        this.setValue (this.getFieldValue (this.chapter));
        
    }
        
    public void setValue (StringWithMarkup v)
    {

        final ChapterFieldAccordionItem _this = this;
    
        this.view.removeAll ();    
        
        if ((v == null)
            ||
            (!v.hasText ())
           )
        {
        
            JLabel l = UIUtils.createClickableLabel ("<i>No " + this.getFieldNamePlural () + " set, click to edit.</i>",
                                                     null);

            l.setBorder (new EmptyBorder (0, 5, 0, 0));
                        
            l.addMouseListener (new MouseEventHandler ()
            {
               
                @Override
                public void handlePress (MouseEvent ev)
                {
                                        
                    _this.edit ();
                    
                }
                
            });
                                                     
            this.view.add (l);

            this.edit.setVisible (false);
            this.view.setVisible (true);
            
            this.validate ();
            this.repaint ();
    
            UIUtils.scrollIntoView (this);
            
            return;
            
        } 
        
        if (this.isBulleted ())
        {
        
            StringBuilder layoutRows = new StringBuilder ();
    
            String t = (v != null ? v.getText () : null);
    
            TextIterator iter = new TextIterator (t);
            
            for (Paragraph p : iter.getParagraphs ())
            {
                
                if (layoutRows.length () > 0)
                {
    
                    layoutRows.append (",");
    
                }
    
                layoutRows.append ("top:p, 6px");
                
            }
    
/*
            StringTokenizer t = new StringTokenizer (v,
                                                     String.valueOf ('\n'));
    
            while (t.hasMoreTokens ())
            {
    
                if (layoutRows.length () > 0)
                {
    
                    layoutRows.append (",");
    
                }
    
                layoutRows.append ("top:p, 6px");
    
                t.nextToken ();
    
            }
  */  
            FormLayout fl = new FormLayout ("3px, pref, 3px, fill:200px:grow, 3px",
                                            layoutRows.toString ());
    
            PanelBuilder pb = new PanelBuilder (fl);
    
            CellConstraints cc = new CellConstraints ();
    
            int r = 1;
    
            Markup m = (v != null ? v.getMarkup () : null);

            for (Paragraph p : iter.getParagraphs ())
            {
                                            
                pb.add (new JLabel (Environment.getIcon (Constants.BULLET_ICON_NAME,
                                                         Constants.ICON_MENU)),
                        cc.xy (2,
                               r));

                JTextPane tp = UIUtils.createTextViewPane (p.markupAsHTML (m),
                                                           this.projectViewer);

                pb.add (tp,
                        cc.xy (4,
                               r));
    
                r += 2;
                               
            }
    
            JPanel p = pb.getPanel ();
    
            p.setOpaque (false);
                    
            this.view.add (p);

        } else {

            FormLayout fl = new FormLayout ("fill:200px:grow",
                                            "p");
    
            PanelBuilder pb = new PanelBuilder (fl);
    
            CellConstraints cc = new CellConstraints ();
                
            JTextPane tp = UIUtils.createObjectDescriptionViewPane (v,
                                                                    this.chapter,
                                                                    this.projectViewer,
                                                                    this.projectViewer.getEditorForChapter (this.chapter)); 

            pb.add (tp,
                        cc.xy (1,
                               1));

            JPanel p = pb.getPanel ();
    
            p.setOpaque (false);
                
            this.view.add (p);
        
        }    
            /*
        this.view.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 this.view.getPreferredSize ().height));        
         */
        
        this.edit.setVisible (false);
        this.view.setVisible (true);
        
        this.validate ();
        this.repaint ();

        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            this.getPreferredSize ().height));
        
        UIUtils.scrollIntoView (this);
        
    }
        
    public JComponent getWrappedText (StringWithMarkup t,
                                      int    initialWidth)
    {
        
        FormLayout fl = new FormLayout ("fill:" + initialWidth + "px:grow",
                                        "p");

        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();
    
        JTextPane tp = UIUtils.createObjectDescriptionViewPane (t,
                                                                this.chapter,
                                                                this.projectViewer,
                                                                this.projectViewer.getEditorForChapter (this.chapter)); 

        pb.add (tp,
                    cc.xy (1,
                           1));

        JPanel p = pb.getPanel ();

        p.setOpaque (false);
        
        return p;
        
    }
    
    public JComponent getContent ()
    {
        
        return this.content;
                
    }

    private void save ()
    {
        /*
        if (!this.typed)
        {
            
            this.update (this.chapter);
            
            return;
            
        }
        */
        
        this.getHeaderControls ().setVisible (true);
        
        StringWithMarkup t = this.editText.getTextWithMarkup ();
                
        if ((t != null)
            &&
            (!t.hasText ())
           )
        {
            
            t.update (null,
                      null);
                
        }
        
        this.setFieldValue (t,
                            this.chapter);

        try
        {
            
            this.projectViewer.saveObject (this.chapter,
                                           true);
            
            this.typed = false;
            
            this.update (this.chapter);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to save " + this.getFieldNamePlural () + " for chapter: " +
                                  this.chapter,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Unable to save the " + this.getFieldNamePlural ());
                        
        }
                
    }
    
    private void edit ()
    {
        /*
        if (this.edit.isVisible ())
        {
            
            this.save ();
            
            return;
            
        }
          */      
        this.setContentVisible (true);
        
        //this.editText.setTextWithMarkup (this.getFieldValue (this.chapter));

        this.view.setVisible (false);
        this.edit.setVisible (true);
        this.getHeaderControls ().setVisible (false);

        this.initEditText ();
/*
        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            this.getPreferredSize ().height));        
  */              
        this.validate ();
        this.repaint ();

        UIUtils.scrollIntoView (this);
        
    }
    
    @Override
    public void init ()
    {
        
        super.init ();

        this.setValue (this.getFieldValue (this.chapter));
                
        List<JButton> conts = new ArrayList ();
        
        final ChapterFieldAccordionItem _this = this;
        
        ActionListener action = new ActionAdapter ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.edit ();
                
            }
            
        };
        
        conts.add (UIUtils.createButton (Constants.EDIT_ICON_NAME,
                                         Constants.ICON_SIDEBAR,
                                         "Click to edit the " + this.getFieldNamePlural (),
                                         action));

        this.setHeaderControls (UIUtils.createButtonBar (conts));        
        
    }
    
    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {

        final ChapterFieldAccordionItem _this = this;
        
        ActionListener action = new ActionAdapter ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.edit ();
                
            }
            
        };
        
        m.add (UIUtils.createMenuItem ("Edit the " + this.getFieldNamePlural (),
                                       Constants.EDIT_ICON_NAME,
                                       action));
    
    }    
    
}