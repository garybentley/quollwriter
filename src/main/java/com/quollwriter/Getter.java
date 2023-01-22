/*
 * Copyright 2006 - Gary Bentley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quollwriter;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * This class is used to perform access into a Java object using a
 * String value with a specific notation.
 * <p>
 * The Accessor uses a dot notation such as <b>field1.method1.method2</b> to
 * perform the access on an object.  Each value in the notation refers to
 * a field or method (a no argument method) of the type of the previous
 * value.
 * For instance if you have the following class structure:
 * </p>
 * <pre>
 * public class A
 * {
 *    public B = new B ();
 * }
 *
 * public class B
 * {
 *    public C = new C ();
 * }
 *
 * public class C
 * {
 *    String d = "";
 * }
 * </pre>
 * <p>
 * You would then use the notation: <b>B.C.d</b> to get access to
 * field <b>d</b> in Class C.
 * <br /><br />
 * The Accessor also supports a <b>[ ]</b> notation for accessing
 * into Lists/Maps and Arrays.  If the value between the <b>[ ]</b>
 * is an integer then we look for the associated type to be either
 * an array or a List, we then index into it with the integer.  If
 * the value is <b>NOT</b> an integer then we use assume the
 * type is a Map and use it as a key into the Map.
 * <br /><br />
 * For instance changing the example above:
 * </p>
 * <pre>
 * public class A
 * {
 *    public List vals = new ArrayList ();
 * }
 * </pre>
 * <p>
 * Now we could use: <b>vals[X]</b> where <b>X</b> is a positive integer.
 * Or changing again:
 * </p>
 * <pre>
 * public class A
 * {
 *    public Map vals = new HashMap ();
 * }
 * </pre>
 * <p>
 * We could use: <b>vals[VALUE]</b> where <b>VALUE</b> would then be
 * used as a Key into the vals HashMap.
 * <br /><br />
 * Note: The Accessor is <b>NOT</b> designed to be an all purpose
 * method of gaining access to a class.  It has specific uses and for
 * most will be of no use at all.  It should be used for general purpose
 * applications where you want to access specific fields of an object
 * without having to know the exact type.  One such application is in
 * the {@link GeneralComparator}, in that case arbitrary Objects can
 * be sorted without having to write complex Comparators or implementing
 * the Comparable interface AND it gives the flexibility that sorting
 * can be changed ad-hoc.
 * <br /><br />
 * The Accessor looks for in the following order:
 * <ul>
 *   <li>Public fields with the specified name.</li>
 *   <li>If no field is found then the name is converted to a "JavaBeans"
 *       <b>get</b> method, so a field name of <b>value</b> would be converted
 *       to <b>getValue</b> and that method is looked for.  The method must take
 *       no arguments.</li>
 *   <li>If we don't find the <b>get*</b> method then we look for a method with
 *       the specified name.  So a field name of <b>value</b> would mean that
 *       a method (that again takes no arguments) is looked for.</li>
 * </ul>
 * <p>
 * Note: we have had to add the 3rd type to allow for methods that don't follow
 * JavaBeans conventions (there are loads in the standard Java APIs which makes
 * accessing impossible otherwise).
 */
public class Getter
{

    private List chain = new ArrayList ();

    private Class clazz = null;
    private int cs = 0;
    private String acc = null;

