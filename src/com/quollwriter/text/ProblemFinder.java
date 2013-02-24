package com.quollwriter.text;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.text.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.quollwriter.*;

import com.quollwriter.text.rules.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.TextUnderlinePainter;
import com.quollwriter.ui.components.ActionAdapter;

public class ProblemFinder extends Box
{

    private SentenceIterator     iter = null;
    private Document             doc = null;
    private QuollEditorPanel     editor = null;
    private int                  lastCaret = -1;
    private BlockPainter         lineHighlight = new BlockPainter (Environment.getHighlightColor ());
    private TextUnderlinePainter issueHighlight = new TextUnderlinePainter (Color.RED);
    private List<IgnoreCheckbox>  ignores = new ArrayList ();
    private Set<Issue>           issuesToIgnore = new TreeSet (new IssueSorter ());
    private Position             lastSentenceEnd = null;
    private Position             lastSentenceStart = null;
    private ActionListener       showIgnores = null;
    private boolean              endReached = false;

    public ProblemFinder(QuollEditorPanel editor)
    {

        super (BoxLayout.Y_AXIS);

        this.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));

        this.editor = editor;

        // Load the ignores.
        try
        {

            this.issuesToIgnore = ((ProjectViewer) this.editor.getProjectViewer ()).getProblemFinderIgnores (this.editor.getChapter (),
                                                                                                             this.editor.getEditor ().getDocument ());

        } catch (GeneralException e)
        {

            Environment.logError ("Unable to load problem finder ignores for chapter: " +
                                  this.editor.getChapter (),
                                  e);

        }

        final ProblemFinder _this = this;

        this.editor.getEditor ().addFocusListener (new FocusAdapter ()
        {
           
            public void focusGained (FocusEvent ev)
            {

                _this.clearHighlights ();
                
            }
            
        });

    }

    public void removeAllIgnores ()
    {
        
        this.issuesToIgnore = new TreeSet (new IssueSorter ());
        
        if (this.showIgnores != null)
        {
        
            this.showIgnores.actionPerformed (new ActionEvent (this,
                                                               0,
                                                               ""));
            
        }

        this.editor.getProjectViewer ().fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                          ProjectEvent.UNIGNORE);
        
    }

    public Set<Issue> getIgnoredIssues ()
    {
        
        return this.issuesToIgnore;
        
    }

    public void saveIgnores ()
    {

        if (this.issuesToIgnore != null)
        {

            // Check to see if they are still valid.
            try
            {

                ((ProjectViewer) this.editor.getProjectViewer ()).saveProblemFinderIgnores (this.editor.getChapter (),
                                                                                            this.issuesToIgnore);

            } catch (GeneralException e)
            {

                Environment.logError ("Unable to save problem finder ignores for chapter: " +
                                      this.editor.getChapter (),
                                      e);

            }

        }

    }

    public String getSentence ()
    {

        return this.iter.current ();

    }

    public void reset ()
    {

        this.getIgnores ();

        this.clearHighlights ();
        this.lastCaret = -1;
        this.iter = null;
        this.lastSentenceEnd = null;

    }

    public void start ()
                throws Exception
    {

        this.iter = new SentenceIterator (this.editor.getEditor ().getText ());

        // Get the caret.
        this.lastCaret = this.editor.getEditor ().getSelectionStart ();

        this.iter.init (this.lastCaret);

    }

    private void clearHighlights ()
    {

        this.editor.getEditor ().removeAllHighlights (this.lineHighlight);
        this.editor.getEditor ().removeAllHighlights (this.issueHighlight);

    }

    public int previous ()
                  throws Exception
    {

        this.getIgnores ();

        this.clearHighlights ();

        while (true)
        {

            if (this.lastSentenceStart != null)
            {

                this.iter.setText (this.editor.getEditor ().getText ());
                this.iter.init (this.lastSentenceStart.getOffset () + 1);

            }

            //this.iter.reinit (this.editor.getEditor ().getText ());

            String s = this.iter.previous ();

            if (s == null)
            {

                return 0;

            }

            List<Issue> issues = RuleFactory.getIssues (s,
                                                        this.iter.inDialogue (),
                                                        this.editor.getProjectViewer ().getProject ().getProperties ());

            this.setSentencePositions (issues);

            if ((issues.size () != 0)
                &&
                (!this.allIgnores (issues))
               )
            {

                this.handleIssues (issues);

                return issues.size ();

            }

        }

    }

    private void addNoProblems ()
    {
        
        this.removeAll ();

        JLabel l = new JLabel ("No problems found.",
                               Environment.getIcon ("information",
                                                    Constants.ICON_MENU),
                               SwingConstants.TRAILING);

        l.setAlignmentX (Component.LEFT_ALIGNMENT);
        l.setOpaque (false);
        l.setBorder (new EmptyBorder (5,
                                      10,
                                      3,
                                      3));
        /*
        l.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         100));
         */

        this.add (l);

        this.getParent ().getParent ().validate ();
        this.getParent ().getParent ().repaint ();
                
    }

    public int next ()
              throws Exception
    {

        this.getIgnores ();

        this.clearHighlights ();

        boolean resetPerformed = false;

        while (true)
        {

            if (this.endReached)
            {

                // No more to be found, maybe ask to start at top again?
                if (resetPerformed)
                {

                    this.addNoProblems ();

                    return 0;

                } else
                {

                    if (JOptionPane.showConfirmDialog (this.editor,
                                                       Environment.replaceObjectNames ("No more problems found.  Return to the start of the {chapter}?"),
                                                       "No more problems found",
                                                       JOptionPane.YES_NO_OPTION,
                                                       JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                    {

                        this.lastSentenceEnd = this.editor.getEditor ().getDocument ().createPosition (0);
                        this.iter.init (0);
                        resetPerformed = true;
                        this.endReached = false;

                    } else {
                        
                        this.addNoProblems ();
                        
                        return 0;
                    
                    }

                }

            }

            this.iter.reinit (this.editor.getEditor ().getText ());

            String s = this.iter.next ();

            if (s == null)
            {
                
                this.endReached = true;
                
                continue;
/*
                this.addNoProblems ();

                return 0;
*/
            }

            List<Issue> issues = RuleFactory.getIssues (s,
                                                        this.iter.inDialogue (),
                                                        this.editor.getProjectViewer ().getProject ().getProperties ());

            // This needs to be done here because the ignore needs the sentence start/end positions.
            this.setSentencePositions (issues);

            if ((issues.size () != 0)
                &&
                (!this.allIgnores (issues))
               )
            {

                this.handleIssues (issues);

                return issues.size ();

            }

        }

    }

    private void setSentencePositions (List<Issue> issues)
    {

        try
        {

            int sentenceStart = this.iter.getOffset ();
            int sentenceEnd = sentenceStart + this.getSentence ().trim ().length () - 1;

            this.lastSentenceEnd = this.editor.getEditor ().getDocument ().createPosition (sentenceStart + this.getSentence ().length ());
            this.lastSentenceStart = this.editor.getEditor ().getDocument ().createPosition (sentenceStart);

            for (Issue i : issues)
            {

                i.setSentenceStartPosition (this.editor.getEditor ().getDocument ().createPosition (sentenceStart));

                // Set the end position of the sentence.
                i.setSentenceEndPosition (this.editor.getEditor ().getDocument ().createPosition (sentenceEnd));

            }

        } catch (Exception e)
        {

            // Ignore.

        }

    }

    public void removeCheckboxesForRule (Rule r)
    {

        for (IgnoreCheckbox b : this.ignores)
        {

            if (r.getId ().equals (b.issue.getRule ().getId ()))
            {

                this.remove (b);

            }

        }

        this.getParent ().getParent ().validate ();
        this.getParent ().getParent ().repaint ();

        // this.validate ();
        // this.repaint ();

    }

    private void getIgnores ()
    {

        for (IgnoreCheckbox b : this.ignores)
        {

            if (b.isSelected ())
            {

                this.issuesToIgnore.add (b.issue);

                this.editor.getProjectViewer ().fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                                  ProjectEvent.IGNORE);

            } else {
                
                if (this.issuesToIgnore.remove (b.issue))
                {
    
                    this.editor.getProjectViewer ().fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                                      ProjectEvent.UNIGNORE);

                }
                
            }

        }

        this.ignores = new ArrayList ();

    }

    private IgnoreCheckbox createIssueItem (Issue iss)
    {

        final ProblemFinder _this = this;

        final QTextEditor ed = this.editor.getEditor ();
        
        IgnoreCheckbox cb = new IgnoreCheckbox ("<html>" + iss.getDescription () + "</html>",
                                                iss,
                                                this.editor);
        cb.setAlignmentX (Component.LEFT_ALIGNMENT);
        cb.setOpaque (false);
        cb.setBorder (new EmptyBorder (5,
                                       10,
                                       3,
                                       3));
        // cb.setToolTipText (i.getRule ().getDescription ());

        this.ignores.add (cb);

        final int sentenceStart = this.iter.getOffset ();
        final int sentenceEnd = sentenceStart + this.getSentence ().trim ().length ();

        final int start = TextUtilities.getWordPosition (this.getSentence (),
                                                         iss);

        final Issue issue = iss;

        cb.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {

                try
                {

                    ed.addHighlight (sentenceStart + start,
                                     sentenceStart + start + issue.getLength (),
                                     _this.issueHighlight,
                                     false);

                } catch (Exception e)
                {

                    // Ignore.

                }

            }

            public void mouseExited (MouseEvent ev)
            {

                ed.removeAllHighlights (_this.issueHighlight);

            }

        });
        
        return cb;
        
    }

    private void handleIssues (List<Issue> issues)
                        throws Exception
    {

        Collections.sort (issues,
                          new IssueSorter ());

        List<Issue> ignored = this.removeIgnores (issues);

        if (issues.size () > 0)
        {

            this.removeAll ();

            final ProblemFinder _this = this;

            this.lastCaret = this.editor.getEditor ().getSelectionStart ();

            // Convert to the view.
            int height = -1;

            SwingUtilities.invokeLater (new Runnable ()
            {

                public void run ()
                {
                    
                    try
                    {
                        
                        _this.editor.scrollToPosition (_this.iter.getOffset ());
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to scroll to: " +
                                              _this.iter.getOffset (),
                                              e);
                        
                    }
                    
                }
                
            });

            final QTextEditor ed = this.editor.getEditor ();

            for (Issue i : issues)
            {

                this.add (this.createIssueItem (i));

            }

            if (ignored.size () > 0)
            {

                final JLabel l = UIUtils.createClickableLabel (ignored.size () + " ignored problem" + (ignored.size () == 1 ? "" : "s") + ".  Click to view.",
                                                               Environment.getIcon ("warning",
                                                                                    Constants.ICON_MENU));
                
                l.setBorder (new EmptyBorder (5,
                                              10,
                                              3,
                                              3));

                this.add (l);

                final java.util.List<IgnoreCheckbox> ignoredItems = new ArrayList ();

                for (Issue i : ignored)
                {

                    IgnoreCheckbox icb = this.createIssueItem (i);

                    ignoredItems.add (icb);
                    icb.setVisible (false);
                    icb.setSelected (true);
                    this.add (icb);

                    this.editor.getProjectViewer ().fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                                      ProjectEvent.UNIGNORE);
                                        
                }

                this.showIgnores = new ActionAdapter ()
                {
                  
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        for (IgnoreCheckbox icb : ignoredItems)
                        {
                            
                            icb.setVisible (true);

                            icb.setSelected ("selected".equals (ev.getActionCommand ()));

                            //_this.issuesToIgnore.remove (icb.issue);
                                
                        }

                        l.setVisible (false);

                        _this.getParent ().getParent ().validate ();
                        _this.getParent ().getParent ().repaint ();
                        
                    }
                    
                };

                l.addMouseListener (new MouseAdapter ()
                {
                    
                    public void mousePressed (MouseEvent ev)
                    {

                        _this.showIgnores.actionPerformed (new ActionEvent (l,
                                                                            0,
                                                                            "selected"));

                        _this.editor.getProjectViewer ().fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                                           ProjectEvent.UNIGNORE);
                        
                    }
                    
                });
                
            }

            final int sentenceStart = this.iter.getOffset ();
            final int sentenceEnd = sentenceStart + this.getSentence ().trim ().length ();

            ed.addHighlight (sentenceStart,
                             sentenceEnd,
                             this.lineHighlight,
                             false);

            this.getParent ().getParent ().validate ();
            this.getParent ().getParent ().repaint ();

        }

    }

    private boolean allIgnores (List<Issue> issues)
    {

        for (Issue i : issues)
        {
            
            if (!this.shouldIgnore (i))
            {
                
                return false;
                
            }
            
        }
        
        return true;
        
    }

    private List<Issue> removeIgnores (List<Issue> issues)
    {

        Iterator<Issue> iter = issues.iterator ();

        List<Issue> ignored = new ArrayList ();

        while (iter.hasNext ())
        {

            Issue i = iter.next ();

            if (this.shouldIgnore (i))
            {

                ignored.add (i);
                iter.remove ();

            }

        }

        return ignored;

    }

    private boolean shouldIgnore (Issue iss)
    {

        for (Issue i : this.issuesToIgnore)
        {

            if (i.equals (iss))
            {

                return true;

            }

        }

        return false;

    }

}
