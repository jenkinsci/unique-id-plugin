package org.jenkinsci.plugins.uniqueid.impl;

import hudson.ExtensionPoint;

import jenkins.model.Jenkins;

import java.io.IOException;

import javax.annotation.Nullable;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * An abstraction to persistently store and retrieve unique id's
 * for various Jenkins model objects.
 *
 * These keys are guaranteed to be unique with a Jenkins
 * and immutable across the lifetime of the given object.
 * 
 * Implementations should not store the ID inside any specific item configuration as it is
 * common for users top copy items either through the UI or manually and this will cause the
 * IDs to become non-unique.
 * 
 *
 * @param <T>
 */
@Restricted(NoExternalUse.class)
@Deprecated
public abstract class LegacyIdStore<T> implements ExtensionPoint {

    private final Class<T> type;

    public LegacyIdStore (Class<T> forType) {
        this.type = forType;
    }

    /**
     * Remove the unique id associated with the given object.
     * @param object
     */
    public abstract void remove(T object) throws IOException;

    /**
     * Get the id for this given object.
     * @param object
     * @return the id or null if none assigned.
     */
    @Nullable
    public abstract String get(T object);

    public boolean supports(Class clazz) {
        return type.isAssignableFrom(clazz);
    }

    /**
     * Retrieve an {@link LegacyIdStore} for the given type
     * @param clazz
     * @param <C>
     * @return the store which supports the type, or null if none
     */
    @Nullable
    public static <C> LegacyIdStore<C> forClass(Class<C> clazz) {
        final Jenkins jenkins = Jenkins.getInstance();
        if (jenkins != null) {
            for (LegacyIdStore store : jenkins.getExtensionList(LegacyIdStore.class)) {
                if (store.supports(clazz)) {
                    return store;
                }
            }
        }
        return null;
    }

    /**
     * Convenience method which makes the id for the given object.
     *
     * @throws java.lang.IllegalArgumentException if the type is not supported.
     * @throws IOException if we could not remove the ID from the Object.
     */
    public static void removeId(Object object) throws IOException{
        LegacyIdStore store = forClass(object.getClass());
        if (store == null) {
            throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
        } else {
            store.remove(object);
        }
    }

    /**
     * Convenience method which retrieves the id for the given object.
     *
     * @throws java.lang.IllegalArgumentException if the type is not supported.
     */
    public static String getId(Object object) {
        LegacyIdStore store = forClass(object.getClass());
        if (store == null) {
            throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
        } else {
            return store.get(object);
        }
    }

}
