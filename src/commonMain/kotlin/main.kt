import com.github.yeicor.kraphviz.Kraphviz
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.image.vector.format.SVG
import korlibs.image.vector.render
import korlibs.io.serialization.xml.Xml
import korlibs.korge.Korge
import korlibs.korge.annotations.KorgeExperimental
import korlibs.korge.input.onClick
import korlibs.korge.scene.Scene
import korlibs.korge.scene.delay
import korlibs.korge.scene.sceneContainer
import korlibs.korge.ui.*
import korlibs.korge.view.SContainer
import korlibs.korge.view.image
import korlibs.korge.view.onStageResized
import korlibs.logger.Logger
import korlibs.math.geom.Anchor
import korlibs.math.geom.Scale
import korlibs.math.geom.ScaleMode
import korlibs.math.geom.Size
import korlibs.render.openFileDialog
import korlibs.time.TimeSpan
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.math.min

suspend fun main() =
    Korge(
        title = "Yet Another Graphviz Editor",
        backgroundColor = Colors["#2b2b2b"],
        clipBorders = false,
        scaleMode = ScaleMode.NO_SCALE,
    ) { sceneContainer().changeTo({ MyScene(DEFAULT_SOURCE) }) }

class MyScene(private val initialSource: String) : Scene() {
    private val logger = Logger("MyScene")

    @OptIn(KorgeExperimental::class)
    override suspend fun SContainer.sceneInit() {
        var uiTextInput: UITextInput? = null
        var uiButton: UIButton? = null
        var uiHorizontalStack2: UIHorizontalStack? = null
        val uiHorizontalStack1 = uiHorizontalStack {
            uiTextInput = uiTextInput(initialSource) {
                onStageResized { _, _ ->
                    width = views.actualVirtualBounds.width - 64f
                }
            }
            uiButton("LOAD") {
                width = 64f
                onClick {
                    val file = gameWindow.openFileDialog()
                    file.lastOrNull()?.readString()?.let {
                        uiTextInput!!.text = it
                        uiButton?.simulateClick(views)
                    }
                }
            }
            onStageResized { _, _ ->
                x = views.actualVirtualBounds.left
                y = views.actualVirtualBounds.top
                width = views.actualVirtualBounds.width
                height = 32f
            }
        }
        uiHorizontalStack2 = uiHorizontalStack {
            onStageResized { _, _ ->
                x = views.actualVirtualBounds.left
                y = uiHorizontalStack1.y + uiHorizontalStack1.height
                width = views.actualVirtualBounds.width
                height = 32f
            }
            uiButton = uiButton("RENDER") {
                onStageResized { _, _ ->
                    width = views.actualVirtualBounds.width - 64f
                }
                onClick { this@sceneInit.renderSvg(uiTextInput!!.text, uiHorizontalStack2!!).await() }
            }
            uiButton("SAVE") {
                width = 64f
                onClick {
                    val svgStr = this@sceneInit.renderSvg(uiTextInput!!.text, uiHorizontalStack2!!).await()
                    val file = gameWindow.openFileDialog(filter = "svg", write = true)
                    file.lastOrNull()?.writeString(svgStr)
                }
            }
        }
        //setImage(resourcesVfs["korge.png"].readBitmap(), uiButton)
        setImage(svgRender(RENDERING_SVG.replace("Rendering", "No graph yet")), uiButton!!)
    }

    private suspend fun SContainer.renderSvg(dotStr: String, uiView: UIView): Deferred<String> {
        setImage(svgRender(RENDERING_SVG), uiView)
        return async {
            delay(TimeSpan(100.0)) // "Ensure" the UI is updated before blocking
            logger.info { "renderSvg: $dotStr" }
            val svgStr = Kraphviz().render(dotStr)
            logger.info { "svgStr: $svgStr" }
            setImage(svgRender(svgStr), uiView)
            return@async svgStr
        }
    }

