package com.quollwriter.ui.sidebars;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Enumeration;
import java.util.Iterator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import com.quollwriter.ui.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.components.ScrollableBox;

public class ProblemFinderSideBar extends AbstractSideBar<ProjectViewer> implements DocumentListener, ProjectEventListener
{

   public static final String ID = "problemfinder";
    private static final String TITLE_PREFIX = "Find Problems";

    private ChapterProblemResultsBox results = null;
    private Rule rule = null;
    private Box content = null;
    private JLabel noMatches = null;
    private JLabel ruleSummary = null;
    private JLabel ignoredProblemsLabel = null;
    private JLabel searchingLabel = null;
    private java.util.Timer updateTimer = null;
    private Set<Issue> ignored = null;
    
    public ProblemFinderSideBar (ProjectViewer v,
                                 Rule          r)
    {

        super (v);

        this.rule = r;

        v.addProjectEventListener (this);

    }

    @Override
    public String getId ()
    {
      
         return ID;
      
    }
    
    @Override
    public void eventOccurred (ProjectEvent ev)
    {

        if (ev.getType ().equals (ProjectEvent.PROBLEM_FINDER))
        {

            this.find ();

        }

    }

    @Override
    public boolean canClose ()
    {

        return true;

    }

    @Override
    public String getTitle ()
    {

        return Environment.getUIString (LanguageStrings.project,
                                        LanguageStrings.sidebar,
                                        LanguageStrings.problemfinder,
                                        LanguageStrings.title);
        //TITLE_PREFIX;

    }

    @Override
    public void onHide ()
    {

    }

    @Override
    public void onClose ()
    {

        this.viewer.removeChapterDocumentListener (this);
        this.updateTimer.cancel ();
        this.viewer.removeProjectEventListener (this);

    }

    @Override
    public String getIconType ()
    {

        return Constants.PROBLEM_FINDER_ICON_NAME;

    }

    @Override
    public List<JComponent> getHeaderControls ()
    {

        final ProblemFinderSideBar _this = this;

        final List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.problemfinder);
        prefix.add (LanguageStrings.headercontrols);
        prefix.add (LanguageStrings.items);
        
        List<JComponent> buts = new ArrayList ();

