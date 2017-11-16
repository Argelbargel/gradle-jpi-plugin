package org.jenkinsci.gradle.plugins.jpi

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME

class TestInsertionTask extends DefaultTask {
    public static final String TASK_NAME = 'insertTest'

    @OutputFile
    File testSuite

    @TaskAction
    void generateInjectedTest() {
        JpiExtension jpiExtension = project.extensions.getByType(JpiExtension)
        JavaPluginConvention javaConvention = project.convention.getPlugin(JavaPluginConvention)
        SourceSet mainSourceSet = javaConvention.sourceSets.getByName(MAIN_SOURCE_SET_NAME)
        SourceSet testSourceSet = javaConvention.sourceSets.getByName(TEST_SOURCE_SET_NAME)

        testSuite.parentFile.mkdirs()
        testSuite.text = """import java.util.HashMap;
import junit.framework.Test;
import junit.framework.TestCase;
import org.jvnet.hudson.test.PluginAutomaticTestBuilder;

/**
 * Entry point to auto-generated tests (generated by gradle-jpi-plugin).
 */
public class ${jpiExtension.injectedTestName} extends TestCase {
    public static Test suite() throws Exception {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("basedir", ${quote(project.projectDir.absolutePath)});
        parameters.put("artifactId", ${quote(jpiExtension.shortName)});
        parameters.put("outputDirectory", ${quote(mainSourceSet.output.resourcesDir.absolutePath)});
        parameters.put("testOutputDirectory", ${quote(testSourceSet.output.resourcesDir.absolutePath)});
        parameters.put("packaging", ${quote(jpiExtension.fileExtension)});
        parameters.put("requirePI", ${quote(String.valueOf(jpiExtension.requirePI))});
        return PluginAutomaticTestBuilder.build(parameters);
    }
}
"""
    }

    private static String quote(String s) {
        "\"${s.replace('\\', '\\\\')}\""
    }
}
