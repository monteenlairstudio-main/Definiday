package fr.definiday

import android.net.Uri
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Glose's name (docs/voix.md): spelt by one string only, fed to the others, elided by de(), typed in Réglages › Apparence. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE) // the card's signature is compared pixel by pixel
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class VoixTest {
    private val ctx = RuntimeEnvironment.getApplication()

    @Test fun onlyTheDefaultSpellsTheName() {
        val res = ctx.resources
        val texts = R.string::class.java.fields.filter { it.name != "glose_name" }.map { it.name to res.getString(it.getInt(null)) } +
            R.plurals::class.java.fields.flatMap { f -> listOf(1, 2).map { f.name to res.getQuantityString(f.getInt(null), it) } }
        assertEquals(emptyList<Pair<String, String>>(), texts.filter { "Glose" in it.second })
        assertEquals("Monsieur Glose", Prefs(ctx).gloseName)
    }

    @Test fun deElidesBeforeAVowelOnly() {
        assertEquals(listOf("d’Anatole", "d’Émile", "d’Œdipe", "de Monsieur Glose", "de Hugo", "de Yann", "de A.", "de É"),
            listOf("Anatole", "Émile", "Œdipe", "Monsieur Glose", "Hugo", "Yann", "A.", "É").map(::de))
    }

    @Test fun aNewNameReachesTheScreensAndJunkFallsBack() {
        val p = Prefs(ctx)
        p.gloseName = "  Anatole "
        assertEquals("Anatole", p.gloseName)
        val a = Robolectric.buildActivity(CollectionActivity::class.java).setup().get()
        val views = generateSequence(listOf<View>(a.window.decorView)) { l ->
            l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null }
        }.flatten().toList()
        assertTrue(views.any { (it as? TextView)?.text == "Collection d’Anatole" })
        assertEquals("Collection d’Anatole", a.title.toString()) // the window title (TalkBack, Recents), not « Collection %1$s »
        assertTrue(views.any { it.contentDescription == "Anatole, chat bibliothécaire" })
        // a restored or tampered value: the field's rule, else the default (an emoji counts one)
        listOf("", "x".repeat(25), "Ana\ntole", "‮Anatole", "Ana​tole", "Ana tole", "Ana\uD83Dtole")
            .forEach { p.gloseName = it; assertEquals(it, "Monsieur Glose", p.gloseName) }
        p.gloseName = "😺".repeat(24)
        assertEquals("😺".repeat(24), p.gloseName)
    }

    private fun settings() = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
    private fun EditText.type(s: String) { setText(s); onEditorAction(EditorInfo.IME_ACTION_DONE) }

    @Test fun aNameTypedInSettingsReachesHomeCollectionAndCard() {
        val w = Words.load(ctx).first()
        fun card() = WordCard.share(ctx, w, Styles.of(ctx, w), null, 540, LocalDate.of(2026, 11, 7))
        val signedGlose = card()
        assertTrue(signedGlose.sameAs(card())) // deterministic: a difference below comes from the name
        val field = settings().findViewById<EditText>(R.id.glose_name)
        assertEquals("Monsieur Glose", field.text.toString())
        field.type("  Anatole ")
        assertEquals("Anatole", Prefs(ctx).gloseName)
        assertEquals("Anatole", field.text.toString()) // trimmed in the field too
        assertNull(field.error)
        val home = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        assertEquals("Une lettre d’Anatole", home.findViewById<TextView>(R.id.title).text.toString())
        assertEquals("Collection d’Anatole", Robolectric.buildActivity(CollectionActivity::class.java).setup().get().title.toString())
        assertFalse(card().sameAs(signedGlose)) // the lock-screen card and the shared picture sign with the name
    }

    @Test fun invalidNamesAreRefusedWithAMessageAndResetRestoresTheDefault() {
        val s = settings()
        val field = s.findViewById<EditText>(R.id.glose_name)
        mapOf("   " to R.string.name_empty, "x".repeat(25) to R.string.name_long, "‮Anatole" to R.string.name_bad,
            "Ana tole" to R.string.name_bad, "Ana​tole" to R.string.name_bad, "Ana\ttole" to R.string.name_bad).forEach { (typed, why) ->
            field.setText(typed)
            assertEquals(typed, s.getString(why), field.error?.toString()) // shown as typed
            field.onEditorAction(EditorInfo.IME_ACTION_DONE)
            field.clearFocus()
            assertEquals(typed, "Monsieur Glose", Prefs(ctx).gloseName)
            assertEquals(typed, s.getString(why), field.error?.toString())
        }
        field.type("😺".repeat(24))
        assertEquals("😺".repeat(24), Prefs(ctx).gloseName)
        s.findViewById<Button>(R.id.name_reset).apply { assertEquals("Rétablir « Monsieur Glose »", text.toString()) }.performClick()
        assertEquals("Monsieur Glose", field.text.toString())
        assertEquals("Monsieur Glose", Prefs(ctx).gloseName)
        assertNull(field.error)
    }

    /** Owner 2026-11-08 « on sécurise l'entrée »: the name is normalized and a refused one is never stored, whatever the caller. */
    @Test fun theNameIsNormalizedAndARefusedOneIsNeverStored() {
        val p = Prefs(ctx)
        p.gloseName = "  Ana \u00A0  tole  "
        assertEquals("Ana tole", p.gloseName)
        p.gloseName = "E\u0301mile" // accent typed apart
        assertEquals("\u00C9mile", p.gloseName)
        assertEquals("d’\u00C9mile", de(p.gloseName))
        for (bad in listOf("\u202EAnatole", "Ana\ttole", "Ana\ntole", "Ana\uE000", "x".repeat(25))) {
            p.gloseName = bad
            assertEquals(bad, "", ctx.getSharedPreferences("definiday", android.content.Context.MODE_PRIVATE).getString("gloseName", null))
            assertEquals(bad, "Monsieur Glose", p.gloseName)
        }
        ctx.getSharedPreferences("definiday", android.content.Context.MODE_PRIVATE).edit().putString("gloseName", "Ana\uE000").commit() // restored, tampered
        assertEquals("Monsieur Glose", p.gloseName)
    }

    @Test fun leavingSettingsWhileTypingKeepsTheNameButARestoredProfileWins() {
        val c = Robolectric.buildActivity(SettingsActivity::class.java).setup()
        c.get().findViewById<EditText>(R.id.glose_name).setText("Anatole") // no Done: Back
        c.pause()
        assertEquals("Anatole", Prefs(ctx).gloseName)
        val f = Uri.fromFile(File(ctx.cacheDir, "nom.json"))
        assertTrue(Profile.export(ctx, f))
        Prefs(ctx).gloseName = ""
        assertTrue(Profile.import(ctx, f)) // export/import keeps the name
        assertEquals("Anatole", Prefs(ctx).gloseName)
        c.resume()
        c.get().findViewById<EditText>(R.id.glose_name).setText("Émile")
        Prefs(ctx).gloseName = "Octave" // a profile restored meanwhile (the import dialog, then recreate())
        c.pause()
        assertEquals("Octave", Prefs(ctx).gloseName) // the typed name doesn't overwrite it
        // a hostile profile: U+202E in the name falls back to the default
        File(ctx.cacheDir, "nom.json").apply { writeText(readText().replace("Anatole", "‮Anatole")) }
        assertTrue(Profile.import(ctx, f))
        assertEquals("Monsieur Glose", Prefs(ctx).gloseName)
    }

    /** No Kotlin literal nor XML attribute spells the name (the default lives in glose_name only), and no XML shows a format
     *  string raw (« Collection %1$s » as the manifest label, N5): those are filled in code. */
    @Test fun noCodeOrLayoutHardcodesTheName() {
        val main = File("src/main")
        val literal = Regex(""""(?:[^"\\\n]|\\.)*"""")
        val kt = main.resolve("java").walk().filter { it.extension == "kt" }.flatMap { f ->
            f.readLines().mapIndexedNotNull { i, l ->
                val code = l.trim().takeUnless { it.startsWith("//") || it.startsWith("*") || it.startsWith("/*") }?.substringBefore(" // ") ?: ""
                literal.findAll(code).map { it.value }.filter { "Glose" in it }.toList().takeIf { it.isNotEmpty() }?.let { "${f.name}:${i + 1} $it" }
            }
        }.toList()
        assertEquals(emptyList<String>(), kt)
        val format = Regex("""%\d\$""")
        val xml = (main.resolve("res").walk().filter { it.extension == "xml" && !it.parentFile.name.startsWith("values") } + main.resolve("AndroidManifest.xml"))
            .flatMap { f ->
                val text = f.readText().replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
                Regex("=\"([^\"]*)\"").findAll(text).map { it.groupValues[1] }.filter { v ->
                    if (v.startsWith("@string/")) format in ctx.getString(ctx.resources.getIdentifier(v.drop(8), "string", ctx.packageName))
                    else "Glose" in v && !v.startsWith("@") && !v.startsWith("?")
                }.map { "${f.name} $it" }
            }.toList()
        assertEquals(emptyList<String>(), xml)
    }
}
