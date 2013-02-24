package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.*;


public class IconColumn extends JPanel implements DocumentListener
{

    // public static final int SHOW_OUTLINE_ITEM_ACTION_TYPE = 1;
    // public static final String SHOW_OUTLINE_ITEM_ACTION_COMMAND = "showOutlineItemAction";

    public static final Color defaultBGColor = UIUtils.getIconColumnColor ();

    private class OutlineItemWrapper
    {

        public OutlineItem                  item = null;
        public ImagePanel                   imagePanel = null;
        public ShowOutlineItemActionHandler handler = null;

    }

    private class NoteWrapper
    {

        public Note                  note = null;
        public ImagePanel            imagePanel = null;
        public ShowNoteActionHandler handler = null;

    }

    private class SceneWrapper
    {

        public Scene                  scene = null;
        public ImagePanel             imagePanel = null;
        public ShowSceneActionHandler handler = null;

    }

    private QuollEditorPanel                   qep = null;
    private ProjectViewer                      projectViewer = null;
    private QTextEditor                        editor = null;
    private java.util.List<OutlineItemWrapper> outlineItems = new ArrayList ();
    private java.util.List<NoteWrapper>        notes = new ArrayList ();
    private java.util.List<SceneWrapper>       scenes = new ArrayList ();
    //private Image                              multiIcon = null;
    private Image                              singleIcon = null;
    //private Image                              multiNoteIcon = null;
    private Image                              singleNoteIcon = null;
    private Image                              singleEditNeededNoteIcon = null;
    //private Image                              multiSceneIcon = null;
    private Image                              singleSceneIcon = null;

    public IconColumn(QuollEditorPanel qep,
                      ProjectViewer    pv)
    {

        this.setLayout (null);

        this.qep = qep;
        this.projectViewer = pv;
        this.editor = this.qep.getEditor ();

        this.editor.getDocument ().addDocumentListener (this);

        this.setBackground (IconColumn.defaultBGColor);
/*
        this.multiIcon = Environment.getIcon (OutlineItem.OBJECT_TYPE + "-multi",
                                              Constants.ICON_COLUMN).getImage ();
                                              */
        this.singleIcon = Environment.getIcon (OutlineItem.OBJECT_TYPE,
                                               Constants.ICON_COLUMN).getImage ();
/*
        this.multiNoteIcon = Environment.getIcon (Note.OBJECT_TYPE + "-multi",
                                                  Constants.ICON_COLUMN).getImage ();
                                                  */
        this.singleNoteIcon = Environment.getIcon (Note.OBJECT_TYPE,
                                                   Constants.ICON_COLUMN).getImage ();
        this.singleEditNeededNoteIcon = Environment.getIcon ("edit-needed-note",
                                                             Constants.ICON_COLUMN).getImage ();
/*
        this.multiSceneIcon = Environment.getIcon (Scene.OBJECT_TYPE + "-multi",
                                                   Constants.ICON_COLUMN).getImage ();
                                                   */
        this.singleSceneIcon = Environment.getIcon (Scene.OBJECT_TYPE,
                                                    Constants.ICON_COLUMN).getImage ();

    }

    public void changedUpdate (DocumentEvent e)
    {

        this.repaint ();

    }

    public void insertUpdate (DocumentEvent e)
    {

        this.repaint ();

    }

    public void removeUpdate (DocumentEvent e)
    {

        this.repaint ();

    }

