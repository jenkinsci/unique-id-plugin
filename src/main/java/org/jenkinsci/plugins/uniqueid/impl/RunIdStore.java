package org.jenkinsci.plugins.uniqueid.impl;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Controls id's for runs.
 */
@Extension
@Deprecated
@Restricted(NoExternalUse.class)
public class RunIdStore extends LegacyIdStore<Run> {
 
    public RunIdStore() {
        super(Run.class);
    }

    @Override
    public void remove(Run run) throws IOException {
        List<Action> actionList = run.getActions();
        List<Id> ids = run.getActions(Id.class);
        if (!ids.isEmpty()) {
            actionList.removeAll(ids);
            saveIfNeeded(run);
        }
    }

    @Override
    public String get(Run thing) {
        return Id.getId((Actionable) thing);
    }

    private void saveIfNeeded(Run run) throws IOException {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins is null, so it is impossible to migrate the ID");
        }
        File marker = new File(jenkins.getRootDir(), IdStoreMigratorV1ToV2.MARKER_FILE_NAME);
        if (marker.exists()) {
            // The background migration thread already finished (all builds have been already loaded at least once),
            // so this is a belated load on a run that was not migrated in the main process (for some reason).
            // Let's save it now.
            run.save();
        }
    }
}
