import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import liveplugin.implementation.ToolWindows
import org.codehaus.groovy.tools.shell.Shell
import org.codehaus.groovy.tools.shell.ShellRunner
import groovy.json.JsonSlurper
import java.util.Random

import static liveplugin.PluginUtil.*

class Robo {

    Map execute(Project project, String command) {
        def getRoboListCommand = "/Users/legge/bin/run-in-folder ${project.basePath} ${command}"
        return execute(getRoboListCommand)
    }

    void listCommands(Project project) {
        def result = this.execute(project, "${project.basePath}/vendor/bin/robo list --format=json")

        if (result.exitCode == 0) {
            def resultText = result["stdout"] as String

            def jsonSlurper = new JsonSlurper()
            def fileList = jsonSlurper.parseText(resultText)

            def popupMenuDescription = [:]

            def commandList = fileList['namespaces'] as ArrayList
            commandList.each {
                def nestedCommandList = it['commands'] as ArrayList
                nestedCommandList.each {
                    def nestedName = it as String
                    popupMenuDescription[nestedName] = {
                        show("Running ${nestedName}")
                        def toolwindow = ToolWindows.findToolWindow('Terminal', project)
                        toolwindow.show(null)

                        def uid = new Random().nextInt() % 600
                        def id = "${nestedName.replaceAll(':', '_')}-${uid}"

                        execute("/usr/local/bin/tmux new-window -n '${id}' -c ${project.basePath}")
                        execute("/usr/local/bin/tmux send -t '${id}' ';./robo \"${nestedName}\"'")
                        execute("/usr/local/bin/tmux send -t '${id}' ';sleep 5'")
                        execute("/usr/local/bin/tmux send -t '${id}' ';exit'")
                        execute("/usr/local/bin/tmux send -t '${id}' ENTER")
                    }
                }
            }

            showPopupMenu(popupMenuDescription, "Robo commands")
        }
    }
}

registerAction("List robo commands", "ctrl alt C", { AnActionEvent event ->
    def robo = new Robo()
    robo.listCommands(event.project)
})



if (!isIdeStartup) show("Loaded 'robo'<br/>Use ctrl+alt+C")
