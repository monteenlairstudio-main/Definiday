package fr.definiday

import android.content.ClipData
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File

/** Serves the last shared card picture (cache/definiday.png), read-only, to the app picked in the share sheet, through the
 *  read grant of the share intent (not exported). Whatever the path asked, only that file: no traversal. No AndroidX FileProvider. */
class ShareProvider : ContentProvider() {
    override fun onCreate() = true
    override fun getType(uri: Uri) = "image/png"
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor = ParcelFileDescriptor.open(file(context!!), ParcelFileDescriptor.MODE_READ_ONLY)
    /** Name and size: some apps ask before reading. */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, args: Array<String>?, sort: String?) =
        MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)).apply { addRow(arrayOf<Any>(NAME, file(context!!).length())) }
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, args: Array<String>?) = 0
    override fun delete(uri: Uri, selection: String?, args: Array<String>?) = 0

    companion object {
        /** "${applicationId}.share" in the manifest: the test build (fr.definiday.dev) has its own, beside the real app. */
        fun authority(ctx: Context) = ctx.packageName + ".share"
        private const val NAME = "definiday.png"
        fun uri(ctx: Context): Uri = Uri.parse("content://${authority(ctx)}/$NAME")
        private fun file(ctx: Context) = File(ctx.cacheDir, NAME) // overwritten at each share: nothing piles up

        /** The word as text: what every app can take. */
        fun text(ctx: Context, item: Item) = when (item) {
            is Word -> listOfNotNull("${item.mot}, ${item.nature}", item.definition, item.exemple.takeIf { it.isNotEmpty() }?.let { "« $it »" })
            is Quote -> listOf("« ${item.texte} »", "— ${item.signature}")
        }.joinToString("\n") + "\n\n" + ctx.getString(R.string.share_via)

        /** Share sheet with the text and, when [picture] could be written, the card as an image (apps take one or both). */
        fun intent(ctx: Context, item: Item, picture: Bitmap?): Intent {
            val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text(ctx, item))
            if (picture != null && runCatching { file(ctx).outputStream().use { picture.compress(Bitmap.CompressFormat.PNG, 100, it) } }.getOrDefault(false)) {
                send.setType("image/png").putExtra(Intent.EXTRA_STREAM, uri(ctx)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                send.clipData = ClipData.newRawUri(null, uri(ctx)) // the chooser passes the grant on and shows a preview
            }
            return Intent.createChooser(send, ctx.getString(R.string.share_title))
        }
    }
}
