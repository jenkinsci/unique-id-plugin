package org.jenkinsci.plugins.uniqueid;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Project;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import hudson.model.User;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import com.cloudbees.hudson.plugins.folder.Folder;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class IdTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void project() throws Exception {
        FreeStyleProject p = jenkinsRule.createFreeStyleProject();
        assertNull(IdStore.getId(p));
        IdStore.makeId(p);
        String id = IdStore.getId(p);
        FreeStyleBuild build = jenkinsRule.buildAndAssertSuccess(p);
        
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
    public void user() throws Exception {
        User user = User.get("omgfakeuser", true, Collections.emptyMap());
        user.save();
        String id = IdStore.getId(user);

        assertNotNull(id);

        jenkinsRule.jenkins.reload();
        assertEquals(id, IdStore.getId(Objects.requireNonNull(User.get("omgfakeuser", false, Collections.emptyMap()))));
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
    
    @Test
    @Issue("JENKINS-28913")
    public void correctJenkins28913() throws Exception {
        Project<?, ?> p = jenkinsRule.createFreeStyleProject();
        File f = new File(p.getRootDir(), "unique-id.txt");
        assertThat(f.exists(), is(false));
        
        assertThat("no Id yet made", IdStore.getId(p), nullValue());
        
        assertThat(f.createNewFile(), is(true));
        
        String id = IdStore.getId(p);
        assertThat("id file was empty should have been re-gernerated", id.length(), greaterThan(20));
    }
}
