/*
 * Copyright (c) 1996, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */

package java.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import sun.security.action.GetPropertyAction;
import sun.util.PropertyResourceBundleCharset;
import sun.util.ResourceBundleEnumeration;

/**
 * <code>PropertyResourceBundle</code> is a concrete subclass of
 * <code>ResourceBundle</code> that manages resources for a locale
 * using a set of static strings from a property file. See
 * {@link ResourceBundle ResourceBundle} for more information about resource
 * bundles.
 *
 * <p>
 * Unlike other types of resource bundle, you don't subclass
 * <code>PropertyResourceBundle</code>.  Instead, you supply properties
 * files containing the resource data.  <code>ResourceBundle.getBundle</code>
 * will automatically look for the appropriate properties file and create a
 * <code>PropertyResourceBundle</code> that refers to it. See
 * {@link ResourceBundle#getBundle(String, Locale, ClassLoader) ResourceBundle.getBundle}
 * for a complete description of the search and instantiation strategy.
 *
 * <p>
 * The following <a id="sample">example</a> shows a member of a resource
 * bundle family with the base name "MyResources".
 * The text defines the bundle "MyResources_de",
 * the German member of the bundle family.
 * This member is based on <code>PropertyResourceBundle</code>, and the text
 * therefore is the content of the file "MyResources_de.properties"
 * (a related <a href="ListResourceBundle.html#sample">example</a> shows
 * how you can add bundles to this family that are implemented as subclasses
 * of <code>ListResourceBundle</code>).
 * The keys in this example are of the form "s1" etc. The actual
 * keys are entirely up to your choice, so long as they are the same as
 * the keys you use in your program to retrieve the objects from the bundle.
 * Keys are case-sensitive.
 * <blockquote>
 * <pre>
 * # MessageFormat pattern
 * s1=Die Platte \"{1}\" enth&auml;lt {0}.
 *
 * # location of {0} in pattern
 * s2=1
 *
 * # sample disk name
 * s3=Meine Platte
 *
 * # first ChoiceFormat choice
 * s4=keine Dateien
 *
 * # second ChoiceFormat choice
 * s5=eine Datei
 *
 * # third ChoiceFormat choice
 * s6={0,number} Dateien
 *
 * # sample date
 * s7=3. M&auml;rz 1996
 * </pre>
 * </blockquote>
 *
 * @apiNote
 * {@code PropertyResourceBundle} can be constructed either
 * from an {@code InputStream} or a {@code Reader}, which represents a property file.
 * Constructing a {@code PropertyResourceBundle} instance from an {@code InputStream}
 * requires that the input stream be encoded in {@code UTF-8}. By default, if a
 * {@link java.nio.charset.MalformedInputException} or an
 * {@link java.nio.charset.UnmappableCharacterException} occurs on reading the
 * input stream, then the {@code PropertyResourceBundle} instance resets to the state
 * before the exception, re-reads the input stream in {@code ISO-8859-1}, and
 * continues reading. If the system property
 * {@systemProperty java.util.PropertyResourceBundle.encoding} is set to either
 * "ISO-8859-1" or "UTF-8", the input stream is solely read in that encoding,
 * and throws the exception if it encounters an invalid sequence.
 * If "ISO-8859-1" is specified, characters that cannot be represented in
 * ISO-8859-1 encoding must be represented by Unicode Escapes as defined in section
 * 3.3 of <cite>The Java&trade; Language Specification</cite>
 * whereas the other constructor which takes a {@code Reader} does not have that limitation.
 * Other encoding values are ignored for this system property.
 * The system property is read and evaluated when initializing this class.
 * Changing or removing the property has no effect after the initialization.
 *
 * @implSpec
 * The implementation of a {@code PropertyResourceBundle} subclass must be
 * thread-safe if it's simultaneously used by multiple threads. The default
 * implementations of the non-abstract methods in this class are thread-safe.
 *
 * @see ResourceBundle
 * @see ListResourceBundle
 * @see Properties
 * @since 1.1
 */
public class PropertyResourceBundle extends ResourceBundle {

    // Check whether the strict encoding is specified.
    // The possible encoding is either "ISO-8859-1" or "UTF-8".
    private static final String encoding = GetPropertyAction
        .privilegedGetProperty("java.util.PropertyResourceBundle.encoding", "")
        .toUpperCase(Locale.ROOT);

    /**
     * Creates a property resource bundle from an {@link java.io.InputStream
     * InputStream}. This constructor reads the property file in UTF-8 by default.
     * If a {@link java.nio.charset.MalformedInputException} or an
     * {@link java.nio.charset.UnmappableCharacterException} occurs on reading the
     * input stream, then the PropertyResourceBundle instance resets to the state
     * before the exception, re-reads the input stream in {@code ISO-8859-1} and
     * continues reading. If the system property
     * {@code java.util.PropertyResourceBundle.encoding} is set to either
     * "ISO-8859-1" or "UTF-8", the input stream is solely read in that encoding,
     * and throws the exception if it encounters an invalid sequence. Other
     * encoding values are ignored for this system property.
     * The system property is read and evaluated when initializing this class.
     * Changing or removing the property has no effect after the initialization.
     *
     * @param stream an InputStream that represents a property file
     *        to read from.
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if <code>stream</code> is null
     * @throws IllegalArgumentException if {@code stream} contains a
     *     malformed Unicode escape sequence.
     * @throws MalformedInputException if the system property
     *     {@code java.util.PropertyResourceBundle.encoding} is set to "UTF-8"
     *     and {@code stream} contains an invalid UTF-8 byte sequence.
     * @throws UnmappableCharacterException if the system property
     *     {@code java.util.PropertyResourceBundle.encoding} is set to "UTF-8"
     *     and {@code stream} contains an unmappable UTF-8 byte sequence.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public PropertyResourceBundle (InputStream stream) throws IOException {
        this(new InputStreamReader(stream,
            "ISO-8859-1".equals(encoding) ?
                StandardCharsets.ISO_8859_1.newDecoder() :
                new PropertyResourceBundleCharset("UTF-8".equals(encoding)).newDecoder()));
    }

    /**
     * Creates a property resource bundle from a {@link java.io.Reader
     * Reader}.  Unlike the constructor
     * {@link #PropertyResourceBundle(java.io.InputStream) PropertyResourceBundle(InputStream)},
     * there is no limitation as to the encoding of the input property file.
     *
     * @param reader a Reader that represents a property file to
     *        read from.
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if <code>reader</code> is null
     * @throws IllegalArgumentException if a malformed Unicode escape sequence appears
     *     from {@code reader}.
     * @since 1.6
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public PropertyResourceBundle (Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        lookup = new HashMap(properties);
    }

    // Implements java.util.ResourceBundle.handleGetObject; inherits javadoc specification.
    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    /**
     * Returns an <code>Enumeration</code> of the keys contained in
     * this <code>ResourceBundle</code> and its parent bundles.
     *
     * @return an <code>Enumeration</code> of the keys contained in
     *         this <code>ResourceBundle</code> and its parent bundles.
     * @see #keySet()
     */
    public Enumeration<String> getKeys() {
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(),
                (parent != null) ? parent.getKeys() : null);
    }

    /**
     * Returns a <code>Set</code> of the keys contained
     * <em>only</em> in this <code>ResourceBundle</code>.
     *
     * @return a <code>Set</code> of the keys contained only in this
     *         <code>ResourceBundle</code>
     * @since 1.6
     * @see #keySet()
     */
    protected Set<String> handleKeySet() {
        return lookup.keySet();
    }

    // ==================privates====================

    private final Map<String,Object> lookup;
}