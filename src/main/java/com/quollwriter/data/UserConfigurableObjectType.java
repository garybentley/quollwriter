package com.quollwriter.data;

import java.util.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.scene.image.*;

import org.dom4j.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class UserConfigurableObjectType extends NamedObject
{

    public static final String ICON16 = "icon16";
    public static final String ICON24 = "icon24";
    public static final String OBJECT_TYPE_NAME_PLURAL = "objectTypeNamePlural";
    public static final String OBJECT_TYPE_NAME = "objectTypeName";
    public static final String OBJECT_TYPE = "userconfigobjtype";

    private StringProperty objectTypeNamePluralProp = null;
    //private Image icon24x24 = null;
    //private Image icon16x16 = null;
    private ObjectProperty<Image> icon16x16Prop = null;
    private ObjectProperty<Image> icon24x24Prop = null;
    private String layout = null;
    private String userObjectType = null;
    private boolean isAsset = false;
    private javax.swing.KeyStroke createShortcutKeyStroke = null;
    private boolean pluralNameSet = false;
    private boolean singularNameSet = false;
    private ObservableList<FieldsColumn> sortableFieldsColumns = null;
    private ObjectNameUserConfigurableObjectTypeField primaryNameField = null;
    private ListChangeListener<UserConfigurableObjectTypeField> fieldsListener = null;
    private boolean ignoreFieldsStateChanges = false;
    private Project project = null;

    public UserConfigurableObjectType (Project forProject)
    {

        super (OBJECT_TYPE);

        //this.fields = FXCollections.observableSet (new LinkedHashSet<> ());
        this.objectTypeNamePluralProp = new SimpleStringProperty ();
        this.icon16x16Prop = new SimpleObjectProperty<> ();
        this.icon24x24Prop = new SimpleObjectProperty<> ();
        this.sortableFieldsColumns = FXCollections.observableList (new ArrayList<> ());
        this.project = forProject;

        this.fieldsListener = ch ->
        {

            while (ch.next ())
            {

                this.updateSortableFieldsState ();

            }

        };

        this.sortableFieldsColumns.addListener ((ListChangeListener<FieldsColumn>) ch ->
        {

            while (ch.next ())
            {

                if (ch.wasRemoved ())
                {

                    this.updateSortableFieldsState ();

                    for (FieldsColumn rem : ch.getRemoved ())
                    {

                        rem.fields ().removeListener (this.fieldsListener);

                    }

                }

                if (ch.wasAdded ())
                {

                    this.updateSortableFieldsState ();

                    for (FieldsColumn add : ch.getAddedSubList ())
                    {

                        add.fields ().addListener (this.fieldsListener);
                        add.showFieldLabelsProperty ().addListener ((pr, oldv, newv) ->
                        {

                            this.updateSortableFieldsState ();

                        });

                        add.titleProperty ().addListener ((pr, oldv, newv) ->
                        {

                            this.updateSortableFieldsState ();

                        });

                    }

                }

            }

        });

    }

    public void setIgnoreFieldsState (boolean v)
    {

        this.ignoreFieldsStateChanges = v;

    }

    public void updateSortableFieldsState ()
    {

        if (this.ignoreFieldsStateChanges)
        {

            return;

        }

        try
        {

            this.setProperty (Constants.USER_CONFIGURABLE_OBJECT_TYPE_SORTABLE_FIELDS_LAYOUT_PROPERTY_NAME,
                              this.getSortableFieldsState ().asString ());

        } catch (Exception e) {

            Environment.logError ("Unable to set fields layout to: " +
                                  this.getSortableFieldsState (),
                                  e);

        }

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "objectTypeId",
                                    this.getObjectTypeId ());
        this.addToStringProperties (props,
                                    "objectTypeName",
                                    this.getName ());
        this.addToStringProperties (props,
                                    "objectTypeNamePlural",
                                    this.getObjectTypeNamePlural ());
        this.addToStringProperties (props,
                                    "isAsset",
                                    this.isAssetObjectType ());
        this.addToStringProperties (props,
                                    "layout",
                                    this.getLayout ());
        this.addToStringProperties (props,
                                    "createShortcutKey",
                                    this.createShortcutKeyStroke);
        this.addToStringProperties (props,
                                    "fields",
                                    this.getConfigurableFields ().size ());

        try
        {

            this.addToStringProperties (props,
                                        "columnState",
                                        this.getSortableFieldsState ().asString ());

        } catch (Exception e) {

            this.addToStringProperties (props,
                                        "columnState",
                                        "Unable to encode state: " + e);

        }

    }

    public FieldsColumn addNewColumn (Collection<UserConfigurableObjectTypeField> fields)
    {

        FieldsColumn fc = new FieldsColumn (fields);

        this.sortableFieldsColumns.add (fc);

        return fc;

    }

    public State getSortableFieldsState ()
    {

        List<Map> cols = new ArrayList<> ();

        for (FieldsColumn c : this.sortableFieldsColumns)
        {

            Map data = new HashMap ();
            data.put ("title",
                      c.titleProperty ().getValue ());
            data.put ("showFieldLabels",
                      c.showFieldLabelsProperty ().getValue ());

            data.put ("fields",
                      c.fields ().stream ()
                        .map (f ->
                        {

                            Map idata = new HashMap ();
                            idata.put ("id",
                                       f.getObjectReference ().asString ());

                            return idata;

                        })
                        .collect (Collectors.toList ()));

            cols.add (data);

        }

        State state = new State ();

        state.set ("columns",
                   cols);

        return state;

    }

    public void setConfigurableFields (List<UserConfigurableObjectTypeField> fields)
                                throws Exception
    {

        this.ignoreFieldsStateChanges = true;

        this.sortableFieldsColumns.stream ()
            .forEach (c -> c.fields ().clear ());

        this.sortableFieldsColumns.clear ();

        for (UserConfigurableObjectTypeField f : fields)
        {

            if (f.getType () == UserConfigurableObjectTypeField.Type.objectname)
            {

                this.primaryNameField = (ObjectNameUserConfigurableObjectTypeField) f;

            }

            f.setUserConfigurableObjectType (this);

        }

        // Set the layout, we store it in the properties since it is "state".
        String lt = this.getProperty (Constants.USER_CONFIGURABLE_OBJECT_TYPE_SORTABLE_FIELDS_LAYOUT_PROPERTY_NAME);

        if (lt == null)
        {

            // Legacy... pre version 3.

            UserConfigurableObjectTypeField name = null;
            UserConfigurableObjectTypeField image = null;
            UserConfigurableObjectTypeField desc = null;

            for (UserConfigurableObjectTypeField f : fields)
            {

                if (f.getType () == UserConfigurableObjectTypeField.Type.objectname)
                {

                    name = f;
                }

                if (f.getType () == UserConfigurableObjectTypeField.Type.objectdesc)
                {

                    desc = f;

                }

                if (f.getType () == UserConfigurableObjectTypeField.Type.objectimage)
                {

                    image = f;

                }

            }

            if (this.layout == null)
            {

                this.layout = Constants.ASSET_LAYOUT_0;

            }

            if (this.layout.equals (Constants.ASSET_LAYOUT_0))
            {

                // Create a single column.
                FieldsColumn col = new FieldsColumn ();
                fields.stream ()
                    .forEach (f ->
                    {

                        if (f == this.primaryNameField)
                        {

                            return;

                        }

                        col.addField (f);

                    });
                this.sortableFieldsColumns.add (col);

            }

            if ((this.layout.equals (Constants.ASSET_LAYOUT_1))
                ||
                (this.layout.equals (Constants.ASSET_LAYOUT_2))
               )
            {

                FieldsColumn left = new FieldsColumn (Arrays.asList (image, desc));
                left.setShowFieldLabels (false);
                FieldsColumn right = new FieldsColumn ();

                // Put everything else into a single column.
                for (UserConfigurableObjectTypeField f : fields)
                {

                    if ((f != name)
                        &&
                        (f != image)
                        &&
                        (f != desc)
                       )
                    {

                        right.addField (f);

                    }

                }

                this.sortableFieldsColumns.add (left);
                this.sortableFieldsColumns.add (right);

            }

            if (this.layout.equals (Constants.ASSET_LAYOUT_3))
            {

                // Create a two column layout.
                FieldsColumn left = new FieldsColumn ();
                FieldsColumn right = new FieldsColumn ();

                int c = 1;

                for (UserConfigurableObjectTypeField f : fields)
                {

                    if (c % 2 == 0)
                    {

                        right.addField (f);

                    } else {

                        left.addField (f);

                    }

                    c++;

                }

                this.sortableFieldsColumns.add (left);
                this.sortableFieldsColumns.add (right);

            }

            if (this.layout.equals (Constants.ASSET_LAYOUT_4))
            {

                FieldsColumn left = new FieldsColumn ();
                FieldsColumn right = new FieldsColumn (Arrays.asList (image, desc));
                right.setShowFieldLabels (false);

                // Put everything else into a single column.
                for (UserConfigurableObjectTypeField f : fields)
                {

                    if ((f != name)
                        &&
                        (f != image)
                        &&
                        (f != desc)
                       )
                    {

                        left.addField (f);

                    }

                }

                this.sortableFieldsColumns.add (left);
                this.sortableFieldsColumns.add (right);

            }

            if (this.layout.equals (Constants.ASSET_LAYOUT_5))
            {

                FieldsColumn left = new FieldsColumn ();
                FieldsColumn right = new FieldsColumn (Arrays.asList (desc));
                right.setShowFieldLabels (false);

                // Put everything else into a single column.
                for (UserConfigurableObjectTypeField f : fields)
                {

                    if ((f != name)
                        &&
                        (f != image)
                        &&
                        (f != desc)
                       )
                    {

                        left.addField (f);

                    }

                }

                this.sortableFieldsColumns.add (left);
                this.sortableFieldsColumns.add (right);

            }

            if (this.layout.equals (Constants.ASSET_LAYOUT_6))
            {

                FieldsColumn left = new FieldsColumn (Arrays.asList (desc));
                left.setShowFieldLabels (false);
                FieldsColumn right = new FieldsColumn ();

                // Put everything else into a single column.
                for (UserConfigurableObjectTypeField f : fields)
                {

                    if ((f != name)
                        &&
                        (f != image)
                        &&
                        (f != desc)
                       )
                    {

                        right.addField (f);

                    }

                }

                this.sortableFieldsColumns.add (left);
                this.sortableFieldsColumns.add (right);

            }

            if (this.layout.equals (Constants.ASSET_LAYOUT_7))
            {

                FieldsColumn left = new FieldsColumn (Arrays.asList (desc));
                FieldsColumn right = new FieldsColumn ();
                right.addField (image);

                // Put everything else into a single column.
                for (UserConfigurableObjectTypeField f : fields)
                {

                    if ((f != name)
                        &&
                        (f != image)
                        &&
                        (f != desc)
                       )
                    {

                        right.addField (f);

                    }

                }

                this.sortableFieldsColumns.add (left);
                this.sortableFieldsColumns.add (right);

            }

            if (this.layout.equals (Constants.ASSET_LAYOUT_8))
            {

                FieldsColumn left = new FieldsColumn ();
                FieldsColumn right = new FieldsColumn (Arrays.asList (desc));

                right.setShowFieldLabels (false);
                left.addField (image);

                // Put everything else into a single column.
                for (UserConfigurableObjectTypeField f : fields)
                {

                    if ((f != name)
                        &&
                        (f != image)
                        &&
                        (f != desc)
                       )
                    {

                        left.addField (f);

                    }

                }

                this.sortableFieldsColumns.add (left);
                this.sortableFieldsColumns.add (right);

            }

            this.setProperty (Constants.USER_CONFIGURABLE_OBJECT_TYPE_SORTABLE_FIELDS_LAYOUT_PROPERTY_NAME,
                              this.getSortableFieldsState ().asString ());

        } else {

            Map<String, UserConfigurableObjectTypeField> _fields = new HashMap<> ();

            for (UserConfigurableObjectTypeField f : fields)
            {

                _fields.put (f.getObjectReference ().asString (),
                            f);

            }

            State colState = new State (lt);

            List<Map> _cols = colState.getAsList ("columns",
                                                  Map.class);

            for (Map d : _cols)
            {

                String title = (String) d.get ("title");

                List fieldIds = (List) d.get ("fields");
                FieldsColumn col = new FieldsColumn (title);
                if (d.containsKey ("showFieldLabels"))
                {

                    col.setShowFieldLabels ((Boolean) d.get ("showFieldLabels"));

                }

                this.sortableFieldsColumns.add (col);

                for (Object fid : fieldIds)
                {

                    if (fid == null)
                    {

                        continue;

                    }

                    String fieldId = null;

                    if (fid instanceof String)
                    {

                        fieldId = fid.toString ();

                    }

                    if (fid instanceof Map)
                    {

                        Map fieldData = (Map) fid;

                        fieldId = fieldData.get ("id").toString ();

                    }

                    UserConfigurableObjectTypeField f = _fields.get (fieldId);

                    if (f == null)
                    {

                        continue;

                    }

                    col.addField (f);

                }

            }

        }

        this.ignoreFieldsStateChanges = false;

    }

    /**
     * Get the field by the form name (ignoring case)
     */
    public UserConfigurableObjectTypeField getConfigurableFieldByFormName (String formName)
    {

        return this.getConfigurableFields ().stream ()
        .filter (t -> name.equalsIgnoreCase (t.getFormName ()))
        .findFirst ()
        .orElse (null);

    }

    /**
     * Get the field by the name (ignoring case)
     */
    public UserConfigurableObjectTypeField getConfigurableFieldByName (String name)
    {

        return this.getConfigurableFields ().stream ()
        .filter (t ->
        {

            if (name == null)
            {

                return false;

            }

            return name.equalsIgnoreCase (t.getName ());

        })
        .findFirst ()
        .orElse (null);

    }

    public Set<UserConfigurableObjectTypeField> getConfigurableFields ()
    {

        Set<UserConfigurableObjectTypeField> fields = new LinkedHashSet<> ();

        if (this.primaryNameField != null)
        {

            fields.add (this.primaryNameField);

        }

        this.sortableFieldsColumns.stream ()
            .forEach (c ->
            {
                fields.addAll (c.fields ());

            });

        return fields;

    }

    public ObservableList<FieldsColumn> getSortableFieldsColumns ()
    {

        return this.sortableFieldsColumns;

    }

    public Set<UserConfigurableObjectTypeField> getSortableFields ()
    {

        Set<UserConfigurableObjectTypeField> sfs = new LinkedHashSet<> ();

        sfs.add (this.getPrimaryNameField ());
        for (UserConfigurableObjectTypeField f : this.getConfigurableFields ())
        {

            if (f.isSortable ())
            {

                sfs.add (f);

            }

        }

        return sfs;

    }

    public void setCreateShortcutKeyStroke (javax.swing.KeyStroke k)
    {

        this.createShortcutKeyStroke = k;

    }

    public javax.swing.KeyStroke getCreateShortcutKeyStroke ()
    {

        return this.createShortcutKeyStroke;

    }

    public boolean isLegacyObjectType ()
    {

        return this.getUserObjectType () != null;

    }

    public String getUserObjectType ()
    {

        return this.userObjectType;

    }

    public void setUserObjectType (String t)
    {

        this.userObjectType = t;

    }

    @Override
    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new LinkedHashSet<> (this.getConfigurableFields ());

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public String getLayout ()
    {

        return this.layout;

    }

    public void setLayout (String l)
    {

        this.layout = l;

    }

