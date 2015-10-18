package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.text.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ActionAdapter;

public class Finder extends AbstractSideBar<AbstractProjectViewer>
{

    private static final String TITLE_PREFIX = "Find";
    private JTextField text = null;
    private Object highlightId = null;
    private QTextEditor highlightedEditor = null;
    private Box content = null;
    private JLabel noMatches = null;
    private String currentSearch = null;
        
    public Finder (AbstractProjectViewer v)
    {
        
        super (v);
        
    }

    public void setText (String t)
    {
        
        this.text.setText (t);
        
        this.search ();
        
    }

    public boolean canClose ()
    {
        
        return true;
        
    }
    
    public String getTitle ()
    {
        
        return TITLE_PREFIX + (this.currentSearch != null ? ": " + this.currentSearch : "");
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    @Override
    public void onHide ()
    {
        
        this.clearHighlight ();
        
    }
    
    @Override
    public void onClose ()
    {
        
    }
    
    public String getIconType ()
    {
        
        return Constants.FIND_ICON_NAME;
        
    }
    
    public List<JComponent> getHeaderControls ()
    {
        
        return null;
        
    }
    
    public JComponent getContent ()
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);

        final Finder _this = this;
        
        this.text = UIUtils.createTextField ();
        this.text.setBorder (new CompoundBorder (UIUtils.createPadding (5, 10, 5, 10),
                                                 this.text.getBorder ()));

        this.viewer.fireProjectEvent (ProjectEvent.FIND,
                                      ProjectEvent.SHOW);

        KeyAdapter vis = new KeyAdapter ()
        {

            private Timer searchT = new Timer (750,
                                               new ActionAdapter ()
                                               {

                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                      _this.search ();

                                                  }

                                                });

            public void keyPressed (KeyEvent ev)
            {

                this.searchT.setRepeats (false);
                this.searchT.stop ();

                // If enter was pressed then search, don't start the timer.
                if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                {
                    
                    _this.search ();
                    return;
                    
                }

                this.searchT.start ();

            }

        };

        this.text.addKeyListener (vis);
        this.text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 this.text.getPreferredSize ().height));

        b.add (this.text);

        this.content = new Box (BoxLayout.Y_AXIS);
        this.content.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.content.setOpaque (false);

        this.content.setMinimumSize (new Dimension (250,
                                         250));

        this.noMatches = UIUtils.createInformationLabel ("No matches found.");

        this.noMatches.setVisible (false);
        
        this.noMatches.setBorder (UIUtils.createPadding (5, 10, 0, 5));
        
        b.add (this.noMatches);
                                         
        b.add (this.wrapInScrollPane (this.content));

        return b;        
        
    }
        
    public void init ()
               throws GeneralException
    {

        super.init ();
                    
    }

    public void setFindText (String text)
    {
        
        if (text != null)
        {

            if (text.trim ().length () == 0)
            {
                
                return;
                
            }
            
            this.text.setText (text);
            
            this.search ();
            
        }
        
    }
    
    @Override
    public void onShow ()
    {
        
        this.text.grabFocus ();        
        
    }
    
    private void search ()
    {

        this.content.removeAll ();
    
        this.clearHighlight ();

        String t = this.text.getText ().trim ();
        
        if (t.length () == 0)
        {
            
            this.setTitle (this.getTitle ());
            
            this.currentSearch = null;
            
            return;
            
        }
        
        Set<FindResultsBox> res = this.viewer.findText (t);
        
        for (FindResultsBox r : res)
        {
            
            r.init ();
            
            this.content.add (r);
            
        }

        this.noMatches.setVisible (res.size () == 0);
                
        this.currentSearch = t;

        this.setTitle (this.getTitle ());
        
        this.validate ();
        this.repaint ();

    }

    public void clearHighlight ()
    {
        
        if (this.highlightedEditor != null)
        {
            
            this.highlightedEditor.removeHighlight (this.highlightId);

        }
        
    }
/*
    public JButton[] getButtons ()
    {

        final Finder _this = this;

        JButton b = new JButton ("Finish");

        b.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.clearHighlight ();

                    _this.close ();

                }

            });

        JButton[] buts = new JButton[1];
        buts[0] = b;

        return buts;

    }
*/
    public void showSegment (final Object  o,
                             final Segment s)
    {

        this.clearHighlight ();
                
        if (o instanceof Chapter)
        {
        
            final Chapter c = (Chapter) o;        

            final Finder _this = this;            
                
            this.viewer.viewObject ((DataObject) o,
                                    new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {
            
                    AbstractEditorPanel p = null;

                    if (_this.viewer instanceof ProjectViewer)
                    {
                    
                        ProjectViewer pv = (ProjectViewer) _this.viewer;
                
                        p = pv.getEditorForChapter (c);
                
                    }
        
                    if (_this.viewer instanceof WarmupsViewer)
                    {
                        
                        WarmupsViewer wv = (WarmupsViewer) _this.viewer;
                        
                        p = wv.getEditorForWarmup (c);
                        
                    }
                        
                    try
                    {
                        
                        p.scrollToPosition (s.getBeginIndex ());
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to scroll to: " + s.getBeginIndex (),
                                              e);
                        
                        return;
                        
                    }

                    final QTextEditor ed = p.getEditor ();
                                
                    _this.highlightId = ed.addHighlight (s.getBeginIndex (),
                                                         s.getEndIndex (),
                                                         null,
                                                         true);
            
                    _this.highlightedEditor = ed;
                    
                }
                
            });

        }
        
    }

}
