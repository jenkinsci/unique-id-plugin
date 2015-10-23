package org.jenkinsci.plugins.uniqueid.impl;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Run;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
            IdStoreMigratorV1ToV2.saveIfNeeded(run);
        }
    }

    @Override
    public String get(Run thing) {
        return Id.getId((Actionable) thing);
    }
}
