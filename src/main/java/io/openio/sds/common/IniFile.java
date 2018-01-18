/*
 * Copyright (C) 2013 Aerospace https://stackoverflow.com/a/15638381/537768
 * Copyright (C) 2018 OpenIO SAS
 */
package io.openio.sds.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple class to load an INI format configuration file.
 */
public class IniFile {

    private Pattern _section = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private Pattern _keyValue = Pattern.compile("\\s*([^=]*)=(.*)");
    private Map<String, Map<String, String>> _entries = new HashMap<>();

    /**
     * Load and parse the content of a file.
     *
     * @param path
     *            the path to the file to load
     * @throws IOException
     *             If an I/O error occurs
     * @throws FileNotFoundException
     *             If the file does not exist
     */
    public IniFile(String path) throws IOException {
        load(path);
    }

    /**
     * Load and parse the content of a file. This can be called several times to
     * load several files.
     *
     * @param path
     *            the path to the file to load
     * @throws IOException
     *             If an I/O error occurs
     * @throws FileNotFoundException
     *             If the file does not exist
     */
    public void load(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            String section = null;
            while ((line = br.readLine()) != null) {
                Matcher m = _section.matcher(line);
                if (m.matches()) {
                    section = m.group(1).trim();
                } else if (section != null) {
                    m = _keyValue.matcher(line);
                    if (m.matches()) {
                        String key = m.group(1).trim();
                        String value = m.group(2).trim();
                        Map<String, String> kv = _entries.get(section);
                        if (kv == null) {
                            _entries.put(section, kv = new HashMap<>());
                        }
                        kv.put(key, value);
                    }
                }
            }
        }
    }

    /**
     * Check if a section exists.
     *
     * @param section The configuration section.
     * @return {@code true} if the section exists in any of the loaded files.
     */
    public boolean hasSection(String section) {
        return _entries.get(section) != null;
    }

    /**
     * Get a string associated with the given configuration key, from the given
     * section.
     *
     * @param section
     *            The configuration section
     * @param key
     *            The configuration key
     * @param defaultValue
     *            The value to return if {@code section} or {@code key} does not
     *            exist
     * @return The string associated with the given configuration key and
     *         section, or {@code defaultValue} if either does not exist
     */
    public String getString(String section, String key, String defaultValue) {
        Map<String, String> kv = _entries.get(section);
        if (kv == null) {
            return defaultValue;
        }
        return kv.getOrDefault(key, defaultValue);
    }

    /**
     * Get an integer associated with the given configuration key, from the
     * given section.
     *
     * @param section
     *            The configuration section
     * @param key
     *            The configuration key
     * @param defaulVvalue
     *            The value to return if {@code section} or {@code key} does not
     *            exist
     * @return The {@code int} associated with the given configuration key and
     *         section, or {@code defaultValue} if either does not exist
     * @throws NumberFormatException
     *             if the value cannot be parsed as an integer
     */
    public int getInt(String section, String key, int defaulVvalue) {
        String sVal = getString(section, key, null);
        if (sVal == null)
            return defaulVvalue;
        return Integer.parseInt(sVal);
    }

    /**
     * Get a float associated with the given configuration key, from the given
     * section.
     *
     * @param section
     *            The configuration section
     * @param key
     *            The configuration key
     * @param defaulVvalue
     *            The value to return if {@code section} or {@code key} does not
     *            exist
     * @return The {@code float} associated with the given configuration key and
     *         section, or {@code defaultValue} if either does not exist
     * @throws NumberFormatException
     *             if the value cannot be parsed as a float
     */
    public float getFloat(String section, String key, float defaulVvalue) {
        String sVal = getString(section, key, null);
        if (sVal == null)
            return defaulVvalue;
        return Float.parseFloat(sVal);
    }

}
