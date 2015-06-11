package org.jenkinsci.plugins.uniqueid.impl;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Item;
import hudson.model.PersistenceRoot;
import hudson.model.Job;
import hudson.model.Run;

import jenkins.model.Jenkins;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.jenkinsci.plugins.uniqueid.implv2.PersistenceRootIdStore;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/** 
 * Converts legacy UniqueIDs that are stored inside a Folder/Job/Run configuration to UniqueIDs that are stored alongside the Folder/Job/Run.
 *
 */
@Restricted(NoExternalUse.class)
@Extension
public class IdStoreMigratorV1ToV2 {
    
    private static Logger LOGGER = Logger.getLogger(IdStoreMigratorV1ToV2.class.getName());

    private static final String MARKER_FILE_NAME = "unique-id-migration.txt";
    
    /**
     * Migrates any IDs stored in Folder/Job/Run configuration 
     * @throws IOException
     */
   
    @Initializer(after=InitMilestone.JOB_LOADED, before=InitMilestone.COMPLETED, fatal=true)
    public static void migrateIdStore() throws IOException {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins is null, so it is impossible to migrate the IDs");
        }
        File marker = new File(jenkins.getRootDir(), MARKER_FILE_NAME);
        if (marker.exists()) {
            LOGGER.log(Level.FINE, "Migration of IDStore already performed, so skipping migration.");
            return;
        }
        LOGGER.log(Level.INFO, "Starting migration of IDs");

        performMigration(jenkins);

    }

    @SuppressWarnings("unchecked")
    static void performMigration(@Nonnull Jenkins jenkins) {
        List<Item> allItems = jenkins.getAllItems();

        for (Item item : allItems) {
            // can only be Folder or Job here (not a run) - and these both implement PersistenceRoot
            if (item instanceof PersistenceRoot) {
                migrate((PersistenceRoot) item);
            }
            else {
                LOGGER.log(Level.WARNING, "Expected item of type Folder or Job which implement PersistenceRoot, but got a {0} so can not migrate the IdStore for this item",
                           item.getClass().getName());
            }
        }
        LOGGER.log(Level.INFO, "migration of unique IDs for Jobs and Folders complete - will continue to process Runs in the background.");

        Thread t = new Thread(new RunIDMigrationThread(), "unique-id background migration thread");
        t.setDaemon(true);
        t.start();
    }

   static void migrate(PersistenceRoot pr) {
       LOGGER.log(Level.FINE, "migrating {0}" , pr);
       try {
            String id = LegacyIdStore.getId(pr);
            if (id != null) {
                PersistenceRootIdStore.create(pr, id);
                LegacyIdStore.removeId(pr);
            }
       } catch (IOException ex) {
           // need to rethrow (but add some context first) otherwise the migration will continue to run
           // and it will not have migrated everything :-(
           throw new IDStoreMigrationException("Failure whilst migrating " + pr.toString(), ex);
       }
    }

   /**
    * Exception to indicate a failure to migrate the IDStore.
    */
   private static class IDStoreMigrationException extends RuntimeException {

       public IDStoreMigrationException(String message, Throwable cause) {
           super(message,cause);
       }
   }
   
    private static class RunIDMigrationThread implements Runnable {

        public void run() {
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                throw new IllegalStateException("Jenkins is null, so it is impossible to migrate the IDs");
            }
            // if new jobs are added that is ok - as their runs will not need to be migrated.
            // if jobs are deleted from Jenkins we need to handle that fact!
            List<Job> allJobs = jenkins.getAllItems(Job.class);
            int totalJobs = allJobs.size();
            int migratedJobs = 0;
            int migratedBuilds = 0;
            final long startTime = System.currentTimeMillis();
            long lastLog = System.currentTimeMillis();
            for (Job job : allJobs) {
                // Force the loading of the builds.
                migratedJobs++;
                if (job.getConfigFile().getFile().exists()) {
                    // we have not been deleted!
                    for (Iterator iterator = job.getBuilds().iterator(); iterator.hasNext();) {
                        // the build is migrated by the action in Id.onLoad(Run)
                        // touch something in the build just to force loading incase it gets more lazy in the future.
                        Object r = iterator.next();
                        if (r != null && r instanceof Run) {
                            ((Run)r).getAllActions();
                        }
                        migratedBuilds++;
                    }
                }
                if ((System.currentTimeMillis() - lastLog) > (60 * 1000L) ) {
                    lastLog = System.currentTimeMillis();
                    LOGGER.log(Level.INFO, "Processed {0} builds,  and have inspected all runs from {1} out of {2} jobs.", 
                               new Object[] {migratedBuilds, migratedJobs, totalJobs});
                }
            }
            // all done...
            final long duration = System.currentTimeMillis() - startTime;
            final long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - startTime);
            final long seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime - TimeUnit.MINUTES.toMillis(minutes));

            LOGGER.log(Level.INFO, "Finished unique-id migration of builds in {0} minutes {1} seconds.  Processed {2} runs from {3} jobs.", 
                       new Object[] {minutes, seconds,  migratedBuilds, migratedJobs});
            File marker = new File(jenkins.getRootDir(), MARKER_FILE_NAME);
            try {
                if (!marker.createNewFile()) {
                    LOGGER.log(Level.WARNING, "Failed to record the completion of the IDStore Migration.  " + 
                                      "This will cause performance issues on subsequent startup.  " + 
                                      "Please create an empty file at '" + marker.getCanonicalPath() + "'");
                }
            }
            catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to record the completion of the IDStore Migration.  " + 
                                                "This will cause performance issues on subsequent startup.  " + 
                                                "Please create an empty file in the Jenkins home directory called  '" + MARKER_FILE_NAME + "'.", ex);
            }
        }
    }

}
