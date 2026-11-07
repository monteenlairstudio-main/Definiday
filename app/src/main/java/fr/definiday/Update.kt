package fr.definiday

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import org.json.JSONObject
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.security.MessageDigest

/**
 * Self-update from the GitHub releases (docs/mise_a_jour.md): update.json says which APK is the latest and its SHA-256;
 * the APK is installed over this app with PackageInstaller. Same package + same signing key: Android keeps all the data,
 * and refuses an APK signed with another key whatever its hash. Network only on the user's tap, or once a week if allowed.
 */
object Update {
    const val REPO = "https://github.com/monteenlairstudio-main/Definiday/releases"
    const val LATEST = "$REPO/latest/download/update.json"
    const val EXTRA = "fr.definiday.update" // update.json text, from the home screen's offer to Réglages
    private const val WEEK = 7 * 24 * 3600 * 1000L
    private const val MAX_JSON = 64 * 1024L
    private const val MAX_APK = 64L * 1024 * 1024

    data class Info(val code: Long, val name: String, val apk: String, val sha256: String, val minSdk: Int, val notes: String, val json: String)

    /** Only this repository's releases, over HTTPS. */
    fun trusted(url: String) = runCatching { URI(url) }.getOrNull()?.let {
        it.scheme == "https" && it.host == "github.com" && it.port == -1 && it.rawUserInfo == null &&
            it.rawPath.startsWith("/monteenlairstudio-main/Definiday/releases/") && ".." !in it.rawPath
    } ?: false

    /** Throws on anything unexpected: missing field, untrusted APK URL, malformed hash. */
    fun parse(json: String) = JSONObject(json).run {
        Info(getLong("versionCode"), getString("versionName").take(32), getString("apk"), getString("sha256").lowercase(),
            optInt("minSdk", 26), optString("notes").take(2000).trim(), json)
    }.also { require(trusted(it.apk)) { "untrusted ${it.apk}" }; require(it.sha256.matches(Regex("[0-9a-f]{64}"))) }

    fun newer(i: Info, current: Long = BuildConfig.VERSION_CODE.toLong(), sdk: Int = Build.VERSION.SDK_INT) = i.code > current && i.minSdk <= sdk

    /** The weekly check: allowed, and never done or done 7 days ago (or the clock went back). */
    fun due(p: Prefs, now: Long) = p.updateAuto && now - p.updateCheckedAt !in 0 until WEEK

    /** The one network door, swapped by the tests. HttpURLConnection follows redirects but never from https to http. */
    internal var open: (String) -> URLConnection = { (URL(it).openConnection() as HttpURLConnection).apply { connectTimeout = 10_000; readTimeout = 20_000 } }

    /** GETs a trusted [url] into [out], at most [max] bytes; [progress] in percent when the size is known. */
    fun get(url: String, out: OutputStream, max: Long, progress: (Int) -> Unit = {}) {
        require(trusted(url)) { "untrusted $url" }
        val c = open(url)
        c.getInputStream().use { input ->
            if (c is HttpURLConnection) check(c.responseCode == 200 && c.url.protocol == "https") { "HTTP ${c.responseCode} ${c.url}" }
            val size = c.contentLengthLong
            check(size <= max) { "too big: $size" }
            val b = ByteArray(64 * 1024)
            var done = 0L
            while (true) {
                val n = input.read(b).takeIf { it >= 0 } ?: break
                done += n
                check(done <= max) { "too big" }
                out.write(b, 0, n)
                if (size > 0) progress((done * 100 / size).toInt())
            }
        }
    }

    fun check(): Info = parse(java.io.ByteArrayOutputStream().also { get(LATEST, it, MAX_JSON) }.toString("UTF-8"))

