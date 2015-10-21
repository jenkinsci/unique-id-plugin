package org.jenkinsci.plugins.uniqueid.impl;

import java.io.File;
import java.io.IOException;

import hudson.model.Build;

public class LongLoadingBuild extends Build<LongLoadingProject, LongLoadingBuild> {

    private String check = null;

    public LongLoadingBuild(LongLoadingProject project) throws IOException {
        super(project);
    }

    public LongLoadingBuild(LongLoadingProject project, File buildDir) throws IOException {
        super(project, buildDir);
    }

    @Override
    public synchronized void save() throws IOException {
        // Will produce a null pointer exception if called before onLoad finished.
        check.toString();
        super.save();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        check = "Checked!";
    }

    @Override
    public void run() {
    }

}
