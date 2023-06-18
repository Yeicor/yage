import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.io.async.runBlockingNoSuspensions
import korlibs.korge.input.onClick
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.tween.get
import korlibs.korge.tween.tween
import korlibs.korge.view.solidRect
import korlibs.math.geom.Rectangle
import korlibs.time.seconds
import kotlin.test.Test
import kotlin.test.assertEquals

class MyTest : ViewsForTesting() {
    @Test
    fun test() = viewsTest {
        val log = arrayListOf<String>()
        val rect = solidRect(100, 100, Colors.RED)
        rect.onClick {
            log += "clicked"
        }
        assertEquals(1, views.stage.numChildren)
        rect.simulateClick()
        assertEquals(true, rect.isVisibleToUser())
        tween(rect::x[-102], time = 10.seconds)
        assertEquals(Rectangle(x = -102, y = 0, width = 100, height = 100), rect.globalBounds)
        assertEquals(false, rect.isVisibleToUser())
        assertEquals(listOf("clicked"), log)
    }

    @Test
    fun svg2PngRender() = runBlockingNoSuspensions {
        val texture = svgRender(
            """
            <svg width="100" height="100" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                <rect x="0" y="0" width="100" height="100" fill="red"/>
            </svg>
            """.trimIndent()
        )
        println("Rendered: $texture")
        require(texture.getRgba(0, 0) == RGBA(255, 0, 0, 255)) { "Expected red" }
    }
}
