package jenkins.mvn;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 * @author Dominik Bartholdi (imod)
 * @since 1.491
 */
public abstract class SettingsProvider extends AbstractDescribableImpl<SettingsProvider> implements ExtensionPoint {

    /**
     * <p>
     *     Configure maven launcher argument list with adequate settings path.
     * </p>
     * <p>Implementations should
     * <ul>Be aware that this method might get called multiple times during a build.</ul>
     * <ul>Implement this method. This class provides a default implementation throwing an {@link UnsupportedOperationException}
     * so that implementations have time to adapt.</ul>
     * </p>
     *
     * @param run       the build / run to provide the settings for
     * @param workspace the workspace in which the build / run takes place
     * @param listener the listener of this given build / run
     * @return the filepath to the provided file. <code>null</code> if no settings will be provided.
     */
    @CheckForNull
    public FilePath supplySettings(@Nonnull Run<?, ?> run,  @Nonnull FilePath workspace, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        throw new AbstractMethodError("Class " + getClass() + " must override the new supplySettings overload");
    }

    /**
     * Configure maven launcher argument list with adequate settings path. Implementations should be aware that this method might get called multiple times during a build.
     *
     * @param build
     * @return the filepath to the provided file. <code>null</code> if no settings will be provided.
     * @deprecated use {@link #supplySettings(Run, FilePath, TaskListener)}
     */
    @Deprecated
    public FilePath supplySettings(AbstractBuild<?, ?> build, TaskListener listener) {
        try {
            return supplySettings(build, build.getWorkspace(), listener);
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare Maven settings.xml for " + build +
                    " in workspace " + build.getWorkspace(), e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to prepare Maven settings.xml for " + build +
                    " in workspace " + build.getWorkspace(), e);
        }
    }

    public static SettingsProvider parseSettingsProvider(StaplerRequest req) throws Descriptor.FormException, ServletException {
        JSONObject settings = req.getSubmittedForm().getJSONObject("settings");
        if (settings == null) {
            return new DefaultSettingsProvider();
        }
        return req.bindJSON(SettingsProvider.class, settings);
    }

    /**
     * Convenience method handling all <code>null</code> checks. Provides the path on the (possible) remote settings file.
     * 
     * @param settings
     *            the provider to be used
     * @param build
     *            the active build
     * @param listener
     *            the listener of the current build
     * @return the path to the settings.xml
     * @deprecated directly invoke {@link SettingsProvider#supplySettings(Run, FilePath, TaskListener)}
     */
    public static final FilePath getSettingsFilePath(SettingsProvider settings, AbstractBuild<?, ?> build, TaskListener listener) {
        FilePath settingsPath;
        if (settings == null) {
            settingsPath = null;
        } else {
            try {
                settingsPath = settings.supplySettings(build, build.getWorkspace(), listener);
            } catch (IOException e) {
                throw new RuntimeException("Failed to prepare Maven settings.xml for " + build +
                        " in workspace " + build.getWorkspace(), e);
            } catch (InterruptedException e) {
                throw new RuntimeException("Failed to prepare Maven settings.xml for " + build +
                        " in workspace " + build.getWorkspace(), e);
            }
        }
        return settingsPath;
    }

    /**
     * Convenience method handling all <code>null</code> checks. Provides the path on the (possible) remote settings file.
     * 
     * @param settings
     *            the provider to be used
     * @param build
     *            the active build
     * @param listener
     *            the listener of the current build
     * @return the path to the settings.xml
     * @deprecated directly invoke {@link SettingsProvider#supplySettings(Run, FilePath, TaskListener)}
     */
    @Deprecated
    public static final String getSettingsRemotePath(SettingsProvider settings, AbstractBuild<?, ?> build, TaskListener listener) {
        FilePath fp = getSettingsFilePath(settings, build, listener);
        return fp == null ? null : fp.getRemote();
    }

}
