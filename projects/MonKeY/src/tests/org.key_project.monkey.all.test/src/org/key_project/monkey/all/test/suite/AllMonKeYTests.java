package org.key_project.monkey.all.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.key_project.key4eclipse.starter.core.test.suite.AllStarterCoreTests;
import org.key_project.key4eclipse.test.suite.AllKeY4EclipseTests;
import org.key_project.monkey.product.ui.test.suite.AllMonKeYProductUITests;
import org.key_project.util.test.suite.AllUtilTests;

/**
 * Run all contained JUnit 4 test cases.
 * @author Martin Hentschel
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AllKeY4EclipseTests.class,
    AllUtilTests.class,
    AllStarterCoreTests.class,
    AllMonKeYProductUITests.class
})
public class AllMonKeYTests {
}