package com.quollwriter.ui.fx.swing;

import java.awt.Container;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.datatransfer.*;

import java.net.*;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Collections;

import javax.swing.*;
import javax.swing.text.html.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;

import javafx.beans.property.*;

import org.imgscalr.Scalr;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.events.PropertyChangedAdapter;
import com.quollwriter.events.PropertyChangedEvent;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;

public class SwingUIUtils
{

    private static int DEFAULT_POPUP_WIDTH = 450;
    public static final IconProvider iconProvider = new DefaultIconProvider ();

    public static IconProvider getDefaultDictionaryProvider ()
    {

        return iconProvider;

    }

    public static final DropShadowBorderX popupPanelDropShadow = new DropShadowBorderX (getColor ("#cccccc"), //UIManager.getColor ("Control"),
                                                                                        1,
                                                                                        12);

    public static Image getTransparentImage ()
    {

        return new ImageIcon (SwingUIUtils.class.getResource (Constants.IMGS_DIR + Constants.TRANSPARENT_PNG_NAME)).getImage ();

    }

    public static void setAsButton (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

    }

    public static JScrollPane createEditorScrollPane (JComponent  content,
                                               QTextEditor editor,
                                               BooleanProperty typeWriterScrollingEnabled)
    {

        JScrollPane scrollPane = new JScrollPane (content);

        // This ensures that the viewport is always in sync with the text area size.
        editor.addKeyListener (new KeyAdapter ()
        {

            @Override
            public void keyPressed (KeyEvent ev)
            {

                // Get the caret.
                if (editor.getCaretPosition () >= editor.getText ().length ())
                {

                    Dimension d = editor.getSize ();

                    scrollPane.getViewport ().setViewSize (new Dimension (d.width,
                                                                          d.height + 200));

                }

            }

            @Override
            public void keyTyped (KeyEvent ev)
            {

                if ((ev.getModifiersEx () & KeyEvent.CTRL_DOWN_MASK) != 0)
                {

                    return;

                }

                Environment.playKeyStrokeSound ();

            }
        });

        content.setAlignmentX (Component.LEFT_ALIGNMENT);

        scrollPane.setBorder (null);
        scrollPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        scrollPane.getViewport ().setOpaque (false);
        scrollPane.getVerticalScrollBar ().setUnitIncrement (20);

        // TODO Remove? this.origEditorMargin = this.editor.getMargin ();
        scrollPane.addMouseWheelListener (new MouseWheelListener ()
        {

            public void mouseWheelMoved (MouseWheelEvent ev)
            {

                if (typeWriterScrollingEnabled.getValue ())
                {

                    try
                    {

                        int c = ev.getWheelRotation ();

                        Rectangle r = getRectForOffset (editor, editor.getCaret ().getDot ());

                        int ny = r.y + (c * r.height);

                        int d = editor.viewToModel2D (new Point (r.x,
                                                               ny));

                        editor.getCaret ().setDot (d);

                    } catch (Exception e) {

                        // Just ignore

                    }

                }

            }

        });

        return scrollPane;

    }

    public static Color getComponentColor ()
    {

        return getColor ("#fdfdfd");

    }

    public static Color getInnerBorderColor ()
    {

        return getColor ("#dddddd");

    }

    public static Color getTitleColor ()
    {

        return getColor ("#333333");

    }

    public static Color getHighlightColor ()
    {

        // #DAE4FC
        return new Color (218,
                          228,
                          252);

    }

    public static Color getBorderColor ()
    {

        return getColor ("#aaaaaa");

    }

    public static Color getDragIconColor ()
    {

        return getColor ("#aaaaaa");

    }

    public static Color getIconColumnColor ()
    {

        return getColor ("#f0f0f0");//"f5fcfe");

    }

    public static String colorToHex (Color c)
    {

        return "#" + Integer.toHexString (c.getRGB ()).substring (2);

    }

    public static Color getColor (String hexCode)
    {

        if (hexCode.startsWith ("#"))
        {

            hexCode = hexCode.substring (1);

        }

        hexCode = hexCode.toUpperCase ();

        return new Color (Integer.parseInt (hexCode.substring (0,
                                                               2),
                                            16),
                          Integer.parseInt (hexCode.substring (2,
                                                               4),
                                            16),
                          Integer.parseInt (hexCode.substring (4),
                                            16));
    }

