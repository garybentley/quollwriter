package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.jdom.*;

import com.gentlyweb.xml.*;
import com.gentlyweb.properties.*;

import wordcram.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.whatsnewcomps.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.FormItem;

public class WordCloudPanel extends BasicQuollPanel<AbstractProjectViewer>
{
    
    public static final String PANEL_ID = "wordcloud";
    
    private JSplitPane splitPane = null;
    
    public WordCloudPanel (AbstractProjectViewer pv)
                    throws Exception
    {
        
        super (pv,
               "Word Cloud",
               Constants.INFO_ICON_NAME,
               null);

    }

    public JComponent getContent ()
    {
        
        this.splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
                                         false);
        this.splitPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.splitPane.setDividerSize (UIUtils.getSplitPaneDividerSize ());
        this.splitPane.setOpaque (false);

        this.splitPane.setBorder (null);
        
        Box b = new ScrollableBox (BoxLayout.Y_AXIS);
        
        b.setBorder (UIUtils.createPadding (0, 5, 0, 0));
        b.setOpaque (false);
        
        Header h = UIUtils.createHeader ("Show",
                                         Constants.SUB_PANEL_TITLE,
                                         null,
                                         null);
        
        b.add (h);

        b.add (Box.createVerticalStrut (5));        
        
        WordBox w = new WordBox ();
        
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        w.setBorder (UIUtils.createPadding (0, 5, 5, 5));
        
        b.add (w);
        
        StringBuilder t = new StringBuilder ();
        
        Book bk = this.viewer.getProject ().getBooks ().get (0);
        
        java.util.List<Chapter> chapters = bk.getChapters ();

        for (Chapter c : chapters)
        {

            t.append (this.viewer.getCurrentChapterText (c));
            
            t.append ("\n\n");
        
        }
                        
        WordFilter wf = new DefaultWordFilter ()
        {
            
            @Override
            public boolean accept (String word)
            {
                
                if (!super.accept (word))
                {
                    
                    return false;
                    
                }
                
                if (word.equals ("Ben"))
                {
                    
                    return false;
                    
                }
                
                if (word.equals ("Sam"))
                {
                    
                    return false;
                    
                }
                
                if (word.equals ("father"))
                {
                    
                    return false;
                    
                }

                if (word.equals ("job"))
                {
                    
                    return false;
                    
                }
                
                return true;

            }
                        
        };
        
        WordsList wl = new WordCounter ().withFilter (wf).count (t.toString (), new RenderOptions ());
                
        wl.add (new Word ("The Acquirer", 1f));
        wl.add (new Word ("Ben", 0.7f));
        wl.add (new Word ("Sam", 0.7f));
        wl.add (new Word ("Father", 0.5f));
        wl.add (new Word ("Job", 0.5f));
        wl.add (new Word ("Susan", 0.3f));
        wl.add (new Word ("Sister Meg", 0.3f));
        wl.add (new Word ("Sammy", 0.3f));
        wl.add (new Word ("Brothers", 0.3f));
        final WordCram wc = new WordCram ();
        
        int width = 300;
        int height = 700;
        
        //wc.withDimensions (new Dimension (width, height));
        
        wc.fromWords (wl);
        
        //wc.sizedByWeight(10, 30);
        wc.allowWordsWithinWords (false);
        wc.maxNumberOfWordsToDraw (500);
        wc.maxAttemptsToPlaceWord (10000);
        wc.withWordPadding (new Insets (2, 2, 2, 2));
        wc.withFont(new Font ("Futura",Font.BOLD, 1));
        
        ImageShaper is = new ImageShaper ();
                
        wc.withPlacer (new ShapeBasedPlacer (is.shape (UIUtils.getImage (new java.io.File ("d:/development/github/quollwriter/imgs/shape-arrow.png")),
                                                       java.awt.Color.black)));        

        java.awt.image.BufferedImage im = UIUtils.createBufferedImage (1000, 700);
        
        im = UIUtils.drawStringOnImage (im,
                                        "The\nAcquirer",
                                        new Font ("Futura", Font.BOLD, 500),
                                        java.awt.Color.black,
                                        new java.awt.Point (0, 0));        
                                                       
        wc.withPlacer (new ShapeBasedPlacer (is.shape (im,
                                                       java.awt.Color.black)));        


