package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.text.*;

import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class AboutPopup extends PopupContent
{

    public static final String POPUP_ID = "about";

    public AboutPopup (AbstractViewer viewer)
    {

        super (viewer);

        final AboutPopup _this = this;

        List<String> prefix = Arrays.asList (about,LanguageStrings.popup);

        Date d = new Date ();

        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy");

        String year = sdf.format (d);

        String relNotesUrl = UserProperties.get (Constants.QUOLL_WRITER_RELEASE_NOTES_URL_PROPERTY_NAME);

        relNotesUrl = StringUtils.replaceString (relNotesUrl,
                                                 "[[VERSION]]",
                                                 Environment.getQuollWriterVersion ().getVersion ().replace ('.',
                                                                                                             '_'));

        Form f = Form.builder ()
            .confirmButton (getUILanguageStringProperty (buttons,close))
            .item (getUILanguageStringProperty (Utils.newList (prefix, qwversion)),
                   QuollLabel.builder ()
                        .label (new SimpleStringProperty (Environment.getQuollWriterVersion ().getVersion ()))
                        .build ())
            .item (getUILanguageStringProperty (Utils.newList (prefix, copyright)),
                   QuollLabel.builder ()
                        .label (new SimpleStringProperty (String.format (UserProperties.get (Constants.COPYRIGHT_PROPERTY_NAME),
                                                                        year)))
                        .build ())
            .item (QuollHyperlink.createLink (viewer,
                                              getUILanguageStringProperty (Utils.newList (prefix,releasenotes)),
                                              relNotesUrl))
            .item (QuollHyperlink.createLink (viewer,
                                              getUILanguageStringProperty (Utils.newList (prefix,acknowledgments)),
                                              UserProperties.get (Constants.QUOLL_WRITER_ACKNOWLEDGMENTS_URL_PROPERTY_NAME)))
            .item (getUILanguageStringProperty (Utils.newList (prefix,website)),
                   QuollHyperlink.createLink (viewer,
                                              new SimpleStringProperty (Environment.getQuollWriterWebsite ()),
                                              Environment.getQuollWriterWebsite ()))
            .item (getUILanguageStringProperty (Utils.newList (prefix, sourcecode)),
                   QuollHyperlink.createLink (viewer,
                                              new SimpleStringProperty (UserProperties.get (Constants.SOURCE_CODE_WEBSITE_PROPERTY_NAME)),
                                              UserProperties.get (Constants.SOURCE_CODE_WEBSITE_PROPERTY_NAME)))
            .item (getUILanguageStringProperty (Utils.newList (prefix,supportqw)),
                   QuollHyperlink.createLink (viewer,
                                              new SimpleStringProperty ("Patreon"),
                                              UserProperties.get (Constants.PATREON_WEBSITE_PROPERTY_NAME)))
            .item (QuollHyperlink.createLink (viewer,
                                              getUILanguageStringProperty (Utils.newList (prefix,makeadonation)),
                                              UserProperties.get (Constants.GOFUNDME_WEBSITE_PROPERTY_NAME)))
            .withHandler (this.viewer)
            .build ();

        f.setOnConfirm (ev ->
        {

            this.close ();

        });

        this.getChildren ().add (f);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (about,LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.ABOUT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

}
