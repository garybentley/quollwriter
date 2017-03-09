package com.quollwriter.editors.ui.panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.*;
import java.beans.*;

import java.io.*;
import java.net.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.Set;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.imageio.*;
import javax.activation.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ScrollableBox;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ImagePanel;

public class RegisterAsAnEditorPanel extends QuollPanel<AbstractViewer>
{

    public static final String PANEL_ID = "registerasaneditor";

    protected JScrollPane    scrollPane = null;

    private TextFormItem nameF = null;
    private TextFormItem emailF = null;
    private TextFormItem passF = null;
    private TextFormItem aboutF = null;
    private SelectFormItem genresF = null;
    private CheckboxFormItem typesF = null;
    private ImageSelectorFormItem avatarF = null;
    
    private Map<String, FormItem> formItems = new LinkedHashMap ();
    
    public RegisterAsAnEditorPanel (AbstractViewer pv)
                             throws GeneralException
    {

        super (pv);
        
    }
        
    public void close ()
    {
                
    }

    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        return true;

    }

    public String getPanelId ()
    {

        // Gonna regret this...
        return RegisterAsAnEditorPanel.PANEL_ID;

    }
                    
    public void init ()
               throws GeneralException
    {
/*
        final RegisterAsAnEditorPanel _this = this;

        Header h = UIUtils.createHeader ("Register as an Editor",
                                         Constants.PANEL_TITLE,
                                         Constants.EDIT_ICON_NAME,
                                         null);
        this.add (h);
                               
        EditorsWebServiceHandler wsHandler = EditorsEnvironment.getEditorsWebServiceHandler ();
        
        EditorAccount acc = EditorsEnvironment.getUserAccount ();
        
        EditorEditor ed = null;
        
        String email = null;
        String pass = null;
        
        if (acc != null)
        {
            
            email = acc.getEmail ();
                        
            ed= acc.getEditor ();
                                         
        }
                
        String yourName = null;
        File avatarFile = null;
        String about = null;
        Set<String> selectedGenres = null;
        Set<String> selectedTypes = null;
        
        if (ed != null)
        {
            
            yourName = ed.getName ();
            
            DataSource ds = null; //ed.getAvatar ();
            
            if (ds != null)
            {
                
                if (!(ds instanceof FileDataSource))
                {
                    
                    Environment.logError ("Illegal data source type for editor avatar image: " +
                                          ds.getClass ().getName ());
                    
                } else {
                
                    avatarFile = ((FileDataSource) ds).getFile ();
                    
                }

            }
            
            about = ed.getAbout ();
            selectedGenres = ed.getGenres ();
            selectedTypes = EditorProject.WordCountLength.getDescriptions (ed.getWordCountLengths ());
            
        }
        
        final Box box = new ScrollableBox (BoxLayout.Y_AXIS);
        box.setAlignmentX (Component.LEFT_ALIGNMENT);
                                         
        box.add (this.createHeader (Environment.replaceObjectNames ("Types of things you are interested in editing")));
                
        Vector<String> gitems = new Vector (EditorsEnvironment.getWritingGenres ());
        
        gitems.add (0,
                    "Any");
        
        this.genresF = new SelectFormItem ("Genres",
                                           gitems,
                                           5,
                                           selectedGenres,
                                           -1,
                                           false,
                                           "Select the genres you are interested in.  Use Ctrl+click to select multiple items.");
        this.formItems.put ("genres",
                            this.genresF);
        box.add (this.genresF);

        Vector<String> typeVals = new Vector ();
        typeVals.add ("Any");
        typeVals.add (EditorProject.WordCountLength.shortStory.getDescription ());//"Short story (less than 7,500 words)");
        typeVals.add (EditorProject.WordCountLength.novelette.getDescription ());//"Novellette (7,500 to 17,5000 words");
        typeVals.add (EditorProject.WordCountLength.novella.getDescription ());//"Novella (17,500 to 40,000 words");
        typeVals.add (EditorProject.WordCountLength.novel.getDescription ());//"Novel (over 40,000 words");
        
        this.typesF = new CheckboxFormItem ("{Project} lengths",
                                            typeVals,
                                            selectedTypes,
                                            false,
                                            "Select the lengths of project you are prepared to edit.");
        
        this.formItems.put ("types",
                            this.typesF);

        box.add (this.typesF);

        box.add (this.createHeader (Environment.replaceObjectNames ("About you")));
        Box aboutBox = new Box (BoxLayout.Y_AXIS);
        aboutBox.setAlignmentX (Component.LEFT_ALIGNMENT);        
                
        this.nameF = new TextFormItem ("Your name",
                                       false,
                                       1,
                                       yourName,
                                       250,
                                       "chars",
                                       true,
                                       null);
        this.formItems.put ("name",
                            this.nameF);

        aboutBox.add (this.nameF);

        java.util.List<String> fileTypes = new ArrayList ();
        fileTypes.add ("jpg");
        fileTypes.add ("jpeg");
        fileTypes.add ("png");
        fileTypes.add ("gif");
                
        this.avatarF = new ImageSelectorFormItem ("Your picture/avatar",
                                                  fileTypes,
                                                  avatarFile,
                                                  new Dimension (75, 75),
                                                  false,
                                                  "Note: animated gifs are not supported.");

        this.avatarF.getImageSelector ().addChangeListener (new ChangeListener ()
        {
        
            public void stateChanged (ChangeEvent ev)
            {
                
                File f = _this.avatarF.getImageSelector ().getFile ();

                if (f != null)
                {
                
                    try
                    {
                
                        // Get the first image, check for animated gif.
                        if (UIUtils.isAnimatedGif (f))
                        {
                            
                            UIUtils.showErrorMessage (_this,
                                                      "Sorry animated gifs are not supported.");
                            
                            _this.avatarF.getImageSelector ().setFile (null);
                            
                            return;
                            
                        }
                
                        BufferedImage im = UIUtils.getScaledImage (f,
                                                                   300);
                        
                        ByteArrayOutputStream bout = new ByteArrayOutputStream ();
                        
                        ImageIO.write (im,
                                       Utils.getFileType (f),
                                       bout);
                        
                        String s = Base64.encodeBytes (bout.toByteArray ());
                        
                        if (s.length () > EditorsWebServiceHandler.MAX_IMAGE_SIZE)
                        {
                            
                            UIUtils.showErrorMessage (_this,
                                                      "Sorry your image/avatar is too large.");
                            
                        }

                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to check image file: " + f,
                                              e);
                        
                    }
                }
                                                  
            }
            
        });
        
        this.formItems.put ("avatar",
                            this.avatarF);
        
        aboutBox.add (this.avatarF);

        this.aboutF = new TextFormItem ("About you",
                                        true,
                                        5,
                                        about,
                                        1000,
                                        "words",
                                        true,
                                        "Let Authors know the type of things you are interested in and the type of projects you are looking for.");

        this.formItems.put ("about",
                            this.aboutF);
        aboutBox.add (this.aboutF);
                
        box.add (aboutBox);
        
        box.add (this.createHeader (Environment.replaceObjectNames ("Login details")));
                
        box.add (this.createHelpText ("In order to communicate with authors you need to create an account with us.  Please provide an email address and a password."));

        this.emailF = new TextFormItem ("Email",
                                        false,
                                        1,
                                        email,
                                        250,
                                        "chars",
                                        true,
                                        null);
        
        this.formItems.put ("email",
                            this.emailF);
        box.add (this.emailF);

        this.passF = new TextFormItem ("Password",
                                       false,
                                       1,
                                       pass,
                                       -1,
                                       null,
                                       true,
                                       null);
        this.formItems.put ("pass",
                            this.passF);
        
        box.add (this.passF);

        box.add (this.createHeader (Environment.replaceObjectNames ("Ready to go?")));
        
        box.add (this.createHelpText ("Ready to go???"));

        JButton[] buts = new JButton[2];
        
        buts[0] = new JButton ("Advertise");
        
        buts[0].addActionListener (new ActionAdapter ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.createAccount ();
                
            }
            
        });
        
        buts[1] = new JButton ("Cancel");
        
        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
        bp.setOpaque (false);
        
        box.add (this.setAsSubItem (bp));

        box.add (Box.createVerticalStrut (15));
        
        box.setBorder (null);
        box.add (Box.createVerticalGlue ());
        box.setMaximumSize (new Dimension (500,
                                           Short.MAX_VALUE));

        final JScrollPane lscroll = new JScrollPane (box);
        lscroll.setBorder (new EmptyBorder (1, 0, 0, 0));
        lscroll.setOpaque (false);
        lscroll.getViewport ().setBorder (null);
        lscroll.getViewport ().setOpaque (false);
        lscroll.getVerticalScrollBar ().setUnitIncrement (20);
        lscroll.setAlignmentX (Component.LEFT_ALIGNMENT);

        lscroll.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
        {
           
            public void adjustmentValueChanged (AdjustmentEvent ev)
            {
                
                if (lscroll.getVerticalScrollBar ().getValue () > 0)
                {
                
                    lscroll.setBorder (new MatteBorder (1, 0, 0, 0,
                                                    UIUtils.getInnerBorderColor ()));

                } else {
                    
                    lscroll.setBorder (new EmptyBorder (1, 0, 0, 0));
                    
                }
                    
            }
            
        });
                                
        box.setBorder (new EmptyBorder (5, 10, 10, 10));
        
        this.add (lscroll);
        
        for (FormItem it : this.formItems.values ())
        {
            
            it.init ();
            
        }

        SwingUtilities.invokeLater (new Runnable ()
        {
            
            public void run ()
            {
                
                lscroll.getVerticalScrollBar ().setValue (0);
                
            }
            
        });
        */
    }

    private void createAccount ()
    {
                         
        FormItem error = null;
             
        for (FormItem it : this.formItems.values ())
        {
            
            //it.setError (false);
            
            if (it.hasError ())
            {
                
                //it.updateRequireLabel ();
                
                //it.setError (true);
                
                if (error == null)
                {

                    error = it;
                    
                }

            }
                        
        }
        
        if (error != null)
        {
            
            //error.scrollIntoView ();

            return;
            
        }
        
        final RegisterAsAnEditorPanel _this = this;
        
        new Thread (new Runnable ()
        {
            
            public void run ()
            {
/*
                try
                {
                    
                    // Wipe out any existing image file.
                    EditorsEnvironment.getEditorsEditorAvatarImageFile ().delete ();
                    
System.out.println ("SAVING ACCOUNT");                
                    EditorsWebServiceHandler h = EditorsEnvironment.getEditorsWebServiceHandler ();
                                        
                    //h.saveAccount (_this.emailF.getValue (),
                    //                 _this.passF.getValue ());

        System.out.println ("CREATED/SAVED ACCOUNT");
System.out.println ("AV FILE: " + _this.avatarF.getValue ());
                    h.saveEditor (_this.nameF.getValue (),
                                  _this.aboutF.getValue (),
                                  _this.avatarF.getValue (),
                                  _this.genresF.getValue (),
                                  EditorProject.WordCountLength.convert (_this.typesF.getValue ()));
        System.out.println ("CREATED/SAVED EDITOR");

                } catch (Exception e) {
                    
                    UIUtils.showErrorMessage (_this,
                                              "Sorry, unable to create/update your information at the moment.  Please try again later.");
                    
                    Environment.logError ("Unable to create/update editor",
                                          e);
                    
                }
  */              
            }
            
        }).start ();
        
    }
    
    private JComponent setAsSubItem (JComponent c)
    {

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);    
    
        c.setBorder (new CompoundBorder (new EmptyBorder (0, 5, 0, 0),
                                         c.getBorder ()));
        
        return c;
        
    }

    private Header createHeader (String title)
    {
        
        Header h = UIUtils.createHeader (title,
                                         Constants.SUB_PANEL_TITLE);
        
        h.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, Environment.getBorderColor ()),
                                                             new EmptyBorder (0, 0, 3, 0)));
        h.setBorder (new CompoundBorder (new EmptyBorder (0, 0, 5, 0),
                                         h.getBorder ()));
        return h;
        
    }
    
    private JComponent createWrapper (JComponent c)
    {
        
        if (c instanceof JComboBox)
        {

            c.setMaximumSize (c.getPreferredSize ());

        }

        c.setAlignmentX (Component.LEFT_ALIGNMENT);
        c.setAlignmentY (Component.TOP_ALIGNMENT);

        if (!(c instanceof Box))
        {

            Box _b = new Box (BoxLayout.X_AXIS);
            _b.add (Box.createHorizontalStrut (5));
            _b.add (c);
            _b.add (Box.createHorizontalGlue ());
            _b.setAlignmentX (Component.LEFT_ALIGNMENT);
            _b.setAlignmentY (Component.TOP_ALIGNMENT);

            c = _b;
                        
        } else {
            
            c.setBorder (new EmptyBorder (0, 5, 0, 0));
            
        }
        
        return c;
    
    }
    
    private JTextPane createHelpText (String text)
    {

        JTextPane t = UIUtils.createHelpTextPane (text,
                                                  this.viewer);
        t.setBorder (new EmptyBorder (0,
                                      5,
                                      10,
                                      5));
        t.enableInputMethods (false);
        t.setFocusable (false);

        return t;

    }
    
    public void fillToolBar (JToolBar acts,
                             final boolean  fullScreen)
    {

    }

    public void saveObject ()
                     throws Exception
    {

    }
    
    public void fillPopupMenu (final MouseEvent ev,
                               final JPopupMenu popup)
    {

        
    }

    public void setState (final Map<String, String> s,
                          boolean                   hasFocus)
    {

        this.setReadyForUse (true);

    }

    public void getState (Map<String, Object> m)
    {

    }

    public List<Component> getTopLevelComponents ()
    {

        List<Component> l = new ArrayList ();

        return l;

    }

    public void refresh (NamedObject n)
    {

        // No need to do anything.

    }

    @Override
    public String getTitle ()
    {
        
        return "Register as an Editor";
        
    }
    
    @Override
    public ImageIcon getIcon (int type)
    {

        return Environment.getIcon (Constants.EDIT_ICON_NAME,
                                    type);

    }
    
    public JScrollPane getScrollPane ()
    {

        return this.scrollPane;

    }

}
