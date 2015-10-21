package org.jenkinsci.plugins.uniqueid.impl;

import jenkins.model.RunAction2;

import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Run;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nullable;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * DO NOT USE
 * @deprecated users should not use this as it ties the ID explicitly to the item and as will not work when copying items (for example). 
 */
@Deprecated
@Restricted(NoExternalUse.class)
class Id implements Action , RunAction2 {

    private final static Logger LOGGER = Logger.getLogger(Id.class.getName());

    private final String id;

    protected Id() {
        this.id = Base64.encodeBase64String(UUID.randomUUID().toString()
                .getBytes(StandardCharsets.UTF_8)).substring(0, 30);
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }

    public String getId() {
        return id;
    }

    
    /**
     * @deprecated Sub classes should not use this as it stores the ID in the actionable item.
     * @return
     */
    @Nullable
    @Deprecated
    protected static String getId(Actionable actionable) {
        Id id = actionable.getAction(Id.class);
        if (id != null) {
            return id.getId();
        } else {
            return null;
        }
    }



    public void onAttached(Run<?, ?> r) {
        // NO-OP
    }

    /**
     * Migrates the run away from using this Action.
     */
    public void onLoad(Run<?, ?> r) {
        IdStoreMigratorV1ToV2.migrate(r);
    }
}