        WordPainter wp = new WordPainter ()
        {
           
            @Override
            public Paint paintFor (Word w)
            {
                
                if (w.getWeight () >= 0.7f)
                {
                    
                    return UIUtils.getColor ("#DBC900");
                    
                }
                
                if (w.getWeight () >= 0.5f)
                {
                    
                    return UIUtils.getColor ("#ED7D3A");
                    
                }

                if (w.getWeight () >= 0.3f)
                {
                    
                    return UIUtils.getColor ("#348AA7");
                    
                }

                return java.awt.Color.black;
                
            }
            
        };
        
        wc.withAngler (new WordAngler ()
        {
           
            @Override
            public float angleFor (Word w)
            {

                if (w.getWeight () >= 0.5f)
                {

                    return 0;
                
                }
                
                if (w.getWeight () >= 0.3f)
                {

                    return Anglers.upAndDown ().angleFor (w);
                
                }

                return Anglers.mostlyHoriz ().angleFor (w);
                
            }
            
        });
        
        wc.withFonter (new WordFonter ()
        {
           
            @Override
            public Font fontFor (Word w)
            {
                
                if (w.getWeight () >= 0.5f)
                {

                    return new Font ("Futura", Font.BOLD, 1);
                    
                }
                
                return new Font ("Futura", Font.BOLD, 1);
                
            }
            
        });
        
        wc.withSizer (new WordSizer ()
        {
           
            @Override
            public float sizeFor(Word word, int wordRank, int wordCount)
            {
                
                if (word.getWeight () == 1)
                {
                    
                    return 50;
                    
                }
                
                if (word.getWeight () == 0.7f)
                {
                    
                    return 30;
                    
                }

                if (word.getWeight () == 0.3f)
                {
                    
                    return 20;
                    
                }

                return WordUtils.interpolate (10,
                                              20,
                                              word.getWeight ());
                
            }
            
        });

        final WordCramPanel wcp = new WordCramPanel (wc.layout (),
                                                     new DefaultRenderer (wp));
            
        JScrollPane sp = UIUtils.createScrollPane (b);
        
        sp.setBorder (null);
                
        this.splitPane.setLeftComponent (sp);
        sp.setMinimumSize (new Dimension (50, 200));
        sp.setPreferredSize (new Dimension (225, 200));
        this.splitPane.setRightComponent (wcp);        
                
        javax.swing.plaf.basic.BasicSplitPaneDivider div = ((javax.swing.plaf.basic.BasicSplitPaneUI) this.splitPane.getUI ()).getDivider ();
        div.setBorder (new MatteBorder (0, 0, 0, 1, UIUtils.getComponentColor ()));                
                
        return this.splitPane;            
                
    }
    
    public boolean isWrapContentInScrollPane ()
    {
        
        return false;
        
    }
    
    public String getPanelId ()
    {

        return PANEL_ID;
    
    }
        
    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {
                
    }
    
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {
        
    }

    public <T extends NamedObject> void refresh (T n)
    {
        
    }
    
    private class WordBox extends Box
    {
        
        private TextArea text = null;
        private JComboBox sizes = null;
        private JComboBox fonts = null;
        private JCheckBox italic = null;
        private JCheckBox underline = null;
        private JCheckBox bold = null;
        private JComboBox angle = null;
        private JColorChooser colorSel = null;
        
        public WordBox ()
        {
            
            super (BoxLayout.Y_AXIS);
            
            Set<FormItem> items = new LinkedHashSet ();
            
            this.text = new TextArea ("Enter your words here, separate each word/phrase with a newline.",
                                      3,
                                      -1);
            
            this.fonts = UIUtils.getFontsComboBox ("Futura",
                                                   null);
            
            Vector<Integer> sizes = new Vector ();
            sizes.add (10);
            sizes.add (20);
            sizes.add (30);
            sizes.add (40);
            sizes.add (50);
            sizes.add (100);
            sizes.add (150);
            sizes.add (200);
            
            this.sizes = UIUtils.createNumberComboBox (sizes,
                                                       100);
            
            Vector<Integer> angs = new Vector ();
            angs.add (0);
            angs.add (30);
            angs.add (45);
            angs.add (60);
            angs.add (90);
            angs.add (120);
            angs.add (135);
            angs.add (150);
            angs.add (180);
            
            this.angle = UIUtils.createNumberComboBox (angs,
                                                       0);
            
            
            items.add (new FormItem ("Words",
                                     this.text));
                                                
            items.add (new FormItem ("Font",
                                     this.fonts));

            items.add (new FormItem ("Font Size",
                                     this.sizes));

            this.bold = UIUtils.createCheckBox ("Bold");
                                     
            items.add (new FormItem (null,
                                     this.bold));
                                                            
            items.add (new FormItem ("Angle",
                                     this.angle));

            this.add (UIUtils.createForm (items));
                
        }
        
    }
}