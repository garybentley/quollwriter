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

    private QuollEditorPanel     editor = null;
    private int                  lastCaret = -1;
    private BlockPainter         lineHighlight = new BlockPainter (Environment.getHighlightColor ());
    private TextUnderlinePainter issueHighlight = new TextUnderlinePainter (Color.RED);
    private List<IgnoreCheckbox>  ignores = new ArrayList ();
    private Set<Issue>           issuesToIgnore = new TreeSet (new IssueSorter ());
    private ActionListener       showIgnores = null;
    private boolean              endReached = false;
    private boolean              startReached = false;
    private boolean              doSentences = false;
    private boolean              donePara = false;
    private String               currPara = null;
    private Paragraph            paragraph = null;
    private Sentence             sentence = null;
    private TextIterator         textIter = null;
    private boolean              inited = false;

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

        this.editor.getEditor ().getCaret ().addChangeListener (new javax.swing.event.ChangeListener ()
        {
           
            public void stateChanged (javax.swing.event.ChangeEvent ev)
            {

                if (!_this.isShowing ())
                {
                    
                    return;
                    
                }
            
                _this.reset ();
                            
                _this.inited = false;
                
            }
            
        });

        this.editor.getEditor ().addFocusListener (new FocusAdapter ()
        {
           
            public void focusGained (FocusEvent ev)
            {

                //_this.clearHighlights ();
                
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

    public void reset ()
    {

        this.getIgnores ();

        this.clearHighlights ();
        
        this.textIter = null;
        this.sentence = null;
        this.paragraph = null;        

    }

    public int start ()
                      throws Exception
    {
        
        int c = 0;
        
        if (!this.inited)
        {
        
            c = this.start (this.editor.getEditor ().getSelectionStart ());
            
            this.inited = true;
            
        } else {
            
            c = this.start (0);
            
        }
        
        return c;
        
    }
    
    public int startPrevious ()
                              throws Exception
    {
        
        int c = 0;
        
        if (!this.inited)
        {
        
            c = this.startPrevious (this.editor.getEditor ().getSelectionStart ());
            
            this.inited = true;
            
        } else {
            
            c = this.startPrevious (this.editor.getEditor ().getText ().length ());
            
        }
        
        return c;
        
    }

    private int startPrevious (int    startAt)
                               throws Exception
    {
        
        this.donePara = true;
        
        this.textIter = new TextIterator (this.editor.getEditor ().getText ());
        this.paragraph = this.textIter.getPreviousClosestParagraphTo (startAt);

        if (this.paragraph == null)
        {
            
            // Show no problems.
            this.addNoProblems ();
            
            return 0;
            
        }
        
        this.sentence = this.paragraph.getPreviousClosestSentenceTo (startAt - this.paragraph.getAllTextStartOffset ());
        
        if (this.sentence == null)
        {
            
            // How???
            throw new GeneralException ("Unable to find sentence in paragraph");
            
        }
            
        int count = this.handleSentence (this.sentence);
                
        if (count == 0)
        {
            
            return this.previous ();
            
        }
        
        return count;
        
    }
    
    public int start (int    startAt)
                      throws Exception
    {
        
        this.doSentences = true;
        this.endReached = false;

        if (startAt == this.editor.getEditor ().getText ().length ())
        {
            
            startAt = 0;
            
        }
            
        this.textIter = new TextIterator (this.editor.getEditor ().getText ());
        this.paragraph = this.textIter.getNextClosestParagraphTo (startAt);

        if (this.paragraph == null)
        {
            
            if (startAt == 0)
            {
                
                return 0;
                
            }
            
            return this.start (0);
                        
        }
                    
        int count = -1;
        
        // If the sentence is the first then we start with the paragraph.
        if ((startAt == this.paragraph.getAllTextStartOffset ())
            ||
            (startAt < this.paragraph.getAllTextStartOffset ())
           )
        {
            
            count = this.handleParagraph (this.paragraph);
                
        } else {
        
            this.sentence = this.paragraph.getNextClosestSentenceTo (startAt - this.paragraph.getAllTextStartOffset ());
            
            if (this.sentence == null)
            {
                
                // How???
                throw new GeneralException ("Unable to find sentence in paragraph");
                
            }
        
            count = this.handleSentence (this.sentence);
            
        }
        
        if (count == 0)
        {
            
            return this.next ();
            
        }
        
        return count;
                        
    }

    private Sentence moveToNextSentence ()
    {

        if (this.sentence == null)
        {
            
            if (this.paragraph != null)
            {
                            
                this.sentence = this.paragraph.getFirstSentence ();
                
            }
            
        } else {
            
            this.sentence = this.sentence.getNext ();
                        
        }
        
        return this.sentence;
        
    }
        
    private Paragraph moveToNextParagraph ()
    {
        
        if (this.paragraph == null)
        {
            
            return null;
        
        }
                        
        this.paragraph = this.paragraph.getNext ();
        
        return this.paragraph;
        
    }
    
    private Paragraph moveToPreviousParagraph ()
    {
                    
        if (this.paragraph == null)
        {
            
            return null;
                    
        }
        
        this.paragraph = this.paragraph.getPrevious ();
        
        return this.paragraph;
        
    }

    private Sentence moveToPreviousSentence ()
    {
                
        if (this.sentence == null)
        {
            
            if (this.paragraph != null)
            {
                            
                this.sentence = this.paragraph.getLastSentence ();
                
            }
            
        } else {
            
            this.sentence = this.sentence.getPrevious ();
                        
        }
        
        return this.sentence;  
        
    }
    
    private void init ()
    {
        
        if (true)
        {
            
            return;
            
        }
        
        this.doSentences = false;
        this.endReached = false;
        this.donePara = true;
        
        if (this.lastCaret == this.editor.getEditor ().getText ().length ())
        {
            
            this.lastCaret = 0;
            
        }
            
        this.textIter = new TextIterator (this.editor.getEditor ().getText ());
        this.sentence = this.textIter.getSentenceAt (this.lastCaret);
        this.paragraph = this.textIter.getParagraphAt (this.lastCaret);
                  
    }
    
    private void clearHighlights ()
    {

        this.editor.getEditor ().removeAllHighlights (this.lineHighlight);
        this.editor.getEditor ().removeAllHighlights (this.issueHighlight);

    }

    private int handleParagraph (Paragraph p)
                                 throws    Exception
    {
        
        List<Issue> issues = RuleFactory.getParagraphIssues (p,
                                                             this.editor.getProjectViewer ().getProject ().getProperties ());

        this.setPositions (issues);
        
        if ((issues.size () != 0)
            &&
            (!this.allIgnores (issues))
           )
        {

            this.handleIssues (issues,
                               p);
            
            return issues.size ();
            
        }
        
        return 0;
        
    }
    
    private int handleSentence (Sentence s)
                                throws   Exception
    {
        
        List<Issue> issues = RuleFactory.getSentenceIssues (s,
                                                            this.editor.getProjectViewer ().getProject ().getProperties ());
        
        this.setPositions (issues);
        
        if ((issues.size () != 0)
            &&
            (!this.allIgnores (issues))
           )
        {

            this.handleIssues (issues,
                               s);
            
            return issues.size ();
            
        }
        
        return 0;
        
    }
    /*
    private int handleSentence (String s,
                                String para)
                                throws Exception
    {
        
        List<Issue> issues = RuleFactory.getSentenceIssues (s,
                                                            this.sentIter.inDialogue (),
                                                            this.sentIter.clone (),
                                                            this.editor.getProjectViewer ().getProject ().getProperties ());

        // This needs to be done here because the ignore needs the sentence start/end positions.
        this.setSentencePositions (issues);

        if ((issues.size () != 0)
            &&
            (!this.allIgnores (issues))
           )
        {
            this.handleIssues (issues,
                               this.sentIter.getOffset () + this.paraIter.getOffset (),
                               this.paraIter.getOffset () + this.sentIter.getOffset () + this.sentIter.current ().trim ().length (),
                               true);

            return issues.size ();

        }

        return 0;
        
    }
    */
/*
    private int handleParagraph (String p)
                                 throws Exception
    {
        
        this.donePara = true;
        
        List<Issue> pissues = RuleFactory.getParagraphIssues (p,
                                                              this.paraIter.inDialogue (),
                                                              this.paraIter.clone (),
                                                              this.editor.getProjectViewer ().getProject ().getProperties ());

        this.setParagraphPositions (pissues);

        if ((pissues.size () != 0)
            &&
            (!this.allIgnores (pissues))
           )
        {

            this.handleIssues (pissues,
                               this.paraIter.getOffset (),
                               this.paraIter.getOffset () + this.paraIter.current ().trim ().length (),
                               false);
                                    
            return pissues.size ();

        }                        
        
        return 0;
        
    }
  */  
    public int next ()
              throws Exception
    {

        // Needed in case we move previous, forces it to check the sentences first.
        this.donePara = true;
    
        if (this.textIter == null)
        {
            
            return this.start ();
                        
        }
        
        this.getIgnores ();

        this.clearHighlights ();

        boolean resetPerformed = false;
        
        final ProblemFinder _this = this;
        
        while (true)
        {

            if (!this.doSentences)
            {
                
                this.moveToNextParagraph ();
                                
                if (this.paragraph == null)
                {

                    this.addNoProblems ();
                    
                    UIUtils.createQuestionPopup (this.editor.getProjectViewer (),
                                                 "No more problems found",
                                                 Constants.INFO_ICON_NAME,
                                                 "No more problems found.  Return to the start of the {chapter}?",
                                                 "Yes, return to the start",
                                                 null,
                                                 new ActionListener ()
                                                 {
                                                    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        try
                                                        {
                                                            
                                                            _this.start (0);
                                                            
                                                        } catch (Exception e) {
                                                            
                                                            Environment.logError ("Unable to move back to start",
                                                                                  e);
                                                            
                                                            UIUtils.showErrorMessage (_this.editor.getProjectViewer (),
                                                                                      "Unable to move back to start of {chapter}");
                                                            
                                                        }
                                                        
                                                    }
                                                    
                                                 },
                                                 new ActionListener ()
                                                 {
                                                    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        _this.reset ();
                                                        
                                                    }
                                                    
                                                 },
                                                 null);
                    
                    return 0;
                                        
                } else {
                    
                    int count = this.handleParagraph (this.paragraph);
                    
                    if (count > 0)
                    {
                        
                        return count;
                        
                    }
                    
                }

                this.doSentences = true;

            } else {
                
                this.moveToNextSentence ();

                if (this.sentence == null)
                {
                    this.doSentences = false;
                    
                    continue;
                    
                } else {
                    
                    int count = this.handleSentence (this.sentence);
                    
                    if (count > 0)
                    {
                        
                        return count;
                        
                    }
                    
                }

            }

        }

    }

    public int previous ()
                  throws Exception
    {

        if (this.textIter == null)
        {

            return this.startPrevious ();
                        
        }
    
        this.getIgnores ();

        this.clearHighlights ();
        
        while (true)
        {
            
            if (!this.donePara)
            {
                
                this.moveToPreviousParagraph ();

                this.sentence = null;

                if (this.paragraph == null)
                {
                    
                    this.addNoProblems ();
    
                    UIUtils.showMessage (this.editor,
                                         "No more problems found");
                    
                    this.reset ();
                    
                    return 0;
                    
                }

                int count = this.handleParagraph (this.paragraph);
                
                if (count > 0)
                {
                    
                    return count;
                    
                }
                                
                this.donePara = true;
                
            } else {
                
                this.moveToPreviousSentence ();

                if (this.sentence == null)
                {
                    
                    this.donePara = false;
                    continue;
                    
                }

                int count = this.handleSentence (this.sentence);
                
                if (count > 0)
                {
                    
                    return count;
                    
                }
                
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

        this.add (l);

        this.getParent ().getParent ().validate ();
        this.getParent ().getParent ().repaint ();
                
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

    private IgnoreCheckbox createSentenceIssueItem (final Issue     iss,
                                                    final TextBlock textBlock)
    {

        final ProblemFinder _this = this;

        final QTextEditor ed = this.editor.getEditor ();
        
        IgnoreCheckbox cb = this.createIssueItem (iss);
        
        final int sentenceStart = sentence.getAllTextStartOffset ();
        final int sentenceEnd = sentence.getAllTextEndOffset ();

        final Issue issue = iss;

        cb.addMouseListener (new MouseAdapter ()
        {

            public void mouseEntered (MouseEvent ev)
            {

                try
                {

                    ed.addHighlight (iss.getStartIssuePosition (),
                                     iss.getEndIssuePosition (),
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

    private IgnoreCheckbox createIssueItem (Issue iss)
    {
        
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

        return cb;
        
    }

    private void setPositions (List<Issue> issues)
    {
        
        try
        {

            for (Issue i : issues)
            {

                i.setStartPosition (this.editor.getEditor ().getDocument ().createPosition (i.getStartIssuePosition ()));

                // Set the end position of the sentence.
                i.setEndPosition (this.editor.getEditor ().getDocument ().createPosition (i.getEndIssuePosition ()));

            }

        } catch (Exception e)
        {

            // Ignore.

        }        
        
    }
    
    private void handleIssues (final List<Issue> issues,
                               final TextBlock   textBlock)
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
                        
                        _this.editor.scrollToPosition (textBlock.getAllTextStartOffset ());//selStart);//_this.sentIter.getOffset ());
                        
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to scroll to: " +
                                              textBlock.getAllTextStartOffset (), //selStart, //_this.sentIter.getOffset (),
                                              e);
                        
                    }
                    
                }
                
            });

            final QTextEditor ed = this.editor.getEditor ();

            for (Issue i : issues)
            {

                if (i.getRule () instanceof SentenceRule)
                {
            
                    this.add (this.createSentenceIssueItem (i,
                                                            textBlock));
                    
                } else {
                    
                    this.add (this.createIssueItem (i));
                    
                }

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

            ed.addHighlight (textBlock.getAllTextStartOffset (), //sentenceStart,
                             textBlock.getAllTextEndOffset (), //sentenceEnd,
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