    fun sha256(f: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        f.inputStream().use { s -> val b = ByteArray(64 * 1024); while (true) md.update(b, 0, s.read(b).takeIf { it >= 0 } ?: break) }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    /** Downloads the APK to [f]; false (and [f] deleted) if its SHA-256 isn't the one update.json announced. */
    fun download(i: Info, f: File, progress: (Int) -> Unit = {}): Boolean {
        f.outputStream().use { get(i.apk, it, MAX_APK, progress) }
        return (sha256(f) == i.sha256).also { if (!it) f.delete() }
    }

    /** At most once a week, if the user allowed it (Réglages): a newer version is offered in a dialog. Returns the check's thread. */
    fun autoCheck(a: Activity, now: Long = System.currentTimeMillis()): Thread? {
        val p = Prefs(a)
        if (!BuildConfig.SELF_UPDATE || !due(p, now)) return null
        p.updateCheckedAt = now // before the request: offline or failing, still one try a week
        return Thread {
            val i = runCatching { check() }.getOrNull()?.takeIf { newer(it) } ?: return@Thread
            a.runOnUiThread {
                if (a.isFinishing || a.isDestroyed) return@runOnUiThread
                AlertDialog.Builder(a)
                    .setTitle(a.getString(R.string.update_new, i.name))
                    .setMessage(i.notes.ifEmpty { a.getString(R.string.update_kept) })
                    .setPositiveButton(R.string.update_see) { _, _ -> a.startActivity(Intent(a, SettingsActivity::class.java).putExtra(EXTRA, i.json)) }
                    .setNegativeButton(R.string.update_later, null)
                    .show()
            }
        }.apply { start() }
    }

    /** Downloads, verifies and hands [i] to the system installer; [status] gets each step's text on the UI thread, last = true at the end. */
    fun install(a: Activity, i: Info, status: (String, Boolean) -> Unit) {
        if (!a.packageManager.canRequestPackageInstalls()) {
            AlertDialog.Builder(a)
                .setTitle(R.string.update_allow_title)
                .setMessage(R.string.update_allow)
                .setPositiveButton(R.string.update_allow_open) { _, _ ->
                    runCatching { a.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${a.packageName}"))) }
                }
                .setNegativeButton(R.string.update_later, null)
                .show()
            return status("", true)
        }
        status(a.getString(R.string.update_downloading, 0), false)
        Thread {
            val f = File(a.cacheDir, "update.apk")
            val r = runCatching {
                if (!download(i, f) { pct -> a.runOnUiThread { status(a.getString(R.string.update_downloading, pct), false) } }) return@runCatching R.string.update_bad_hash
                // a test build (fr.definiday.dev) can't update into the published app: another package, a separate app
                if (a.packageManager.getPackageArchiveInfo(f.path, 0)?.packageName.let { it != null && it != a.packageName }) return@runCatching R.string.update_test_build
                val pi = a.packageManager.packageInstaller
                val id = pi.createSession(PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                    setAppPackageName(a.packageName); setSize(f.length())
                })
                pi.openSession(id).use { s ->
                    f.inputStream().use { input -> s.openWrite("definiday.apk", 0, f.length()).use { input.copyTo(it); s.fsync(it) } }
                    // mutable: the installer fills in the status extras; explicit component, so nobody else receives it
                    s.commit(PendingIntent.getBroadcast(a, id, Intent(a, UpdateReceiver::class.java), PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT).intentSender)
                }
                R.string.update_installing
            }.getOrDefault(R.string.update_failed)
            f.delete() // the session holds its own copy
            a.runOnUiThread { status(a.getString(r), true) }
        }.start()
    }
}

/** PackageInstaller's answer: the confirmation screen to show, or a kind word when it failed. Success replaces (and restarts) the app. */
class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val msg = when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                @Suppress("DEPRECATION") val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                confirm?.let { runCatching { ctx.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
                return
            }
            PackageInstaller.STATUS_SUCCESS -> return
            PackageInstaller.STATUS_FAILURE_ABORTED -> R.string.update_aborted
            PackageInstaller.STATUS_FAILURE_CONFLICT -> R.string.update_other_key // an APK signed with another key
            PackageInstaller.STATUS_FAILURE_STORAGE -> R.string.update_storage
            else -> R.string.update_failed
        }
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
    }
}