    private fun SContainer.setImage(
        texture: Bitmap,
        uiView: UIView,
    ) {
        val newImage = image(texture, anchor = Anchor.CENTER) {
            width / height
            onStageResized { _, _ ->
                val offsetY = uiView.y + uiView.height - views.actualVirtualBounds.top
                val availableX = views.actualVirtualBounds.width
                val availableY = views.actualVirtualBounds.height - offsetY
                val scaleVal = min(availableX / texture.width, availableY / texture.height)
                logger.warn { "setImage: $availableX x $availableY, $scale | $offsetY" }
                scale = Scale(scaleVal, scaleVal)
                x = views.actualVirtualBounds.left + availableX / 2
                y = views.actualVirtualBounds.top + offsetY + availableY / 2
                invalidate()
                invalidateRender()
                invalidateLocalBounds()
            }
        }
        if (this.children.size > 2) this.removeChildAt(2)
        this.addChild(newImage)
    }

    override fun onSizeChanged(size: Size) {
        logger.warn { "onSizeChanged: $size" }
    }
}

/** An almost-working plaform-independent SVG to PNG renderer. Waiting for better support from KorGE... */
@Suppress("unused")
internal fun svgRenderDefault(svgStr: String): Bitmap = SVG(Xml(svgStr)).render()

expect suspend fun svgRender(svgStr: String): Bitmap

private const val DEFAULT_SOURCE =
    """digraph G {bgcolor="#0000FF44:#FF000044" gradientangle=90 subgraph cluster_0 { style=filled; color=lightgrey; fillcolor="darkgray:gold"; gradientangle=0 node [fillcolor="yellow:green" style=filled gradientangle=270] a0; node [fillcolor="lightgreen:red"] a1; node [fillcolor="lightskyblue:darkcyan"] a2; node [fillcolor="cyan:lightslateblue"] a3; a0 -> a1 -> a2 -> a3; label = "process #1"; } subgraph cluster_1 { node [fillcolor="yellow:magenta" style=filled gradientangle=270] b0; node [fillcolor="violet:darkcyan"] b1; node [fillcolor="peachpuff:red"] b2; node [fillcolor="mediumpurple:purple"] b3; b0 -> b1 -> b2 -> b3; label = "process #2"; color=blue fillcolor="darkgray:gold"; gradientangle=0 style=filled; } start -> a0; start -> b0; a1 -> b3; b2 -> a3; a3 -> a0; a3 -> end; b3 -> end; start [shape=Mdiamond , fillcolor="pink:red", gradientangle=90, style=radial]; end [shape=Msquare, fillcolor="lightyellow:orange", style=radial, gradientangle=90]; }"""

private const val RENDERING_SVG =
    """<svg width="147.07mm" height="25.4mm" inkscape:version="1.2.2 (b0a8486541, 2022-12-01)" sodipodi:docname="text.svg" version="1.1" viewBox="0 0 147.07 25.4" xmlns="http://www.w3.org/2000/svg" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"><sodipodi:namedview bordercolor="#eeeeee" borderopacity="1" inkscape:current-layer="layer1" inkscape:cx="299.13714" inkscape:cy="287.80619" inkscape:deskcolor="#505050" inkscape:document-units="mm" inkscape:pagecheckerboard="0" inkscape:pageopacity="0" inkscape:showpageshadow="0" inkscape:window-height="1416" inkscape:window-maximized="1" inkscape:window-width="3440" inkscape:window-x="1920" inkscape:window-y="0" inkscape:zoom="0.66190376" pagecolor="#505050" showgrid="false"/><g transform="translate(-25.648 -72.234)" inkscape:groupmode="layer" inkscape:label="Layer 1"><rect x="21.251" y="69.037" width="153.1" height="31.179" fill="#fff" stroke="#000" stroke-width="1.5" style="paint-order:stroke fill markers"/><text x="23.184387" y="91.538353" fill="#000000" font-family="sans-serif" font-size="25.4px" stroke-width=".26458" style="line-height:1.25" xml:space="preserve"><tspan x="23.184387" y="91.538353" font-size="25.4px" stroke-width=".26458" sodipodi:role="line">Rendering...</tspan></text></g></svg>"""