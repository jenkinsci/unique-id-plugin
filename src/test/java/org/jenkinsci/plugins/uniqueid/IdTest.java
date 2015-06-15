package org.jenkinsci.plugins.uniqueid;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Project;

import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.cloudbees.hudson.plugins.folder.Folder;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class IdTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void project() throws Exception {
        Project p = jenkinsRule.createFreeStyleProject();
        assertNull(IdStore.getId(p));
        IdStore.makeId(p);
        String id = IdStore.getId(p);
        AbstractBuild build = jenkinsRule.buildAndAssertSuccess(p);
        
        // to be unique we need not null and at least a minimum size.
        assertThat("project id", id, notNullValue());
        assertThat("project id", id.length(), greaterThan(20));

        // a build will get an id computed from its parent.
        String buildId = IdStore.getId(build);
        assertEquals(buildId, id+'_'+build.getId());

        // should be a no-op
        IdStore.makeId(build);
        assertEquals(IdStore.getId(build), buildId);

        jenkinsRule.jenkins.reload();

        AbstractProject resurrectedProject = jenkinsRule.jenkins.getItemByFullName(p.getFullName(), AbstractProject.class);
        assertEquals(id, IdStore.getId(resurrectedProject));
        assertEquals(buildId, IdStore.getId(resurrectedProject.getBuild(build.getId())));
    }

    @Test
    public void folder() throws Exception {
        Folder f = jenkinsRule.jenkins.createProject(Folder.class,"folder");
        assertNull(IdStore.getId(f));
        IdStore.makeId(f);
        String id = IdStore.getId(f);
        
        // to be unique we need not null and at least a minimum size.
        assertThat("folder id", id, notNullValue());
        assertThat("folder id", id.length(), greaterThan(20));

        jenkinsRule.jenkins.reload();
        assertEquals(id, IdStore.getId(jenkinsRule.jenkins.getItemByFullName("folder", Folder.class)));
    }
    
    @Test
    public void uniqueness() throws Exception {
        Set<String> ids = new HashSet<String>();
        for (int i=0;i<100;i++) {
            Project p = jenkinsRule.createFreeStyleProject();
            IdStore.makeId(p);
            ids.add(IdStore.getId(p));
            
            Folder f = jenkinsRule.jenkins.createProject(Folder.class, "Folder"+i);
            IdStore.makeId(f);
            ids.add(IdStore.getId(f));
        }
        assertThat(ids, hasSize(200));
    }
}
