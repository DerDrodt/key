package org.key_project.key4eclipse.util.eclipse.job;

import org.eclipse.core.runtime.jobs.Job;

import de.uka.ilkd.key.gui.MainWindow;

/**
 * <p>
 * Provides a basic implementation of {@link Job}s which does something
 * with the {@link MainWindow} of KeY.
 * </p>
 * <p>
 * It is not possible to execute multiple {@link AbstractKeYMainWindowJob}
 * instances at the same time. It is ensured thanks to an
 * {@link ObjectchedulingRule} that uses the class of {@link MainWindow}
 * as conflicting {@link Object}.
 * </p>
 * @author Martin Hentschel
 */
public abstract class AbstractKeYMainWindowJob extends Job {
   /**
    * Constructor.
    * @param name The name of this {@link Job}.
    */
   public AbstractKeYMainWindowJob(String name) {
      super(name);
      setRule(new ObjectchedulingRule(MainWindow.class));
   }
}