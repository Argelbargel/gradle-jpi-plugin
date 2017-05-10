package org.jenkinsci.gradle.plugins.jpi

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.util.GFileUtils

class UpdateServerTask extends DefaultTask {
    public static final String TASK_NAME = 'updateServer'

    @Input includeTest = true

    @TaskAction
    def start() {
        generateHpl()
        copyPluginDependencies()
    }

    void generateHpl() {
        def m = new JpiHplManifest(project)
        def conv = project.extensions.getByType(JpiExtension)

        def hpl = new File(conv.workDir, "plugins/${conv.shortName}.hpl")
        hpl.parentFile.mkdirs()
        hpl.withOutputStream { m.write(it) }
    }

    private copyPluginDependencies() {
        // create new configuration with plugin dependencies, ignoring the (jar) extension to get the HPI/JPI files
        Configuration plugins = project.configurations.create('plugins')
        project.configurations.getByName(JpiPlugin.PLUGINS_DEPENDENCY_CONFIGURATION_NAME).dependencies.each {
            project.dependencies.add(plugins.name, "${it.group}:${it.name}:${it.version}")
        }

        project.configurations.getByName(JpiPlugin.OPTIONAL_PLUGINS_DEPENDENCY_CONFIGURATION_NAME).dependencies.each {
            project.dependencies.add(plugins.name, "${it.group}:${it.name}:${it.version}")
        }

        if (includeTest) {
            project.configurations.getByName(JpiPlugin.JENKINS_TEST_DEPENDENCY_CONFIGURATION_NAME).dependencies.each {
                project.dependencies.add(plugins.name, "${it.group}:${it.name}:${it.version}")
            }
        }

        // copy the resolved HPI/JPI files to the plugins directory
        def workDir = project.extensions.getByType(JpiExtension).workDir
        plugins.resolvedConfiguration.resolvedArtifacts.findAll { it.extension in ['hpi', 'jpi'] }.each {
            GFileUtils.copyFile(it.file, new File(workDir, "plugins/${it.name}.${it.extension}"))
        }
    }
}
