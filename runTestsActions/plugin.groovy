import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.codehaus.groovy.tools.shell.Shell
import org.codehaus.groovy.tools.shell.ShellRunner

import static liveplugin.PluginUtil.*

interface TestBuilder {
    String build()
}

class FileTestBuilder implements TestBuilder {
    String fileName
    String filePath
    String projectRoot
    Integer lineNumber

    FileTestBuilder(String fileName, String filePath, String projectRoot, int lineNumber) {
        this.fileName = fileName
        this.filePath = filePath
        this.projectRoot = projectRoot
        this.lineNumber = lineNumber
    }

    String build(){
        return "-f ${filePath} -r ${projectRoot} -l ${lineNumber}"
    }
}

class TypeTestBuilder implements TestBuilder {
    String fileType

    TypeTestBuilder(String fileType) {
        this.fileType = fileType
    }

    String build(){
        return "-t ${fileType}"
    }
}

class Test {
    final String commandWrapper = "/usr/local/bin/tmux"
    String description = ""
    TestBuilder testBuilder

    Test(String description, TestBuilder testBuilder) {
        this.description = description
        this.testBuilder = testBuilder
    }

    String getCommand() {
        def prefix = "${commandWrapper} send -t test \"clear; tools/test-this"
        def postfix = "-p\" ENTER"
        def testCommand = testBuilder.build()

        return "${prefix} ${testCommand} ${postfix}"
    }
}

class TestRunner {
    Test lastCommand = null

    void testCurrentFile(Project project) {
        def filePath = currentFileIn(project).canonicalPath
        def fileName = currentFileIn(project).name
        def projectRoot = project.basePath
        def lineNumber = currentEditorIn(project).caretModel.logicalPosition.line

        def test = new Test(fileName, new FileTestBuilder(fileName, filePath, projectRoot, lineNumber))
        runCommand(test)
    }

    void testFilesOfType(String type) {
        def test = new Test(type, new TypeTestBuilder(type))
        runCommand(test)
    }

    void runCommand(Test test) {
        lastCommand = test

        def command = test.command
        show("Testing ${test.description}")
        execute(command)
    }

    void repeatLastTest() {
        if(lastCommand) {
            runCommand(lastCommand)
        } else {
            show "No command has been run before"
        }
    }
}

def testRunner = new TestRunner()

registerAction("Test PHP files", { AnActionEvent event ->
    testRunner.testFilesOfType("PHP")
})

registerAction("Test Javascript files", { AnActionEvent event ->
    testRunner.testFilesOfType("JS")
})

registerAction("Test current context", "ctrl meta R") { AnActionEvent event ->
    testRunner.testCurrentFile(event.project)
}

registerAction("Test Last", "shift meta R") { AnActionEvent event ->
    testRunner.repeatLastTest()
}

if (!isIdeStartup) show("Loaded 'runTestsAction'<br/>Use [ctrl, shift]+meta+R to")