    public static Graphics2D createThrowawayGraphicsInstance ()
    {

        return new BufferedImage (1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics ();

    }

    public static ImageIcon getColoredIcon (String name,
                                            int    type,
                                            Color  c)
    {

        ImageIcon ic = iconProvider.getIcon (name,
                                             type);

        if (ic == null)
        {

            return null;

        }

        return getColoredIcon (ic.getImage (),
                                       c);

    }

    public static ImageIcon getColoredIcon (Image  im,
                                            Color  c)
    {

        if (im == null)
        {

            return null;

        }

        final int crgb = c.getRGB ();

        ImageFilter filter = new RGBImageFilter()
        {
          public final int filterRGB(int x, int y, int rgb)
          {
            if (rgb != 0)
            {

            }
             return (rgb != 0 ? crgb : rgb);

          }
        };

        ImageProducer ip = new FilteredImageSource (im.getSource (), filter);
        return new ImageIcon (Toolkit.getDefaultToolkit().createImage(ip));

    }

    public static JMenuItem createMenuItem (String label,
                                            String icon,
                                            ActionListener action)
    {

        JMenuItem mi = new JMenuItem (Environment.getButtonLabel (label),
                                      iconProvider.getIcon (icon,
                                                            Constants.ICON_MENU));
        mi.addActionListener (action);

        return mi;

    }

    public static JMenuItem createMenuItem (String         label,
                                            ImageIcon      icon,
                                            ActionListener action)
    {

        JMenuItem mi = new JMenuItem (Environment.getButtonLabel (label),
                                      icon);
        mi.addActionListener (action);

        return mi;

    }

    public static JMenuItem createMenuItem (String label,
                                            String icon,
                                            ActionListener action,
                                            String         actionCommand,
                                            KeyStroke      accel)
    {

        JMenuItem mi = SwingUIUtils.createMenuItem (label,
                                               icon,
                                               action);

        mi.setActionCommand (actionCommand);
        mi.setAccelerator (accel);

        return mi;

    }

    public static EmptyBorder createPadding (int top,
                                             int left,
                                             int bottom,
                                             int right)
    {

        return new EmptyBorder (top, left, bottom, right);

    }

    public static JLabel createClickableLabel (String title,
                                               Icon   icon)
    {

        String ns = null;

        return createClickableLabel (title,
                                             icon,
                                             ns);

    }

    public static JLabel createClickableLabel (final String title,
                                               final Icon   icon,
                                               final String gotoURLOnClick)
    {

        return createClickableLabel (title,
                                             icon,
                                             new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (gotoURLOnClick != null)
                {

                    com.quollwriter.ui.fx.UIUtils.openURL (null,
                                     gotoURLOnClick);

                }

            }

        });

    }

    public static JLabel createClickableLabel (final String         title,
                                               final Icon           icon,
                                               final ActionListener onClick)
    {

        final JLabel l = new JLabel (null,
                                     icon,
                                     SwingConstants.LEFT)
        {

            @Override
            public void setText (String t)
            {

                if (t == null)
                {

                    super.setText (null);

                    return;

                }

                super.setText (String.format ("<html>%s</html>",
                                              Environment.replaceObjectNames (t)));

            }

            @Override
            public void setToolTipText (String t)
            {

                if (t == null)
                {

                    super.setToolTipText (null);

                    return;

                }

                super.setToolTipText (String.format ("%s",
                                                     Environment.replaceObjectNames (t)));

            }

        };

        l.setText (title);
        l.setForeground (getColor (Constants.HTML_LINK_COLOR));
        l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        l.setVerticalAlignment (SwingConstants.TOP);
        l.setVerticalTextPosition (SwingConstants.TOP);
        makeClickable (l,
                               onClick);

        return l;

    }

    public static void makeClickable (final JLabel l,
                                      final ActionListener onClick)
    {

        l.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                if (onClick != null)
                {

                    try
                    {

                        onClick.actionPerformed (new ActionEvent (l, 1, "clicked"));

                    } catch (Exception e) {

                        Environment.logError ("Unable to perform action",
                                              e);

                    }

                }

            }

        });

        l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

    }

    public static JButton createToolBarButton (String         icon,
                                               String         tooltip,
                                               String         actionCommand,
                                               ActionListener action)
    {

        JButton bt = new JButton (Environment.getIcon (icon,
                                                       Constants.ICON_TOOLBAR))
        {

            @Override
            public void setText (String t)
            {

                super.setText (Environment.replaceObjectNames (t));

            }

        };

        setAsButton (bt);
        bt.setToolTipText (Environment.replaceObjectNames (tooltip));
        bt.setActionCommand (actionCommand);
        bt.setOpaque (false);

        if (action != null)
        {

            bt.addActionListener (action);

        }

        return bt;

    }

    public static JComponent createPopupMenuButtonBar (String           title,
                                                       final JPopupMenu parent,
                                                       List<JComponent> buttons)
    {

        ActionListener aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                parent.setVisible (false);

            }

        };

        for (JComponent but : buttons)
        {

            if (but instanceof JButton)
            {

                JButton b = (JButton) but;

                b.addActionListener (aa);

            }

        }

        Box b = new Box (BoxLayout.X_AXIS);

        // This is to get around the "icon space" in the menu.
        b.add (Box.createHorizontalStrut (32));

        if (title == null)
        {

            title = "";

        }

        JLabel l = new JLabel (Environment.replaceObjectNames (title));

        l.setForeground (getColor ("#444444"));
        l.setFont (l.getFont ().deriveFont (Font.ITALIC));

        l.setPreferredSize (new Dimension (Math.max (50, l.getPreferredSize ().width),
                                           l.getPreferredSize ().height));

        b.add (l);

        b.add (Box.createHorizontalStrut (10));

        b.add (createButtonBar (buttons));

        b.add (Box.createHorizontalGlue ());

        return b;

    }

    public static void addNewAssetItemsToPopupMenu (final Container     m,
                                                    final Component     showPopupAt,
                                                    final ProjectViewer pv,
                                                    final String        name,
                                                    final String        desc)
    {

        String pref = getUIString (general,shortcut);
        //"Shortcut: ";

        Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType type : types)
        {

            JMenuItem mi = new JMenuItem (type.getObjectTypeName (),
                                          null); // TODO type.getIcon16x16 ());

            m.add (mi);

            KeyStroke k = type.getCreateShortcutKeyStroke ();

            if (k != null)
            {

                mi.setMnemonic (k.getKeyChar ());
                mi.setToolTipText (pref + Utils.keyStrokeToString (k));

            }

/*
 TODO Rework
            mi.addActionListener (UIUtils.createAddAssetActionListener (type,
                                                                        pv,
                                                                        name,
                                                                        desc));
*/
        }

    }

    public static void addNewAssetItemsAsToolbarToPopupMenu (JPopupMenu    m,
                                                             Component     showPopupAt,
                                                             ProjectViewer pv,
                                                             String        name,
                                                             String        desc)
    {

        List<JComponent> buts = new ArrayList<> ();

        Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType type : types)
        {

            JButton but = createButton (null, // TODO type.getIcon16x16 (),
                                                String.format (getUIString (assets,add,button,tooltip),
                                                                //"Click to add a new %s",
                                                               type.objectTypeNameProperty ().getValue ()),
                                                null);

            buts.add (but);

            final UserConfigurableObjectType _type = type;
/*
 TODO Rework.
            but.addActionListener (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    Asset a = null;

                    try
                    {

                        a = Asset.createAsset (_type);

                    } catch (Exception e) {

                        Environment.logError ("Unable to create new asset for object type: " +
                                              _type,
                                              e);

                        ComponentUtils.showErrorMessage (pv,
                                                         getUILanguageStringProperty (Arrays.asList (assets,add,actionerror),
                                                                                      _type.getObjectTypeName ()));
                                                  //"Unable to create new asset type.");

                        return;

                    }

                    if (a == null)
                    {

                        Environment.logError ("Unable to create new asset for object type: " +
                                              _type);

                        ComponentUtils.showErrorMessage (pv,
                                                         getUILanguageStringProperty (Arrays.asList (assets,add,actionerror),
                                                                                      _type.getObjectTypeName ()));
                                                  //"Unable to create new asset type.");

                        return;

                    }

                    if (name != null)
                    {

                        a.setName (name);

                    }

                    if (desc != null)
                    {

                        a.setDescription (new StringWithMarkup (desc));

                    }

                    AssetActionHandler aah = new AssetActionHandler (a,
                                                                     pv);

                    if (showPopupAt instanceof PopupsSupported)
                    {

                        aah.setPopupOver (pv);
                        aah.setShowPopupAt (null,
                                            "below");

                    } else
                    {

                        aah.setShowPopupAt (showPopupAt,
                                            "above");

                    }

                    aah.actionPerformed (ev);

                }

            });
*/
        }

        m.add (createPopupMenuButtonBar (null,
                                                 m,
                                                 buts));

    }

    public static JToolBar createButtonBar (List<? extends JComponent> buttons)
    {

        JToolBar tb = new JToolBar ();
        tb.setOpaque (false);
        tb.setFloatable (false);
        tb.setRollover (true);
        tb.setBackground (new Color (0,
                                     0,
                                     0,
                                     0));

        for (int i = 0; i < buttons.size (); i++)
        {

            JComponent b = buttons.get (i);

            setAsButton2 (b);
            tb.add (b);

        }

        tb.setBackground (null);
        tb.setAlignmentX (Component.LEFT_ALIGNMENT);
        tb.setBorder (null);

        return tb;

    }

    public static JButton createButton (ImageIcon icon)
    {

        return createButton (icon,
                                     null,
                                     null);

    }

    public static JButton createButton (ImageIcon      icon,
                                        String         toolTipText,
                                        ActionListener action)
    {

        JButton b = new JButton (icon);

        b.setFocusPainted (false);
        b.setToolTipText (toolTipText);
        b.setOpaque (false);
        setAsButton (b);

        if (action != null)
        {

            b.addActionListener (action);

        }

        return b;

    }

    public static JButton createButton (String         icon,
                                        int            iconType,
                                        String         toolTipText,
                                        ActionListener action)
    {

        return createButton (iconProvider.getIcon (icon,
                                                          iconType),
                                     toolTipText,
                                     action);

    }

    public static JButton createButton (String label,
                                        String icon)
    {

        JButton b = new JButton (Environment.getButtonLabel (label),
                                 (icon == null ? null : iconProvider.getIcon (icon,
                                                                             Constants.ICON_MENU)));

        return b;

    }

    public static JButton createButton (String label)
    {

        return createButton (label,
                                     (String) null);

    }

    public static JButton createButton (String         label,
                                        ActionListener onClick)
    {

        JButton b = createButton (label,
                                          (String) null);

        if (onClick != null)
        {

            b.addActionListener (onClick);

        }

        return b;

    }

    public static void setAsButton2 (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

        if (c instanceof AbstractButton)
        {

            final AbstractButton b = (AbstractButton) c;

            b.setContentAreaFilled (false);

            // b.setMargin (new Insets (2, 2, 2, 2));
            b.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void mouseEntered (MouseEvent ev)
                {

                    b.setContentAreaFilled (true);

                }

                @Override
                public void mouseExited (MouseEvent ev)
                {

                    b.setContentAreaFilled (false);

                }

            });

        }

    }

    public static Header createHeader (String     title,
                                       int        titleType)
    {

        return createHeader (title,
                                     titleType,
                                     (String) null,
                                     null);

    }

    public static Header createHeader (String     title,
                                       int        titleType,
                                       String     icon,
                                       JComponent controls)
    {

        int iconType = Constants.ICON_MENU;

        if (titleType == Constants.POPUP_WINDOW_TITLE)
        {

            iconType = Constants.ICON_POPUP;

        }

        if (titleType == Constants.SUB_PANEL_TITLE)
        {

            iconType = Constants.ICON_SUB_PANEL_MAIN;

        }

        if (titleType == Constants.PANEL_TITLE)
        {

            iconType = Constants.ICON_PANEL_MAIN;


        }

        if (titleType == Constants.FULL_SCREEN_TITLE)
        {

            iconType = Constants.ICON_PANEL_MAIN;

        }

        ImageIcon ii = null;

        if (icon != null)
        {

            ii = iconProvider.getIcon (icon,
                                      iconType);

        }

        return createHeader (title,
                                     titleType,
                                     ii,
                                     controls);

    }

    public static Header createHeader (String     title,
                                       int        titleType,
                                       Icon       icon,
                                       JComponent controls)
    {

        int fontSize = 10;
        Insets ins = null;

        if (titleType == Constants.POPUP_WINDOW_TITLE)
        {

            fontSize = 16;
            ins = null; //new Insets (3, 3, 3, 3);

        }

        if (titleType == Constants.SUB_PANEL_TITLE)
        {

            fontSize = 14;
            ins = new Insets (5, 5, 0, 0);

        }

        if (titleType == Constants.PANEL_TITLE)
        {

            fontSize = 16;
            ins = new Insets (5, 7, 5, 7);


        }

        if (titleType == Constants.FULL_SCREEN_TITLE)
        {

            fontSize = 18;
            ins = new Insets (5, 10, 8, 5);


        }

        Header h = new Header (Environment.replaceObjectNames (title),
                               icon,
                               controls);

        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        h.setFont (h.getFont ().deriveFont ((float) getScaledFontSize (fontSize)).deriveFont (Font.PLAIN));
        h.setTitleColor (getTitleColor ());
        h.setPadding (ins);

        return h;

    }

    public static JButton createHelpPageButton (final AbstractViewer viewer,
                                                final String  helpPage,
                                                final int     iconType,
                                                final String  helpText)
    {

        final JButton helpBut = new JButton (iconProvider.getIcon ("help",
                                                                  iconType));
        helpBut.setToolTipText ((helpText != null ? helpText : getUIString (help,button,tooltip)));
        //"Click to view the help"));
        helpBut.setOpaque (false);
        setAsButton (helpBut);

        helpBut.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                com.quollwriter.ui.fx.UIUtils.openURL (viewer,
                                 "help://" + helpPage);

            }

        });

        return helpBut;

    }

    public static float getScaledFontSize (int v)
    {

        // Ugh, assume a 96 dpi and let the underlying windows manager handle scaling.
        // We return a float here so that calls to Font.deriveFont can use the value directly rather than
        // having to cast.
        return (float) (Math.round ((float) v * ((float) Toolkit.getDefaultToolkit ().getScreenResolution () / 72f)));

    }

    public static Runnable createDoLater (Runnable r)
    {

        return new Runnable ()
        {

            @Override
            public void run ()
            {

                SwingUIUtils.doLater (r);

            }

        };

    }

    public static void doLater (Runnable r)
    {

        SwingUtilities.invokeLater (() ->
        {

            try
            {

                r.run ();

            } catch (Exception e) {

                Environment.logError ("Unable to perform action",
                                      e);

            }

        });

    }

    public static void doLater (ActionListener l)
    {

        doLater (l,
                         null);

    }

    public static void doLater (final ActionListener l,
                                final Object         c)
    {

        if (l == null)
        {

            return;

        }

        SwingUtilities.invokeLater (new Runnable ()
        {

            public void run ()
            {

               try
               {

                   l.actionPerformed (new ActionEvent ((c != null ? c : this),
                                                       0,
                                                       "do"));

               } catch (Exception e) {

                    Environment.logError ("Unable to perform action",
                                          e);
/*
                    if (c instanceof Component)
                    {

                        UIUtils.showErrorMessage ((Component) c,
                                                  "Unable to perform action");

                    }
*/
               }

            }

        });


    }

    public static boolean clipboardHasContent ()
    {

        try
        {

            return (Toolkit.getDefaultToolkit ().getSystemClipboard ().getData (DataFlavor.stringFlavor) != null);

        } catch (Exception e)
        {

        }

        return false;

    }

    public static int getEditorFontSize (int size)
    {

        // Need to take the screen resolution into account.
        float s = (float) size * ((float) Toolkit.getDefaultToolkit ().getScreenResolution () / 72f);

        return (int) s;

    }

    public static java.awt.Color toSwingColor (javafx.scene.paint.Color c)
    {

        return new java.awt.Color ((int) (c.getRed () * 255), (int) (c.getGreen () * 255), (int) (c.getBlue () * 255), (int) (c.getOpacity () * 255));

    }

    /**
     * Is a wrapper for:
     * <code>
     *  new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getInnerBorderColor ()),
     *                      UIUtils.createPadding (top, left, bottom, right));
     * </code>
     *
     * @returns A compound border with an outer line and an inner padding.
     */
    public static CompoundBorder createBottomLineWithPadding (int top,
                                                              int left,
                                                              int bottom,
                                                              int right)
    {

        return new CompoundBorder (new MatteBorder (0, 0, 1, 0, getInnerBorderColor ()),
                                   createPadding (top, left, bottom, right));

    }

    public static Box createLinkedToItemsBox (final NamedObject           obj,
                                              final AbstractProjectViewer pv,
                                              final QPopup                p,
                                              boolean                     addTitle)
    {

        Box pa = new Box (BoxLayout.Y_AXIS);
        pa.setOpaque (false);

        if (addTitle)
        {

            JLabel h = createLabel (getUIString (linkedto,view,title));
            //"Linked to");
            pa.add (h);
            pa.add (Box.createVerticalStrut (3));

        }

        Set<NamedObject> sl = obj.getOtherObjectsInLinks ();

        Iterator<NamedObject> liter = sl.iterator ();

        while (liter.hasNext ())
        {

            final NamedObject other = liter.next ();

            JLabel l = new JLabel (other.getName ());
            l.setOpaque (false);
            l.setIcon (iconProvider.getIcon (other.getObjectType (),
                                            Constants.ICON_MENU));
            l.setToolTipText (getUIString (viewitem,tooltip));
            //"Click to view the item");
            l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
            l.setForeground (Color.BLUE);
            l.setBorder (new EmptyBorder (0,
                                          5,
                                          0,
                                          0));

            l.setAlignmentX (Component.LEFT_ALIGNMENT);

            pa.add (l);
            pa.add (Box.createVerticalStrut (3));

            l.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void handlePress (MouseEvent ev)
                {

                    // Prevents the popup from disappearing.
                    try
                    {

                        pv.viewObject (other);

                    } catch (Exception e) {

                        Environment.logError ("Unable to view: " +
                                              other,
                                              e);

                        // TODO SHow error

                    }

                    if (p != null)
                    {

                        // Close the popup.
                        p.setVisible (false);

                    }

                }

            });

        }

        pa.setPreferredSize (new Dimension (380,
                                            pa.getPreferredSize ().height));

        return pa;

    }

    public static JLabel createLabel (String message)
    {

        JLabel err = new JLabel ()
        {

            @Override
            public void setText (String t)
            {

                super.setText (String.format ("<html>%s</html>",
                                              t));

            }

        };

        err.setText (message);
        err.setAlignmentX (Component.LEFT_ALIGNMENT);

        return err;

    }

    public static JLabel createLabel (final String         title,
                                      final Icon           icon,
                                      final ActionListener onClick)
    {

        final JLabel l = new JLabel (null,
                                     icon,
                                     SwingConstants.LEFT)
        {

            @Override
            public void setText (String t)
            {

                if (t == null)
                {

                    super.setText (null);

                    return;

                }

                super.setText (String.format ("<html>%s</html>",
                                              t));

            }

            @Override
            public void setToolTipText (String t)
            {

                if (t == null)
                {

                    super.setToolTipText (null);

                    return;

                }

                super.setToolTipText (t);

            }

        };

        l.setText (title);

        if (onClick != null)
        {

            l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
            makeClickable (l,
                                   onClick);

        }

        l.setVerticalAlignment (SwingConstants.TOP);
        l.setVerticalTextPosition (SwingConstants.TOP);

        return l;

    }

    public static JEditorPane createObjectDescriptionViewPane (final String                  description,
                                                             final NamedObject             n,
                                                             final AbstractProjectViewer   pv,
                                                             final ProjectObjectQuollPanel qp)
    {

        HTMLEditorKit kit = new HTMLEditorKit ();
        HTMLDocument  doc = (HTMLDocument) kit.createDefaultDocument ();

        //final JTextPane desc = new JTextPane (doc)
        final JEditorPane desc = new JEditorPane ()
        {

            @Override
            public void setText (String t)
            {

                super.setText (getWithHTMLStyleSheet (this,
                                                              markupStringForAssets (t,
                                                                                             pv.getProject (),
                                                                                             n)));

            }

        };

        desc.setDocument (doc);
        desc.setEditorKit (kit);
        desc.setEditable (false);
        desc.setOpaque (false);

        /*
        desc.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));
*/
        desc.setSize (new Dimension (250, Short.MAX_VALUE));
        desc.addHyperlinkListener (new HyperlinkListener ()
        {

            @Override
            public void hyperlinkUpdate (HyperlinkEvent ev)
            {

                if (ev.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
                {

                    URL url = ev.getURL ();

                    if (url.getProtocol ().equals (Constants.OBJECTREF_PROTOCOL))
                    {

                        try
                        {

                            pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (url.getHost ())));

                        } catch (Exception e) {

                            Environment.logError ("Unable to show object",
                                                  e);

                            // TODO Show error

                        }

                        return;

                    }

                    if (url.getProtocol ().equals (Constants.OBJECTNAME_PROTOCOL))
                    {

                        String un = url.getHost ();

                        try
                        {

                            un = URLDecoder.decode (un, "utf-8");

                        } catch (Exception e) {

                            // Ignore.

                        }

                        Set<NamedObject> objs = pv.getProject ().getAllNamedObjectsByName (un);

                        if ((objs == null)
                            ||
                            (objs.size () == 0)
                           )
                        {

                            return;

                        }

                        if (objs.size () == 1)
                        {

                            try
                            {

                                pv.viewObject (objs.iterator ().next ());

                            } catch (Exception e) {

                                Environment.logError ("Unable to show object: " +
                                                      objs.iterator ().next (),
                                                      e);

                                // TODO Show error

                            }

                            return;

                        } else {

                            try
                            {

                                Point point = getPointForOffset (desc, ev.getSourceElement ().getStartOffset ());
/*
TODO
                                point = SwingUtilities.convertPoint (desc,
                                                                     point,
                                                                     pv);
*/
                                showObjectSelectPopup (objs,
                                                               pv,
                                                               getUIString (selectitem,popup,title),
                                                               //"Select an item to view",
                                                               new ActionListener ()
                                                               {

                                                                    @Override
                                                                    public void actionPerformed (ActionEvent ev)
                                                                    {

                                                                        try
                                                                        {

                                                                            pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (ev.getActionCommand ())));

                                                                        } catch (Exception e) {

                                                                            Environment.logError ("Unable to show object",
                                                                                                  e);

                                                                            // TODO Show error

                                                                        }

                                                                    }

                                                               },
                                                               true,
                                                               point);

                            } catch (Exception e) {

                                Environment.logError ("Unable to show popup",
                                                      e);

                                try
                                {

                                    pv.viewObject (objs.iterator ().next ());

                                } catch (Exception ee) {

                                    Environment.logError ("Unable to show object: " +
                                                          objs.iterator ().next (),
                                                          ee);

                                    // TODO Show error

                                }

                                return;

                            }

                        }

                        return;

                    }

                    if (url.getProtocol ().equals ("mailto"))
                    {

                        return;

                    }

                    try
                    {

                        com.quollwriter.ui.fx.UIUtils.openURL (pv,
                                         url);

                    } catch (Exception e) {

                        Environment.logError ("Unable to show url: " +
                                              url,
                                              e);

                        // TODO Show error

                    }

                }

            }

        });

        if (qp != null)
        {

            PropertyChangedAdapter pca = new PropertyChangedAdapter ()
            {

                public void propertyChanged (PropertyChangedEvent ev)
                {

                    if (ev.getChangeType ().equals (NamedObject.DESCRIPTION))
                    {

                        desc.setText (description);

                    }

                }

            };

            qp.addObjectPropertyChangedListener (pca);

            pca.propertyChanged (new PropertyChangedEvent (qp,
                                                           NamedObject.DESCRIPTION,
                                                           null,
                                                           null));

        } else {

            desc.setText (description);

        }

        return desc;

    }

    public static JEditorPane createObjectDescriptionViewPane (final StringWithMarkup        description,
                                                             final NamedObject             n,
                                                             final AbstractProjectViewer   pv,
                                                             final ProjectObjectQuollPanel qp)
    {

        // TODO: Markup to html?
        return createObjectDescriptionViewPane ((description != null ? description.getMarkedUpText () : null),
                                                        n,
                                                        pv,
                                                        qp);

    }

    public static void showObjectSelectPopup (final Set<? extends NamedObject> objs,
                                              final AbstractViewer             parent,
                                              final String                     popupTitle,
                                              final ActionListener             onSelect,
                                              final boolean                    closeOnSelect,
                                              final Point                      showAt)
    {

        showObjectSelectPopup (objs,
                                       parent,
                                       popupTitle,
                                       new DefaultListCellRenderer ()
                                       {

                                           @Override
                                           public Component getListCellRendererComponent (JList   list,
                                                                                          Object  value,
                                                                                          int     index,
                                                                                          boolean isSelected,
                                                                                          boolean cellHasFocus)
                                           {

                                               NamedObject obj = (NamedObject) value;

                                               JLabel l = (JLabel) super.getListCellRendererComponent (list,
                                                                                                       value,
                                                                                                       index,
                                                                                                       isSelected,
                                                                                                       cellHasFocus);

                                               l.setText (obj.getName ());

                                               l.setFont (l.getFont ().deriveFont (getScaledFontSize (14)).deriveFont (Font.PLAIN));
                                               l.setIcon (iconProvider.getObjectIcon (obj,
                                                                                     Constants.ICON_NOTIFICATION));
                                               l.setBorder (createBottomLineWithPadding (5, 5, 5, 5));

                                               if (cellHasFocus)
                                               {

                                                   l.setBackground (getHighlightColor ());

                                               }

                                               return l;

                                           }

                                       },
                                       onSelect,
                                       closeOnSelect,
                                       showAt);

    }

    public static void showObjectSelectPopup (final Set<? extends NamedObject> objs,
                                              final AbstractViewer             parent,
                                              final String                     popupTitle,
                                              final ListCellRenderer           renderer,
                                              final ActionListener             onSelect,
                                              final boolean                    closeOnSelect,
                                              final Point                      showAt)
    {

        showObjectSelectPopup (objs,
                                       parent,
                                       popupTitle,
                                       renderer,
                                       onSelect,
                                       closeOnSelect,
                                       null,
                                       showAt);

    }

    public static void showObjectSelectPopup (final Set<? extends NamedObject> objs,
                                              final AbstractViewer             parent,
                                              final String                     popupTitle,
                                              final ListCellRenderer           renderer,
                                              final ActionListener             onSelect,
                                              final boolean                    closeOnSelect,
                                              final JComponent                 extra,
                                              final Point                      showAt)
    {

        if (popupTitle == null)
        {

            throw new IllegalArgumentException ("Expected a popup title.");

        }

        doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                final Box content = new Box (BoxLayout.Y_AXIS);

                content.setOpaque (true);
                content.setBackground (getComponentColor ());

                DefaultListModel<NamedObject> m = new DefaultListModel<> ();

                for (NamedObject o : objs)
                {

                    m.addElement (o);

                }

                final JList l = new JList<NamedObject> ();
                l.setModel (m);
                l.setLayoutOrientation (JList.VERTICAL);
                l.setVisibleRowCount (0);
                l.setOpaque (true);
                l.setBackground (getComponentColor ());
                l.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 Short.MAX_VALUE));
                setAsButton (l);

                l.setCellRenderer (renderer);
