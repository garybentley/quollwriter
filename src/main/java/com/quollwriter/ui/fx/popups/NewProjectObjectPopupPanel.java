package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;

import javafx.event.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import com.quollwriter.uistrings.UILanguageStringsManager;
import static com.quollwriter.LanguageStrings.*;

public class NewProjectObjectPopupPanel<E extends AbstractProjectViewer> extends VBox
{

    private LinkedToPanel linkedToPanel = null;

    private NewProjectObjectPopupPanel (Builder<E> b)
    {

        Form.Builder fb = Form.builder ()
            .confirmButton (buttons,save)
            .cancelButton (buttons,cancel);

        b.controls.keySet ().stream ()
            .forEach (l ->
            {

                fb.item (l,
                         b.controls.get (l));

            });

        if (b.showLinkedTo)
        {

            HyperlinkLinkedToPanel lp = new HyperlinkLinkedToPanel (b.viewer.getProject (),
                                                                    b.viewer);

            this.linkedToPanel = lp.getLinkedToPanel ();

            fb.item (lp);

        }

        Form f = fb.build ();

        f.setOnConfirm (ev ->
        {

            if (b.onConfirm != null)
            {

                b.onConfirm.accept (ev, this.getSelectedLinkObjects ());

            }

        });

        f.setOnCancel (b.onCancel);

        this.getChildren ().add (f);

    }

    public Set<NamedObject> getSelectedLinkObjects ()
    {

        if (this.linkedToPanel == null)
        {

            return new LinkedHashSet<> ();

        }

        return this.linkedToPanel.getSelected ();

    }

    public static <E extends AbstractProjectViewer> NewProjectObjectPopupPanel.Builder<E> builder ()
    {

        return new Builder<> ();

    }

    public static class Builder<E extends AbstractProjectViewer> implements IBuilder<Builder<E>, NewProjectObjectPopupPanel<E>>
    {

        private BiConsumer<Form.FormEvent, Set<NamedObject>> onConfirm = null;
        private EventHandler<Form.FormEvent> onCancel = null;
        private boolean showLinkedTo = true;
        private E viewer = null;
        private Map<StringProperty, Node> controls = new LinkedHashMap<> ();

        private Builder ()
        {

        }

        public Builder<E> withViewer (E viewer)
        {

            this.viewer = viewer;
            return this;

        }

        public Builder<E> showLinkedTo (boolean v)
        {

            this.showLinkedTo = v;
            return this;

        }

        public Builder<E> onConfirm (BiConsumer<Form.FormEvent, Set<NamedObject>> on)
        {

            this.onConfirm = on;
            return this;

        }

        public Builder<E> onCancel (EventHandler<Form.FormEvent> on)
        {

            this.onCancel = on;
            return this;

        }

        public Builder<E> item (StringProperty label,
                                Node           control)
        {

            this.controls.put (label,
                               control);
            return this;

        }

        public Builder<E> item (List<String> labelIds,
                                Node         control)
        {

            this.controls.put (getUILanguageStringProperty (labelIds),
                               control);
            return this;

        }

        @Override
        public NewProjectObjectPopupPanel<E> build ()
        {

            return new NewProjectObjectPopupPanel<> (this);

        }

        @Override
        public Builder<E> _this ()
        {

            return this;

        }

    }

}
