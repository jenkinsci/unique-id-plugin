package org.jenkinsci.plugins.uniqueid.impl;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.util.DescribableList;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;
import com.cloudbees.hudson.plugins.folder.Folder;

/**
 * Stores ids for folders as a {@link FolderIdProperty}
 * @deprecated Use {@link org.jenkinsci.plugins.uniqueid.implv2.PersistenceRootIdStore}
 */
@Extension(optional = true)
@Deprecated
@Restricted(NoExternalUse.class)
public class FolderIdStore extends LegacyIdStore<Folder> {
    public FolderIdStore() {
        super(Folder.class);
    }

    @Override
    public void remove(Folder folder) throws IOException {
        DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor> properties = folder.getProperties();
        boolean needSave = false;
        for (Iterator<AbstractFolderProperty<?>> itr = properties.iterator(); itr.hasNext(); ) {
            AbstractFolderProperty<?> prop = itr.next();

            if (prop instanceof FolderIdProperty) {
                itr.remove();
                needSave = true;
            }
        }
        if (needSave) {
            folder.save();
        }
    }

    @Override
    public String get(Folder folder) {
        String id = Id.getId((Actionable) folder);
        if (id != null) {
            return id;
        } else {
            FolderIdProperty idProperty = folder.getProperties().get(FolderIdProperty.class);
            if (idProperty != null) {
                return idProperty.getId();
            }
        }
        return null;
    }

    /**
     * A unique ID for folders.
     */
    public static class FolderIdProperty extends FolderProperty {
        private Id id = new Id();

        @Override
        public Collection<? extends Action> getFolderActions() {
            return Collections.singleton(id);
        }

        @Extension(optional = true)
        public static class DescriptorImpl extends FolderPropertyDescriptor {
            @Override
            public String getDisplayName() {
                return "Unique ID";
            }

            @Override
            public boolean isApplicable(Class<? extends AbstractFolder> containerType) {
                return false;
            }
        }

        /**
         * Since {@link Folder#getAction(Class)} does return actions from a property,
         * (like {@link hudson.model.Job#getAction(Class)} does)
         * this method is added for convenience.
         *
         * @return the id for this folder
         */
        public String getId() {
            return id.getId();
        }
    }

    private final static Logger LOGGER = Logger.getLogger(FolderIdStore.class.getName());

}