/*
                new DefaultListCellRenderer ()
                {

                    public Component getListCellRendererComponent (JList   list,
                                                                   Object  value,
                                                                   int     index,
                                                                   boolean isSelected,
                                                                   boolean cellHasFocus)
                    {

                        NamedObject obj = (NamedObject) value;

                        JLabel l = (JLabel) super.getListCellRendererComponent (list,
                                                                                value,
                                                                                index,
                                                                                isSelected,
                                                                                cellHasFocus);

                        l.setText (obj.getName ());

                        l.setFont (l.getFont ().deriveFont (UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
                        l.setIcon (Environment.getObjectIcon (obj,
                                                              Constants.ICON_NOTIFICATION));
                        l.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));

                        if (cellHasFocus)
                        {

                            l.setBackground (Environment.getHighlightColor ());

                        }

                        return l;

                    }

                });
*/
/*
                int rowHeight = 37;

                Component t = renderer.getListCellRendererComponent (l,
                                                                     objs.iterator ().next (),
                                                                     0,
                                                                     false,
                                                                     false);

                rowHeight = t.getPreferredSize ().height;
*/
                l.setAlignmentX (JComponent.LEFT_ALIGNMENT);
                /*
                final Dimension sSize = new Dimension (this.swatchSize.width + (2 * this.borderWidth) + (2 * this.horizGap),
                                                       this.swatchSize.height + (2 * this.borderWidth) + (2 * this.vertGap));
                */
                JScrollPane sp = new JScrollPane (l);

                sp.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                sp.getVerticalScrollBar ().setUnitIncrement (20);//rowHeight);
                sp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
                sp.setOpaque (false);
