package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;

import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.data.Asset;
import com.quollwriter.data.NamedObject;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.PropertyBinder;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollPopup extends StackPane implements IPropertyBinder
{

    private SimpleObjectProperty<Header> headerProp = null;
    private Runnable onClose = null;
    private double dx = 0;
    private double dy = 0;
    private double mx = 0;
    private double my = 0;
    private String popupId = null;
    private Boolean removeOnClose = false;
    private Set<QuollPopup> childPopups = null;
    private boolean moving = false;
    private PopupsViewer viewer = null;
    private PropertyBinder binder = null;
    private Node content = null;

    private QuollPopup (Builder b)
    {

        this.binder = new PropertyBinder ();

        this.childPopups = new HashSet<> ();

        this.viewer = b.viewer;

        if (b.viewer != null)
        {

            this.viewer.addEventHandler (Viewer.ViewerEvent.CLOSE_EVENT,
                                         ev ->
            {

                this.close ();

            });

        }

        //this.setPrefSize (javafx.scene.layout.Region.USE__SIZE, javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        //this.setMaxSize (javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
        this.removeOnClose = b.removeOnClose;

        final QuollPopup _this = this;

        this.popupId = b.popupId;

        Set<Node> controls = new LinkedHashSet<> ();

        if (b.controls != null)
        {

            controls.addAll (b.controls);

        }

        if (b.withClose)
        {

            QuollButton close = QuollButton.builder ()
                .iconName (StyleClassNames.CLOSE)
                .styleClassName (StyleClassNames.CLOSE)
                .tooltip (actions,clicktoclose)
                .onAction (ev ->
                {
                    ev.consume ();
                    _this.close ();

                })
                .build ();

            controls.add (close);

        }

        if (b.hideOnEscape)
        {

            // TODO
            //this.setHideOnEscape (true);

        }

        this.onClose = b.onClose;
        // TODO
        //this.setOnCloseRequest (ev -> this._close ());

        VBox vb = new VBox ();

        UIUtils.addStyleSheet (this,
                               Constants.POPUP_STYLESHEET_TYPE,
                               StyleClassNames.QPOPUP);

        this.getStyleClass ().add (StyleClassNames.QPOPUP);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.styleSheets != null)
        {

            List<String> sss = new ArrayList<> ();

            sss.addAll (b.styleSheets);

            sss.stream ()
                .forEach (s ->
                {

                    if (s == null)
                    {

                        return;

                    }

                    UIUtils.addStyleSheet (this,
                                           Constants.POPUP_STYLESHEET_TYPE,
                                           s);

                });

        }

        VBox.setVgrow (b.content, Priority.ALWAYS);

        this.content = b.content;
        this.headerProp = new SimpleObjectProperty<> ();

        if (b.title != null)
        {

            Header h = Header.builder ()
                .title (b.title)
                .styleClassName (b.styleName)
                .iconClassName (b.headerIconStyleName)
                .controls (controls)
                .build ();
            h.managedProperty ().bind (h.visibleProperty ());
            h.getStyleClass ().add (StyleClassNames.TITLE);
            this.headerProp.setValue (h);
            vb.getChildren ().add (h);

            h.setOnMousePressed(ev ->
            {
                // record a delta distance for the drag and drop operation.
                _this.dx = ev.getSceneX();
                _this.dy = ev.getSceneY();
                _this.mx = _this.getLayoutX ();
                _this.my = _this.getLayoutY ();
                h.setCursor (Cursor.MOVE);

            });
            h.setOnMouseReleased(ev -> h.setCursor (Cursor.DEFAULT));
            h.setOnMouseDragged(ev ->
            {

                ev.consume ();

                double diffx = ev.getSceneX () - _this.dx;
                double diffy = ev.getSceneY () - _this.dy;

                _this.mx = _this.mx + diffx;
                _this.my = _this.my + diffy;

                //Point2D p = _this.localToScene (ev.getX (), ev.getY ());

                //Bounds _b = vb.localToScene (vb.getBoundsInLocal ());
    /*
                if ((diffx < 1)
                    &&
                    ((_b.getMinX () <= 0)
                     ||
                     (_b.getMinY () <= 0)
                    )
                   )
                {

                    _this.mx = _this.getLayoutX ();
                    _this.my = _this.getLayoutY ();

                    _this.dx = ev.getSceneX();
                    _this.dy = ev.getSceneY();

                    _this.relocate (0, _this.my);

                    return;

                }

                if ((diffx > 0)
                    &&
                    (_b.getWidth () + _b.getMinX () >= _this.getScene ().getWidth ())
                   )
                {

                    _this.dx = ev.getSceneX ();
                    _this.dy = ev.getSceneY ();

                    return;

                }
    */

                _this.moving = true;

                Bounds _b = _this.getParent ().getLayoutBounds ();

                if (_this.mx < (_b.getMinX () - _this.getWidth () + 20))
                {

                    _this.mx = _b.getMinX () - _this.getWidth () + 20;

                }

                if (_this.my < (_b.getMinY () - _this.getHeight () + 20))
                {

                    _this.my = _b.getMinY () - _this.getHeight () + 20;

                }

                if (_this.mx > (_b.getMaxX () - 20))
                {

                    _this.mx = _b.getMaxX () - 20;

                }

                if (_this.my < 0)
                {

                    _this.my = 0;

                }

                _this.relocate (_this.mx, _this.my);

                _this.moving = false;

                _this.dx = ev.getSceneX ();
                _this.dy = ev.getSceneY ();

            });
            h.setOnMouseEntered(ev ->
            {
                if (!ev.isPrimaryButtonDown ())
                {

                    h.setCursor (Cursor.MOVE);

                }
            });
            h.setOnMouseExited(ev ->
            {
                if (!ev.isPrimaryButtonDown())
                {
                    h.setCursor(Cursor.DEFAULT);
                }
            });

        }

        vb.getChildren ().addAll (b.content);

        b.content.getStyleClass ().add (StyleClassNames.CONTENT);

        this.getChildren ().add (vb);

        this.setOnMousePressed (ev ->
        {

            _this.toFront ();

        });

        UIUtils.doOnKeyReleased (this,
                                 KeyCode.F4,
                                 () ->
                                 {

                                     this.close ();

                                 });

        if (b.show)
        {

            if (b.showAt != null)
            {

                this.show (b.showAt,
                           b.showWhere != null ? b.showWhere : Side.BOTTOM);

                return;

            }

            this.show ();

        }

    }

    public Region getContent ()
    {

        return (Region) this.content;

    }

    public void removeStyleSheet (String ss)
    {

        UIUtils.removeStyleSheet (this,
                                  Constants.POPUP_STYLESHEET_TYPE,
                                  ss);

    }

    public void addStyleSheet (String s)
    {

        UIUtils.addStyleSheet (this,
                               Constants.POPUP_STYLESHEET_TYPE,
                               s);

    }

    @Override
    public IPropertyBinder getBinder ()
    {

        return this.binder;

    }

    public void setTitle (StringProperty t)
    {

        this.getHeader ().setTitle (t);

    }

    public void addChildPopup (QuollPopup qp)
    {

        this.childPopups.add (qp);

    }

    public Header getHeader ()
    {

        return this.headerProp.getValue ();

    }

    public ObjectProperty<Header> headerProperty ()
    {

        return this.headerProp;

    }

    /**
     * Get a builder to create a new QuollPopup.
     *
     * Usage: QuollPopup.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollPopup.Builder builder ()
    {

        return new Builder ();

    }

    public static QuestionBuilder questionBuilder ()
    {

        return new QuestionBuilder ();

    }

    public static YesConfirmTextEntryBuilder yesConfirmTextEntryBuilder ()
    {

        return new YesConfirmTextEntryBuilder ();

    }

    public static PasswordEntryBuilder passwordEntryBuilder ()
    {

        return new PasswordEntryBuilder ();

    }

    public static TextEntryBuilder textEntryBuilder ()
    {

        return new TextEntryBuilder ();

    }

    public static <E extends FormBuilder<E>> FormBuilder<E> formBuilder ()
    {

        return new FormBuilder<E> ();

    }

    public static <E extends MessageBuilder<E>> MessageBuilder<E> messageBuilder ()
    {

        return new MessageBuilder<E> ();

    }

    public static <E extends NotificationBuilder<E>> NotificationBuilder<E> notificationBuilder ()
    {


        return new NotificationBuilder<E> ();

    }

    public static <E extends ObjectSelectBuilder<E>> ObjectSelectBuilder<E> objectSelectBuilder ()
    {

        return new ObjectSelectBuilder<E> ();

    }

    public static ErrorBuilder errorBuilder ()
    {

        return new ErrorBuilder ();

    }

    private void _close ()
    {

        this.childPopups.stream ()
            .forEach (p -> p.close ());

        if (this.onClose != null)
        {

            UIUtils.runLater (this.onClose);

        }

    }

    public void close ()
    {

        this.setVisible (false);

        this._close ();

        if (this.removeOnClose)
        {

            if (this.viewer != null)
            {

                this.viewer.removePopup (this);

            }

        }

        this.binder.dispose ();

        this.fireEvent (new PopupEvent (this,
                                        PopupEvent.CLOSED_EVENT));

    }

    public void setOnClose (Runnable r)
    {

        this.onClose = r;

    }

    public void show (Node showAt,
                      Side showWhere)
    {

        if (this.viewer == null)
        {

            throw new IllegalStateException ("Need a viewer to be able to show popup at node: " + showAt);

        }

        this.viewer.showPopup (this,
                               showAt,
                               showWhere);

       this.fireEvent (new PopupEvent (this,
                                       PopupEvent.SHOWN_EVENT));

    }

    public void show ()
    {

        this.show (-1, -1);

    }

    public void show (double x,
                      double y)
    {

        if (this.viewer == null)
        {

            AbstractViewer viewer = Environment.getFocusedViewer ();

            if (viewer != null)
            {

                viewer.showPopup (this,
                                  x,
                                  y);

                return;

            }

            BasicPopupsViewer p = new BasicPopupsViewer (this);

            try
            {

                Viewer v = p.createViewer ();
                p.init (null);

                // Show in the center of the screen.
                v.onShowingProperty ().addListener ((pr, oldv, newv) ->
                {

                    Rectangle2D b = Screen.getPrimary().getVisualBounds();
                    v.setX((b.getWidth() - p.getWidth()) / 2);
                    v.setY((b.getHeight() - p.getHeight()) / 2);

                });

            } catch (Exception e) {

                Environment.logError ("Unable to show popup: " + this,
                                      e);

            }

            this.fireEvent (new PopupEvent (this,
                                            PopupEvent.SHOWN_EVENT));

            return;

        }

        this.viewer.showPopup (this,
                               x,
                               y);

    }

    public void setPopupId (String id)
    {

        this.popupId = id;

    }

    public String getPopupId ()
    {

        return this.popupId;

    }

    public static class YesConfirmTextEntryBuilder extends TextEntryBuilder
    {

        @Override
        public QuollPopup build ()
        {

            this.validator (v ->
            {

                if ((v == null)
                    ||
                    (!v.trim ().equalsIgnoreCase (getUILanguageStringProperty (form,affirmativevalue).getValue ()))
                   )
                {

                    return getUILanguageStringProperty (form,errors,affirmativevalue);
                    //"Please enter the word Yes below.";

                }

                return null;

            });

            return super.build ();

        }

    }

    public static class TextEntryBuilder extends FormBuilder<TextEntryBuilder>
    {

        private Set<Form.Item> items = null;
        private ValueValidator<String> validator = null;
        private StringProperty description = null;
        private EventHandler<Form.FormEvent> onCancel = null;
        private EventHandler<Form.FormEvent> onConfirm = null;
        private StringProperty confirmButtonLabel = null;
        private StringProperty cancelButtonLabel = null;
        private StringProperty entryLabel = null;
        private String text = null;

        public TextEntryBuilder text (String t)
        {

            this.text = t;
            return _this ();

        }

        public TextEntryBuilder item (Form.Item i)
        {

            if (this.items == null)
            {

                this.items = new LinkedHashSet<> ();

            }

            this.items.add (i);
            return _this ();

        }

        public TextEntryBuilder entryLabel (StringProperty prop)
        {

            this.entryLabel = prop;
            return _this ();

        }

        public TextEntryBuilder entryLabel (List<String> prefix,
                                            String...    ids)
        {

            return this.entryLabel (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public TextEntryBuilder entryLabel (String... ids)
        {

            return this.entryLabel (getUILanguageStringProperty (ids));

        }

        public TextEntryBuilder confirmButtonLabel (StringProperty prop)
        {

            this.confirmButtonLabel = prop;
            return _this ();

        }

        public TextEntryBuilder confirmButtonLabel (List<String> prefix,
                                                    String...    ids)
        {

            return this.confirmButtonLabel (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public TextEntryBuilder confirmButtonLabel (String... ids)
        {

            return this.confirmButtonLabel (getUILanguageStringProperty (ids));

        }

        public TextEntryBuilder cancelButtonLabel (StringProperty prop)
        {

            this.cancelButtonLabel = prop;
            return _this ();

        }

        public TextEntryBuilder cancelButtonLabel (List<String> prefix,
                                                   String...    ids)
        {

            return this.cancelButtonLabel (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public TextEntryBuilder cancelButtonLabel (String... ids)
        {

            return this.cancelButtonLabel (getUILanguageStringProperty (ids));

        }

        public TextEntryBuilder onConfirm (EventHandler<Form.FormEvent> c)
        {

            this.onConfirm = c;
            return this;

        }

        public TextEntryBuilder onCancel (EventHandler<Form.FormEvent> c)
        {

            this.onCancel = c;
            return this;

        }

        public TextEntryBuilder description (String... m)
        {

            return this.description (getUILanguageStringProperty (m));

        }

        public TextEntryBuilder description (List<String> prefix,
                                             String...    m)
        {

            return this.description (getUILanguageStringProperty (Utils.newList (prefix, m)));

        }

        public TextEntryBuilder description (StringProperty m)
        {

            this.description = m;
            return _this ();

        }

        public TextEntryBuilder validator (ValueValidator<String> v)
        {

            this.validator = v;
            return _this ();

        }

        @Override
        public TextEntryBuilder _this ()
        {

            return this;

        }

        @Override
        public QuollPopup build ()
        {

            if (this.items == null)
            {

                this.items = new LinkedHashSet<> ();

            }

            QuollTextField tf = QuollTextField.builder ()
                .text (this.text)
                .build ();

            if (this.entryLabel == null)
            {

                this.items.add (new Form.Item (tf));

            } else {

                this.items.add (new Form.Item (this.entryLabel,
                                               tf));

            }
            // TODO Make a constant.
            tf.setId ("text");

            if (this.text != null)
            {

                tf.selectAll ();

            }

            if (this.styleName == null)
            {

                this.styleName = StyleClassNames.QUESTION;

            }

            if (this.validator == null)
            {

                throw new IllegalStateException ("A validator for the text entry must be provided.");

            }

            Form.Builder fb = Form.builder ()
                .styleClassName (StyleClassNames.TEXTENTRY)
                .description (this.description)
                .inViewer (this.viewer)
                .items (this.items)
                .confirmButton ((this.confirmButtonLabel != null ? this.confirmButtonLabel : getUILanguageStringProperty (buttons,confirm)))
                .cancelButton ((this.cancelButtonLabel != null ? this.cancelButtonLabel : getUILanguageStringProperty (buttons,cancel)));

            Form f = fb.build ();

            this.form (f);

            QuollPopup qp = super.build ();

            Runnable r = () ->
            {

                f.hideError ();

                StringProperty m = validator.isValid (tf.getText ());

                if (m != null)
                {

                    f.showError (m);
                    return;

                }

                Form.FormEvent ev = new Form.FormEvent (f,
                                                        Form.FormEvent.CONFIRM_EVENT);

                this.onConfirm.handle (ev);

                if (ev.isConsumed ())
                {

                    return;

                }

                qp.close ();

            };

            f.setOnCancel (this.onCancel);

            f.setOnConfirm (ev ->
            {

                UIUtils.runLater (r);

            });

            qp.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                                ev ->
            {

                UIUtils.forceRunLater (() ->
                {

                    tf.requestFocus ();

                });

            });

            return qp;

        }

    }

    public static class PasswordEntryBuilder extends FormBuilder<PasswordEntryBuilder>
    {

        private ValueValidator<String> validator = null;
        private StringProperty description = null;
        private EventHandler<Form.FormEvent> onCancel = null;
        private EventHandler<Form.FormEvent> onConfirm = null;
        private StringProperty confirmButtonLabel = null;
        private StringProperty cancelButtonLabel = null;
        private StringProperty entryLabel = null;

        public PasswordEntryBuilder entryLabel (StringProperty prop)
        {

            this.entryLabel = prop;
            return _this ();

        }

        public PasswordEntryBuilder entryLabel (List<String> prefix,
                                                String...    ids)
        {

            return this.entryLabel (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public PasswordEntryBuilder entryLabel (String... ids)
        {

            return this.entryLabel (getUILanguageStringProperty (ids));

        }

        public PasswordEntryBuilder confirmButtonLabel (StringProperty prop)
        {

            this.confirmButtonLabel = prop;
            return _this ();

        }

        public PasswordEntryBuilder confirmButtonLabel (List<String> prefix,
                                                        String...    ids)
        {

            return this.confirmButtonLabel (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public PasswordEntryBuilder confirmButtonLabel (String... ids)
        {

            return this.confirmButtonLabel (getUILanguageStringProperty (ids));

        }

        public PasswordEntryBuilder cancelButtonLabel (StringProperty prop)
        {

            this.cancelButtonLabel = prop;
            return _this ();

        }

        public PasswordEntryBuilder cancelButtonLabel (List<String> prefix,
                                                       String...    ids)
        {

            return this.cancelButtonLabel (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public PasswordEntryBuilder cancelButtonLabel (String... ids)
        {

            return this.cancelButtonLabel (getUILanguageStringProperty (ids));

        }

        public PasswordEntryBuilder onConfirm (EventHandler<Form.FormEvent> c)
        {

            this.onConfirm = c;
            return this;

        }

        public PasswordEntryBuilder onCancel (EventHandler<Form.FormEvent> c)
        {

            this.onCancel = c;
            return this;

        }

        public PasswordEntryBuilder description (String... m)
        {

            return this.description (getUILanguageStringProperty (m));

        }

        public PasswordEntryBuilder description (List<String> prefix,
                                                 String...    m)
        {

            return this.description (getUILanguageStringProperty (Utils.newList (prefix, m)));

        }

        public PasswordEntryBuilder description (StringProperty m)
        {

            this.description = m;
            return _this ();

        }

        public PasswordEntryBuilder validator (ValueValidator<String> v)
        {

            this.validator = v;
            return _this ();

        }

        @Override
        public PasswordEntryBuilder _this ()
        {

            return this;

        }

        @Override
        public QuollPopup build ()
        {

            PasswordField tf = new PasswordField ();
            // TODO Make a constant.
            tf.setId ("password");

            if (this.styleName == null)
            {

                this.styleName = StyleClassNames.QUESTION;

            }

            if (this.validator == null)
            {

                throw new IllegalStateException ("A validator for the text entry must be provided.");

            }

            Form f = Form.builder ()
                .styleClassName (StyleClassNames.PASSWORDENTRY)
                .description (this.description)
                //.withHandler (this.viewer)
                .item (this.entryLabel,
                       tf)
                .confirmButton ((this.confirmButtonLabel != null ? this.confirmButtonLabel : getUILanguageStringProperty (buttons,confirm)))
                .cancelButton ((this.cancelButtonLabel != null ? this.cancelButtonLabel : getUILanguageStringProperty (buttons,cancel)))
                .build ();

            this.form (f);

            QuollPopup qp = super.build ();

            Runnable r = () ->
            {

                f.hideError ();

                StringProperty m = validator.isValid (tf.getText ());

                if (m != null)
                {

                    f.showError (m);
                    return;

                }

                Form.FormEvent ev = new Form.FormEvent (f,
                                                        Form.FormEvent.CONFIRM_EVENT);

                this.onConfirm.handle (ev);

                if (ev.isConsumed ())
                {

                    return;

                }

                qp.close ();

            };

            f.setOnCancel (this.onCancel);

            f.setOnConfirm (ev ->
            {

                UIUtils.runLater (r);

            });

            qp.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                                ev ->
            {

                UIUtils.forceRunLater (() ->
                {

                    tf.requestFocus ();

                });

            });

            return qp;

        }

    }

    public static class FormBuilder<X extends FormBuilder<X>> extends Builder<X>
    {

        private Form form = null;

        @Override
        public X _this ()
        {

            // TODO Dodgy cast, fix?
            return (X) this;

        }

        public X form (Form f)
        {

            this.form = f;
            return _this ();

        }

        public QuollPopup build ()
        {

            this.withClose (true);
            this.hideOnEscape (true);
            this.removeOnClose (true);
            this.show ();

            if (this.styleName == null)
            {

                this.styleClassName (StyleClassNames.QUESTION);

            }

            if (this.form == null)
            {

                throw new IllegalArgumentException ("Form must be provided.");

            }

            this.content (this.form);

            QuollPopup qp = super.build ();

            Button cancel = this.form.getCancelButton ();

            if (cancel != null)
            {

                cancel.addEventHandler (ActionEvent.ACTION,
                                        ev -> qp.close ());

            }

            return qp;

        }

    }

    public static class ObjectSelectBuilder<X extends ObjectSelectBuilder<X>> extends Builder<X>
    {

        private StringProperty message = null;
        private Consumer<NamedObject> onClick = null;
        private Set<? extends NamedObject> objs = null;
        private boolean showFinishButton = false;

        public X objects (Set<? extends NamedObject> objs)
        {

            this.objs = objs;
            return _this ();

        }

        public X onClick (Consumer<NamedObject> onClick)
        {

            this.onClick = onClick;
            return _this ();

        }

        public X message (StringProperty message)
        {

            this.message = message;
            return _this ();

        }

        @Override
        public QuollPopup build ()
        {

            this.withClose (true);
            this.hideOnEscape (true);
            this.removeOnClose (true);
            this.show ();

            VBox b = new VBox ();

            if (this.message != null)
            {

                b.getChildren ().add (QuollTextView.builder ()
                    // TODO .text (this.message)
                    .styleClassName (StyleClassNames.MESSAGE)
                    // TODO .withViewer (this.viewer)
                    .build ());

            }

            if (this.title == null)
            {

                this.title (selectitem,popup,LanguageStrings.title);

            }

            if (this.styleName == null)
            {

                this.styleClassName (StyleClassNames.OBJECTSELECT);

            }

            this.content (b);

            QuollPopup qp = new QuollPopup (this);

            for (NamedObject n : this.objs)
            {

                // TODO Add the object image.

                QuollLabel2 l = QuollLabel2.builder ()
                    .build ();

                l.setOnMouseClicked (ev ->
                {

                    if (ev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    this.onClick.accept (n);

                });
/*
TODO
                qp.getBinder ().addChangeListener (l.labelProperty (),
                                                   (pr, oldv, newv) ->
                {

                    l.setLabel (newv);

                });
*/
                if (n instanceof Asset)
                {

                    Asset a = (Asset) n;

                    qp.getBinder ().addChangeListener (a.getUserConfigurableObjectType ().icon24x24Property (),
                                                       (pr, oldv, newv) ->
                    {

                        // TODO l.getImage ().setImage (a.getUserConfigurableObjectType ().getIcon24x24 ());

                    });

                } else {

                    l.getStyleClass ().add (n.getObjectType ());

                }

                b.getChildren ().add (l);

            }

            UIUtils.runLater (() ->
            {

                qp.toFront ();

            });

            return qp;

        }

        @Override
        public X _this ()
        {

            // TODO Dodgy cast, fix?
            return (X) this;

        }

    }

    /**
     * Note will automatically show the popup, switch off with "noAutoShow".
     */
    public static class MessageBuilder<X extends MessageBuilder<X>> extends Builder<X>
    {

        protected StringProperty message = null;
        private Node messageNode = null;
        protected Set<Button> buttons = null;
        private Button closeButton = null;
        private boolean noAutoShow = false;
        //private AbstractViewer viewer = null;

        public X message (Node n)
        {

            this.messageNode = n;
            return _this ();

        }

        @Override
        public X _this ()
        {

            // TODO Dodgy cast, fix?
            return (X) this;

        }
/*
        public X withViewer (AbstractViewer h)
        {

            this.viewer = h;
            return _this ();

        }
*/
        public X message (String... m)
        {

            return this.message (getUILanguageStringProperty (m));

        }

        public X message (List<String> prefix,
                          String...    m)
        {

            return this.message (getUILanguageStringProperty (Utils.newList (prefix, m)));

        }

        public X noAutoShow ()
        {

            this.noAutoShow = true;
            return _this ();

        }

        public X message (StringProperty m)
        {

            this.message = m;
            return _this ();

        }

        public X closeButton (StringProperty label,
                              Runnable       onClose)
        {

            this.closeButton = QuollButton.builder ()
                .label (label != null ? label : getUILanguageStringProperty (LanguageStrings.buttons,close))
                .onAction (ev ->
                {

                    UIUtils.runLater (onClose);

                })
                .build ();

            return this.button (this.closeButton);

        }

        public X closeButton (Runnable onClose)
        {

            return this.closeButton (null,
                                     onClose);

        }

        public X closeButton ()
        {

            return this.closeButton (null,
                                     null);

        }

        public X button (Button bs)
        {

            if (this.buttons == null)
            {

                this.buttons = new LinkedHashSet<> ();

            }

            this.buttons.add (bs);
            return _this ();

        }

        public X buttons (Button... bs)
        {

            if (this.buttons != null)
            {

                throw new IllegalStateException ("Buttons is already defined.");

            }

            this.buttons = new LinkedHashSet<> ();
            this.buttons.addAll (Arrays.asList (bs));
            return _this ();

        }

        public X buttons (Set<Button> bs)
        {

            if (this.buttons != null)
            {

                throw new IllegalStateException ("Buttons is already defined.");

            }

            this.buttons = bs;
            return _this ();

        }

        @Override
        public QuollPopup build ()
        {

            this.withClose (true);
            this.hideOnEscape (true);
            this.removeOnClose (true);

            if (!this.noAutoShow)
            {

                this.show ();

            }

            VBox b = new VBox ();

            Node content = null;

            if (this.messageNode == null)
            {

                if (this.message == null)
                {

                    throw new IllegalArgumentException ("Message must be provided.");

                }

                content = QuollTextView.builder ()//BasicHtmlTextFlow.builder ()
                    .text (this.message)
                    .styleClassName (StyleClassNames.MESSAGE)
                    .inViewer (this.viewer)
                    .build ();

            } else {

                content = this.messageNode;

            }

            if (content == null)
            {

                throw new IllegalArgumentException ("No message or messageNode provided.");

            }

            b.getChildren ().add (content);

            if (this.title == null)
            {

                this.title (generalmessage,LanguageStrings.title);

            }

            if (this.styleName == null)
            {

                this.styleClassName (StyleClassNames.MESSAGE);
                this.headerIconClassName (StyleClassNames.INFORMATION);

            }

            if (this.buttons != null)
            {

                Node bb = QuollButtonBar.builder ()
                    .buttons (this.buttons)
                    .build ();

                b.getChildren ().add (bb);

            }

            this.content (b);

            QuollPopup qp = new QuollPopup (this);

            UIUtils.forceRunLater (() ->
            {

                qp.toFront ();

            });

            if (this.closeButton != null)
            {

                this.closeButton.addEventHandler (ActionEvent.ANY,
                                                  ev ->
                {

                    qp.close ();

                });

            }

            return qp;

        }

    }

    public static class ErrorBuilder extends MessageBuilder<ErrorBuilder>
    {

        private EventHandler<ActionEvent> onConfirm = null;

        @Override
        public QuollPopup build ()
        {

            Button b = QuollButton.builder ()
                .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
                .label (getUILanguageStringProperty (LanguageStrings.buttons,LanguageStrings.close))
                .build ();

            Set<Button> buttons = new LinkedHashSet<> ();
            buttons.add (b);
            this.buttons (buttons);
            this.message (getUILanguageStringProperty (Arrays.asList (errormessage,text),
                                                //"%s<br /><br /><a href='%s:%s'>Click here to contact Quoll Writer support about this problem.</a>",
                                                this.message,
                                                Constants.ACTION_PROTOCOL,
                                                AbstractViewer.CommandId.reportbug));

            if (this.title == null)
            {

                this.title (errormessage,LanguageStrings.title);

            }

            if (this.styleName == null)
            {

                this.styleClassName (StyleClassNames.ERROR);

            }

            this.hideOnEscape (true);
            this.removeOnClose (true);
            this.withClose (true);
            this.headerIconClassName (StyleClassNames.ERROR);
            this.show ();

            QuollPopup qp = super.build ();

            b.setOnAction (ev ->
            {

                qp.close ();

            });

            return qp;

        }

    }

    public static class QuestionBuilder extends MessageBuilder<QuestionBuilder>
    {

        private EventHandler<ActionEvent> onConfirm = null;
        private EventHandler<ActionEvent> onCancel = null;
        private StringProperty confirmButtonLabel = null;
        private StringProperty cancelButtonLabel = null;

        protected QuestionBuilder ()
        {

        }

        @Override
        public QuestionBuilder _this ()
        {

            return this;

        }

        public QuestionBuilder confirmButtonLabel (StringProperty prop)
        {

            this.confirmButtonLabel = prop;
            return _this ();

        }

        public QuestionBuilder confirmButtonLabel (List<String> prefix,
                                                   String...    ids)
        {

            return this.confirmButtonLabel (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public QuestionBuilder confirmButtonLabel (String... ids)
        {

            return this.confirmButtonLabel (getUILanguageStringProperty (ids));

        }

        public QuestionBuilder cancelButtonLabel (StringProperty prop)
        {

            this.cancelButtonLabel = prop;
            return _this ();

        }

        public QuestionBuilder cancelButtonLabel (List<String> prefix,
                                                  String...    ids)
        {

            return this.cancelButtonLabel (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public QuestionBuilder cancelButtonLabel (String... ids)
        {

            return this.cancelButtonLabel (getUILanguageStringProperty (ids));

        }

        public QuestionBuilder onConfirm (EventHandler<ActionEvent> c)
        {

            this.onConfirm = c;
            return this;

        }

        public QuestionBuilder onCancel (EventHandler<ActionEvent> c)
        {

            this.onCancel = c;
            return this;

        }

        @Override
        public QuollPopup build ()
        {

            Set<Button> buts = new LinkedHashSet<> ();

            Button confirm = null;

            if (this.buttons != null)
            {

                buts.addAll (this.buttons);

            } else {

                if (this.onConfirm == null)
                {

                    throw new IllegalArgumentException ("If no buttons are specified then onConfirm must be specified.");

                }

                confirm = QuollButton.builder ()
                    .label ((this.confirmButtonLabel != null ? this.confirmButtonLabel : getUILanguageStringProperty (LanguageStrings.buttons,LanguageStrings.confirm)))
                    .buttonType (ButtonBar.ButtonData.OK_DONE)
                    .iconName (StyleClassNames.CONFIRM)
                    .build ();

                this.button (confirm);

                //buts.add (confirm);

            }

            Button cancel = QuollButton.builder ()
                .label ((this.cancelButtonLabel != null ? this.cancelButtonLabel : getUILanguageStringProperty (LanguageStrings.buttons,LanguageStrings.cancel)))
                .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
                .iconName (StyleClassNames.CANCEL)
                .build ();

            this.button (cancel);

            if (this.styleName == null)
            {

                this.styleClassName (StyleClassNames.QUESTION);

            }

            QuollPopup qp = super.build ();
/*
            cancel.addEventHandler (ActionEvent.ACTION,
                                    ev -> qp.close ());
*/

            if (confirm != null)
            {

                confirm.setOnAction (ev ->
                {

                    if (this.onConfirm != null)
                    {

                        this.onConfirm.handle (ev);

                    }

                    if (!ev.isConsumed ())
                    {

                        qp.close ();

                    }

                });

            }

            cancel.setOnAction (ev ->
            {

                if (this.onCancel != null)
                {

                    this.onCancel.handle (ev);

                }

                if (!ev.isConsumed ())
                {

                    qp.close ();

                }

            });

            return qp;

        }

    }

    public static class NotificationBuilder<X extends NotificationBuilder<X>> extends Builder<X>
    {

        private StringProperty message = null;
        private int hideIn = -1;

        protected NotificationBuilder ()
        {

        }

        @Override
        public X _this ()
        {

            // TODO Dodgy cast, fix?
            return (X) this;

        }

        public X hideIn (int v)
        {

            this.hideIn = v;
            return _this ();

        }

        public X message (String... m)
        {

            return this.message (getUILanguageStringProperty (m));

        }

        public X message (List<String> prefix,
                          String...    m)
        {

            return this.message (getUILanguageStringProperty (Utils.newList (prefix, m)));

        }

        public X message (StringProperty m)
        {

            this.message = m;
            return _this ();

        }

        @Override
        public QuollPopup build ()
        {

            if (this.styleName == null)
            {

                this.styleClassName (StyleClassNames.NOTIFICATION);

            }

            if (this.message == null)
            {

                throw new IllegalArgumentException ("Message must be provided.");

            }

            Node content = QuollTextView.builder ()//BasicHtmlTextFlow.builder ()
                .text (this.message)
                .styleClassName (StyleClassNames.MESSAGE)
                .inViewer (this.viewer)
                .build ();

            this.removeOnClose (true);
            this.content (content);

            QuollPopup qp = super.build ();

            qp.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                                ev ->
            {

                Environment.schedule (() ->
                {

                    UIUtils.runLater (() ->
                    {

                        qp.close ();

                    });

                },
                this.hideIn,
                -1);

            });

            return qp;

        }

    }

    public static class Builder<X extends Builder<X>> implements IBuilder<X, QuollPopup>
    {

        protected StringProperty title = null;
        protected String styleName = null;
        private Set<Node> controls = null;
        private Node content = null;
        private boolean withClose = false;
        private boolean hideOnEscape = false;
        private Runnable onClose = null;
        private String popupId = null;
        protected AbstractViewer viewer = null;
        private boolean show = false;
        private boolean removeOnClose = false;
        private Node showAt = null;
        private Side showWhere = null;
        private List<String> styleSheets = new ArrayList<> ();
        private String headerIconStyleName = null;

        protected Builder ()
        {

        }

        @Override
        public QuollPopup build ()
        {

            return new QuollPopup (this);

        }

        @Override
        public X _this ()
        {

            return (X) this;

        }

        public X headerIconClassName (String s)
        {

            this.headerIconStyleName = s;
            return _this ();

        }

        public X styleSheets (List<String> ss)
        {

            if (ss != null)
            {

                this.styleSheets.addAll (ss);

            }

            return _this ();

        }

        public X styleSheet (String... s)
        {

            if (s != null)
            {

                for (String ss : s)
                {

                    this.styleSheets.add (ss);

                }

            }

            return _this ();

        }

        public X showAt (Node n,
                         Side where)
        {

            this.showAt = n;
            this.showWhere = where;
            return _this ();

        }

        public X show ()
        {

            this.show = true;
            return _this ();

        }

        public X inViewer (AbstractViewer viewer)
        {

            return this.withViewer (viewer);

        }

        public X withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return _this ();

        }

        public X popupId (String id)
        {

            this.popupId = id;
            return _this ();

        }

        public X removeOnClose (Boolean b)
        {

            this.removeOnClose = b;
            return _this ();

        }

        public X onClose (Runnable r)
        {

            this.onClose = r;
            return _this ();

        }

        public X hideOnEscape (boolean v)
        {

            this.hideOnEscape = v;
            return _this ();

        }

        public X withClose (boolean v)
        {

            this.withClose = v;
            return _this ();

        }

        public X content (Node c)
        {

            this.content = c;
            return _this ();

        }

        public X controls (Set<Node> c)
        {

            this.controls = c;

            return _this ();

        }

        public X styleClassName (String n)
        {

            this.styleName = n;

            return _this ();

        }

        public X title (StringProperty prop)
        {

            this.title = prop;
            return _this ();

        }

        public X title (List<String> prefix,
                                 String...    ids)
        {

            return this.title (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public X title (String... ids)
        {

            return this.title (getUILanguageStringProperty (ids));

        }

    }

    public static class PopupEvent extends Event
    {

        public static final EventType<PopupEvent> CLOSED_EVENT = new EventType<> ("popup.closed");
        public static final EventType<PopupEvent> SHOWN_EVENT = new EventType<> ("popup.shown");

        private QuollPopup popup = null;

        public PopupEvent (QuollPopup            popup,
                           EventType<PopupEvent> type)
        {

            super (type);

            this.popup = popup;

        }

        public QuollPopup getPopup ()
        {

            return this.popup;

        }

    }

}
