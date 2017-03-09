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

import javax.activation.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.imageio.*;

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

public class AdvertiseProjectPanel extends QuollPanel<AbstractViewer>
{

    public static final String PANEL_ID = "advertiseproject";

    protected JScrollPane    scrollPane = null;

    private TextFormItem nameF = null;
    private TextFormItem emailF = null;
    private TextFormItem passF = null;
    private TextFormItem aboutF = null;
    private TextFormItem descF = null;
    private TextFormItem expF = null;
    private SelectFormItem genresF = null;
    private ComboBoxFormItem typesF = null;
    private TextFormItem titleF = null;
    private ImageSelectorFormItem avatarF = null;
    
    private Map<String, FormItem> formItems = new LinkedHashMap ();
    
    public AdvertiseProjectPanel (AbstractViewer pv)
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
        return AdvertiseProjectPanel.PANEL_ID;

    }
                    
    public void init ()
               throws GeneralException
    {
/*
        final AdvertiseProjectPanel _this = this;

        Header h = UIUtils.createHeader ("Advertise your {Project}",
                                         Constants.PANEL_TITLE,
                                         Constants.NOTIFY_ICON_NAME,
                                         null);
        this.add (h);
                               
        EditorsWebServiceHandler wsHandler = EditorsEnvironment.getEditorsWebServiceHandler ();
        
        EditorAccount acc = EditorsEnvironment.getUserAccount ();
        
        EditorAuthor auth = null;
        
        String email = null;
        String pass = null;
        
        if (acc != null)
        {
            
            email = acc.getEmail ();
                        
            auth = acc.getAuthor ();
                                         
        }
        
        EditorProject proj = null; //this.projectViewer.getProject ().getEditorProject ();
        
        String title = ""; //this.projectViewer.getProject ().getName ();
        String desc = null;
        Set<String> selectedGenres = null;
        String wcLength = null;
        String exp = null;
        
        if (proj != null)
        {
            
            title = proj.getName ();
            desc = (proj.getDescription () != null ? proj.getDescription ().getText () : null);
            selectedGenres = proj.getGenres ();
            wcLength = proj.getWordCountLength ().getType ();
            exp = proj.getExpectations ();
            
        }
        
        String yourName = null;
        File avatarFile = null;
        String about = null;
        
        if (auth != null)
        {
            
            yourName = auth.getName ();
            
            DataSource ds = auth.getAvatar ();
            
            if (ds != null)
            {
            
                if (!(ds instanceof FileDataSource))
                {
                    
                    Environment.logError ("Illegal data source type for author avatar image: " +
                                          ds.getClass ().getName ());
                    
                } else {                
                
                    avatarFile = ((FileDataSource) ds).getFile ();
                    
                }

            }
            
            about = auth.getAbout ();
            
        }
        
        final Box box = new ScrollableBox (BoxLayout.Y_AXIS);
        box.setAlignmentX (Component.LEFT_ALIGNMENT);
                                         
        box.add (this.createHeader (Environment.replaceObjectNames ("About your {project}")));
        
        this.titleF = new TextFormItem ("Title",
                                        false,
                                        1,
                                        title,
                                        250,
                                        "chars",
                                        true,
                                        null);
        
        this.formItems.put ("title",
                            this.titleF);
        
        box.add (titleF);

        this.descF = new TextFormItem ("Description",
                                       true,
                                       5,
                                       desc,
                                       1000,
                                       "words",
                                       true,
                                       null);
        
        this.formItems.put ("desc",
                            this.descF);

        box.add (this.descF);

        Vector<String> gitems = new Vector (EditorsEnvironment.getWritingGenres ());
        
        this.genresF = new SelectFormItem ("Genres",
                                           gitems,
                                           5,
                                           selectedGenres,
                                           5,
                                           false,
                                           "If your genre(s) is not listed please describe it in the description above.");
        this.formItems.put ("genres",
                            this.genresF);
        box.add (this.genresF);

        Vector<String> typeVals = new Vector ();
        typeVals.add (EditorProject.WordCountLength.shortStory.getDescription ());//"Short story (less than 7,500 words)");
        typeVals.add (EditorProject.WordCountLength.novelette.getDescription ());//"Novellette (7,500 to 17,5000 words");
        typeVals.add (EditorProject.WordCountLength.novella.getDescription ());//"Novella (17,500 to 40,000 words");
        typeVals.add (EditorProject.WordCountLength.novel.getDescription ());//"Novel (over 40,000 words");
        
        this.typesF = new ComboBoxFormItem ("The approximate amount of text you would like to be edited",
                                            typeVals,
                                            wcLength,
                                            null);
        this.formItems.put ("types",
                            this.typesF);

        box.add (this.typesF);

        box.add (this.createHeader (Environment.replaceObjectNames ("Your expectations")));

        this.expF = new TextFormItem ("What you are looking for from editors",
                                       true,
                                       5,
                                       exp,
                                       500,
                                       "words",
                                       true,
                                       null);
        
        this.formItems.put ("exp",
                            this.expF);

        box.add (this.expF);

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
                                        null);

        this.formItems.put ("about",
                            this.aboutF);
        aboutBox.add (this.aboutF);
                
        box.add (aboutBox);
        
        box.add (this.createHeader (Environment.replaceObjectNames ("Login details")));
                
        box.add (this.createHelpText ("In order to communicate with your editors you need to create an account with us.  Please provide an email address and a password."));

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
            
            //it.init ();
            
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
        
        final AdvertiseProjectPanel _this = this;
        
        new Thread (new Runnable ()
        {
            
            public void run ()
            {
        
                try
                {
                
                    // Wipe out any existing avatar image file.
                    EditorsEnvironment.getEditorsAuthorAvatarImageFile ().delete ();
                
                    EditorsWebServiceHandler h = EditorsEnvironment.getEditorsWebServiceHandler ();
                    
                    List<EditorProject> projs = h.findProjects ("Monster%");
                    
                    System.out.println ("FOUND: " + projs);
                    /*
                    h.saveAccount (_this.emailF.getValue (),
                                     _this.passF.getValue ());
*/
        System.out.println ("CREATED/SAVED ACCOUNT");

                    h.saveAuthor (_this.nameF.getValue (),
                                  _this.aboutF.getValue (),
                                  _this.avatarF.getValue ());
        System.out.println ("CREATED/SAVED AUTHOR");
/*
                    h.saveProject (_this.projectViewer,
                                   _this.titleF.getValue (),
                                   _this.descF.getValue (),
                                   _this.genresF.getValue (),
                                   _this.expF.getValue (),
                                   EditorProject.WordCountLength.getWordCountLengthByDescription (_this.typesF.getValue ()),
                                   null);
  */      
System.out.println ("CREATED/SAVED PROJECT");
        
                } catch (Exception e) {

                    UIUtils.showErrorMessage (_this,
                                              "Sorry, unable to create/update your information at the moment.  Please try again later.");
                    
                    Environment.logError ("Unable to create/update editor",
                                          e);
                    
                }
                
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

        final AdvertiseProjectPanel _this = this;

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
        
        return "Advertise your {Project}";
        
    }
    
    @Override
    public ImageIcon getIcon (int type)
    {

        return Environment.getIcon (Constants.NOTIFY_ICON_NAME,
                                    type);

    }
    
    public JScrollPane getScrollPane ()
    {

        return this.scrollPane;

    }

}
