package com.quollwriter.ui.userobjects;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ScrollableBox;

public class LayoutAddEdit extends Box implements ProjectEventListener
{

    private AbstractViewer viewer = null;
    private UserConfigurableObjectType type = null;
    private JTextPane helpText = null;
    private JComponent helpTextBox = null;
    private JList<String> layouts = null;

    public LayoutAddEdit (AbstractViewer             viewer,
                          UserConfigurableObjectType type)
    {

        super (BoxLayout.Y_AXIS);

        final LayoutAddEdit _this = this;

        this.viewer = viewer;
        this.type = type;

        Environment.addUserProjectEventListener (this);

        this.helpText = UIUtils.createHelpTextPane ("",
                                                    viewer);

        this.helpTextBox = new Box (BoxLayout.Y_AXIS);
        this.helpTextBox.add (this.helpText);
        this.helpTextBox.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 8, 0));

        this.helpTextBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.helpTextBox.setVisible (false);

        this.add (this.helpTextBox);

        Vector<String> layoutTypes = new Vector ();
        layoutTypes.add (Constants.ASSET_LAYOUT_0);
        layoutTypes.add (Constants.ASSET_LAYOUT_1);
        layoutTypes.add (Constants.ASSET_LAYOUT_2);
        layoutTypes.add (Constants.ASSET_LAYOUT_3);
        layoutTypes.add (Constants.ASSET_LAYOUT_4);
        layoutTypes.add (Constants.ASSET_LAYOUT_5);
        layoutTypes.add (Constants.ASSET_LAYOUT_6);
        layoutTypes.add (Constants.ASSET_LAYOUT_7);
        layoutTypes.add (Constants.ASSET_LAYOUT_8);

        this.layouts = new JList<String> (layoutTypes);

        this.layouts.addListSelectionListener (new ListSelectionListener ()
        {

            @Override
            public void valueChanged (ListSelectionEvent ev)
            {

                _this.type.setLayout (_this.layouts.getSelectedValue ());

                // Save the type.
                try
                {

                    Environment.updateUserConfigurableObjectType (_this.type);

                } catch (Exception e) {

                    Environment.logError ("Unable to update user object type: " +
                                          _this.type,
                                          e);

                    UIUtils.showErrorMessage (_this.viewer,
                                              Environment.getUIString (LanguageStrings.userobjects,
                                                                       LanguageStrings.layout,
                                                                       LanguageStrings.edit,
                                                                       LanguageStrings.actionerror));
                                                                       //"Unable to update the object type.");

                }

            }

        });

        this.layouts.setCellRenderer (new DefaultListCellRenderer ()
        {

            private Map<String, ImageIcon> images = new HashMap ();

            public Component getListCellRendererComponent (JList   list,
                                                           Object  value,
                                                           int     index,
                                                           boolean isSelected,
                                                           boolean cellHasFocus)
            {

                super.getListCellRendererComponent (list,
                                                    value,
                                                    index,
                                                    isSelected,
                                                    cellHasFocus);

                String imName = value.toString ();

                ImageIcon icon = this.images.get (imName);

                if (icon == null)
                {

                    icon = new ImageIcon (Environment.getImage (Constants.DATA_DIR + imName + ".png"));

                    this.images.put (imName,
                                     icon);

                }

                this.setIcon (icon);
                String text = "";

                java.util.List<String> prefix = new ArrayList ();
                prefix.add (LanguageStrings.userobjects);
                prefix.add (LanguageStrings.layout);
                prefix.add (LanguageStrings.view);
                prefix.add (LanguageStrings.layouts);

                if (imName.equals (Constants.ASSET_LAYOUT_0))
                {

                    text = Environment.getUIString (prefix,
                                                    LanguageStrings.layout0);
                                                    //"Fields are laid out in a single column.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_1))
                {

                    text = Environment.getUIString (prefix,
                                                    LanguageStrings.layout1);
                                                    //"The object image, if present, is displayed in the top left corner and the object description is shown to the right.  Two columns of fields are displayed underneath.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_2))
                {

                    text = Environment.getUIString (prefix,
                                                    LanguageStrings.layout2);
                                                    //"The object image is shown in the top left corner with the object description underneath.  The other fields are shown in a column on the right.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_3))
                {

                    text = Environment.getUIString (prefix,
                                                    LanguageStrings.layout3);
                                                    //"Two columns of fields are displayed.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_4))
                {

                    text = Environment.getUIString (prefix,
                                                    LanguageStrings.layout4);
                                                    //"The object image is shown in the top right corner with the object description underneath.  The other fields are shown in a column on the left.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_5))
                {

                    text = Environment.getUIString (prefix,
                                                    LanguageStrings.layout5);
                                                    //"The object description is shown in a column on the right, all others fields are shown in a column on the left.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_6))
                {

                    text = Environment.getUIString (prefix,
                                                    LanguageStrings.layout6);
                                                    //"The object description is shown in a column on the left, all others fields are shown in a column on the right.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_7))
                {

                    text = Environment.getUIString (prefix,
                                                    LanguageStrings.layout7);
                                                    //"The object description is shown in a column on the left, the object image is shown in the top right corner with all other fields in a column underneath.";

                }

                if (imName.equals (Constants.ASSET_LAYOUT_8))
                {

                    text = Environment.getUIString (prefix,
                                                    LanguageStrings.layout8);
                                                    //"The object image is shown in the top left corner with the object description in a column on the right.  The other object fields are shown underneath the object image.";

                }

                this.setText (String.format ("<html>%s</html>",
                                             text));
                                             //Environment.replaceObjectNames (text)));
                this.setBorder (UIUtils.createPadding (5, 3, 5, 3));
                this.setVerticalTextPosition (SwingConstants.TOP);

                if (isSelected)
                {

                    this.setBorder (new CompoundBorder (UIUtils.createLineBorder (),
                                                        this.getBorder ()));

                }

                return this;

            }

        });

        UIUtils.setAsButton (this.layouts);

        Box lb = new ScrollableBox (BoxLayout.Y_AXIS);
        lb.add (this.layouts);

        JScrollPane sp = UIUtils.createScrollPane (lb);
        sp.getViewport ().setPreferredSize (new Dimension (450,
                                                           400));

        sp.setBorder (null);
        this.add (sp);

    }

    public void setHelpText (String t)
    {

        if (t == null)
        {

            this.helpTextBox.setVisible (false);

            return;

        }

        this.helpText.setText (t);

        this.helpTextBox.setVisible (true);

    }

    @Override
    public void eventOccurred (ProjectEvent ev)
    {

        if (ev.getType ().equals (ProjectEvent.USER_OBJECT_TYPE))
        {

            if (ev.getSource ().equals (this.type))
            {

                this.refresh ();

            }

        }

    }

    public void refresh ()
    {

        final LayoutAddEdit _this = this;

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                String sel = _this.layouts.getSelectedValue ();

                String layout = _this.type.getLayout ();

                if (layout == null)
                {

                    layout = Constants.ASSET_LAYOUT_0;

                }

                if (((sel != null)
                     &&
                     (layout != null)
                     &&
                     (!sel.equals (layout))
                    )
                    ||
                    (sel == null)
                   )
                {

                    _this.layouts.setSelectedValue (layout,
                                                    true);

                }

            }

        });

    }

}
