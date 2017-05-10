package org.jenkinsci.gradle.plugins.jpi

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class UpdateServerTaskSpec extends Specification {
    public static final String BASE_PROJECT = """
            plugins {
                id 'org.jenkins-ci.jpi'
            }

            jenkinsPlugin {
                coreVersion = '1.509.3'
            }
        """
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    Project project = ProjectBuilder.builder().build()

    def 'jenkins home contains developed plugin'() {
        setup:
        prepareProject(BASE_PROJECT)
        def projectDir = new File(temporaryFolder.root, 'test')
        when:
        updateServer(projectDir)

        then:
        new File(projectDir, 'work/plugins/test.hpl').exists()
    }

    def 'jenkins home contains dependencies'() {
        setup:
        prepareProject(BASE_PROJECT, ['jenkinsPlugins' : 'org.jenkins-ci.plugins:random-string-parameter:1.0'])
        def projectDir = new File(temporaryFolder.root, 'test')
        when:
        updateServer(projectDir)

        then:
        new File(projectDir, 'work/plugins/test.hpl').exists()
        new File(projectDir, 'work/plugins/random-string-parameter.hpi').exists()
    }

    def 'jenkins home contains optional dependencies'() {
        setup:
        prepareProject(BASE_PROJECT, ['optionalJenkinsPlugins' : 'org.jenkins-ci.plugins:random-string-parameter:1.0'])
        def projectDir = new File(temporaryFolder.root, 'test')
        when:
        updateServer(projectDir)

        then:
        new File(projectDir, 'work/plugins/test.hpl').exists()
        new File(projectDir, 'work/plugins/random-string-parameter.hpi').exists()
    }

    def 'jenkins home contains test dependencies'() {
        setup:
        prepareProject(BASE_PROJECT, ['jenkinsTest' : 'org.jenkins-ci.plugins:random-string-parameter:1.0'])
        def projectDir = new File(temporaryFolder.root, 'test')
        when:
        updateServer(projectDir)

        then:
        new File(projectDir, 'work/plugins/test.hpl').exists()
        new File(projectDir, 'work/plugins/random-string-parameter.hpi').exists()
    }

    def 'jenkins does not contains test dependencies when disabled'() {
        setup:
        prepareProject(BASE_PROJECT + """
            updateServer {
                includeTest = false
            }
        """,
        ['jenkinsTest' : 'org.jenkins-ci.plugins:random-string-parameter:1.0'])
        def projectDir = new File(temporaryFolder.root, 'test')
        when:
        updateServer(projectDir)

        then:
        new File(projectDir, 'work/plugins/test.hpl').exists()
        !new File(projectDir, 'work/plugins/random-string-parameter.hpi').exists()
    }



    private void updateServer(projectDir) {
        GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments('updateServer')
                .build()
    }

    private void prepareProject(project, dependencies = [:]) {
        if (!dependencies.empty) {
            project += "dependencies {\n"
            dependencies.each{ k, v -> project += "$k '$v'\n" }
            project += "}"
        }
        temporaryFolder.newFolder('test', 'src', 'main', 'java')
        temporaryFolder.newFile('test/build.gradle') << project
        temporaryFolder.newFile('test/src/main/java/TestPlugin.java') << 'class TestPlugin extends hudson.Plugin {}'
    }
}