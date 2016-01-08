package edu.ou.weinmann.repsi.model.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the configuration parameters.
 * 
 * @author Walter Weinmann
 * 
 */
public final class Configurator {

    private static final Logger LOGGER =
            Logger.getLogger(Configurator.class.getPackage().getName());

    private static final String PROPERTIES_FILE_NAME =
            "config/configuration.properties";

    private static Configurator instance;

    private static Properties properties;

    private Configurator() {

        super();
    }

    private void checkPreconditions(final String parKey) {

        if (parKey == null) {
            throw new IllegalArgumentException("Property key missing (null)");
        }

        if ("".equals(parKey)) {
            throw new IllegalArgumentException("Property key missing (empty)");
        }
    }

    /**
     * Returns the current instance of the <code>Configurator</code> class.
     * 
     * @return the current instance of the <code>Configurator</code> class, if
     *         the instantiation was successful, or <code>null</code>
     *         otherwise.
     */
    public static Configurator getInstance() {

        return getInstance(Global.PROPERTIES_FILE_NAME, false);
    }

    /**
     * Returns the current instance of the <code>Configurator</code> class.
     * 
     * @param parFileName The complete file name of the proprties file including
     *            the directory.
     * @param parXml Whether the properties file constitutes an XML document and
     *            <code>false</code> if the properties file consists of a flat
     *            file.
     * 
     * @return an instance of the <code>Configurator</code> class, if the
     *         instantiation was successful, or <code>null</code> otherwise.
     */
    public static Configurator getInstance(final String parFileName,
            final boolean parXml) {

        if (instance != null) {
            return instance;
        }

        if (parFileName == null || "".equals(parFileName)) {
            throw new IllegalArgumentException("Filename is missing");
        }

        properties = new Properties();

        try {
            if (parXml) {
                properties.loadFromXML(new FileInputStream(parFileName));
            } else {
                properties.load(new FileInputStream(parFileName));
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Properties file=" + parFileName
                    + " not available", e);
            return null;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Properties file=" + parFileName
                    + " not available", e);
            return null;
        }

        instance = new Configurator();

        assert instance != null : "Postcondition: instance is null";

        return instance;
    }

    /**
     * Returns the value of a given property key.
     * 
     * @param parKey The name of the property key.
     * 
     * @return the value of the property key.
     */
    public String getProperty(final String parKey) {

        checkPreconditions(parKey);

        final String lvProperty = properties.getProperty(parKey);

        if (lvProperty == null) {
            LOGGER.log(Level.WARNING, "Property " + parKey
                    + " missing in property file " + PROPERTIES_FILE_NAME);
            return null;
        }

        return lvProperty;
    }

    /**
     * Removes the current <code>Configurator</code> object.
     * 
     * @return <code>true</code>, if an instance was existing, or
     *         <code>false</code> otherwise.
     */
    public static boolean removeInstance() {

        if (instance != null) {
            instance = null;
            return true;
        }

        return false;
    }

    /**
     * sets the value of a given property key.
     * 
     * @param parKey The name of the property key.
     * @param parValue The new value of this property key.
     * 
     * @return <code>true</code> if the new value was successfully stored.
     */
    public boolean setProperty(final String parKey, final String parValue) {

        checkPreconditions(parKey);

        if (parValue == null) {
            throw new IllegalArgumentException("Property value missing (null)");
        }

        if (properties.setProperty(parKey, parValue) == null) {
            return false;
        }

        return true;
    }
}