    private void paintOutlineItems (Graphics g)
    {

        Map<Integer, OutlineItemWrapper> its = new HashMap ();

        Iterator iter = this.outlineItems.iterator ();

        while (iter.hasNext ())
        {

            OutlineItemWrapper w = (OutlineItemWrapper) iter.next ();

            Rectangle r = null;

            try
            {

                r = this.editor.modelToView (w.item.getPosition ());

            } catch (Exception e)
            {

                // BadLocationException!
                Environment.logError ("Unable to convert item: " +
                                      w.item +
                                      " at position: " +
                                      w.item.getPosition (),
                                      e);

                continue;

            }

            Integer k = new Integer (r.y);

            OutlineItemWrapper pp = (OutlineItemWrapper) its.get (k);

            if (pp == null)
            {

                its.put (k,
                         w);

                w.imagePanel.setVisible (true);
                w.imagePanel.setImage (this.singleIcon);

            } else
            {

                w.imagePanel.setVisible (false);
                pp.imagePanel.setImage (this.singleIcon);

            }

        }

        Insets insets = this.getInsets ();

        Iterator<Map.Entry<Integer, OutlineItemWrapper>> iter2 = its.entrySet ().iterator ();

        while (iter2.hasNext ())
        {

            Map.Entry<Integer, OutlineItemWrapper> item = iter2.next ();

            Integer r = item.getKey ();

            OutlineItemWrapper w = item.getValue ();

            w.imagePanel.setVisible (true);

            w.imagePanel.setBounds (28 + insets.left,
                                    2 + r.intValue (),
                                    w.imagePanel.getPreferredSize ().width,
                                    w.imagePanel.getPreferredSize ().height);

        }

    }

    private void paintScenes (Graphics g)
    {

        Map<Integer, SceneWrapper> its = new HashMap ();

        Iterator iter = this.scenes.iterator ();

        while (iter.hasNext ())
        {

            SceneWrapper w = (SceneWrapper) iter.next ();

            Rectangle r = null;

            try
            {

                r = this.editor.modelToView (w.scene.getPosition ());

            } catch (Exception e)
            {

                // BadLocationException!
                Environment.logError ("Unable to convert scene: " +
                                      w.scene +
                                      " at position: " +
                                      w.scene.getPosition (),
                                      e);

                continue;

            }

            Integer k = new Integer (r.y);

            SceneWrapper pp = (SceneWrapper) its.get (k);

            if (pp == null)
            {

                its.put (k,
                         w);

                w.imagePanel.setVisible (true);
                w.imagePanel.setImage (this.singleSceneIcon);

            } else
            {

                w.imagePanel.setVisible (false);
                pp.imagePanel.setImage (this.singleSceneIcon);

            }

        }

        Insets insets = this.getInsets ();

        Iterator<Map.Entry<Integer, SceneWrapper>> iter2 = its.entrySet ().iterator ();

        while (iter2.hasNext ())
        {

            Map.Entry<Integer, SceneWrapper> item = iter2.next ();

            Integer r = item.getKey ();

            SceneWrapper w = item.getValue ();

            w.imagePanel.setVisible (true);

            w.imagePanel.setBounds (28 + insets.left,
                                    2 + r.intValue (),
                                    w.imagePanel.getPreferredSize ().width,
                                    w.imagePanel.getPreferredSize ().height);

        }

    }

    private void paintNotes (Graphics g)
    {

        Map<Integer, NoteWrapper> its = new HashMap ();

        Iterator iter = this.notes.iterator ();

        while (iter.hasNext ())
        {

            NoteWrapper w = (NoteWrapper) iter.next ();

            Rectangle r = null;

            try
            {

                r = this.editor.modelToView (w.note.getPosition ());

            } catch (Exception e)
            {

                // BadLocationException!
                Environment.logError ("Unable to convert note: " +
                                      w.note +
                                      " at position: " +
                                      w.note.getPosition (),
                                      e);

                continue;

            }

            Integer k = new Integer (r.y);

            NoteWrapper pp = (NoteWrapper) its.get (k);

            if (pp == null)
            {

                its.put (k,
                         w);

                w.imagePanel.setVisible (true);

                if (w.note.isEditNeeded ())
                {

                    w.imagePanel.setImage (this.singleEditNeededNoteIcon);

                } else
                {

                    w.imagePanel.setImage (this.singleNoteIcon);

                }

            } else
            {

                w.imagePanel.setVisible (false);
                pp.imagePanel.setImage (this.singleNoteIcon);

            }

        }

        Insets insets = this.getInsets ();

        Iterator<Map.Entry<Integer, NoteWrapper>> iter2 = its.entrySet ().iterator ();

        while (iter2.hasNext ())
        {

            Map.Entry<Integer, NoteWrapper> item = iter2.next ();

            Integer r = item.getKey ();

            NoteWrapper w = item.getValue ();

            w.imagePanel.setVisible (true);

            w.imagePanel.setBounds (8 + insets.left,
                                    2 + r.intValue (),
                                    w.imagePanel.getPreferredSize ().width,
                                    w.imagePanel.getPreferredSize ().height);

        }

    }

