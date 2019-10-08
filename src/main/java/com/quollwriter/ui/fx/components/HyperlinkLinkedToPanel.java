package com.quollwriter.ui.fx.components;

import javafx.scene.control.*;
import javafx.beans.binding.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import com.quollwriter.uistrings.UILanguageStringsManager;
import static com.quollwriter.LanguageStrings.*;

public class HyperlinkLinkedToPanel extends VBox
{

    private LinkedToPanel linkedToPanel = null;
    private IPropertyBinder binder = null;

    public HyperlinkLinkedToPanel (NamedObject           obj,
                                   IPropertyBinder       binder,
                                   AbstractProjectViewer viewer)
    {

        this.getStyleClass ().add (StyleClassNames.LINKEDTO);

        this.linkedToPanel = new LinkedToPanel (viewer.getProject (),
                                                binder,
                                                viewer);
        this.linkedToPanel.showEdit ();

        ScrollPane sp = new ScrollPane (this.linkedToPanel);
        sp.managedProperty ().bind (sp.visibleProperty ());
        sp.setVisible (false);

        QuollHyperlink linkedToLink = QuollHyperlink.builder ()
            .styleClassName (StyleClassNames.LINKEDTO)
            .onAction (ev ->
            {

                sp.setVisible (!sp.isVisible ());

            })
            .build ();

        linkedToLink.textProperty ().bind (Bindings.createStringBinding (() ->
        {

            return getUILanguageStringProperty (linkedto, LanguageStrings.tree,(sp.isVisible () ? clicktohide : clicktoview)).getValue ();

        },
        sp.visibleProperty (),
        UILanguageStringsManager.uilangProperty ()));

        this.getChildren ().addAll (linkedToLink, sp);

    }

    public LinkedToPanel getLinkedToPanel ()
    {

        return this.linkedToPanel;

    }

}
