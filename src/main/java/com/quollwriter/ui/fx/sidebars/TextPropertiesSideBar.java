package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.stream.*;

import javafx.beans.property.*;
import javafx.util.*;
import javafx.scene.*;
import javafx.collections.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class TextPropertiesSideBar<E extends AbstractProjectViewer> extends SideBarContent
{

    public static final String SIDEBAR_ID = "textproperties";

    private TextProperties props = null;
    private ComboBox<Font> font = null;
    private Spinner<Integer> fontSize = null;
    private Slider fontSizeSlider = null;
    private ChoiceBox<StringProperty> alignment = null;
    private Spinner<Double> lineSpacing = null;
    private Slider lineSpacingSlider = null;
    private Slider textBorderSlider = null;
    private Spinner<Integer> textBorder = null;
    private CheckBox indentFirstLine = null;
    private CheckBox highlightWritingLine = null;

    private FullScreenPropertiesPanel fullScreenProps = null;
    private TextPropertiesPanel normalTextProps = null;
    private TextPropertiesPanel fullScreenTextProps = null;

    public TextPropertiesSideBar (E              viewer,
                                  TextProperties props)
    {

        super (viewer);

        this.props = props;

        VBox c = new VBox ();
        this.getChildren ().add (c);

        this.fullScreenProps = new FullScreenPropertiesPanel (viewer,
                                                              this.getBinder ());

        this.fullScreenProps.managedProperty ().bind (this.fullScreenProps.visibleProperty ());
        this.getBinder ().addChangeListener (viewer.getViewer ().fullScreenProperty (),
                                             (pr, oldv, newv) ->
        {

            if (newv)
            {

                this.getSideBar ().getHeader ().setTitle (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,LanguageStrings.fullscreentitle));

            } else {

                this.getSideBar ().getHeader ().setTitle (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,LanguageStrings.title));

            }

            this.fullScreenProps.setVisible (newv);

            this.normalTextProps.setVisible (!newv);
            this.fullScreenTextProps.setVisible (newv);

        });
        this.fullScreenProps.setVisible (viewer.getViewer ().isFullScreen ());

        this.normalTextProps = new TextPropertiesPanel (viewer,
                                                        Environment.getProjectTextProperties (),
                                                        getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,description,normal),
                                                        this.getBinder ());
        this.normalTextProps.setVisible (!viewer.getViewer ().isFullScreen ());
        this.fullScreenTextProps = new TextPropertiesPanel (viewer,
                                                            Environment.getFullScreenTextProperties (),
                                                            getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,description,fullscreen),
                                                            this.getBinder ());
        this.fullScreenTextProps.setVisible (viewer.getViewer ().isFullScreen ());
        c.getChildren ().addAll (this.fullScreenProps,
                                    this.normalTextProps,
                                    this.fullScreenTextProps);

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,(this.viewer.getViewer ().isFullScreen () ? fullscreentitle : LanguageStrings.title));

        return SideBar.builder ()
            .title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.EDITPROPERTIES)
            .styleSheet (StyleClassNames.EDITPROPERTIES)
            .headerIconClassName (StyleClassNames.EDITPROPERTIES)
            .withScrollPane (true)
            .canClose (true)
            //.headerControls ()?
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

    }

    @Override
    public void init (State s)
    {

        super.init (s);

    }

    @Override
    public State getState ()
    {

        return super.getState ();

    }

}