    protected void paintChildren (Graphics g)
    {

        this.paintOutlineItems (g);

        this.paintScenes (g);

        this.paintNotes (g);

        super.paintChildren (g);

    }

    public void removeAllItems ()
    {

        java.util.List<OutlineItemWrapper> o = new ArrayList (this.outlineItems);

        for (OutlineItemWrapper i : o)
        {

            this.removeItem (i.item);

        }

        java.util.List<NoteWrapper> n = new ArrayList (this.notes);

        for (NoteWrapper i : n)
        {

            this.removeItem (i.note);

        }

        java.util.List<SceneWrapper> s = new ArrayList (this.scenes);

        for (SceneWrapper i : s)
        {

            this.removeItem (i.scene);

        }

    }

    public void removeItems (java.util.List<? extends ChapterItem> items)
    {

        for (ChapterItem i : items)
        {

            this.removeItem (i);

        }

    }

    public void removeItem (ChapterItem item)
    {

        if (item instanceof OutlineItem)
        {

            OutlineItemWrapper w = null;

            for (int i = 0; i < this.outlineItems.size (); i++)
            {

                w = this.outlineItems.get (i);

                if (w.item == item)
                {

                    break;

                }

            }

            if (w == null)
            {

                return;

            }

            this.outlineItems.remove (w);

            if (w != null)
            {

                this.remove (w.imagePanel);

            }

        }

        if (item instanceof Scene)
        {

            SceneWrapper w = null;

            Scene s = (Scene) item;

            for (int i = 0; i < this.scenes.size (); i++)
            {

                w = this.scenes.get (i);

                if (w.scene == s)
                {

                    break;

                }

            }

            if (w == null)
            {

                return;

            }

            this.scenes.remove (w);

            if (w != null)
            {

                this.remove (w.imagePanel);

            }

        }

        if (item instanceof Note)
        {

            Note n = (Note) item;

            NoteWrapper w = null;

            for (int i = 0; i < this.notes.size (); i++)
            {

                w = this.notes.get (i);

                if (w.note == n)
                {

                    break;

                }

            }

            if (w == null)
            {

                return;

            }

            this.notes.remove (w);

            if (w != null)
            {

                this.remove (w.imagePanel);

            }

        }

        this.repaint ();

    }

    public void setOutlineItems (java.util.Set<OutlineItem>  items,
                                 QuollEditorPanel            qep)
                          throws GeneralException
    {

        for (OutlineItem it : items)
        {

            this.addItem (it,
                          qep);

        }

    }

    public void setScenes (java.util.Set<Scene>  items,
                           QuollEditorPanel      qep)
                    throws GeneralException
    {

        for (Scene s : items)
        {

            this.addItem (s,
                          qep);

            this.setOutlineItems (s.getOutlineItems (),
                                  qep);

        }

    }

    public void setNotes (java.util.Set<Note>  items,
                          QuollEditorPanel     qep)
                   throws GeneralException
    {

        for (Note n : items)
        {

            this.addItem (n,
                          qep);

        }

    }

    public ImagePanel addItem (ChapterItem      c,
                               QuollEditorPanel qep)
                        throws GeneralException
    {

        if (c instanceof OutlineItem)
        {

            return this.addItem ((OutlineItem) c,
                                 qep);

        }

        if (c instanceof Scene)
        {

            return this.addItem ((Scene) c,
                                 qep);

        }

        if (c instanceof Note)
        {

            return this.addItem ((Note) c,
                                 qep);

        }

        throw new GeneralException ("Type: " +
                                    c +
                                    " not supported.");

    }

