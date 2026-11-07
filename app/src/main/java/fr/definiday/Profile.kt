package fr.definiday

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * The profile as a file (Réglages › Mon profil, docs/profil.md), through the Storage Access Framework: no permission.
 * Format: {"format": "definiday-profil", "version": 1, "app": versionName, "date": ISO day,
 *          "files": {"definiday": {key: {"t": type, "v": value}}, "history": {...}, "memoire": {...}}}
 * with type string, int, long, boolean, float or set (a JSON array of strings).
 * wallpaper_state is device-local (a wallpaper id of this phone), never in.
 *
 * For the updater: call [export] before a risky update; [import] replaces the whole profile (no confirmation of its own).
 */
object Profile {
    const val FORMAT = "definiday-profil"
    const val VERSION = 1
    /** The prefs files that make the profile, all backed up too (res/xml backup rules exclude only wallpaper_state). */
    val FILES = listOf("definiday", "history", "memoire")
    private const val MAX_BYTES = 4 shl 20
    private const val MAX_ENTRIES = 100_000
    private const val MAX_TEXT = 10_000

    /** Prefs keys (Prefs) and their type; "wallpaper": the former switch, still read as the mode's default. */
    private val SETTINGS = mapOf(
        "onboarded" to "boolean", "enabled" to "boolean", "hour" to "int", "minute" to "int", "duration" to "int", "mode" to "int",
        "wallpaper" to "boolean", "unlocks" to "int", "wordsPerDay" to "int", "dailyDay" to "long", "dailyMots" to "string",
        "textBottom" to "boolean", "moreQuotes" to "boolean", "sounds" to "boolean", "guessFirst" to "boolean", "rateAsked" to "boolean",
        "quizEnabled" to "boolean", "quizHour" to "int", "quizMinute" to "int", "lastQuizDay" to "long", "streak" to "int",
        "quizzes" to "int", "perfectQuizzes" to "int", "passedQuizzes" to "int", "correctAnswers" to "int",
        "unlocked" to "set", "announced" to "set", "glosePose" to "string", "glosePelage" to "string", "glosePelote" to "string",
        "gloseAcc" to "string", "gloseName" to "string", "warnedDay" to "long", "palettes" to "set", "polices" to "set", "updateAuto" to "boolean",
    )

    /** The type a key may hold in [file], null = unknown key (ignored on import). History: word -> day; Memory: see its doc. */
    private fun type(file: String, key: String): String? = when (file) {
        "definiday" -> SETTINGS[key]
        "history" -> "long"
        else -> when {
            key.startsWith("m:") -> "string"
            key == "favoris" -> "set"
            key == "reviews" || key == "reviewsRight" -> "int"
            else -> null
        }
    }

    fun json(ctx: Context): JSONObject {
        val files = JSONObject()
        FILES.forEach { f ->
            val o = JSONObject()
            ctx.getSharedPreferences(f, Context.MODE_PRIVATE).all.forEach { (k, v) ->
                val t = when (v) { is String -> "string"; is Int -> "int"; is Long -> "long"; is Boolean -> "boolean"; is Float -> "float"; is Set<*> -> "set"; else -> null }
                if (t != null) o.put(k, JSONObject().put("t", t).put("v", if (v is Set<*>) JSONArray(v.toList()) else if (v is Float) v.toDouble() else v))
            }
            files.put(f, o)
        }
        val app = runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull().orEmpty()
        return JSONObject().put("format", FORMAT).put("version", VERSION).put("app", app).put("date", LocalDate.now().toString()).put("files", files)
    }

    /** Default name for ACTION_CREATE_DOCUMENT. */
    fun fileName() = "definiday-profil-${LocalDate.now()}.json"

    /** Writes the profile to [uri] (ACTION_CREATE_DOCUMENT); false if it could not be written. */
    fun export(ctx: Context, uri: Uri): Boolean = runCatching {
        ctx.contentResolver.openOutputStream(uri, "w")!!.use { it.write(json(ctx).toString().toByteArray()) }
    }.isSuccess

    /** The profile in [uri], validated: file -> key -> value; null if it is not a profile (then nothing may change). */
    fun read(ctx: Context, uri: Uri): Map<String, Map<String, Any>>? = runCatching {
        val bytes = ctx.contentResolver.openInputStream(uri)!!.use { s -> // readNBytes is API 33
            java.io.ByteArrayOutputStream().also { o -> val b = ByteArray(8192); while (o.size() <= MAX_BYTES) { val n = s.read(b); if (n < 0) break; o.write(b, 0, n) } }.toByteArray()
        }
        if (bytes.size > MAX_BYTES) null else parse(String(bytes))
    }.getOrNull()

    /** Unknown files and keys are ignored; a known key of the wrong type, out of range or oversized rejects the whole file. */
    fun parse(text: String): Map<String, Map<String, Any>>? = runCatching {
        val root = JSONObject(text)
        if (root.optString("format") != FORMAT || root.optInt("version", 0) !in 1..VERSION) return null
        val files = root.getJSONObject("files")
        FILES.associateWith { f ->
            val o = files.getJSONObject(f) // every file, even empty: an import replaces all of them
            require(o.length() <= MAX_ENTRIES)
            o.keys().asSequence().mapNotNull { k ->
                val t = type(f, k) ?: return@mapNotNull null
                require(k.length <= 200)
                val e = o.getJSONObject(k)
                require(e.getString("t") == t)
                k to value(t, e.get("v"))
            }.toMap()
        }
    }.getOrNull()

    private fun value(t: String, v: Any): Any = when (t) {
        "string" -> (v as String).also { require(it.length <= MAX_TEXT) }
        "int" -> (v as Number).also { require(v !is Double && v.toLong() in Int.MIN_VALUE..Int.MAX_VALUE) }.toInt()
        "long" -> (v as Number).also { require(v !is Double) }.toLong()
        "boolean" -> v as Boolean
        "float" -> (v as Number).toFloat()
        else -> (v as JSONArray).let { a -> require(a.length() <= MAX_ENTRIES); List(a.length()) { (a.get(it) as String).also { s -> require(s.length <= MAX_TEXT) } }.toSet() }
    }

    /** Replaces the profile with the one in [uri]; false (and nothing changed) if it is not a valid profile. */
    fun import(ctx: Context, uri: Uri): Boolean = read(ctx, uri)?.also { apply(ctx, it) } != null

    /** Writes [profile] (from [read]) over every profile file, then re-anchors what depends on it: alarms, unlock watch, widget. */
    fun apply(ctx: Context, profile: Map<String, Map<String, Any>>) {
        profile.forEach { (f, entries) ->
            val e = ctx.getSharedPreferences(f, Context.MODE_PRIVATE).edit().clear()
            @Suppress("UNCHECKED_CAST")
            entries.forEach { (k, v) ->
                when (v) { is String -> e.putString(k, v); is Int -> e.putInt(k, v); is Long -> e.putLong(k, v); is Boolean -> e.putBoolean(k, v); is Float -> e.putFloat(k, v); else -> e.putStringSet(k, v as Set<String>) }
            }
            e.commit()
        }
        DailyReceiver.scheduleNext(ctx)
        DailyReceiver.scheduleQuiz(ctx)
        UnlockService.sync(ctx)
        Widget.update(ctx)
    }
}