/*
                sp.getViewport ().setPreferredSize (new Dimension (400,
                                                                   rowHeight * (objs.size () > 3 ? 3 : objs.size ())));
*/

                l.setVisibleRowCount (Math.min (3, objs.size ()));

                sp.setBorder (null);

                content.add (sp);

                if (extra != null)
                {

                    extra.setBorder (createPadding (10, 5, 10, 5));

                    content.add (extra);

                }

                final QPopup ep = createClosablePopup (popupTitle,
                                                               iconProvider.getIcon (Constants.VIEW_ICON_NAME,
                                                                                    Constants.ICON_POPUP),
                                                               null);

                ep.setContent (content);

                l.addListSelectionListener (new ListSelectionListener ()
                {

                    @Override
                    public void valueChanged (ListSelectionEvent ev)
                    {

                        if (onSelect != null)
                        {

                            NamedObject obj = (NamedObject) l.getSelectedValue ();

                            onSelect.actionPerformed (new ActionEvent (obj,
                                                                       0,
                                                                       obj.getObjectReference ().asString ()));

                            if (closeOnSelect)
                            {

                                ep.removeFromParent ();

                            }

                        }
                    }

                });

                content.setPreferredSize (new Dimension (getPopupWidth (),
                                                    content.getPreferredSize ().height));