    public ImagePanel addItem (final OutlineItem      item,
                               final QuollEditorPanel qep)
                        throws GeneralException
    {

        // Placeholder
        Image      img = null;
        ImagePanel p = new ImagePanel (img,
                                       Environment.getTransparentImage ());

        UIUtils.setAsButton (p);
        final OutlineItemWrapper w = new OutlineItemWrapper ();
        w.imagePanel = p;
        w.item = item;

        this.add (p);

        try
        {

            Position pos = this.editor.getDocument ().createPosition (item.getPosition ());

            item.setTextPosition (pos);

            this.outlineItems.add (w);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to add item: " +
                                        item +
                                        ", document position: " +
                                        item.getPosition () +
                                        " is not valid.",
                                        e);

        }

        p.addMouseListener (new MouseAdapter ()
        {

            public void mousePressed (MouseEvent ev)
            {

                if (ev.isPopupTrigger ())
                {
                    
                    return;
                    
                }
                        
                if (w.handler != null)
                {

                    w.handler.showItem ();

                } else
                {

                    w.handler = new ShowOutlineItemActionHandler (w.item,
                                                                  qep);

                    w.handler.showItem ();

                }

            }

        });

        this.repaint ();

        return p;

    }

    public ImagePanel addItem (final Scene            item,
                               final QuollEditorPanel qep)
                        throws GeneralException
    {

        // Placeholder
        Image      img = null;
        ImagePanel p = new ImagePanel (img,
                                       Environment.getTransparentImage ());
        UIUtils.setAsButton (p);

        final SceneWrapper w = new SceneWrapper ();
        w.imagePanel = p;
        w.scene = item;

        this.add (p);

        try
        {

            Position pos = this.editor.getDocument ().createPosition (item.getPosition ());

            item.setTextPosition (pos);

            this.scenes.add (w);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to add item: " +
                                        item +
                                        ", document position: " +
                                        item.getPosition () +
                                        " is not valid.",
                                        e);

        }

        p.addMouseListener (new MouseAdapter ()
            {

                public void mousePressed (MouseEvent ev)
                {

                    if (w.handler != null)
                    {

                        w.handler.showItem ();

                    } else
                    {

                        w.handler = new ShowSceneActionHandler (w.scene,
                                                                qep);

                        w.handler.showItem ();

                    }

                }

            });

        this.repaint ();

        return p;

    }

    public ImagePanel addItem (final Note             n,
                               final QuollEditorPanel qep)
                        throws GeneralException
    {

        // Placeholder
        Image      img = null;
        ImagePanel p = new ImagePanel (img,
                                       Environment.getTransparentImage ());
        UIUtils.setAsButton (p);
        final NoteWrapper w = new NoteWrapper ();
        w.imagePanel = p;
        w.note = n;

        this.add (p);

        try
        {

            Position pos = this.editor.getDocument ().createPosition (n.getPosition ());

            n.setTextPosition (pos);

            if (n.getEndPosition () > -1)
            {

                n.setEndTextPosition (this.editor.getDocument ().createPosition (n.getEndPosition ()));

            }

            this.notes.add (w);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to add note: " +
                                        n,
                                        e);

        }

        final ProjectViewer pv = this.projectViewer;

        p.addMouseListener (new MouseAdapter ()
            {

                public void mousePressed (MouseEvent ev)
                {

                    try
                    {
                
                        if (w.handler != null)
                        {
    
                            w.handler.showItem ();
    
                        } else
                        {
    
                            w.handler = new ShowNoteActionHandler (w.note,
                                                                   qep);
    
                            w.handler.showItem ();
    
                        }

                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to show item: " +
                                              w.note,
                                              e);
                        
                    }
                    
                }

            });

        this.repaint ();

        return p;

    }

}
