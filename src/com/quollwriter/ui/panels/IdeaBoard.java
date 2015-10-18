package com.quollwriter.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.io.*;

import java.beans.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.jgoodies.forms.factories.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.text.*;

import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.VerticalLayout;


public class IdeaBoard extends QuollPanel
{

    public static final String PANEL_ID = "ideaboard";

    private static final Color lightGrey = UIUtils.getColor ("#aaaaaa");

    public class IdeaBox extends Box
    {

        private Idea       idea = null;
        private TypeBox    typeBox = null;
        private Box        viewBox = null;
        private AddEditBox editBox = null;
        private JTextPane  shortDesc = null;
        private JTextPane  fullDesc = null;
        private IdeaBoard ideaBoard = null;

        public IdeaBox(Idea    i,
                       TypeBox tb,
                       boolean showToolBar)
        {

            super (BoxLayout.Y_AXIS);

            this.typeBox = tb;
            this.idea = i;
            
            this.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.createViewBox (showToolBar);

            this.editBox = new AddEditBox (i,
                                           tb,
                                           this);

            this.editBox.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.add (editBox);

        }

        public Idea getIdea ()
        {
            
            return this.idea;
            
        }

        public void showViewBox ()
        {

            this.viewBox.setVisible (true);
            this.editBox.setVisible (false);

            this.typeBox._repaint ();

        }

