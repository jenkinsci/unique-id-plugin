package org.jenkinsci.plugins.uniqueid.impl;

import org.jvnet.hudson.test.TestExtension;
import hudson.model.ItemGroup;
import hudson.model.Messages;
import hudson.model.Project;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.Jenkins;

public class LongLoadingProject extends Project<LongLoadingProject, LongLoadingBuild> implements TopLevelItem {
    
    public LongLoadingProject(ItemGroup parent, String name) {
        super(parent, name);
    }

    @Override
    protected Class<LongLoadingBuild> getBuildClass() {
        return LongLoadingBuild.class;
    }

    @TestExtension
    public static final class DescriptorImpl extends TopLevelItemDescriptor {
        public String getDisplayName() {
            return Messages.FreeStyleProject_DisplayName();
        }

        public LongLoadingProject newInstance(ItemGroup parent, String name) {
            return new LongLoadingProject(parent,name);
        }
    }

    public TopLevelItemDescriptor getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(LongLoadingProject.class);
    }
}
