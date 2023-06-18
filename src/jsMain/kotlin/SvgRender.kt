import korlibs.image.bitmap.Bitmap
import korlibs.image.format.PNG
import korlibs.io.jsGlobalDynamic
import kotlinx.coroutines.await
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.Promise

@OptIn(ExperimentalEncodingApi::class)
@Suppress("UnnecessaryOptInAnnotation")
actual suspend fun svgRender(svgStr: String): Bitmap {
    jsGlobalDynamic._SVG_STR = svgStr
    val pngBase64Promise: Promise<String> = js(
        """new Promise(function(resolve, reject) {
        var svgStr = globalThis._SVG_STR;
        var canvas = document.createElement('canvas');
        var ctx = canvas.getContext('2d');
        var img = document.createElement('img');
        img.src = 'data:image/svg+xml;base64,' + btoa(svgStr);
        img.onload = function() {
            canvas.width = img.width;
            canvas.height = img.height;
            ctx.drawImage(img, 0, 0);
            var pngStr = canvas.toDataURL('image/png');
            resolve(pngStr.substring('data:image/png;base64,'.length));
        };
        img.onerror = function(err) { reject(err); };
    })
    """
    ) as Promise<String>
    val pngBase64: String = pngBase64Promise.await()
    val pngBytes = Base64.decode(pngBase64)
    return PNG.decodeSuspend(pngBytes)
}