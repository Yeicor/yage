import korlibs.image.bitmap.Bitmap
import korlibs.image.format.PNG
import korlibs.logger.Logger
import org.apache.batik.transcoder.TranscoderException
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.ByteArrayOutputStream
import java.io.StringReader

actual suspend fun svgRender(svgStr: String): Bitmap {
    try {
        // FIXME: The following code causes a crash due to the feature
        //  http://apache.org/xml/features/nonvalidating/load-external-dtd
        //  not existing in android's SAX parser implementation.
        val transcoderInput = TranscoderInput(StringReader(svgStr))
        val baos = ByteArrayOutputStream()
        val transcoderOutput = TranscoderOutput(baos)
        PNGTranscoder().transcode(transcoderInput, transcoderOutput)
        return PNG.decodeSuspend(baos.toByteArray())
    } catch (e: TranscoderException) {
        Logger("svgRender").error { "Exception: $e. Falling back to default svgRender." }
        e.printStackTrace()
    }
    return svgRenderDefault(svgStr)
}