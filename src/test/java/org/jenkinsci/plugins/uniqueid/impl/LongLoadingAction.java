package org.jenkinsci.plugins.uniqueid.impl;

import hudson.model.Run;
import jenkins.model.RunAction2;

public class LongLoadingAction implements RunAction2 {

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }

    public void onAttached(Run<?, ?> r) {
    }

    public void onLoad(Run<?, ?> r) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
