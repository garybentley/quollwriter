package com.quollwriter.ui.sidebars;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.Component;
import java.awt.Image;
import java.awt.RenderingHints;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.*;

import java.awt.event.*;

import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.*;

import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ChangeAdapter;
import com.quollwriter.ui.components.TextProperties;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class TextPropertiesSideBar extends AbstractSideBar<AbstractProjectViewer> implements MainPanelListener, FullScreenListener
{

    public static final String ID = "textproperties";

    private TextPropertiesEditPanel props = null;
    private FullScreenPropertiesEditPanel fsprops = null;

    public TextPropertiesSideBar (AbstractProjectViewer pv,
                                  PopupsSupported       popupParent,
                                  TextProperties        props)
    {

        super (pv);

        this.fsprops = new FullScreenPropertiesEditPanel ();

        final TextPropertiesSideBar _this = this;

        this.props = new TextPropertiesEditPanel (pv,
                                                  props,
                                                  ProjectEvent.TEXT_PROPERTIES,
                                                  true,
                                                  popupParent)
        {

            public void setBackgroundColor (Color c)
            {

                //_this.fsTextProps.setBackgroundColor (c);

                _this.fsprops.setBackgroundColor (c);

            }

        };

        pv.addFullScreenListener (this);

    }

    @Override
    public String getId ()
    {

        return ID;

    }

    @Override
    public void fullScreenExited (FullScreenEvent ev)
    {

        this.onShow ();

        /*
        this.setTitle (this.getTitle ());

        UserProperties.removeListener (this.fsTextProps);

        // Add the listener last so that if we get an update nothing will NPE.
        UserProperties.addListener (this.props);

        this.props.setVisible (true);

        this.fsTextProps.setVisible (false);

        this.fsprops.setVisible (false);
        */
    }

    @Override
    public void fullScreenEntered (FullScreenEvent ev)
    {

        this.onShow ();

        /*
        this.setTitle ("Full Screen Properties");

        final TextPropertiesSideBar _this = this;

        UserProperties.removeListener (this.props);

        FullScreenFrame fsf = this.viewer.getFullScreenFrame ();

        UserProperties.addListener (this.fsTextProps);

        this.props.setVisible (false);
        this.fsprops.setFullScreenFrame (fsf);

        this.fsTextProps.setVisible (true);
        this.fsprops.setVisible (true);

        this.validate ();
        this.repaint ();
        */
    }

    /**
     * Always pref width, 250.
     */
    @Override
    public Dimension getMinimumSize ()
    {

        return new Dimension (this.getPreferredSize ().width,
                              250);
    }

    @Override
    public List<JComponent> getHeaderControls ()
    {

        return null;

    }

    public boolean canClose ()
    {

        return true;

    }

    public String getIconType ()
    {

        return Constants.SETTINGS_ICON_NAME;

    }

    @Override
    public void onHide ()
    {



    }

    @Override
    public void onShow ()
    {

        if (this.viewer.isInFullScreen ())
        {

            this.setTitle (getUIString (project,sidebar,textproperties,fullscreentitle));
            //"Full Screen Properties");

            UserProperties.removeListener (this.props);

            this.props.setTextProperties (Environment.getFullScreenTextProperties ());

            UserProperties.addListener (this.props);

            FullScreenFrame fsf = this.viewer.getFullScreenFrame ();

            this.fsprops.setFullScreenFrame (fsf);

            this.fsprops.setVisible (true);

        } else {

            UserProperties.removeListener (this.props);

            this.props.setTextProperties (Environment.getProjectTextProperties ());

            UserProperties.addListener (this.props);

            this.setTitle (this.getTitle ());

            this.fsprops.setVisible (false);

        }

        this.validate ();
        this.repaint ();


    }

    @Override
    public void onClose ()
    {

        UserProperties.removeListener (this.props);

        this.viewer.removeFullScreenListener (this);

    }

    public String getTitle ()
    {

        return getUIString (project,sidebar,textproperties,title);
        //"Text Properties";

    }

    @Override
    public void init (String saveState)
               throws GeneralException
    {

        super.init (saveState);

    }

    public void panelShown (MainPanelEvent ev)
    {

    }

    public JComponent getContent ()
    {

        Box b = new Box (BoxLayout.Y_AXIS);

        this.fsprops.setBorder (UIUtils.createPadding (0, 0, 10, 0));

        b.add (this.fsprops);

        b.add (this.props);

        this.props.init ();

        UserProperties.addListener (this.props);

        return this.wrapInScrollPane (b);

    }

}
