package com.quollwriter.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;


public class FindBar extends JToolBar implements DocumentListener
{

    private QTextEditor                                text = null;
    private JTextField                                 findField = null;
    private DefaultHighlighter.DefaultHighlightPainter dfp = null;
    private DefaultHighlighter.DefaultHighlightPainter idfp = null;
    private int                                        lastCaret = 0;
    private int                                        lastFindPoint = 0;
    private TreeMap                                    highlights = new TreeMap ();
    private boolean                                    searchActive = false;
    private Color                                      nonFoundFindFieldBg = new Color (251,
                                                                                        113,
                                                                                        79);
    private Color                                      normalFindFieldBg = null;

    public FindBar(QTextEditor t,
                   ImageIcon   nextIcon,
                   ImageIcon   previousIcon,
                   ImageIcon   cancelIcon)
    {

        this.text = t;

        this.dfp = new DefaultHighlighter.DefaultHighlightPainter (new Color (139,
                                                                              220,
                                                                              243));
        this.idfp = new DefaultHighlighter.DefaultHighlightPainter (Color.RED); // new Color (244, 227, 189));

        final FindBar _this = this;

        this.text.addMouseListener (new MouseAdapter ()
            {

                public void mouseClicked (MouseEvent ev)
                {

                    _this.endFind ();

                }

            });

        JButton findCancel = new JButton (cancelIcon);
        findCancel.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.findField.setText ("");

                    _this.setVisible (false);

                    _this.endFind ();

                }

            });

        findCancel.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        this.add (findCancel);
        this.add (Box.createHorizontalStrut (10));

        JLabel findL = new JLabel ("Find");

        this.add (findL);
        this.add (Box.createHorizontalStrut (5));

        this.findField = new JTextField ();
        this.normalFindFieldBg = this.findField.getBackground ();
        this.findField.setBorder (new CompoundBorder (this.findField.getBorder (),
                                                      new EmptyBorder (0,
                                                                       2,
                                                                       0,
                                                                       0)));
        this.findField.setPreferredSize (new Dimension (150,
                                                        -1));
        this.findField.setMinimumSize (new Dimension (150,
                                                      -1));
        this.findField.getDocument ().addDocumentListener (this);

        this.add (this.findField);

        this.add (Box.createHorizontalStrut (5));

        JButton findNext = new JButton (nextIcon);
        findNext.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.goNext ();

                }

            });

        this.add (findNext);

        JButton findPrevious = new JButton (previousIcon);

        findPrevious.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.goPrevious ();

                }

            });

        this.add (findPrevious);

        Component hg = Box.createHorizontalGlue ();
        hg.setPreferredSize (new Dimension (Short.MAX_VALUE,
                                            -1));
        this.add (hg);

        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            this.getPreferredSize ().height));

        this.setBorder (new CompoundBorder (new MatteBorder (1,
                                                             0,
                                                             0,
                                                             0,
                                                             new Color (127,
                                                                        127,
                                                                        127,
                                                                        127)),
                                            new EmptyBorder (3,
                                                             3,
                                                             3,
                                                             3)));
        this.setVisible (false);

    }

    public void setVisible (boolean visible)
    {

        super.setVisible (visible);

        if (visible)
        {

            this.findField.setSelectionStart (0);
            this.findField.setSelectionEnd (this.findField.getText ().length ());

            this.findField.grabFocus ();

            this.doFind ();

        }

    }

    public boolean isSearchActive ()
    {

        return this.searchActive;

    }

    public void mouseClicked (MouseEvent ev)
    {

        this.endFind ();

    }

    public int getLastCaretPosition ()
    {

        return this.lastCaret;

    }

    public void endFind ()
    {

        this.lastFindPoint = 0;
        this.clearHighlights ();
        this.searchActive = false;

        this.findField.setBackground (this.normalFindFieldBg);

    }

    public void clearHighlights ()
    {

        this.text.removeAllHighlights (this.idfp);
        this.text.removeAllHighlights (this.dfp);

    }

    public void doFind ()
    {

        this.clearHighlights ();

        this.lastFindPoint = -1;

        String toFind = this.findField.getText ().toLowerCase ();

        if (toFind.trim ().equals (""))
        {

            this.findField.setBackground (this.normalFindFieldBg);

            return;

        }

        this.searchActive = true;

        String content = this.text.getText ().toLowerCase ();

        // Need to replace the \r character since in the text display it displays a \n\r as a single character.
        content = content.replace (String.valueOf ('\r'),
                                   "");

        boolean found = false;

        int index = -1;

        while ((index = content.indexOf (toFind,
                                         index)) >= 0)
        {

            found = true;

            int end = index + toFind.length ();

            this.text.addHighlight (index,
                                    end,
                                    this.dfp,
                                    true);

            index = end;

        }
/*
            if (findex == index)
            {
System.out.println (index + ", " + end);
                found = true;

                this.lastFindPoint = index;

                try
                {

                    this.text.scrollRectToVisible (this.text.modelToView (this.lastFindPoint));

                    this.highlights.put (index,
                                         this.text.addHighlight (index, end, this.idfp));

                } catch (BadLocationException e) {

                }

            } else {

                try
                {

                    this.highlights.put (index,
                                         );

                } catch (Exception e) {

                    // Ignore.

                }

            }

        }
*/

        // Find the closest match to the current caret (if present).
        // Get the visible bounds of the content.
        int findex = this.text.getCaret ().getDot ();

        this.lastCaret = findex;

        if ((findex < 0) ||
            (findex == content.length ()))
        {

            findex = -1;

        } else
        {

            findex = this.lastFindPoint;

        }

        findex = content.indexOf (toFind,
                                  findex);

        if (findex < 0)
        {

            this.findField.setBackground (this.nonFoundFindFieldBg);

        } else
        {

            // Add a highlight.
            this.text.addHighlight (findex,
                                    findex + toFind.length (),
                                    this.idfp,
                                    true);

            this.lastFindPoint = findex + toFind.length ();

            this.findField.setBackground (this.normalFindFieldBg);

        }

    }

    public void goNext ()
    {

        if (this.highlights.size () == 0)
        {

            this.doFind ();

            return;

        }

        int toFindL = this.findField.getText ().length ();

        int nextP = 0;

        Object last = null;
        Object next = null;

        Iterator iter = this.highlights.keySet ().iterator ();

        while (iter.hasNext ())
        {

            Integer ind = (Integer) iter.next ();

            int i = ind.intValue ();

            if (last != null)
            {

                nextP = i;
                next = this.highlights.get (ind);

                break;

            }

            if (i == this.lastFindPoint)
            {

                last = this.highlights.get (ind);

            }

        }

        if (next == null)
        {

            // Get the first.
            Integer ind = (Integer) this.highlights.firstKey ();

            nextP = ind.intValue ();

            next = this.highlights.get (ind);

        }

        try
        {

            // Remove the last.
            this.text.removeHighlight (last);

            this.highlights.put (this.lastFindPoint,
                                 this.text.addHighlight (this.lastFindPoint,
                                                         this.lastFindPoint + toFindL,
                                                         this.dfp,
                                                         true));

            this.lastFindPoint = nextP;

            // Get the highlight.
            this.text.removeHighlight (next);

            this.highlights.put (this.lastFindPoint,
                                 this.text.addHighlight (this.lastFindPoint,
                                                         this.lastFindPoint + toFindL,
                                                         this.idfp,
                                                         true));

            this.text.scrollRectToVisible (this.text.modelToView (this.lastFindPoint));

        } catch (Exception e)
        {

            // Ignore.

        }

    }

    public void goPrevious ()
    {

        if (this.highlights.size () == 0)
        {

            this.doFind ();

            return;

        }

        int toFindL = this.findField.getText ().length ();

        int prevP = 0;

        Object last = null;
        Object prev = null;

        Iterator iter = this.highlights.keySet ().iterator ();

        while (iter.hasNext ())
        {

            Integer ind = (Integer) iter.next ();

            last = this.highlights.get (ind);

            int i = ind.intValue ();

            if (i == this.lastFindPoint)
            {

                break;

            }

            prev = last;

        }

        if (prev == null)
        {

            // Get the last.
            Integer ind = (Integer) this.highlights.lastKey ();

            prevP = ind.intValue ();

            prev = this.highlights.get (ind);

        }

        try
        {

            // Remove the last.
            this.text.removeHighlight (last);

            this.highlights.put (this.lastFindPoint,
                                 this.text.addHighlight (this.lastFindPoint,
                                                         this.lastFindPoint + toFindL,
                                                         this.dfp,
                                                         true));

            this.lastFindPoint = prevP;

            // Get the highlight.
            this.text.removeHighlight (prev);

            this.highlights.put (this.lastFindPoint,
                                 this.text.addHighlight (this.lastFindPoint,
                                                         this.lastFindPoint + toFindL,
                                                         this.idfp,
                                                         true));

            this.text.scrollRectToVisible (this.text.modelToView (this.lastFindPoint));

        } catch (Exception e)
        {

            // Ignore.

        }

    }

    public void insertUpdate (DocumentEvent ev)
    {

        this.doFind ();

    }

    public void changedUpdate (DocumentEvent ev)
    {

    }

    public void removeUpdate (DocumentEvent ev)
    {

        this.doFind ();

    }

}
