import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import groovy.json.JsonSlurper

import static liveplugin.PluginUtil.*

class BundleResolver {

    void resolveCurrentFile(Project project) {
        def jsonSlurper = new JsonSlurper()

        def filePath = currentFileIn(project).canonicalPath

        def command = "/bin/bash -c 'cd \"${project.basePath}\" && tools/getFileBundles.py -e json ${filePath}'"
        def result = execute(command)

        if (result.exitCode == 0) {
            def resultText = result["stdout"] as String
            def fileList = jsonSlurper.parseText(resultText) as ArrayList

            if(fileList.size() == 0)
                return

            def popupMenuDescription = ["default": {}]
            popupMenuDescription.remove("default")

            fileList.each {
                def relativePath = it["path"] as String
                def name = relativePath
                def absolutePath = project.basePath + "/" + relativePath
                popupMenuDescription[name] = {
                    openInEditor(absolutePath, project)
                }
            }


            showPopupMenu(popupMenuDescription, "Bundles containing current file")
        }
    }
}

def bundleResolver = new BundleResolver()

registerAction("Show bundles for file", "ctrl meta B", { AnActionEvent event ->
    bundleResolver.resolveCurrentFile(event.project)
})

if (!isIdeStartup) show("Loaded 'showBundles'<br/>Use shift+meta+B")