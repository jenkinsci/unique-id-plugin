/*
 * The MIT License
 *
 * Copyright (c) 2015, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