        private void createViewBox (boolean showToolBar)
        {

            final IdeaBox _this = this;

            this.viewBox = new Box (BoxLayout.Y_AXIS);
            this.add (this.viewBox);
            this.viewBox.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.shortDesc = UIUtils.createObjectDescriptionViewPane ((String) null,
                                                                      this.typeBox.ideaBoard.getProjectViewer ().getProject (),
                                                                      this.typeBox.ideaBoard.getProjectViewer (),
                                                                      this.typeBox.ideaBoard);
            this.shortDesc.setBorder (new EmptyBorder (3, 5, 0, 5));
            this.viewBox.add (this.shortDesc);
            this.shortDesc.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.fullDesc = UIUtils.createObjectDescriptionViewPane ((String) null,
                                                                     this.typeBox.ideaBoard.getProjectViewer ().getProject (),
                                                                     this.typeBox.ideaBoard.getProjectViewer (),
                                                                     this.typeBox.ideaBoard);
            this.fullDesc.setBorder (new EmptyBorder (3, 5, 0, 5));
            this.updateViewText ();

            this.viewBox.add (this.fullDesc);
            this.fullDesc.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.fullDesc.setVisible (false);

            this.viewBox.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.viewBox.setMinimumSize (new Dimension (300,
                                                        50));

            this.viewBox.add (Box.createVerticalStrut (3));

            List<JButton> buttons = new ArrayList ();

            // Create the actions bar.
            JButton mb = new JButton (Environment.getIcon ("edit",
                                                           Constants.ICON_MENU));
            mb.setToolTipText ("Click to edit this item");
            mb.setOpaque (false);
            mb.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.viewBox.setVisible (false);
                        _this.editBox.setVisible (true);
                        _this.typeBox._repaint ();

                    }

                });

            buttons.add (mb);

            // mb = new JButton (Environment.getIcon (Link.OBJECT_TYPE,
            // false));
            // mb.setToolTipText ("Click to link this item to other items/objects");
            // mb.setOpaque (false);

            // aah = this.getEditItemActionHandler ();

            // aah.setPopupOver (this.quollEditorPanel); //this.projectViewer.getEditorForChapter (it.getChapter ()));
            // aah.setShowLinkTo (true);
            // mb.addActionListener (aah);
            // mb.addActionListener (aa);
            // buttons.add (mb);

            mb = new JButton (Environment.getIcon (Constants.CONVERT_ICON_NAME,
                                                   Constants.ICON_MENU));
            mb.setToolTipText (Environment.replaceObjectNames ("Click to convert this idea to an item such as a {Chapter}, {Object} etc."));
            mb.setOpaque (false);

            mb.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        JPopupMenu p = new JPopupMenu ();

                        String oName = Environment.getObjectTypeName (Chapter.OBJECT_TYPE);
            
                        JMenuItem mi = new JMenuItem (oName,
                                                      Environment.getIcon (Chapter.OBJECT_TYPE,
                                                                           Constants.ICON_MENU));
            
                        char fc = Character.toUpperCase (oName.charAt (0));
            
                        mi.setMnemonic (fc);
                        //mi.setToolTipText (pref + fc);

                        // Get the last chapter from the project.
                        final Chapter ch = _this.typeBox.ideaBoard.projectViewer.getProject ().getBook (0).getLastChapter ();
        
                        mi.addActionListener (new AddChapterActionHandler (ch.getBook (),
                                                                           ch,
                                                                           _this.typeBox.ideaBoard.projectViewer));

                        p.add (mi);

                        UIUtils.addNewAssetItemsToPopupMenu (p,
                                                             _this.typeBox.ideaBoard,
                                                             _this.typeBox.ideaBoard.projectViewer,
                                                             null,
                                                             _this.idea.getDescriptionText ());

                        Component c = (Component) ev.getSource ();

                        p.show (c,
                                10,
                                10);

                    }

                });

            buttons.add (mb);

            mb = new JButton (Environment.getIcon (Constants.DELETE_ICON_NAME,
                                                   Constants.ICON_MENU));
            mb.setOpaque (false);
            mb.setToolTipText ("Click to delete this item");

            mb.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    Point p = SwingUtilities.convertPoint ((Component) ev.getSource (),
                                                           0,
                                                           0,
                                                           IdeaBoard.this.projectViewer);
                
                    UIUtils.createQuestionPopup (_this.typeBox.getIdeaBoard ().projectViewer,
                                                 "Delete the {idea}?",
                                                 Constants.DELETE_ICON_NAME,
                                                 "Please confirm you wish to delete this {idea}?",
                                                 "Yes, delete it",
                                                 null,
                                                 new ActionListener ()
                                                 {
                                                    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        if (_this.typeBox.getIdeaBoard ().projectViewer.deleteIdea (_this.idea))
                                                        {
                                                        
                                                            _this.idea.getType ().removeIdea (_this.idea);
                                                        
                                                            _this.typeBox.removeIdea (_this.idea);
                                                        
                                                        }
                                                        
                                                    }
                                                    
                                                 },
                                                 null,
                                                 null,
                                                 p);

                }
                
            });
            
            buttons.add (mb);

            JButton hideBut = new JButton (Environment.getIcon (Constants.UP_ICON_NAME,
                                                                Constants.ICON_MENU));
            hideBut.setOpaque (false);
            hideBut.setToolTipText ("Click to hide");
            
            buttons.add (hideBut);

            JToolBar tb = UIUtils.createButtonBar (buttons);

            tb.setAlignmentX (Component.LEFT_ALIGNMENT);

            final Box tbb = new Box (BoxLayout.X_AXIS);
            tbb.setAlignmentX (Component.LEFT_ALIGNMENT);
            tbb.setBorder (new EmptyBorder (3,
                                            3,
                                            0,
                                            3));
            tbb.add (tb);
            tbb.add (Box.createHorizontalGlue ());

            tbb.setVisible (showToolBar);

            this.viewBox.add (tbb);

            hideBut.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.shortDesc.setVisible (true);
                    _this.fullDesc.setVisible (false);
                    tbb.setVisible (false);

                    _this.typeBox._repaint ();

                }
                
            });
            
            MouseEventHandler vis = new MouseEventHandler ()
            {

                @Override
                public void handlePress (MouseEvent ev)
                {
                    
                    _this.shortDesc.setVisible (false);
                    _this.fullDesc.setVisible (true);
                    tbb.setVisible (true);

                    _this.typeBox._repaint ();
                    
                }

            };

            this.shortDesc.addMouseListener (vis);
            
            StarBar sb = new StarBar ();

            sb.setToolTipText ("Rate this item.");
            sb.setSelected (this.idea.getRating ());

            // Now add the property change listener.
            sb.addPropertyChangeListener (StarBar.RATING,
                                          new PropertyChangeAdapter ()
                                          {

                                              public void propertyChange (PropertyChangeEvent ev)
                                              {

                                                  if (!ev.getPropertyName ().equals (StarBar.RATING))
                                                  {

                                                      return;

                                                  }

                                                  _this.idea.setRating ((Integer) ev.getNewValue ());

                                                  _this.updateIdea ();

                                                  _this.typeBox.getIdeaBoard ().getProjectViewer ().fireProjectEvent (Idea.OBJECT_TYPE,
                                                                                                                      ProjectEvent.RATE);

                                              }

                                          });

            tbb.add (sb);