    /**
     * Get the getter associated with the named reference.  Return
     * null if there isn't one, or if we can't access it.
     *
     * @param ref The reference for the getter.
     * @param clazz The Class to get the field from.
     */
    public Getter (String ref,
		   Class  clazz)
	                  throws IllegalArgumentException
    {

	if (clazz == null)
	{

	    throw new IllegalArgumentException ("Class must be specified");

	}

	this.acc = ref;
	this.clazz = clazz;

	StringTokenizer t = new StringTokenizer (ref,
						 ".");

	Class c = clazz;

	while (t.hasMoreTokens ())
	{

	    String tok = t.nextToken ();

	    String index = "";

	    // Get the Fields.
	    Field[] fields = c.getFields ();

	    Field f = null;

	    // See if the token matches...
	    for (int i = 0; i < fields.length; i++)
	    {

		if (fields[i].getName ().equals (tok))
		{

		    // Found it...
		    f = fields[i];

		    break;

		}

	    }

	    if (f != null)
	    {

		c = f.getType ();

		this.chain.add (f);

	    } else {

		Method m = this.getNoParmJavaGetMethod (tok,
							c);

		if (m == null)
		{

		    throw new IllegalArgumentException ("Cannot find method with name: " +
							tok +
							" in class: " +
							c.getName ());

		}

		// Need to set the method as being accessible here to workaround
		// an annoying Java reflection bug that seems to have been around
		// since the year dot.  See bug: 4071957.
		m.setAccessible (true);

		c = m.getReturnType ();

		if (Void.class.isAssignableFrom (c))
		{

		    throw new IllegalArgumentException ("Method: " +
							m.getName () +
							" cannot be called on class: " +
							c.getName () +
							" since return type is void");

		}

		this.chain.add (m);

	    }

	}

	this.cs = this.chain.size ();

    }

    public Class getBaseClass ()
    {

	return this.clazz;

    }

    /**
     * Get the class of the type of object we would return from the {@link #getValue(Object)}
     * method.
     *
     * @return The class.
     */
    public Class getType ()
    {

	Object o = this.chain.get (this.chain.size () - 1);

	// See what type the accessor is...
	if (o instanceof Method)
	{

	    Method m = (Method) o;

	    return m.getReturnType ();

	}

	if (o instanceof Field)
	{

	    // It's a field...so...
	    Field f = (Field) o;

	    return f.getType ();

	}

	return null;

    }

    public Object getValue (Object obj)
	                    throws IllegalAccessException,
                                   InvocationTargetException
    {

	// If the object is null then return null.
	if (obj == null)
	{

	    return null;

	}

	// For our accessor chain, use the Field and Methods
	// to get the actual value.
	Object retdata = obj;

	for (int i = 0; i < this.cs; i++)
	{

	    Object o = this.chain.get (i);

	    // See what type the accessor is...
	    if (o instanceof Method)
	    {

		Method m = (Method) o;

		Object[] parms = {};

		// Invoke the method...
		try
		{

		    retdata = m.invoke (retdata,
					parms);

		} catch (Exception e) {

		    this.throwException (obj,
					 e);

		}

		if (retdata == null)
		{

		    return null;

		}

	    }

	    if (o instanceof Field)
	    {

		// It's a field...so...
		Field f = (Field) o;

		// Now get the value...
		try
		{

		    retdata = f.get (retdata);

		} catch (Exception e) {

		    this.throwException (obj,
					 e);

		}

	    }

	}

	return retdata;

    }

    private void throwException (Object    o,
				 Exception e)
    {

	throw new RuntimeException ("Unable to get value from instance of: " +
				    o.getClass ().getName () +
				    ", using accessor: " +
				    this.acc +
				    " expected type to be: " +
				    this.clazz.getName (),
				    e);


    }

    public static Method getNoParmJavaGetMethod (String method,
						 Class  c)
    {

	StringBuffer b = new StringBuffer (method);

	Method m = null;

	// First look for a "get" method.
	try
	{

	    b.setCharAt (0,
			 Character.toUpperCase (method.charAt (0)));

	    b.insert (0,
		      "get");

	    String getMN = b.toString ();

	    m = c.getMethod (getMN);

	    if (m != null)
	    {

		return m;

	    }

	} catch (Exception e) {

	    // Painful to have to do it this way...

	}

	try
	{

	    b = new StringBuffer (method);

	    b.setCharAt (0,
			 Character.toUpperCase (method.charAt (0)));

	    b.insert (0,
		      "is");

	    String isMN = b.toString ();

	    m = c.getMethod (isMN);

	    if (m != null)
	    {

		return m;

	    }

	} catch (Exception e) {

	    // Sigh...

	}

	try
	{

	    return c.getMethod (method);

	} catch (Exception e) {

	    // Ignore...

	}

	return null;

    }

    public String getAccessor ()
    {

	return this.acc;

    }

    public String toString ()
    {

	return "Accessor: " + this.acc + " from class: " + this.clazz.getName ();

    }

}
