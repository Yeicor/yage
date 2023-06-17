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
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiTextInput
import korlibs.korge.view.SContainer
import korlibs.korge.view.image
import korlibs.korge.view.onStageResized
import korlibs.logger.Logger
import korlibs.math.geom.Anchor
import korlibs.math.geom.Scale
import korlibs.math.geom.ScaleMode
import korlibs.math.geom.Size
import kotlin.math.min

suspend fun main() =
    Korge(
        title = "Yet Another Graphviz Editor",
        backgroundColor = Colors["#2b2b2b"],
        clipBorders = false,
        scaleMode = ScaleMode.NO_SCALE,
    ) { sceneContainer().changeTo({ MyScene("digraph{a->b}") }) }

class MyScene(private val initialSource: String) : Scene() {
    private val logger = Logger("MyScene")

    @OptIn(KorgeExperimental::class)
    override suspend fun SContainer.sceneInit() {
        val uiTextInput = uiTextInput(initialSource) {
            onStageResized { _, _ ->
                x = views.actualVirtualBounds.left
                y = views.actualVirtualBounds.top
                width = views.actualVirtualBounds.width
                height = 32f
            }
        }
        var uiButton: UIButton? = null
        uiButton = uiButton("RENDER") {
            onStageResized { _, _ ->
                x = views.actualVirtualBounds.left
                y = uiTextInput.y + uiTextInput.height
                width = views.actualVirtualBounds.width
                height = 32f
            }
            onClick { this@sceneInit.renderSvg(uiTextInput.text, uiButton!!) }
        }
    }

    private fun SContainer.renderSvg(dotStr: String, uiButton: UIButton) {
        // TODO: Dispatch on worker thread (blocking operation)
        logger.info { "renderSvg: $dotStr" }
        val svgStr = Kraphviz.render(dotStr)
        //svgStr = svgStr.replace("font-family=\"[^\"]*\" *".toRegex(), "")
        logger.info { "svgStr: $svgStr" }
        val newSvg = SVG(Xml(svgStr))
        val newImage = image(newSvg.render(), anchor = Anchor.CENTER) {
            width / height
            onStageResized { _, _ ->
                val uiButtonBottomY = (uiButton.y + uiButton.height)
                val availableX = views.actualVirtualBounds.width
                val availableY = views.actualVirtualBounds.height - uiButtonBottomY
                val newScale = min(availableX / newSvg.width, availableY / newSvg.height)
                //logger.warn { "onStageResized: $availableX, $availableY => newScale $newScale" }
                scale = Scale(newScale, newScale)
                x = views.actualVirtualBounds.left + availableX / 2
                y = uiButtonBottomY + availableY / 2
                invalidate()
                invalidateLocalBounds()
                invalidateRender()
            }
        }
        if (this.children.size > 2) this.removeChildAt(2)
        this.addChild(newImage)
    }

    override fun onSizeChanged(size: Size) {
        logger.warn { "onSizeChanged: $size" }
    }
}
