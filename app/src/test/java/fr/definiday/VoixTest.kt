package fr.definiday

import android.view.View
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Glose's name (docs/voix.md): spelt by one string only, fed to the others, elided by de(). */
@RunWith(RobolectricTestRunner::class)
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
        assertTrue(views.any { it.contentDescription == "Anatole, chat bibliothécaire" })
        listOf("", "x".repeat(25), "Ana\ntole").forEach { p.gloseName = it; assertEquals("Monsieur Glose", p.gloseName) }
    }
}