/*
TODO REmove
    public void addConfigurableField (UserConfigurableObjectTypeField f)
    {

        this.fields.add (f);

        f.setUserConfigurableObjectType (this);

        if (f.getOrder () == -1)
        {

            f.setOrder (this.fields.size () - 1);

        }

    }
*/

/*
    public void reorderFields ()
    {

        TreeMap<Integer, UserConfigurableObjectTypeField> s = new TreeMap ();

        for (UserConfigurableObjectTypeField f : this.fields)
        {

            if (f.getOrder () == -1)
            {

                throw new IllegalStateException ("Field must have an order value: " +
                                                 f);

            }

            s.put (f.getOrder (),
                   f);

        }

        this.fields = new LinkedHashSet (s.values ());

    }
*/
    public UserConfigurableObjectTypeField getLegacyField (String id)
    {

        for (FieldsColumn fc : this.sortableFieldsColumns)
        {

            UserConfigurableObjectTypeField f = fc.getLegacyField (id);

            if (f != null)
            {

                return f;

            }

        }

        return null;

    }

    /**
     * Return how many non object name/object description fields there are.
     *
     * @return The count.
     */
    public int getNonCoreFieldCount ()
    {

        int c = 0;

        for (UserConfigurableObjectTypeField f : this.getConfigurableFields ())
        {

            // TODO: Need a better way!
            if (f instanceof ObjectNameUserConfigurableObjectTypeField)
            {

                continue;

            }

            if (f instanceof ObjectDescriptionUserConfigurableObjectTypeField)
            {

                continue;

            }

            c++;

        }

        return c;

    }

    /**
     * Warning!  This shouldn't really be used.  It is a hack to allow the fields/types to be copied from
     * one db to another.
     * The primary name field can only be set if it is currently null, otherwise an IllegalStateException is thrown.
     */
    public void setPrimaryNameField (ObjectNameUserConfigurableObjectTypeField f)
    {

        if (this.primaryNameField != null)
        {

            throw new IllegalStateException ("Primary name field already set");

        }

        this.primaryNameField = f;

    }

    public ObjectNameUserConfigurableObjectTypeField getPrimaryNameField ()
    {

        return this.primaryNameField;

    }

    public ObjectDescriptionUserConfigurableObjectTypeField getObjectDescriptionField ()
    {

        for (UserConfigurableObjectTypeField f : this.getConfigurableFields ())
        {

            // TODO: Need a better way!
            if (f instanceof ObjectDescriptionUserConfigurableObjectTypeField)
            {

                return (ObjectDescriptionUserConfigurableObjectTypeField) f;

            }

        }

        return null;

    }

    public boolean hasField (UserConfigurableObjectTypeField.Type t)
    {

        for (UserConfigurableObjectTypeField f : this.getConfigurableFields ())
        {

            if (f.getType () == t)
            {

                return true;

            }

        }

        return false;

    }

    public ObjectImageUserConfigurableObjectTypeField getObjectImageField ()
    {

        for (UserConfigurableObjectTypeField f : this.getConfigurableFields ())
        {

            // TODO: Need a better way!
            if (f instanceof ObjectImageUserConfigurableObjectTypeField)
            {

                return (ObjectImageUserConfigurableObjectTypeField) f;

            }

        }

        return null;

    }

    public void setIcon24x24 (Image im)
    {

        Image oldim = this.icon24x24Prop.getValue ();

        //this.icon24x24 = im;
        this.icon24x24Prop.setValue (im);//this.icon24x24);

        this.firePropertyChangedEvent (ICON24,
                                       oldim,
                                       im);

    }

    public Image getIcon24x24 ()
    {

        return this.icon24x24Prop.getValue ();

    }

    public ObjectProperty<Image> icon24x24Property ()
    {

        return this.icon24x24Prop;

    }

    public void setIcon16x16 (Image im)
    {

        Image oldim = this.icon16x16Prop.getValue ();

        //this.icon16x16 = im;
        this.icon16x16Prop.setValue (im);//this.icon16x16);

        this.firePropertyChangedEvent (ICON16,
                                       oldim,
                                       im);

    }

    public Image getIcon16x16 ()
    {

        return this.icon16x16Prop.getValue ();

    }

    public ObjectProperty<Image> icon16x16Property ()
    {

        return this.icon16x16Prop;

    }

    public void setObjectTypeName (String n)
    {

        String oldName = this.getName ();

        this.singularNameSet = (n != null);

        if (n == null)
        {

            n = getUIString (objectnames,singular,this.userObjectType);

        }

        this.setName (n);

        this.firePropertyChangedEvent (OBJECT_TYPE_NAME,
                                       oldName,
                                       n);

    }

    public String getObjectTypeName ()
    {

        if (this.singularNameSet)
        {

            return super.getName ();

        }

        StringProperty s = Environment.getObjectTypeName (this.userObjectType);

        if (s != null)
        {

            return s.getValue ();

        }

        return null;

    }

    @Override
    public StringProperty nameProperty ()
    {

        if (this.singularNameSet)
        {

            return super.nameProperty ();

        }

        return Environment.getObjectTypeNameSingular_int (this.userObjectType);

    }

    public StringProperty objectTypeNameProperty ()
    {

        return this.nameProperty ();

    }

    public String getActualObjectTypeName ()
    {

        return this.getName ();

    }

    public String getObjectTypeNamePlural ()
    {

        if (this.pluralNameSet)
        {

            return this.objectTypeNamePluralProp.getValue ();

        }

        StringProperty s = Environment.getObjectTypeNamePlural_int (this.userObjectType);

        if (s != null)
        {

            return s.getValue ();

        }

        return null;

    }

    public String getActualObjectTypeNamePlural ()
    {

        return this.objectTypeNamePluralProp.getValue ();

    }

    public void setObjectTypeNamePlural (String n)
    {

        String oldName = this.objectTypeNamePluralProp.getValue ();

        this.pluralNameSet = (n != null);

        if (n == null)
        {

            n = getUIString (objectnames,plural,this.userObjectType);

        }

        this.objectTypeNamePluralProp.setValue (n);

        // Is there a change?
        if (Objects.equals (oldName,
                            n))
        {

            this.firePropertyChangedEvent (OBJECT_TYPE_NAME_PLURAL,
                                           oldName,
                                           n);

        }

    }

    public StringProperty objectTypeNamePluralProperty ()
    {

        if (this.pluralNameSet)
        {

            return this.objectTypeNamePluralProp;

        }

        return Environment.getObjectTypeNamePlural_int (this.userObjectType);

    }

    public UserConfigurableObjectTypeField getField (long key)
    {

        UserConfigurableObjectTypeField f = this.getConfigurableFields ().stream ()
            .filter (l -> l.getKey () == key)
            .findFirst ()
            .orElse (null);

        if (f == null)
        {

            // See if we have a mapping.
            String prop = this.project.getProperty ("userConfigObjTypeFieldsEnvToProjKeyMap");

            if (prop != null)
            {

                Map m = (Map) JSONDecoder.decode (prop);

                Double v = (Double) m.get (String.valueOf (key));

                if (v == null)
                {

                    return null;

                }

                long nkey = v.longValue ();

                f = this.getConfigurableFields ().stream ()
                    .filter (l -> l.getKey () == nkey)
                    .findFirst ()
                    .orElse (null);

            }

        }

        return f;

    }

    public String getObjectTypeId ()
    {

        if (this.getUserObjectType () != null)
        {

            return this.getUserObjectType ();

        } else {

            if (this.isAssetObjectType ())
            {

                return "asset:" + this.getKey ();

            } else {

                return this.getObjectReference ().asString ();

            }

        }

    }

    public void addFieldToColumn (int                             ind,
                                  UserConfigurableObjectTypeField f)
    {

        this.getSortableFieldsColumns ().get (ind).addField (f);

    }

    public void setAssetObjectType (boolean v)
    {

        this.isAsset = v;

    }

    public boolean isAssetObjectType ()
    {

        return this.isAsset;

    }

    public static class FieldsColumn
    {

        private StringProperty titleProp = null;
        private BooleanProperty showFieldLabelsProp = null;
        private ObservableList<UserConfigurableObjectTypeField> fields = null;

        public FieldsColumn ()
        {

            this.titleProp = new SimpleStringProperty ();
            this.showFieldLabelsProp = new SimpleBooleanProperty (true);
            this.fields = FXCollections.observableList (new ArrayList<> ());

        }

        public FieldsColumn (String title)
        {

            this ();
            this.titleProp.setValue (title);

        }

        public FieldsColumn (String                                      title,
                             Collection<UserConfigurableObjectTypeField> fields)
        {

            this (title);

            if (fields != null)
            {

                for (UserConfigurableObjectTypeField f : fields)
                {

                    if (f != null)
                    {

                        // Bit of a hack here but it saves a lot of pfaffing elsewhere.
                        if (f.getType () == UserConfigurableObjectTypeField.Type.objectname)
                        {

                            continue;

                        }

                        this.fields.add (f);

                    }

                }

            }

        }

        public FieldsColumn (Collection<UserConfigurableObjectTypeField> fields)
        {

            this (null,
                  fields);

        }

        public UserConfigurableObjectTypeField getLegacyField (String id)
        {

            for (UserConfigurableObjectTypeField f : this.fields)
            {

                if ((f.getLegacyFieldId () != null)
                    &&
                    (f.getLegacyFieldId ().equals (id))
                   )
                {

                    return f;

                }

            }

            return null;

        }

        public boolean isShowFieldLabels ()
        {

            return this.showFieldLabelsProp.getValue ();

        }

        public void setShowFieldLabels (boolean v)
        {

            this.showFieldLabelsProp.setValue (v);

        }

        public BooleanProperty showFieldLabelsProperty ()
        {

            return this.showFieldLabelsProp;

        }

        public String getTitle ()
        {

            return this.titleProp.getValue ();

        }

        public void setTitle (String t)
        {

            this.titleProp.setValue (t);

        }

        public StringProperty titleProperty ()
        {

            return this.titleProp;

        }

        void addField (UserConfigurableObjectTypeField f)
        {

            if (f == null)
            {

                return;

            }

            this.fields.add (f);

        }

        public ObservableList<UserConfigurableObjectTypeField> fields ()
        {

            return this.fields;

        }

    }

    @Override
    public boolean supportsLinks ()
    {

        return false;

    }

}