        JButton but = UIUtils.createButton ("refresh",
                                            Constants.ICON_SIDEBAR,
                                            Environment.getUIString (prefix,
                                                                     LanguageStrings.findall,
                                                                     LanguageStrings.tooltip),
                                            //"Click to find all problems for this rule",
                                            new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.find ();

            }

        });

        buts.add (but);

        but = UIUtils.createButton (Constants.OPTIONS_ICON_NAME,
                                    Constants.ICON_SIDEBAR,
                                    Environment.getUIString (prefix,
                                                             LanguageStrings.options,
                                                             LanguageStrings.tooltip),
                                    //"Click to view the the options",
                                    new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {
               
                JMenuItem mi = null;

                JPopupMenu popup = new JPopupMenu ();

                popup.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                            LanguageStrings.options,
                                                                            LanguageStrings.popupmenu,
                                                                            LanguageStrings.items,
                                                                            LanguageStrings.edit,
                                                                            LanguageStrings.tooltip),
                                                       //"Edit this rule",
                                                   Constants.EDIT_ICON_NAME,
                                                   new ActionListener ()
                                                   {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            ProblemFinderRuleConfig config = _this.viewer.getProblemFinderRuleConfig ();

                                                            _this.viewer.showProblemFinderRuleConfig ();

                                                            config.editRule (_this.rule,
                                                                             false);

                                                        }

                                                   }));

               popup.add (UIUtils.createMenuItem (Environment.getUIString (prefix,
                                                                            LanguageStrings.options,
                                                                            LanguageStrings.popupmenu,
                                                                            LanguageStrings.items,
                                                                            LanguageStrings.view,
                                                                            LanguageStrings.tooltip),
                                                  //"Show the Problem Finder rules",
                                                  Constants.VIEW_ICON_NAME,
                                                  new ActionListener ()
                                                  {

                                                       public void actionPerformed (ActionEvent ev)
                                                       {

                                                          _this.viewer.showProblemFinderRuleConfig ();

                                                       }

                                                  }));

              JComponent s = (JComponent) ev.getSource ();

              popup.show (s,
                          s.getWidth () / 2,
                          s.getHeight ());

            }

        });

        buts.add (but);

        return buts;

    }

    public JComponent getContent ()
    {

        final ProblemFinderSideBar _this = this;
    
        Box b = new Box (BoxLayout.Y_AXIS);

        this.content = new Box (BoxLayout.Y_AXIS);
        this.content.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.content.setOpaque (false);

        this.content.setMinimumSize (new Dimension (250,
                                         250));

        this.noMatches = UIUtils.createInformationLabel (Environment.getUIString (LanguageStrings.project,
                                                                                  LanguageStrings.sidebar,
                                                                                  LanguageStrings.problemfinder,
                                                                                  LanguageStrings.notfound));
        //"No problems found.");

        this.noMatches.setVisible (false);

        this.noMatches.setBorder (UIUtils.createPadding (5, 10, 0, 5));

        this.ruleSummary = UIUtils.createInformationLabel (this.rule.getSummary ());

        this.ruleSummary.setBorder (UIUtils.createPadding (5, 10, 5, 5));

        b.add (this.ruleSummary);

        this.ignoredProblemsLabel = UIUtils.createClickableLabel ("",
                                                                  Environment.getIcon ("warning",
                                                                                       Constants.ICON_MENU));

        this.ignoredProblemsLabel.setBorder (UIUtils.createPadding (5, 5, 5, 5));

        this.ignoredProblemsLabel.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                List<String> prefix = new ArrayList ();
                prefix.add (LanguageStrings.project);
                prefix.add (LanguageStrings.sidebar);
                prefix.add (LanguageStrings.problemfinder);
                prefix.add (LanguageStrings.unignore);
            
                int s = _this.ignored.size ();
                String pl = (s > 1 ? "s" : "");
            
                UIUtils.createQuestionPopup (_this.viewer,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.confirmpopup,
                                                                      LanguageStrings.title),
                                             Constants.PROBLEM_FINDER_ICON_NAME,
                                             String.format (Environment.getUIString (prefix,
                                                                                     LanguageStrings.confirmpopup,
                                                                                     LanguageStrings.text),
                                                            s,
                                                            _this.rule.getSummary ()),
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.confirmpopup,
                                                                      LanguageStrings.confirm),
                                             //String.format ("Yes, un-ignore %s",
                                             //               (s == 1 ? "it" : "them")),
                                             null,
                                             new ActionListener ()
                                             {
 
                                                public void actionPerformed (ActionEvent ev)
                                                {
 
                                                    Set<Chapter> chaps = new HashSet ();
 
                                                    for (Issue ig : _this.ignored)
                                                    {
 
                                                        ig.getChapter ().getProblemFinderIgnores ().remove (ig);
 
                                                        chaps.add (ig.getChapter ());
 
                                                    }
 
                                                    try
                                                    {
 
                                                        for (Chapter c : chaps)
                                                        {
 
                                                            _this.viewer.saveProblemFinderIgnores (c);
 
                                                        }
 
                                                    } catch (Exception e) {
 
                                                        Environment.logError ("Unable to update problem finder ignores",
                                                                              e);
 
                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                  Environment.getUIString (prefix,
                                                                                                           LanguageStrings.actionerror));
                                                                                  //"Unable to update problem finder ignores.");
 
                                                    }
 
                                                    _this.viewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                                                   ProjectEvent.UNIGNORE);
 
                                                    _this.find ();
 
                                                }
 
                                             },
                                             null,
                                             null,
                                             null);

            }

        });        
        
        this.ignoredProblemsLabel.setVisible (false);
        
        b.add (this.ignoredProblemsLabel);
        
        this.searchingLabel = UIUtils.createLoadingLabel (Environment.getUIString (LanguageStrings.project,
                                                                                   LanguageStrings.sidebar,
                                                                                   LanguageStrings.problemfinder,
                                                                                   LanguageStrings.loading));
                                                          //"Finding problems... please wait...");
        this.searchingLabel.setBorder (UIUtils.createPadding (5, 5, 5, 5));
        
        this.searchingLabel.setVisible (false);
        
        b.add (this.searchingLabel);
        
        b.add (this.noMatches);

        b.add (this.wrapInScrollPane (this.content,
                                      true));

        return b;

    }

    @Override
    public void init (String saveState)
               throws GeneralException
    {

        super.init (saveState);

        this.viewer.addChapterDocumentListener (this);

        // Find all the matches.
        this.find ();

    }

    @Override
    public void insertUpdate (DocumentEvent ev)
    {

        this.find ();

    }

    @Override
    public void changedUpdate (DocumentEvent ev)
    {

        this.find ();

    }

    @Override
    public void removeUpdate (DocumentEvent ev)
    {

        this.find ();

    }

    public void setRule (Rule r)
    {

        this.rule = r;

        this.ruleSummary.setText (this.rule.getSummary ());

        this.find ();

    }

    private void update (final Map<Chapter, Set<Issue>> probs,
                         final Set<Issue>               ignored)
    {

        final ProblemFinderSideBar _this = this;

         this.searchingLabel.setVisible (false);
        
        boolean expandSearchResults = false;

        java.util.List<TreePath> openPaths = new ArrayList ();

        if (this.results != null)
        {

            Enumeration<TreePath> paths = this.results.getTree ().getExpandedDescendants (new TreePath (this.results.getTree ().getModel ().getRoot ()));

            if (paths != null)
            {

                while (paths.hasMoreElements ())
                {

                    openPaths.add (paths.nextElement ());

                }

            }

        } else {

            expandSearchResults = UserProperties.getAsBoolean (Constants.SHOW_EACH_CHAPTER_FIND_RESULT_PROPERTY_NAME);

        }

        this.content.removeAll ();

        this.ignored = ignored;
        
        this.ignoredProblemsLabel.setVisible (this.ignored.size () > 0);        
        
        List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.problemfinder);
        prefix.add (LanguageStrings.ignored);
        
        if (ignored.size () > 0)
        {

            int s = ignored.size ();

            String t = "";
            
            if (s == 1)
            {
                
                t = Environment.getUIString (prefix,
                                             LanguageStrings.single);
                //"%s problem%s for this rule has been ignored, click to un-ignore it.";
                
            } else {
                
                t = String.format (Environment.getUIString (prefix,
                                                            LanguageStrings.plural),
                                   Environment.formatNumber (s));
                                   //"%s problem%s for this rule have been ignored, click to un-ignore them.";
                
            }
            
            this.ignoredProblemsLabel.setText (t);

        }

        // Remove the ignored.
        // This is very inefficient and may need some pre-filtering such as putting the ignored
        // into chapter groups.
        for (Issue igi : ignored)
        {

            Chapter igc = igi.getChapter ();

            if (igc != null)
            {

                Set<Issue> issues = probs.get (igc);

                Iterator<Issue> iter = issues.iterator ();

                while (iter.hasNext ())
                {

                    Issue i = iter.next ();

                    if (igi.equals (i))
                    {

                        iter.remove ();

                    }

                }

                if (issues.size () == 0)
                {

                    probs.remove (igc);

                }

            }

        }

        ChapterProblemResultsBox r = new ChapterProblemResultsBox (Environment.getUIString (LanguageStrings.project,
                                                                                            LanguageStrings.sidebar,
                                                                                            LanguageStrings.problemfinder,
                                                                                            LanguageStrings.results,
                                                                                            LanguageStrings.title),
                                                                   //"Found",
                                                                   null,
                                                                   Chapter.OBJECT_TYPE,
                                                                   this.viewer,
                                                                   probs);

        this.results = r;

        r.init ();

        DefaultTreeModel dtm = (DefaultTreeModel) this.results.getTree ().getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        for (TreePath p : openPaths)
        {

            this.results.getTree ().expandPath (UIUtils.getTreePathForUserObject (root,
                                                                                  ((DefaultMutableTreeNode) p.getLastPathComponent ()).getUserObject ()));

        }

        this.content.add (r);

        if (expandSearchResults)
        {

            r.exapndAllResultsInTree ();

        }

        this.noMatches.setVisible (probs.size () == 0);

        this.setTitle (this.getTitle ());

        this.validate ();
        this.repaint ();

    }

    public void find ()
    {

         boolean immediate = (this.updateTimer == null);
 
         if (!immediate)
         {
 
             this.updateTimer.cancel ();
 
         }
 
         this.searchingLabel.setVisible (true);
         this.ignoredProblemsLabel.setVisible (false);
         
         if (this.results != null)
         {
            
            this.results.clearHighlight ();
            this.results.setVisible (false);
            
         }
              
        this.updateTimer = new java.util.Timer ("problem-finder-side-bar", true);

        final ProblemFinderSideBar _this = this;

        this.updateTimer.schedule (new java.util.TimerTask ()
        {

            @Override
            public void run ()
            {

                Thread.currentThread ().setPriority (Thread.MIN_PRIORITY);

                Set<Issue> _ignored = new HashSet ();

                try
                {

                    _ignored = _this.viewer.getProblemFinderIgnores (_this.rule);

                } catch (Exception e) {

                    Environment.logError ("Unable to get problem finder ignores for rule: " +
                                          _this.rule,
                                          e);

                }

                final Set<Issue> ignored = _ignored;
                final Map<Chapter, Set<Issue>> probs = _this.viewer.getProblemsForAllChapters (_this.rule);

                UIUtils.doLater (new ActionListener ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {
                    
                        _this.update (probs,
                                      ignored);

                    }

                });

            }

        },
        (immediate ? 0 : 750));

    }

    @Override
    public void onShow ()
    {

    }

}