/*
            UIUtils.addListenerToChildren (vis,
                                           this.viewBox);
*/
            this.viewBox.add (Box.createVerticalStrut (3));

            this.viewBox.setBorder (new MatteBorder (0,
                                                     0,
                                                     1,
                                                     0,
                                                     Environment.getBorderColor ()));

        }

        public void saveIdea (StringWithMarkup text)
        {
        
            if (!text.hasText ())
            {

                return;

            }

            this.idea.setDescription (text);

            this.updateIdea ();

            this.showViewBox ();

        }

        private void updateIdea ()
        {

            try
            {

                this.typeBox.ideaBoard.projectViewer.saveObject (this.idea,
                                                                 false);

            } catch (Exception e)
            {

                Environment.logError ("Unable to update idea: " +
                                      this.idea,
                                      e);

                UIUtils.showErrorMessage (this.typeBox.ideaBoard,
                                          "Unable to update idea.");

            }

            this.updateViewText ();

        }

        private void updateViewText ()
        {

            StringWithMarkup sm = this.idea.getDescription ();
        
            Paragraph p = new Paragraph (sm.getText (),
                                         0);
        
            String firstSent = "";
        
            if (p.getSentenceCount () > 0)
            {
                
                firstSent = p.getFirstSentence ().markupAsHTML (sm.getMarkup ());
            
                if (p.getSentenceCount () > 1)
                {
                    
                    firstSent += " <b><i>More...</i></b>";
                    
                }
                
            }
                    
            this.shortDesc.setText (UIUtils.getWithHTMLStyleSheet (this.shortDesc,
                                                                   UIUtils.markupStringForAssets (firstSent,
                                                                                                  this.typeBox.ideaBoard.getProjectViewer ().getProject (),
                                                                                                  null)));

            this.shortDesc.setToolTipText ("Click to show the full text");
                                                                                                  
            this.fullDesc.setText (UIUtils.getWithHTMLStyleSheet (this.fullDesc,
                                                                  UIUtils.markupStringForAssets (this.idea.getDescription ().getMarkedUpText (),
                                                                                                 this.typeBox.ideaBoard.getProjectViewer ().getProject (),
                                                                                                 null)));

        }

    }

    public class AddEditBox extends Box
    {

        private Idea      idea = null;
        private TextArea  text = null;
        private TypeBox   typeBox = null;
        private IdeaBox   ideaBox = null;

        public AddEditBox(final Idea    i,
                          final TypeBox typeBox,
                          final IdeaBox ideaBox)
        {

            super (BoxLayout.Y_AXIS);

            this.idea = i;
            this.typeBox = typeBox;
            this.ideaBox = ideaBox;

            this.setBorder (new CompoundBorder (new EmptyBorder (3,
                                                                 3,
                                                                 3,
                                                                 3),
                                                UIUtils.createLineBorder ()));

            this.text = UIUtils.createTextArea (typeBox.getIdeaBoard ().projectViewer,
                                                "Enter your {Idea} here...",
                                                5,
                                                -1);
            this.text.setCanFormat (true);
            this.text.setAutoGrabFocus (true);
            this.text.setBorder (null);
            
            final AddEditBox _this = this;

            UIUtils.addDoActionOnReturnPressed (this.text,
                                                new ActionListener ()
                                                {
                                                    
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.save ();
                                                        
                                                    }

                                                });

            this.initTextArea ();
            
            this.add (this.text);

            this.setVisible (false);

            this.add (Box.createVerticalStrut (5));

            JButton save = UIUtils.createButton (Constants.SAVE_ICON_NAME,
                                                 Constants.ICON_MENU,
                                                 "Click to save the idea",
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

                                                        _this.typeBox.hideAdd ();
                                
                                                        if (i != null)
                                                        {
                                
                                                            ideaBox.showViewBox ();
                                
                                                        }
                                                        
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

            buttons.setBorder (UIUtils.createTopLineWithPadding (3, 3, 3, 3));

            this.add (buttons);

        }

        private void save ()
        {

            StringWithMarkup t = this.text.getTextWithMarkup ();
            
            if (!t.hasText ())
            {
                
                return;
                
            }
                            
            if (this.idea == null)
            {

                typeBox.createIdea (t);

            } else
            {

                ideaBox.saveIdea (t);

            }
            
            this.text.setTextWithMarkup (null);

        }

        public void setVisible (boolean v)
        {

            super.setVisible (v);

            this.initTextArea ();

        }

        private void initTextArea ()
        {

            if (this.idea != null)
            {

                this.text.setTextWithMarkup (this.idea.getDescription ());

            }

        }

    }

    public class TypeBox extends QPopup
    {

        private Box        ideaBox = null;
        private AddEditBox addBox = null;
        private JTextPane  helpText = null;
        private IdeaBoard  ideaBoard = null;
        private IdeaType   ideaType = null;

        public TypeBox(IdeaType  it,
                       IdeaBoard ideaBoard)
        {

            super ("name",
                   ((it.getIconType () == null) ? null : Environment.getIcon (it.getIconType (),
                                                                              Constants.ICON_POPUP)),
                   null);

            this.ideaType = it;

            this.setAllowRemoveOnEscape (false);
            
            this.ideaBoard = ideaBoard;

            this.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.setAlignmentY (Component.TOP_ALIGNMENT);

            final TypeBox _this = this;

            List<JComponent> buttons = new ArrayList ();
            final JButton add = UIUtils.createButton ("add",
                                                      Constants.ICON_MENU,
                                                      "Click to add a new " + this.getIdeaTypeName () + " Idea",
                                                      null);
            buttons.add (add);
            
/*
            final ImagePanel add = new ImagePanel (Environment.getIcon ("add",
                                                                        false),
                                                   null);

            UIUtils.setAsButton (add);
            add.setToolTipText ("Click to add a new " + this.getIdeaTypeName () + " Idea");
            add.setBorder (null);
            add.setVisible (false);
*/
            add.setVisible (false);
            add.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void mouseEntered (MouseEvent ev)
                {

                    add.setVisible (true);

                }

                @Override
                public void mouseExited (MouseEvent ev)
                {

                    add.setVisible (false);

                }

                @Override
                public void handlePress (MouseEvent ev)
                {

                    _this.showAdd ();

                }

            });

            JComponent bb = UIUtils.createButtonBar (buttons);
            bb.setBorder (null);
            this.getHeader ().setControls (bb);
            this.getHeader ().setPadding (new java.awt.Insets (4, 5, 4, 3));

            this.ideaBox = new Box (BoxLayout.Y_AXIS);

            UIUtils.setAsButton (this.getHeader ());
            this.getHeader ().addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void mouseEntered (MouseEvent ev)
                {

                    add.setVisible (true);

                }

                @Override
                public void mouseExited (MouseEvent ev)
                {

                    add.setVisible (false);

                }

                @Override
                public void fillPopup (JPopupMenu p,
                                       MouseEvent ev)
                {

                    JMenuItem mi = new JMenuItem ("Add new Idea",
                                                  Environment.getIcon ("add",
                                                                       Constants.ICON_MENU));
                    mi.addActionListener (new ActionAdapter ()
                        {

                            public void actionPerformed (ActionEvent ev)
                            {

                                _this.showAdd ();

                            }

                        });

                    p.add (mi);

                    JMenu sortMenu = new JMenu ("Sort Ideas by");

                    p.add (sortMenu);

                    ButtonGroup group = new ButtonGroup ();

                    sortMenu.setIcon (Environment.getIcon (Constants.SORT_ICON_NAME,
                                                           Constants.ICON_MENU));

                    mi = new JRadioButtonMenuItem ("Rating",
                                                   Environment.getIcon (Constants.STAR_ICON_NAME,
                                                                        Constants.ICON_MENU));

                    if (_this.ideaType.getSortBy ().equals (IdeaType.SORT_BY_RATING))
                    {

                        mi.setSelected (true);

                    }

                    group.add (mi);

                    mi.addActionListener (new ActionAdapter ()
                        {

                            public void actionPerformed (ActionEvent ev)
                            {

                                _this.sortIdeas (IdeaType.SORT_BY_RATING);

                                _this.ideaBoard.getProjectViewer ().fireProjectEvent (Idea.OBJECT_TYPE,
                                                                                      ProjectEvent.SORT,
                                                                                      IdeaType.SORT_BY_RATING);

                            }

                        });

                    sortMenu.add (mi);

                    mi = new JRadioButtonMenuItem ("Date Added",
                                                   Environment.getIcon (Constants.DATE_ICON_NAME,
                                                                        Constants.ICON_MENU));

                    mi.addActionListener (new ActionAdapter ()
                        {

                            public void actionPerformed (ActionEvent ev)
                            {

                                _this.sortIdeas (IdeaType.SORT_BY_DATE);

                                _this.ideaBoard.getProjectViewer ().fireProjectEvent (Idea.OBJECT_TYPE,
                                                                                      ProjectEvent.SORT,
                                                                                      IdeaType.SORT_BY_DATE);

                            }

                        });

                    if (_this.ideaType.getSortBy ().equals (IdeaType.SORT_BY_DATE))
                    {

                        mi.setSelected (true);

                    }

                    sortMenu.add (mi);

                    group.add (mi);

                    mi = new JRadioButtonMenuItem ("Alphabetical",
                                                   Environment.getIcon (Constants.SPELLCHECKER_ICON_NAME,
                                                                        Constants.ICON_MENU));

                    mi.addActionListener (new ActionAdapter ()
                        {

                            public void actionPerformed (ActionEvent ev)
                            {

                                _this.sortIdeas (IdeaType.SORT_BY_TEXT);

                                _this.ideaBoard.getProjectViewer ().fireProjectEvent (Idea.OBJECT_TYPE,
                                                                                      ProjectEvent.SORT,
                                                                                      IdeaType.SORT_BY_TEXT);

                            }

                        });

                    if (_this.ideaType.getSortBy ().equals (IdeaType.SORT_BY_TEXT))
                    {

                        mi.setSelected (true);

                    }

                    sortMenu.add (mi);

                    group.add (mi);

                    mi = new JMenuItem ((_this.ideaBox.isVisible () ? "Hide" : "Show") + " Ideas");

                    mi.addActionListener (new ActionAdapter ()
                        {

                            public void actionPerformed (ActionEvent ev)
                            {

                                _this.showIdeas (!_this.ideaBox.isVisible ());

                            }

                        });

                    p.add (mi);

                    mi = new JMenuItem ("Edit the name of this type",
                                        Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                             Constants.ICON_MENU));

                    p.add (mi);

                    mi.addActionListener (new EditIdeaTypeActionHandler (_this.ideaType,
                                                                         _this.ideaBoard));

                    mi = new JMenuItem ("Delete this type of Idea",
                                        Environment.getIcon (Constants.DELETE_ICON_NAME,
                                                             Constants.ICON_MENU));

                    p.add (mi);

                    if (_this.ideaType.getIdeaCount () == 0)
                    {


                        mi.addActionListener (new ActionAdapter ()
                            {

                                public void actionPerformed (ActionEvent ev)
                                {

                                    // Just delete.
                                    _this.ideaBoard.deleteIdeaType (_this.ideaType);

                                }

                            });

                    } else
                    {

                        mi.addActionListener (new DeleteIdeaTypeActionHandler (_this.ideaType,
                                                                               _this.ideaBoard));

                    }
                    
                }
                
                @Override
                public void handlePress (MouseEvent ev)
                {

                    _this.showIdeas (!_this.ideaBox.isVisible ());

                }

            });

            this.getHeader ().setToolTipText ("Click to show/hide the Ideas");
            
            Box content = new Box (BoxLayout.Y_AXIS);
            content.setMinimumSize (new Dimension (300,
                                                   50));
            content.setBackground (Color.WHITE);
            content.setOpaque (true);

            this.ideaBox.setMinimumSize (new Dimension (300,
                                                        50));

            //this.ideaBox.setBackground (Color.WHITE);
            this.ideaBox.setOpaque (false);//true);
            this.setContent (content);//this.ideaBox);

            this.helpText = UIUtils.createHelpTextPane ("You currently have no <b>" + this.getIdeaTypeName () + "</b> ideas recorded.  To add a new Idea perform one of the actions below:<ul><li>Use the green plus button on the header.</li><li>Click anywhere in this box.</li><li>Right click on the header and select <b>Add new Idea</b>.</li></ul>",
                                                        IdeaBoard.this.projectViewer);

            this.helpText.addMouseListener (new MouseAdapter ()
                {

                    public void mouseClicked (MouseEvent ev)
                    {

                        _this.showAdd ();

                    }
                });

            content.add (this.helpText);

            //this.ideaBox.add (this.helpText);

            // Create the add box.
            this.addBox = new AddEditBox (null,
                                          this,
                                          null);

            this.ideaBox.add (this.addBox);

            content.add (this.ideaBox);

            this.addIdeasToBox ();

            // Sort the boxes, done this way to prevent having to recreate the boxes.

            if (this.ideaType.getIdeaCount () == 0)
            {

                this.showIdeas (false);

            }

            this.setBorder (new CompoundBorder (com.quollwriter.ui.components.UIUtils.internalPanelDropShadow,
                                                new MatteBorder (1, 1, 1, 1, UIUtils.getBorderColor ())));
            
            this.showHelpText (false);

            this.setBoxTitle ();

        }

        public void updateTitle ()
        {

            this.setBoxTitle ();

        }

        public IdeaBoard getIdeaBoard ()
        {
            
            return this.ideaBoard;
            
        }

        public void sortIdeas (String sortBy)
        {

            this.ideaType.setSortBy (sortBy);

            try
            {

                this.ideaBoard.projectViewer.saveObject (this.ideaType,
                                                         false);

            } catch (Exception e)
            {

                Environment.logError ("Unable to update idea type: " +
                                      this.ideaType +
                                      ", cannot set sort type: " +
                                      sortBy,
                                      e);

            }

            this.addIdeasToBox ();

        }

        private void addIdeasToBox ()
        {

            int n = this.ideaBox.getComponentCount ();

            // Remove any component starting at 2.
            for (int i = n - 1; i > 0; i--)
            {

                this.ideaBox.remove (i);

            }

            List<Idea> ideas = this.ideaType.getIdeas ();

            for (int i = ideas.size () - 1; i > -1; i--)
            {

                Idea idea = ideas.get (i);

                this.addIdea (idea,
                              false);

            }

            this._repaint ();

        }

        private String getIdeaTypeName ()
        {

            return Environment.replaceObjectNames (this.ideaType.getName ());

        }

        public boolean areIdeasVisible ()
        {

            return this.ideaBox.isVisible ();

        }

        public void showIdeas (boolean vis)
        {

            this.showHelpText ((vis && (this.ideaType.getIdeaCount () == 0)));

            this.ideaBox.setVisible (vis);

            this.setBoxTitle ();

        }

        private void setBoxTitle ()
        {

            String suff = " (" + this.ideaType.getIdeaCount () + ")";

            this.getHeader ().setTitle (this.getIdeaTypeName () + suff);

        }

        public Component.BaselineResizeBehavior getBaselineResizeBehavior ()
        {
            return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
        }

        @Override public int getBaseline (int width,
                                          int height)
        {
            return 0;
        }

        public void hideAdd ()
        {

            // Only show help if there are no ideas.
            this.showHelpText (this.ideaType.getIdeas ().size () == 0);

            this.addBox.setVisible (false);

            this._repaint ();

        }

        private void _repaint ()
        {

            if (this.getParent () != null)
            {

                this.getParent ().validate ();
                this.getParent ().repaint ();

            }

        }

        public void showAdd ()
        {

            this.showHelpText (false);

            this.ideaBox.setVisible (true);

            this.addBox.setVisible (true);

            this.setBoxTitle ();

            this._repaint ();

        }

        public void showHelpText (boolean v)
        {

            this.helpText.setVisible (v);//this.ideaType.getIdeaCount () == 0);

            this._repaint ();

        }

        public void createIdea (StringWithMarkup text)
        {
        
            if (!text.hasText ())
            {

                return;

            }

            text.update (text);
            
            Idea i = new Idea ();
            i.setDescription (text);
            i.setType (this.ideaType);

            // Ask the project viewer to save the new object.
            try
            {

                this.ideaBoard.projectViewer.addNewIdea (i);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save new idea: " +
                                      i,
                                      e);

                UIUtils.showErrorMessage (this,
                                          "Unable to save new Idea.");

                return;

            }

            // Add the new idea to the list.
            this.addIdea (i,
                          true);

            this.hideAdd ();

            this.showHelpText (false);

        }

        private void removeIdea (Idea i)
        {
            
            int c = this.ideaBox.getComponentCount ();
            
            for (int x = 0; x < c; x++)
            {
                
                Component cc = (Component) this.ideaBox.getComponent (x);
                
                if (cc instanceof IdeaBox)
                {
                    
                    IdeaBox ib = (IdeaBox) cc;
                    
                    if (ib.getIdea () == i)
                    {
                        
                        this.ideaBox.remove (ib);

                        this.ideaType.removeIdea (i);

                        this.setBoxTitle ();
                        
                        this._repaint ();

                        break;
                        
                    }
                    
                }
                
            }
                        
        }

        private void addIdea (Idea    i,
                              boolean showToolBar)
        {

            this.ideaBox.add (new IdeaBox (i,
                                           this,
                                           showToolBar),
                              1);

            this.setBoxTitle ();

        }

    }

    private BackgroundImagePanel          categories = null;
    private Header          header = null;
    private JSplitPane      splitPane = null;
    private JScrollPane     itemsScroll = null;
    protected ProjectViewer projectViewer = null;
    //private Paint           background = null;
    //private JComponent      help = null;
    private Object          backgroundObject = null;
    private QPopup          backgroundSelectorPopup = null;

    private Map<IdeaType, TypeBox> ideaTypes = new HashMap ();

    public IdeaBoard (ProjectViewer pv)
    {

        super (pv,
               pv.getProject ());

        // Get the idea types.
        this.projectViewer = pv;

        this.setBackground (UIUtils.getComponentColor ());
        this.setOpaque (true);
        
    }

    public String getPanelId ()
    {

        return PANEL_ID;

    }

    public void init ()
               throws GeneralException
    {

        final IdeaBoard _this = this;

        // Add a panel with a flowlayout.
        // FlowLayout layout = new FlowLayout (FlowLayout.CENTER, 10, 10);

        VerticalLayout layout = new VerticalLayout (7,
                                                    7,
                                                    300);

        // layout.setAlignOnBaseline (true);
        //this.categories = new JPanel (layout)

        this.categories = new BackgroundImagePanel (layout);
        this.categories.setToolTipText ("Double click to add a new type of Idea.  Right click to change the background.");
        
        //this.categories.setBackgroundObject (Environment.getProperty (Constants.DEFAULT_IDEA_BOARD_BG_IMAGE_PROPERTY_NAME));
    
        this.categories.setBorder (new EmptyBorder (20,
                                                    20,
                                                    20,
                                                    20));

        this.categories.setBackground (null);
        this.categories.setOpaque (false);

        final JScrollPane cscroll = new JScrollPane (this.categories);

        cscroll.setBorder (null);
        
        cscroll.setBorder (new EmptyBorder (1, 0, 0, 0));
     
        cscroll.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
        {
           
            public void adjustmentValueChanged (AdjustmentEvent ev)
            {
                
                if (cscroll.getVerticalScrollBar ().getValue () > 0)
                {
                
                    cscroll.setBorder (new MatteBorder (1, 0, 0, 0,
                                                        UIUtils.getInnerBorderColor ()));

                } else {
                    
                    cscroll.setBorder (new EmptyBorder (1, 0, 0, 0));
                    
                }
                    
            }
            
        });
        
        cscroll.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cscroll.getVerticalScrollBar ().setUnitIncrement (20);
        cscroll.setOpaque (false);
        cscroll.setBackground (null);

        this.categories.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handleDoublePress (MouseEvent ev)
            {

                new NewIdeaTypeActionHandler (_this).actionPerformed (new ActionEvent (_this, 0, "show"));
                
            }
                
            @Override
            public void fillPopup (JPopupMenu p,
                                   MouseEvent ev)
            {
                
                JMenuItem add = new JMenuItem ("Add a new type of Idea",
                                               Environment.getIcon ("add",
                                                                    Constants.ICON_MENU));
                add.addActionListener (new NewIdeaTypeActionHandler (_this));

                p.add (add);

                p.add (_this.getSelectBackgroundButton (true));

            }
                
        });

        java.util.List<JButton> buts = new ArrayList ();

        buts.add (UIUtils.createButton ("add",
                                        Constants.ICON_PANEL_ACTION,
                                        "Click to add a new type of Idea",
                                        new NewIdeaTypeActionHandler (this)));
    
        buts.add (UIUtils.createHelpPageButton ("idea-board/overview",
                                                Constants.ICON_PANEL_ACTION,
                                                null));

        this.header = UIUtils.createHeader ("Idea Board",
                                            Constants.PANEL_TITLE,
                                            "idea",
                                            UIUtils.createButtonBar (buts));

        this.add (this.header);

        // Get all the current idea types.
        List<IdeaType> its = this.projectViewer.getProject ().getIdeaTypes ();

        if (its.size () == 0)
        {

            this.projectViewer.setIgnoreProjectEvents (true);

            // Add the default types, they are inserted in reverse since
            // we insert at the top of the container rather than adding to the bottom.
            this.addNewType (Environment.replaceObjectNames ("{Scenes}"),
                             "scene",
                             false);

            this.addNewType ("Other",
                             null,
                             false);

            this.addNewType (Environment.replaceObjectNames ("{Locations}"),
                             "location",
                             false);

            this.addNewType (Environment.replaceObjectNames ("{Items}"),
                             "object",
                             false);

            this.addNewType ("Dialogue",
                             "dialogue",
                             false);

            this.addNewType (Environment.replaceObjectNames ("{Characters}"),
                             "character",
                             false);

            this.addNewType (Environment.replaceObjectNames ("{Chapters}"),
                             "chapter",
                             false);

            this.projectViewer.setIgnoreProjectEvents (false);

        } else
        {

            its = new ArrayList (its);

            Collections.reverse (its);

            for (IdeaType it : its)
            {

                this.addType (it);

            }

        }

        this.add (cscroll);
        
    }

    public boolean hasTypeWithName (String n)
    {

        Iterator<IdeaType> iter = ideaTypes.keySet ().iterator ();

        while (iter.hasNext ())
        {

            IdeaType it = iter.next ();

            if (it.getName ().equalsIgnoreCase (n))
            {

                return true;

            }

        }

        return false;

    }

    private AbstractButton getSelectBackgroundButton (boolean showText)
    {

        final IdeaBoard _this = this;

        AbstractButton mi = null;

        if (showText)
        {

            mi = new JMenuItem ("Select a background image/color");

        } else
        {

            mi = new JButton ();
            UIUtils.setAsButton (mi);
            mi.setOpaque (false);
            mi.setToolTipText ("Click to show/hide the background image/color selector");

        }

        mi.setIcon (Environment.getIcon ("bg-select",
                                         (showText ? Constants.ICON_MENU : Constants.ICON_TOOLBAR)));

        mi.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.showBackgroundSelector ();

                }

            });

        return mi;

    }

    private AbstractButton getSelectOpacityButton (boolean showText)
    {

        final IdeaBoard _this = this;

        AbstractButton mi = null;

        if (showText)
        {

            mi = new JMenuItem ("Set how transparent the background is");

        } else
        {

            mi = new JButton ();
            UIUtils.setAsButton (mi);
            mi.setOpaque (false);
            mi.setToolTipText ("Click to show/hide the background transparency selector");

        }

        mi.setIcon (Environment.getIcon ("transparency",
                                         (showText ? Constants.ICON_MENU : Constants.ICON_TOOLBAR)));

        mi.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.showBackgroundSelector ();

            }

        });

        return mi;

    }    
    
    public void showBackgroundSelector ()
    {

        final IdeaBoard _this = this;

        if (this.backgroundSelectorPopup == null)
        {

            this.backgroundSelectorPopup = BackgroundSelector.getBackgroundSelectorPopup (new ChangeAdapter ()
            {

                public void stateChanged (ChangeEvent ev)
                {

                    BackgroundChangeEvent bce = (BackgroundChangeEvent) ev;

                    if (!bce.getValue ().equals (_this.categories.getBackgroundObject ()))
                    {

                        _this.projectViewer.fireProjectEvent (ProjectEvent.IDEA_BOARD,
                                                              ProjectEvent.CHANGE_BACKGROUND);

                    }

                    _this.setBoardBackground (bce.getValue ());

                }

            },
            new Dimension (75,
                           75),
            this.backgroundObject);

            this.backgroundSelectorPopup.setDraggable (this);

            this.projectViewer.addPopup (this.backgroundSelectorPopup,
                                         true,
                                         false);

        } else
        {

            if (this.backgroundSelectorPopup.isVisible ())
            {

                this.backgroundSelectorPopup.setVisible (false);

                return;

            }

        }

        this.projectViewer.showPopupAt (this.backgroundSelectorPopup,
                                        new Point (100,
                                                   100),
                                        true);

        ((BackgroundSelector) this.backgroundSelectorPopup.getContent ()).setSelected (this.backgroundObject);

    }

    public void setBoardOpacity (float v)
    {
        
        this.categories.setBackgroundOpacity (v);
        
    }
    
    public void setBoardBackground (Object o)
    {

        this.categories.setBackgroundObject (o);
        
    }

    public void addNewType (String  name,
                            String  iconType,
                            boolean showAdd)
                     throws GeneralException
    {

        IdeaType it = new IdeaType ();
        it.setName (name);
        it.setIconType (iconType);

        this.projectViewer.getProject ().addIdeaType (it);
        
        this.projectViewer.addNewIdeaType (it);

        TypeBox cb = this.addType (it);

        if (showAdd)
        {

            cb.showAdd ();

        }

    }

    public void updateIdeaType (IdeaType type)
    {

        this.projectViewer.updateIdeaType (type);

        TypeBox tb = this.ideaTypes.get (type);

        tb.updateTitle ();

    }

    public void deleteIdeaType (IdeaType type)
    {

        if (this.projectViewer.deleteIdeaType (type))
        {

            TypeBox ic = this.ideaTypes.get (type);

            this.categories.remove (ic);

            this.ideaTypes.remove (type);

            this.repaint ();

        }

    }

    private TypeBox addType (IdeaType type)
    {

        TypeBox ic = new TypeBox (type,
                                  this);

        this.ideaTypes.put (type,
                            ic);

        this.categories.add (ic,
                             0);

        return ic;

    }

    public void fillPopupMenu (final MouseEvent ev,
                               final JPopupMenu popup)
    {

    }

    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        this.saveObject ();

        return true;

    }

    @Override
    public void close ()
    {

    }

    public void getState (Map<String, Object> m)
    {

        String bg = this.categories.getBackgroundObjectAsString ();
            
        // Legacy < v2
        m.remove ("backgroundImage");
    
        m.put ("background",
               bg);

        m.put ("opacity",
               String.valueOf (this.categories.getBackgroundOpacity ()));
               
        // Get all the type boxes that are "open".
        StringBuilder sb = new StringBuilder ();

        Iterator iter = ideaTypes.entrySet ().iterator ();

        while (iter.hasNext ())
        {

            Map.Entry en = (Map.Entry) iter.next ();

            IdeaType it = (IdeaType) en.getKey ();
            TypeBox  b = (TypeBox) en.getValue ();

            if (b.areIdeasVisible ())
            {

                if (sb.length () > 0)
                {

                    sb.append (",");

                }

                sb.append (it.getKey ());

            }

        }

        if (sb.length () > 0)
        {

            m.put ("openTypes",
                   sb.toString ());

        }

    }

    public void setState (final Map<String, String> s,
                          boolean                   hasFocus)
    {

        String bg = s.get ("background");

        // Legacy < v2
        if (bg == null)
        {
            
            bg = s.get ("backgroundImage");
            
            if (bg != null)
            {
                
                bg = "bg:" + bg;
                
            }
            
        }
        
        if (bg == null)
        {
            
            bg = "bg:" + Environment.getProperty (Constants.DEFAULT_IDEA_BOARD_BG_IMAGE_PROPERTY_NAME);
            
        }
        
        try
        {
        
            this.setBoardBackground (bg);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to set board background to: " +
                                  bg,
                                  e);
            
            this.setBoardBackground ("bg:" + Environment.getProperty (Constants.DEFAULT_IDEA_BOARD_BG_IMAGE_PROPERTY_NAME));
            
        }
        
        String op = s.get ("opacity");
        
        float opv = 0.7f;
        
        try
        {
            
            opv = Float.parseFloat (op);
            
        } catch (Exception e) {
            
            // Ignore.
            
        }
  
        this.setBoardOpacity (opv);
    
        // Open the types.
        String openTypes = s.get ("openTypes");

        if (openTypes != null)
        {

            Map<Long, TypeBox> m = new HashMap ();

            Iterator iter = this.ideaTypes.entrySet ().iterator ();

            while (iter.hasNext ())
            {

                Map.Entry en = (Map.Entry) iter.next ();

                IdeaType it = (IdeaType) en.getKey ();
                TypeBox  b = (TypeBox) en.getValue ();

                m.put (it.getKey (),
                       b);

            }

            StringTokenizer t = new StringTokenizer (openTypes,
                                                     ",");

            while (t.hasMoreTokens ())
            {

                try
                {

                    long k = Long.parseLong (t.nextToken ().trim ());

                    TypeBox b = m.get (k);

                    if (b != null)
                    {

                        b.showIdeas (true);

                    }

                } catch (Exception e)
                {

                    // Ignore.

                }

            }

        }

        this.validate ();
        this.repaint ();

        this.setReadyForUse (true);
        
    }

    public void fillToolBar (JToolBar acts,
                             boolean  fullScreen)
    {

        JButton add = new JButton (Environment.getIcon ("add",
                                                        Constants.ICON_TOOLBAR));
        add.setToolTipText ("Click to add a new type of Idea");
        add.setOpaque (false);
        UIUtils.setAsButton (add);
        add.addActionListener (new NewIdeaTypeActionHandler (this));
        acts.add (add);

        acts.add (this.getSelectBackgroundButton (false));

        //acts.add (this.getSelectOpacityButton (false));        
        
        JButton helpBut = UIUtils.createHelpPageButton ("idea-board/overview",
                                                        Constants.ICON_TOOLBAR,
                                                        null);

        acts.add (helpBut);

    }

    public String getTitle ()
    {
        
        return "Idea Board";
        
    }
    
    public String getIconType ()
    {

        return "idea";

    }

    public List<Component> getTopLevelComponents ()
    {

        return new ArrayList ();

    }

    public void refresh (NamedObject t)
    {


    }

}
