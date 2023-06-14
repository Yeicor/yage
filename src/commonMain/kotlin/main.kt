import com.github.yeicor.kraphviz.Kraphviz
import korlibs.image.color.Colors
import korlibs.image.vector.format.SVG
import korlibs.image.vector.render
import korlibs.io.serialization.xml.Xml
import korlibs.korge.Korge
import korlibs.korge.annotations.KorgeExperimental
import korlibs.korge.input.onClick
import korlibs.korge.scene.Scene
import korlibs.korge.scene.sceneContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiTextInput
import korlibs.korge.ui.uiVerticalFill
import korlibs.korge.view.SContainer
import korlibs.korge.view.image
import korlibs.math.geom.Size

suspend fun main() =
    Korge(title = "Yet Another Graphviz Editor", windowSize = Size(512, 512), backgroundColor = Colors["#2b2b2b"]) {
        val sceneContainer = sceneContainer()

        sceneContainer.changeTo({ MyScene("digraph{a->b}", null) })
    }

class MyScene(private val initialSource: String, private val svg: SVG?) : Scene() {

    @OptIn(KorgeExperimental::class)
    override suspend fun SContainer.sceneMain() {
        uiVerticalFill(parent!!.size) {
            val uiTextInput = uiTextInput(initialSource, size = Size(512f, 32f))
            uiButton("RENDER", size = Size(512f, 32f)) {
                onClick { renderSvg(uiTextInput.text) }
            }
            svg?.let { svg ->
                uiButton("EXPORT TO CONSOLE") { onClick { println(svg.root.outerXml) } }
                image(svg.render())
            }
        }
    }

    private suspend fun renderSvg(dotStr: String) {
        println("renderSvg: $dotStr")
        val renderedSvg = Kraphviz.render(dotStr)
        val newSvg = SVG(Xml(renderedSvg))
        sceneContainer.changeTo({ MyScene(dotStr, newSvg) })
    }
}
