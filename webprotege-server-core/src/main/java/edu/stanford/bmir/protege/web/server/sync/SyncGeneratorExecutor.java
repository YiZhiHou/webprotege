package edu.stanford.bmir.protege.web.server.sync;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * sync
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface SyncGeneratorExecutor {

}
