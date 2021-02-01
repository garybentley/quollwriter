package com.quollwriter.ui;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.concurrent.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;

import javax.swing.event.*;
import javax.swing.*;
import javax.swing.text.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.forms.*;

public class LanguageStringsImageIdBox extends LanguageStringsIdBox<ImageValue, File>
{

    //private AbstractLanguageStringsEditor editor = null;
    private ImageSelector userValue = null;
    //private ImageValue baseValue = null;
    //private ImageValue stringsValue = null;

    //private JTextPane errors = null;
    //private Box errorsWrapper = null;
    //private JLabel errorsLabel = null;

    public LanguageStringsImageIdBox (final ImageValue              baseValue,
                                      final ImageValue              stringsValue,
                                      final LanguageStringsIdsPanel panel)
    {

        super (baseValue,
               stringsValue,
               panel);

    }

    public Component getFocusableComponent ()
    {

        return this.userValue;

    }

    public Set<FormItem> getFormItems ()
    {

        final LanguageStringsImageIdBox _this = this;

        Set<FormItem> items = new LinkedHashSet<> ();

        AnyFormItem view = new AnyFormItem (null,
                                            UIUtils.createClickableLabel ("View English Image Online",
                                                                          Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                               Constants.ICON_MENU),
                                                                          Environment.getQuollWriterWebsite () + this.baseValue.getUrl ()));

        items.add (view);
/*
        this.userValue = new ImageSelector ((this.stringsValue != null ? this.stringsValue.getImageFile () : null),
                                            UIUtils.imageFileFilter,
                                            new Dimension (150,
                                                           150));
        this.userValue.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.userValue.setBorder (UIUtils.createLineBorder ());
*/
        this.userValue.addChangeListener (new ChangeListener ()
        {

            @Override
            public void stateChanged (ChangeEvent ev)
            {

                _this.updateSideBar (_this.baseValue);

            }

        });

        Box _b = new Box (BoxLayout.X_AXIS);
        _b.add (this.userValue);
        _b.add (Box.createHorizontalGlue ());

        items.add (new AnyFormItem ("<html><i>Your Image</i></html>",
                                    _b));

        return items;

    }

    public void saveValue ()
                    throws GeneralException
    {

        File uv = this.getUserValue ();

        if (uv != null)
        {

            if (this.stringsValue == null)
            {

                this.stringsValue = this.getEditor ().userStrings.insertImageValue (this.baseValue.getId ());

            }

            this.stringsValue.setImageFile (uv.toPath ());

        } else {

            this.getEditor ().userStrings.removeNode (this.baseValue.getId ());

        }

    }

    @Override
    public File getUserValue ()
    {

        return this.userValue.getFile ();

    }

    @Override
    public boolean hasErrors ()
    {

        File s = this.getUserValue ();

        if (s == null)
        {

            return false;

        }

        return false;


    }

    public boolean showErrors (boolean requireUserValue)
    {

        this.hideErrors ();

        File s = this.getUserValue ();

        if ((s == null)
            &&
            (!requireUserValue)
           )
        {

            return false;

        }

        Set<String> errs = null;

        if (s == null)
        {

            errs = new LinkedHashSet<> ();

            errs.add ("Cannot show a preview, no value provided.");

        } else {

        }

        Node root = this.baseValue.getRoot ();

        this.updateSideBar (this.baseValue);

        return false;

    }

    public void showPreview ()
    {

        if (this.showErrors (false))
        {

            return;

        }

        File s = this.getUserValue ();

        if (s == null)
        {

            this.updateSideBar (this.baseValue);

            return;

        }

        this.updateSideBar (this.baseValue);

        this.validate ();
        this.repaint ();

    }

    @Override
    public Dimension getMaximumSize ()
    {

        return new Dimension (Short.MAX_VALUE,
                              this.getPreferredSize ().height);

    }

}
