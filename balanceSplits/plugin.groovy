import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.ui.Splitter

import javax.swing.JComponent
import javax.swing.JPanel

import static liveplugin.PluginUtil.*

def editorManager = FileEditorManagerEx.getInstanceEx(project)
def panel = (JPanel) editorManager.splitters.components.first()
def splitters = findAllSplitters(panel)

show splitters
splitters.each { it.splitter.proportion = 1f / (it.childCount + 1) }

static traverse(Splitter splitter, ArrayList<Split> list) {
    def vCount = 0
    def hCount = 0

    def first = splitter.firstComponent.components.first()
    if(first instanceof Splitter) {
        def (cvCount, chCount) = traverse((Splitter) first, list)
        vCount += cvCount
        hCount += chCount
    }

    def second = splitter.secondComponent.components.first()
    if(second instanceof Splitter) {
        def (cvCount, chCount) = traverse((Splitter) second, list)
        vCount += cvCount
        hCount += chCount
    }

    def childCount
    if(splitter.vertical) {
        vCount++
        childCount = vCount
    } else {
        hCount++
        childCount = hCount
    }

    list.add(new Split(splitter, childCount))
    return [vCount, hCount]
}

static findAllSplitters(JPanel panel) {
    def component = panel.components.first()
    def splitters = new ArrayList<Split>()

    if(component instanceof Splitter) {
        traverse((Splitter) component, splitters)
    }

    return splitters
}

class Split {
    Splitter splitter
    int childCount

    Split(Splitter splitter, int childCount) {
        this.splitter = splitter
        this.childCount = childCount
    }

    String toString() {
        return "Split{children: ${childCount}"
    }
}