package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.exporter.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;

public class DictionaryManager extends Box 
{

    private JComboBox   exportOthersType = null;
    private JComboBox   exportChaptersType = null;
    private JComboBox   fileType = null;
    private JTextField  fileField = null;
    private JTree       itemsTree = null;
    private JScrollPane itemsTreeScroll = null;
    private Project     proj = null;
    private JTable      wordTable = null;
    private FileWatcher watcher = null;
    private AbstractViewer viewer = null;

    public DictionaryManager(AbstractViewer pv)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = pv;

    }

    private void update ()
    {
        
        if (this.wordTable == null)
        {
            
            return;
            
        }
        
        // Get the words.
        File userDict = Environment.getUserDictionaryFile ();
        
        Vector<Vector> words = new Vector ();

        String w = null;

        try
        {

            w = IOUtils.getFile (userDict);

        } catch (Exception e)
        {

            w = "";
        
            Environment.logError ("Unable to get user dictionary file: " +
                                  userDict,
                                  e);

        }

        StringTokenizer tt = new StringTokenizer (w,
                                                  String.valueOf ('\n'));

        java.util.List<String> wwords = new ArrayList ();

        while (tt.hasMoreTokens ())
        {

            wwords.add (tt.nextToken ());

        }

        Collections.sort (wwords);

        for (String i : wwords)
        {

            Vector v = new Vector ();
            v.add (i);
            words.add (v);

        }

        Vector<String> cols = new Vector ();
        cols.add ("Word");

        this.wordTable.setModel (new DefaultTableModel (words,
                                                        cols)
        {

            public boolean isCellEditable (int row,
                                           int col)
            {

                return false;

            }

        });

        this.validate ();
        this.repaint ();
        
    }
    
    public void init () //JComponent getContentPanel ()
    {

        final DictionaryManager _this = this;
    
        this.watcher = new FileWatcher ();
        this.watcher.addFile (Environment.getUserDictionaryFile ());

        this.watcher.addFileChangeListener (new FileChangeListener ()
                                  {
                                                                
                                      public void fileChanged (FileChangeEvent ev,
                                                               int             types)
                                      {
                                                                    
                                          _this.update ();
                                                                    
                                      }
                                                                
                                  },
                                  FileChangeEvent.MODIFIED | FileChangeEvent.EXISTS);

        this.watcher.start ();    

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.dictionary);
        prefix.add (LanguageStrings.manage);
        
        this.add (UIUtils.createBoldSubHeader (Environment.getUIString (prefix,
                                                                        LanguageStrings.newwords,
                                                                        LanguageStrings.title),
                                               //"New Words",
                                               null));

        JTextPane tp = UIUtils.createHelpTextPane (Environment.getUIString (prefix,
                                                                            LanguageStrings.newwords,
                                                                            LanguageStrings.text),
                                                   //"Enter the new words to add below, separate the words with commas or semi-colons.",
                                                   this.viewer);

        tp.setBorder (new EmptyBorder (5,
                                       5,
                                       0,
                                       5));

        this.add (tp);

        final JTextField newWords = UIUtils.createTextField ();
        newWords.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.wordTable = UIUtils.createTable ();
        this.wordTable.setTableHeader (null);
        
        this.update ();
        
        Box fb = new Box (BoxLayout.Y_AXIS);
        fb.setAlignmentX (Component.LEFT_ALIGNMENT);
        fb.add (newWords);
        fb.add (Box.createVerticalStrut (5));
        
        fb.setBorder (new EmptyBorder (5,
                                       5,
                                       20,
                                       5));

        final JButton add = UIUtils.createButton (Environment.getUIString (prefix,
                                                                           LanguageStrings.newwords,
                                                                           LanguageStrings.add));
                                         //"Add");

        JButton[] buts = new JButton[] { add };
        
        final ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {
            
                    String n = newWords.getText ();
    
                    StringTokenizer t = new StringTokenizer (n,
                                                             ",;");
    
                    while (t.hasMoreTokens ())
                    {
    
                        String w = t.nextToken ().trim ();
    
                        DictionaryProvider.addUserWord (w);
    
                        _this.viewer.fireProjectEvent (ProjectEvent.PERSONAL_DICTIONARY,
                                                       ProjectEvent.ADD_WORD,
                                                       w);
    
                        DefaultTableModel m = (DefaultTableModel) wordTable.getModel ();
    
                        Vector r = new Vector ();
                        r.add (w);
                        m.insertRow (0,
                                     r);
    
                    }
    
                    newWords.setText ("");

                } catch (Exception e) {
                    
                    Environment.logError ("Unable to add new words to dictionary",
                                          e);
                    
                    UIUtils.showErrorMessage (_this,
                                              Environment.getUIString (LanguageStrings.dictionary,
                                                                       LanguageStrings.manage,
                                                                       LanguageStrings.newwords,
                                                                       LanguageStrings.actionerror));
                    
                }
            }

        };

        add.addActionListener (aa);

        UIUtils.addDoActionOnReturnPressed (newWords,
                                            aa);        

        fb.add (UIUtils.createButtonBar2 (buts, Component.LEFT_ALIGNMENT));
        
        this.add (Box.createVerticalStrut (5));

        this.add (fb);
        
        this.add (UIUtils.createBoldSubHeader (Environment.getUIString (prefix,
                                                                        LanguageStrings.table,
                                                                        LanguageStrings.title),
                                               //"Words in Dictionary",
                                               null));

        fb = new Box (BoxLayout.Y_AXIS);
        fb.setAlignmentX (Component.LEFT_ALIGNMENT);

        fb.setBorder (new EmptyBorder (5,
                                       5,
                                       0,
                                       5));
        
        final JScrollPane ppsp = UIUtils.createScrollPane (wordTable);

        wordTable.setPreferredScrollableViewportSize (new Dimension (-1,
                                                                     (wordTable.getRowHeight () + 3) * 5));
        
        fb.add (ppsp);
        fb.add (Box.createVerticalStrut (5));
        
        final JButton remove = UIUtils.createButton (Environment.getUIString (prefix,
                                                                              LanguageStrings.table,
                                                                              LanguageStrings.remove));
                                            //"Remove Selected");

        buts = new JButton[] { remove };

        remove.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {
            
                    DefaultTableModel m = (DefaultTableModel) wordTable.getModel ();
    
                    int[] selection = wordTable.getSelectedRows ();
    
                    for (int i = selection.length - 1; i > -1; i--)
                    {
    
                        DictionaryProvider.removeUserWord (m.getValueAt (selection[i],
                                                                         0).toString ());
    
                        // Remove the row.
                        m.removeRow (selection[i]);
    
                    }
    
                    // Clear the selection.
                    // ((DefaultListSelectionModel) wordTable.getSelectionModel ()).clearSelection ();

                } catch (Exception e) {
                    
                    Environment.logError ("Unable to remove words from dictionary.",
                                          e);
                    
                    UIUtils.showErrorMessage (_this,
                                              Environment.getUIString (LanguageStrings.dictionary,
                                                                       LanguageStrings.manage,
                                                                       LanguageStrings.table,
                                                                       LanguageStrings.delete,
                                                                       LanguageStrings.actionerror));
                    
                }
                
            }

        });

        fb.add (UIUtils.createButtonBar2 (buts, Component.LEFT_ALIGNMENT));
                                         
        this.add (fb);

        this.add (Box.createVerticalStrut (10));
        
        JButton finish = UIUtils.createButton (Environment.getUIString (prefix,
                                                                        LanguageStrings.finish));
                                      //"Finish");

        finish.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.closePopupParent (_this.getParent ());

            }

        });

        buts = new JButton[] { finish };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT); 
        bp.setOpaque (false);

        this.add (bp);        
        
    }

}
