package org.jenkinsci.plugins.uniqueid;

import hudson.ExtensionPoint;
import jenkins.model.Jenkins;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
public abstract class IdStore<T> implements ExtensionPoint {

    private final Class<T> type;

    public IdStore (Class<T> forType) {
        this.type = forType;
    }

    /**
     * Creates an unique id for the given object.
     * Subsequent calls are idempotent.
     *
     * @param object the object to make the id for.
     */
    public abstract void make(T object);

    /**
     * Get the id for this given object.
     * @param object the object.
     * @return the id or {@code null} if none assigned.
     */
    @Nullable
    public abstract String get(T object);

    public boolean supports(Class clazz) {
        return type.isAssignableFrom(clazz);
    }

    /**
     * Retrieve an {@link IdStore} for the given type.
     * @param clazz the type of object.
     * @param <C> the type of object.
     * @return the store which supports the type, or {@code null} if none.
     */
    @Nullable
    public static <C> IdStore<C> forClass(Class<C> clazz) {
        for (IdStore store : Jenkins.getInstance().getExtensionList(IdStore.class)) {
            if (store.supports(clazz)) {
                return store;
            }
        }
        return null;
    }

    /**
     * Convenience method which makes the id for the given object.
     *
     * @throws java.lang.IllegalArgumentException if the type is not supported.
     */
    public static void makeId(Object object) throws IllegalArgumentException {
        IdStore store = forClass(object.getClass());
        if (store == null) {
            throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
        } else {
            store.make(object);
        }
    }

    /**
     * Convenience method which retrieves the id for the given object.
     *
     * @throws java.lang.IllegalArgumentException if the type is not supported.
     */
    public static String getId(Object object) throws IllegalArgumentException {
        IdStore store = forClass(object.getClass());
        if (store == null) {
            throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
        } else {
            return store.get(object);
        }
    }

    /**
     * Returns the generic filename that a store should use for storing the unique id.
     *
     * @return the generic filename.
     * @since 2.1.0
     */
    @Nonnull
    public static String fileName() {
        return "unique-id.txt";
    }

    /**
     * Generates a new unique ID.
     * Subclasses do not need to use this to create unique IDs and are free to create IDs by other methods.
     * @return a string that should be unique against all jenkins instances.
     */
    protected static String generateUniqueID() {
        return Base64.encodeBase64String(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)).substring(0, 30);
    }

}
