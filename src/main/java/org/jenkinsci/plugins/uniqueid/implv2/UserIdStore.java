package org.jenkinsci.plugins.uniqueid.implv2;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.PersistenceRoot;
import hudson.model.Run;
import hudson.model.User;
import org.jenkinsci.plugins.uniqueid.IdStore;

/**
 * Manages Unique IDs for User.
 * We could make a unique file for every user. But a user already has a jenkins wide unique id
 * that we can just tap into
 */
@Extension(ordinal=1) // needs to take priority over the PersistenceRootIdStore
public class UserIdStore extends IdStore<User> {
    public UserIdStore() {
        super(User.class);
    }

    @Override
    public void make(User user) {
        // do nothing cause we already have a an id
    }

    @Override
    public String get(User user) {
        return user.getId();
    }

}
