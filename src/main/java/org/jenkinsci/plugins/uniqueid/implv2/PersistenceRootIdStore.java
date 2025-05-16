package org.jenkinsci.plugins.uniqueid.implv2;

import hudson.Extension;
import hudson.model.PersistenceRoot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.uniqueid.IdStore;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;


/**
 * The {@link PersistenceRootIdStore} allows the storing of a Unique ID for any PersistenceRoot item.
 */
@Extension
public class PersistenceRootIdStore extends IdStore<PersistenceRoot> {

    /** Our Logger. */
    private final static Logger LOGGER = Logger.getLogger(PersistenceRootIdStore.class.getName());

    public PersistenceRootIdStore() {
        super(PersistenceRoot.class);
    }

    @Override
    public void make(PersistenceRoot object) {
        File f = new File(object.getRootDir(), IdStore.fileName());
        if (!f.exists()) {
            File tmp = null;
            try {
                tmp = File.createTempFile(".unique-id_", ".tmp", object.getRootDir());
                FileUtils.writeStringToFile(tmp, IdStore.generateUniqueID(), StandardCharsets.UTF_8);
                try {
                    Files.move(tmp.toPath(), f.toPath(), StandardCopyOption.ATOMIC_MOVE);
                }
                catch (FileAlreadyExistsException ignored) {
                    FileUtils.deleteQuietly(tmp);
                    return; // we already have an id.
                }
            } 
            catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to store unique ID for " + object.toString(), ex);
            }
        }
    }

    @Override
    public String get(PersistenceRoot object) {
        File f = new File(object.getRootDir(), IdStore.fileName());
        if (f.exists() && f.canRead()) {
            try {
                String str = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
                if (str.length() == 0) {
                    // regenerate it JENKINS-28913
                    Files.deleteIfExists(f.toPath());
                    make(object);
                    str = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
                }
                return str;
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to retrieve unique ID for " + object.toString(), ex);
            }
        }
        return null;
    }

    @Restricted(NoExternalUse.class)
    public static void create(PersistenceRoot object, String uniqueId) throws IOException {
        File f = new File(object.getRootDir(), IdStore.fileName());
        if (!f.exists()) {
            LOGGER.log(Level.FINE, "Creating file ({1}) to store ID for ({0}) whose RootDir is ({2}).", new Object[] {object.toString(), f, object.getRootDir()});
            // no need to migrate if its there to begin with!
            FileUtils.writeStringToFile(f, uniqueId, StandardCharsets.UTF_8);
        }
        else {
            LOGGER.log(Level.FINE, "**NOT** creating file ({1}) to store ID for ({0}) whose RootDir is ({2}).", new Object[] {object.toString(), f, object.getRootDir()});
        }
    }

}
