import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.ui.Splitter

import javax.swing.*

import static liveplugin.PluginUtil.registerAction
import static liveplugin.PluginUtil.show

class BalanceSplitters {

    private JPanel rootElement

    BalanceSplitters(JPanel rootElement) {
        this.rootElement = rootElement
    }

    def execute() {
        def splitters = getAllSplitters()
        splitters.each { it.splitter.proportion = 1f / (it.childCount + 1) }
    }

    private traverse(Splitter splitter, ArrayList<Split> list) {
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

    private getAllSplitters() {
        def component = rootElement.components.first()
        def splitters = new ArrayList<Split>()

        if(component instanceof Splitter) {
            traverse((Splitter) component, splitters)
        }

        return splitters
    }
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

registerAction("Balance Splits", "ctrl W, B") { AnActionEvent event ->
    def editorManager = FileEditorManagerEx.getInstanceEx(event.project)
    def panel = (JPanel) editorManager.splitters.components.first()
    def balance = new BalanceSplitters(panel)

    balance.execute()
}

if (!isIdeStartup) show("Loaded 'balanceSplits'<br/>Use ctrl+W, B")
