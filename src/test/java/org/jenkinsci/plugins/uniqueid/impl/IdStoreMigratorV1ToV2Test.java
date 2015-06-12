package org.jenkinsci.plugins.uniqueid.impl;

import hudson.model.ItemGroup;
import hudson.model.PersistenceRoot;
import hudson.model.Job;
import hudson.model.Run;

import jenkins.model.Jenkins;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.uniqueid.IdStore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.Extension;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import java.io.IOException;
import jenkins.model.AbstractTopLevelItem;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class IdStoreMigratorV1ToV2Test {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    @Issue("JENKINS-28843")
    @LocalData
    public void testMigration() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        assertThat("All jobs loaded correctly", jenkins.getAllItems(), hasSize(4));
        
        Folder folderNoID = jenkins.getItem("folderNoID", jenkins, Folder.class);
        Folder folderWithID = jenkins.getItem("folderWithID", jenkins, Folder.class);
        
        Job jobNoID = jenkins.getItem("jobNoID", (ItemGroup)folderWithID, Job.class);
        Job jobWithID = jenkins.getItem("jobWithID", (ItemGroup)folderWithID, Job.class);
        
        assertThat(folderNoID, notNullValue());
        assertThat(folderWithID, notNullValue());
        assertThat(jobNoID, notNullValue());
        assertThat(jobWithID, notNullValue());
        
        checkID(folderNoID, null);
        checkID(folderWithID, "YzUxN2JiZTYtNGVhZS00NDQxLTg5NT");
        
        checkID(jobNoID, null);
        checkID(jobWithID, "ZGQxMDNhYzUtMTJlOC00YTc4LTgzOT");
        

        checkID(jobNoID.getBuildByNumber(1), null);
        checkID(jobNoID.getBuildByNumber(2), null);
        
        // build 1 had no id so its generated on the fly from the parent
        checkID(jobWithID.getBuildByNumber(1), "ZGQxMDNhYzUtMTJlOC00YTc4LTgzOT_" + jobWithID.getBuildByNumber(1).getId()); 
        checkID(jobWithID.getBuildByNumber(2), "NGQ0ODM2NjktZGM0OS00MjdkLWE3NT");
    }
    
    @Test
    @Issue("JENKINS-28883")
    public void migrateUnsupportedType() throws Exception {
        // Add new item, which is not supported by LegacyIdStore
        jenkinsRule.jenkins.putItem(new PersistedRootItem(jenkinsRule.jenkins, "Hello"));
        
        // Restart and perform migration w/o checking "already migrated" tokens, there should not be failures
        IdStoreMigratorV1ToV2.performMigration(jenkinsRule.jenkins);
    }

    private static void checkID(PersistenceRoot obj, String expectedID) throws Exception {
        assertThat("Checking " + obj.toString(), IdStore.getId(obj), is(expectedID));
        if (expectedID != null) {
            File f = new File(obj.getRootDir(), "config.xml");
            
            String string = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
            // main config should not contain a reference to the unique ID any more.
            assertThat("config.xml for " + obj.toString() + " still contains the ID", string, not(containsString(expectedID)));
        }
    }

    private static void checkID(Run obj, String expectedID) throws Exception {
        assertThat("Checking " + obj.toString(), IdStore.getId(obj), is(expectedID));
        if (expectedID != null) {
            File f = new File(obj.getRootDir(), "build.xml");
            
            String string = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
            // main config should not contain a reference to the unique ID any more.
            assertThat("build.xml for " + obj.toString() + " still contains the ID", string, not(containsString(expectedID)));
        }
    } 
    
    public static class PersistedRootItem extends AbstractTopLevelItem {
        
        public PersistedRootItem(ItemGroup parent, String name) {
            super(parent, name);
        }
        
        @Extension
        public static class DescriptorImpl extends TopLevelItemDescriptor {

            @Override
            public String getDisplayName() {
                return "Stub item";
            }

            @Override
            public TopLevelItem newInstance(ItemGroup parent, String name) {
                return new PersistedRootItem(parent, name);
            }
            
        }
    }
}