/*
TODO
                parent.showPopupAt (ep,
                                    (showAt != null ? showAt : getCenterShowPosition (parent,
                                                                                              ep)),
                                    false);
                ep.setDraggable (parent);
*/
            }

        });

    }

    public static String markupStringForAssets (String      t,
                                                Project     p,
                                                NamedObject ignore)
    {

        if (t == null)
        {

            return t;

        }

        Set<NamedObject> objs = p.getAllNamedChildObjects (Asset.class);

        Set<NamedObject> chaps = p.getAllNamedChildObjects (Chapter.class);

        if (chaps.size () > 0)
        {

            objs.addAll (chaps);

        }

        if (objs.size () == 0)
        {

            return t;

        }

        if (ignore != null)
        {

            objs.remove (ignore);

        }

        Map<String, List<NamedObjectNameWrapper>> items = new HashMap<> ();

        for (NamedObject o : objs)
        {

            NamedObjectNameWrapper.addTo (o,
                                          items);

        }

        NavigableMap<Integer, NamedObjectNameWrapper> reps = new TreeMap<> ();

        TextIterator ti = new TextIterator (t);

        for (NamedObject n : objs)
        {

            Set<Integer> matches = null;

            for (String name : n.getAllNames ())
            {

                matches = ti.findAllTextIndexes (name,
                                                 null);

                // TODO: This needs to be on a language basis.
                Set<Integer> matches2 = ti.findAllTextIndexes (name + "'s",
                                                               null);

                matches.addAll (matches2);

                Iterator<Integer> iter = matches.iterator ();

                while (iter.hasNext ())
                {

                    Integer ind = iter.next ();

                    // Now search back through the string to make sure
                    // we aren't actually part of a http or https string.
                    int httpInd = t.lastIndexOf ("http://",
                                                 ind);
                    int httpsInd = t.lastIndexOf ("https://",
                                                  ind);

                    if ((httpInd > -1)
                        ||
                        (httpsInd > -1)
                       )
                    {

                        // Check forward to ensure there is no white space.
                        String ss = t.substring (Math.max (httpInd, httpsInd),
                                                 ind);

                        boolean hasWhitespace = false;

                        char[] chars = ss.toCharArray ();

                        for (int i = 0; i < chars.length; i++)
                        {

                            if (Character.isWhitespace (chars[i]))
                            {

                                hasWhitespace = true;

                                break;

                            }

                        }

                        if (!hasWhitespace)
                        {

                            // This name is part of a http/https link so ignore.
                            continue;

                        }

                    }

                    // Check the char at the index, if it's uppercase then we upper the word otherwise lower.
                    if (Character.isLowerCase (t.charAt (ind)))
                    {

                        reps.put (ind,
                                  new NamedObjectNameWrapper (name.toLowerCase (),
                                                              n));

                    } else {

                        // Uppercase each of the words in the name.
                        reps.put (ind,
                                  new NamedObjectNameWrapper (TextUtilities.capitalize (name),
                                                              n));

                    }

                }

            }

        }

        NavigableMap<Integer, NamedObjectNameWrapper> nreps = new TreeMap<> ();

        List<Integer> mis = new ArrayList<> (reps.keySet ());

        // Sort by location.
        Collections.sort (mis);

        // Prune out the overlaps.
        for (int i = 0; i < mis.size (); i++)
        {

            Integer curr = mis.get (i);

            NamedObjectNameWrapper wrap = reps.get (curr);

            nreps.put (curr,
                       wrap);

            int ni = i + 1;

            if (ni < mis.size ())
            {

                Integer next = mis.get (ni);

                // Does the next match start before the end of the current match?
                if (next.intValue () <= curr.intValue () + wrap.name.length ())
                {

                    // Move to the next, when the loop goes to the next it will
                    // increment again moving past it.
                    i = ni;

                }

            }

        }

        reps = nreps;

        StringBuilder b = new StringBuilder (t);

        // We iterate backwards over the indexes so we don't have to choppy-choppy the string.
        Iterator<Integer> iter = reps.descendingKeySet ().iterator ();

        while (iter.hasNext ())
        {

            Integer ind = iter.next ();

            NamedObjectNameWrapper obj = reps.get (ind);
/*
            b = b.replace (ind,
                           ind + obj.name.length (),
                           "<a href='" + Constants.OBJECTREF_PROTOCOL + "://" + obj.namedObject.getObjectReference ().asString () + "'>" + obj.name + "</a>");
  */

            String w = obj.name;

            try
            {

                w = URLEncoder.encode (obj.name, "utf-8");

            } catch (Exception e) {

                // Ignore.

            }

            b = b.replace (ind,
                           ind + obj.name.length (),
                           String.format ("<a href='%s://%s'>%s</a>",
                                          Constants.OBJECTNAME_PROTOCOL,
                                          w,
                                          obj.name));

        }

        t = b.toString ();

        t = StringUtils.replaceString (t,
                                       String.valueOf ('\n'),
                                       "<br />");

        return t;

    }

    public static void doActionLater (final ActionListener action)
    {

        doActionLater (action,
                               null,
                               null);

    }

    public static void doActionLater (final ActionListener action,
                                      final Object         contextObject,
                                      final String         actionTypeName)
    {

        SwingUtilities.invokeLater (() ->
        {

            try
            {

                action.actionPerformed (new ActionEvent ((contextObject != null ? contextObject : action),
                                                         1,
                                                         (actionTypeName != null ? actionTypeName : "any")));

            } catch (Exception e) {

                Environment.logError ("Unable to perform action",
                                      e);

            }

        });

    }

    public static QPopup createClosablePopup (final String         title,
                                              final Icon           icon,
                                              final ActionListener onClose,
                                              final Component      content,
                                              final AbstractViewer viewer,
                                                    Point          showAt)
    {

        final QPopup ep = createClosablePopup (title,
                                                       icon,
                                                       onClose);

        Box b = new Box (BoxLayout.Y_AXIS);

        b.setBorder (createPadding (10, 10, 10, 10));

        b.add (content);

        ep.setContent (b);

        b.setPreferredSize (new Dimension (getPopupWidth (),
                                           b.getPreferredSize ().height));

        if (showAt == null)
        {
/*
 TODO
            showAt = getCenterShowPosition (viewer,
                                                    ep);
*/
        }

        /*
        TODO
        viewer.showPopupAt (ep,
                            showAt,
                            false);
                            */
        // TODO ep.setDraggable (viewer);

        return ep;

    }

    public static QPopup createClosablePopup (final String         title,
                                              final Icon           icon,
                                              final ActionListener onClose)
    {

        final QPopup qp = new QPopup (Environment.replaceObjectNames (title),
                                      icon,
                                      null)
        {

            public void setVisible (boolean v)
            {

                if ((!v)
                    &&
                    (onClose != null)
                   )
                {

                    onClose.actionPerformed (new ActionEvent (this,
                                                              0,
                                                              "closing"));

                }

                super.setVisible (v);

            }

        };

        JButton close = createButton (Constants.CLOSE_ICON_NAME,
                                              Constants.ICON_MENU,
                                              getUIString (actions,clicktoclose),
                                              //"Click to close",
                                              new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                qp.removeFromParent ();

            }

        });

        List<JButton> buts = new ArrayList ();
        buts.add (close);

        qp.getHeader ().setControls (createButtonBar (buts));

        return qp;

    }

    public static int getPopupWidth ()
    {

        return getScreenScaledWidth (DEFAULT_POPUP_WIDTH);

    }

    public static int getScreenScaledWidth (int w)
    {

        return Math.round ((float) w * ((float) java.awt.Toolkit.getDefaultToolkit ().getScreenResolution () / (float) 96f));

    }

    public static String getWithHTMLStyleSheet (JTextComponent desc,
                                                String         text)
    {

        return getWithHTMLStyleSheet (desc,
                                              text,
                                              null,
                                              null);

    }

    public static String getWithHTMLStyleSheet (JTextComponent desc,
                                                String         text,
                                                String         linkColor,
                                                String         textColor)
    {

        if (text == null)
        {

            text = "";

        }

        text = StringUtils.replaceString (text,
                                          String.valueOf ('\n'),
                                          "<br />");

        StringBuilder t = new StringBuilder ();
        t.append ("<html><head>");
        t.append (getHTMLStyleSheet (desc,
                                             linkColor,
                                             textColor));
        t.append ("</head><body><span>");
        t.append (markupLinks (text));
        t.append ("</span></body></html>");

        return t.toString ();

    }

    public static Point getCenterShowPosition (Component parent,
                                               Component show)
    {

        Dimension pd = (parent.isShowing () ? parent.getSize () : parent.getPreferredSize ());
        Dimension sd = (show.isShowing () ? show.getSize () : show.getPreferredSize ());

        return new Point ((pd.width - sd.width) / 2,
                          (pd.height - sd.height) / 2);

    }

    public static String markupLinks (String s)
    {

        if (s == null)
        {

            return s;

        }

        //s = Environment.replaceObjectNames (s);

        s = markupLinks ("http://",
                                 s);

        s = markupLinks ("https://",
                                 s);

        // Replace <a with "<a style=' etc...
        s = StringUtils.replaceString (s,
                                       "<a ",
                                       "<a style='color: " + Constants.HTML_LINK_COLOR + ";' ");

        return s;

    }

    public static String markupLinks (String urlPrefix,
                                      String s)
    {

        int ind = 0;

        while (true)
        {

            ind = s.indexOf (urlPrefix,
                             ind);

            if (ind != -1)
            {

                // Now check the previous character to make sure it's not " or '.
                if (ind > 0)
                {

                    char c = s.charAt (ind - 1);

                    if ((c == '"') ||
                        (c == '\''))
                    {

                        ind += 1;

                        continue;

                    }

                }

                // Find the first whitespace char after...
                char[] chars = s.toCharArray ();

                StringBuilder b = new StringBuilder ();

                for (int i = ind + urlPrefix.length (); i < chars.length; i++)
                {

                    if ((!Character.isWhitespace (chars[i]))
                        &&
                        (chars[i] != '<')
                       )
                    {

                        b.append (chars[i]);

                    } else {

                        break;

                    }

                }

                // Now replace whatever we got...
                String st = b.toString ().trim ();

                if ((st.length () == 0)
                    ||
                    (st.equals (urlPrefix))
                   )
                {

                    ind = ind + urlPrefix.length ();

                    continue;

                }

                // Not sure about this but "may" be ok.
                if ((st.endsWith (";")) ||
                    (st.endsWith (",")) ||
                    (st.endsWith (".")))
                {

                    st = st.substring (0,
                                       st.length () - 1);

                }

                String w = null;

                try
                {

                    w = "<a href='" + urlPrefix + st /*URLEncoder.encode (st, "utf-8")*/ + "'>" + urlPrefix + st + "</a>";

                } catch (Exception e) {

                    // Won't happen.

                }

                StringBuilder ss = new StringBuilder (s);

                s = ss.replace (ind,
                                ind + st.length () + urlPrefix.length (),
                                w).toString ();

                ind = ind + w.length ();

            } else
            {

                // No more...
                break;

            }

        }

        return s;

    }

    public static String getHTMLStyleSheet (JTextComponent desc,
                                            String         textColor,
                                            String         linkColor)
    {

        return getHTMLStyleSheet (desc,
                                          textColor,
                                          linkColor,
                                          -1);

    }

    public static String getHTMLStyleSheet (JTextComponent desc,
                                            String         textColor,
                                            String         linkColor,
                                            int            textSize)
    {

        StringBuilder t = new StringBuilder ();

        Font f = null;

        if (desc != null)
        {

            f = desc.getFont ();

        } else {

            f = new JLabel ().getFont ();

        }

        if (linkColor == null)
        {

            linkColor = Constants.HTML_LINK_COLOR;

        }

        if (!linkColor.startsWith ("#"))
        {

            linkColor = "#" + linkColor;

        }

        if (textColor == null)
        {

            textColor = "#000000";

        }

        if (!textColor.startsWith ("#"))
        {

            textColor = "#" + textColor;

        }

        int size = textSize;

        if (size < 1)
        {

            size = (int) f.getSize ();

        }

        t.append ("<style>");
        t.append ("*{font-family: \"" + f.getFontName () + "\"; font-size: " + size + "px; background-color: transparent; color: " + textColor + ";}\n");
        t.append ("body{padding: 0px; margin: 0px;color: " + textColor + "; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}");
        t.append ("body{overflow-x: hidden; overflow-y: hidden;}");
        t.append ("h1, h2, h3{font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("span{color: " + textColor + "; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("p{padding: 0px; margin: 0px; color: " + textColor + "; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("img{vertical-align:middle; padding: 3px; border: solid 1px transparent; line-height: 22px;}");
        t.append ("a{color: " + linkColor + "; text-decoration: none; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("a.link{color: " + linkColor + "; text-decoration: none; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");

        t.append ("a:hover {text-decoration: underline; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("b {font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");

        t.append ("ul{margin-left: 20px;}\n");
        t.append ("li{font-size: " + ((int) f.getSize ()) + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("p.error{padding: 0px; margin: 0px; color: red;}");
        t.append ("h1.help{font-size:" + (((int) f.getSize ()) + 6) + "pt; padding: 0px; margin: 0px; border-bottom: solid 1px " + colorToHex (getBorderColor ()) + "; font-weight: normal;}");
        t.append ("p.help{margin: 0px; margin-left: 5px; margin-top: 5px; padding: 0px; margin-bottom: 10px;}");
        t.append ("p.help img{padding-left: 3px; padding-right: 3px;}");
        t.append (".error{color: red;}");
        t.append (".warning{color: red;}");

        // A pox on the java html renderer.
        t.append ("span.b{font-weight: bold;}");
        t.append ("span.bi{font-style: italic; font-weight: bold;}");
        t.append ("span.bu{font-weight: bold; text-decoration: underline;}");
        t.append ("span.biu{font-style: italic; font-weight: bold; text-decoration: underline;}");
        t.append ("span.i{font-style: italic;}");
        t.append ("span.iu{font-style: italic; text-decoration: underline;}");
        t.append ("span.u{text-decoration: underline;}");

        // Sigh, this is needed otherwise inner "a" tags won't inherit the styles.
        t.append ("span.b a{font-weight: bold;}");
        t.append ("span.bi a{font-style: italic; font-weight: bold;}");
        t.append ("span.bu a{font-weight: bold; text-decoration: underline;}");
        t.append ("span.biu a{font-style: italic; font-weight: bold; text-decoration: underline;}");
        t.append ("span.i a{font-style: italic;}");
        t.append ("span.iu a{font-style: italic; text-decoration: underline;}");
        t.append ("span.u a{text-decoration: underline;}");
        t.append ("span.iu a{font-style: italic; text-decoration: underline;}");
        t.append ("span a img{text-decoration: none; border: 0px;}");
        t.append ("</style>");

        return t.toString ();

    }

    public static String getHTMLStyleSheet (JTextComponent desc)
    {

        return getHTMLStyleSheet (desc,
                                          "#000000",
                                          Constants.HTML_LINK_COLOR);

    }

    public static Point getPointForOffset (AbstractEditorPanel comp,
                                           int                 offset)
                                    throws BadLocationException

    {

        return getPointForOffset (comp.getEditor (),
                                  offset);

    }

    public static Point getPointForOffset (JTextComponent comp,
                                           int            offset)
                                    throws BadLocationException

    {

        Rectangle r = getRectForOffset (comp,
                                        offset);

        return r.getLocation ();

    }

    public static Rectangle getRectForOffset (AbstractEditorPanel comp,
                                              int                 offset)
                                       throws BadLocationException

    {

        return getRectForOffset (comp.getEditor (),
                                 offset);

    }

    public static Rectangle getRectForOffset (JTextComponent comp,
                                              int            offset)
                                       throws BadLocationException
    {

        Rectangle2D r = comp.modelToView2D (offset);

        return new Rectangle ((int) r.getX (), (int) r.getY (), (int) r.getWidth (), (int) r.getHeight ());

    }

}
