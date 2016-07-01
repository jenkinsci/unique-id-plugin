package org.jenkinsci.plugins.uniqueid.impl;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Stores ids for jobs in {@link JobIdProperty}
 * @deprecated Use {@link org.jenkinsci.plugins.uniqueid.implv2.PersistenceRootIdStore}
 */
@Extension
@Deprecated
@Restricted(NoExternalUse.class)
public class JobIdStore extends LegacyIdStore<Job> {

    public JobIdStore() {
        super(Job.class);
    }

    @Override
    public void remove(Job job) {
        try {
            while (job.removeProperty(JobIdProperty.class) != null) {}
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to remove property from " + job.getFullName(), ex);
        }
    }
    
    @Override
    public String get(Job thing) {
        return Id.getId((Actionable) thing);
    }


    /**
     * A unique Id for Jobs.
     */
    @Deprecated
    public static class JobIdProperty extends JobProperty<Job<?,?>> {
        private Id id = new Id();

        @Override
        public Collection<? extends Action> getJobActions(Job job) {
            return Collections.singleton(id);
        }
        @Extension
        public static class DescriptorImpl extends JobPropertyDescriptor {
            @Override
            public String getDisplayName() {
                return "Unique ID";
            }
        }
    }

    private final static Logger LOGGER = Logger.getLogger(JobIdStore.class.getName());

}
