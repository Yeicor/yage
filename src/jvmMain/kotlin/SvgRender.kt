import korlibs.image.bitmap.Bitmap
import korlibs.image.format.PNG
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.ByteArrayOutputStream
import java.io.StringReader

actual suspend fun svgRender(svgStr: String): Bitmap {
    val transcoderInput = TranscoderInput(StringReader(svgStr))
    val baos = ByteArrayOutputStream()
    val transcoderOutput = TranscoderOutput(baos)
    PNGTranscoder().transcode(transcoderInput, transcoderOutput)
    return PNG.decodeSuspend(baos.toByteArray())
}