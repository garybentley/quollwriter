package com.quollwriter.data;

import java.util.*;

import javafx.scene.*;
import javafx.beans.value.*;
import javafx.collections.*;

public class PropertyBinder implements IPropertyBinder
{

    private Map<ObservableValue, Set<ChangeListener>> changeListeners = new HashMap<> ();

    private List<AbstractListenerHandle> handles = new ArrayList<> ();

    public PropertyBinder ()
    {

    }

    @Override
    public IPropertyBinder getBinder ()
    {

        return this;

    }

    @Override
    public <T> ListenerHandle addChangeListener (ObservableValue<T> value,
                                                 ChangeListener<T> listener)
    {

        if (value == null)
        {

            return null;

        }

        ChangeListenerHandle<T> h = new ChangeListenerHandle<> (this,
                                                                value,
                                                                listener);

        this.handles.add (h);

        return h;

    }

    @Override
    public <K, V> ListenerHandle addMapChangeListener (ObservableMap<K, V>     map,
                                                       MapChangeListener<K, V> listener)
    {

        if (map == null)
        {

            return null;

        }

        MapChangeListenerHandle<K, V> h = new MapChangeListenerHandle<> (this,
                                                                         map,
                                                                         listener);

        this.handles.add (h);

        return h;

    }

    @Override
    public <T> ListenerHandle addSetChangeListener (ObservableSet<T>     set,
                                                    SetChangeListener<T> listener)
    {

        if (set == null)
        {

            return null;

        }

        SetChangeListenerHandle<T> h = new SetChangeListenerHandle<> (this,
                                                                      set,
                                                                      listener);

        this.handles.add (h);

        return h;

    }

    @Override
    public <T> IPropertyBinder.ListenerHandle addListChangeListener (ObservableList<T>     list,
                                                     ListChangeListener<T> listener)
    {

        if (list == null)
        {

            return null;

        }

        ListChangeListenerHandle<T> h = new ListChangeListenerHandle<T> (this,
                                                                         list,
                                                                         listener);

        this.handles.add (h);

        return h;

    }

    @Override
    public void dispose ()
    {

        new ArrayList<> (this.handles).stream ()
            .forEach (h -> h.dispose ());

        for (ObservableValue v : this.changeListeners.keySet ())
        {

            for (ChangeListener l : this.changeListeners.get (v))
            {

                v.removeListener (l);

            }

        }

        this.changeListeners.clear ();

    }

    void removeHandle (ListenerHandle h)
    {

        this.handles.remove (h);

    }

    public abstract class AbstractListenerHandle implements IPropertyBinder.ListenerHandle
    {

        protected PropertyBinder binder = null;

        public AbstractListenerHandle (PropertyBinder binder)
        {

            this.binder = binder;

        }

        public IPropertyBinder getBinder ()
        {

            return this.binder;

        }

        public void dispose ()
        {

            this.binder.removeHandle (this);

        }

    }

    public class SetChangeListenerHandle<T> extends AbstractListenerHandle
    {

        private ObservableSet<T> set = null;
        private SetChangeListener<T> listener = null;

        public SetChangeListenerHandle (PropertyBinder       binder,
                                        ObservableSet<T>     set,
                                        SetChangeListener<T> listener)
        {

            super (binder);

            this.set = set;
            this.listener = listener;

            this.set.addListener (this.listener);

        }

        @Override
        public void dispose ()
        {

            this.set.removeListener (this.listener);

            super.dispose ();

        }

    }

    public class MapChangeListenerHandle<K, V> extends AbstractListenerHandle
    {

        private ObservableMap<K, V> map = null;
        private MapChangeListener<K, V> listener = null;

        public MapChangeListenerHandle (PropertyBinder          binder,
                                        ObservableMap<K, V>     map,
                                        MapChangeListener<K, V> listener)
        {

            super (binder);

            this.map = map;
            this.listener = listener;

            this.map.addListener (this.listener);

        }

        @Override
        public void dispose ()
        {

            this.map.removeListener (this.listener);

            super.dispose ();

        }

    }

    public class ListChangeListenerHandle<T> extends AbstractListenerHandle
    {

        private ObservableList<T> list = null;
        private ListChangeListener<T> listener = null;

        public ListChangeListenerHandle (PropertyBinder        binder,
                                         ObservableList<T>     list,
                                         ListChangeListener<T> listener)
        {

            super (binder);

            this.list = list;
            this.listener = listener;

            this.list.addListener (this.listener);

        }

        @Override
        public void dispose ()
        {

            this.list.removeListener (this.listener);

            super.dispose ();

        }

    }

    public class ChangeListenerHandle<T> extends AbstractListenerHandle
    {

        private ObservableValue<T> list = null;
        private ChangeListener<T> listener = null;

        public ChangeListenerHandle (PropertyBinder     binder,
                                     ObservableValue<T> list,
                                     ChangeListener<T>  listener)
        {

            super (binder);

            this.list = list;
            this.listener = listener;

            this.list.addListener (this.listener);

        }

        @Override
        public void dispose ()
        {

            this.list.removeListener (this.listener);

            super.dispose ();

        }

    }

}
