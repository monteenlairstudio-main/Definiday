package fr.definiday

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class GloseTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val parts = GloseLayers.layers.values.flatten().map { it.second }.toSet() - GloseLayers.layers.keys // layers alone are not drawings
    private val all = R.drawable::class.java.fields.filter { it.name.startsWith("glose_") }.associate { it.name to it.getInt(null) }.filterValues { it !in parts }
    private val overlays = R.style::class.java.fields.filter { it.name.startsWith("Glose_") }.associate { it.name to it.getInt(null) }

    private fun render(res: Int, styles: List<Int> = emptyList(), size: Int = 256) = Glose.bitmap(Glose.drawable(ctx, res, styles), size, size)
    private fun painted(b: Bitmap) = IntArray(b.width * b.height).also { b.getPixels(it, 0, b.width, 0, 0, b.width, b.height) }.count { Color.alpha(it) > 0 }
    private fun diff(a: Bitmap, b: Bitmap) = (0 until a.width * a.height).count { a.getPixel(it % a.width, it / a.width) != b.getPixel(it % a.width, it / a.width) }

    /** Every generated VD/AVD inflates and draws, alone and with every overlay at once. */
    @Test fun everyDrawableInflatesAndDraws() {
        assertTrue(all.size >= 20)
        all.forEach { (name, res) ->
            assertTrue(name, painted(render(res)) > 2000)
            assertTrue(name, painted(render(res, overlays.values.toList())) > 2000)
        }
    }

    /** Theme attributes reach the paths: each accessory/pelage/pelote changes the picture, and only when applied. */
    @Test fun overlaysChangeThePicture() {
        val base = render(R.drawable.glose_assis_vd)
        assertTrue(diff(base, render(R.drawable.glose_assis_vd)) == 0)
        // éclats are scaled to 0 in the still frame
        overlays.filterKeys { it != "Glose_Eclats" }.forEach { (name, style) -> assertTrue(name, diff(base, render(R.drawable.glose_assis_vd, listOf(style))) > 50) }
    }

    /** The unlock table, as agreed with the owner (base: thresholds count quizzes; 2026 wave from the catalogue; d: = one quiz in the window). */
    @Test fun unlockTable() {
        val table = mapOf("assis" to "q0", "tigre" to "q0", "bordeaux" to "q0", "ecrit_mot" to "q1", "ecrit_encore" to "q1", "ecrit_bravo" to "r1",
            "noeud" to "p1", "soir" to "s3", "ecrit_dodo" to "s3", "moutarde" to "q5", "echarpe" to "s7", "livres" to "q10", "creme" to "p5",
            "bonnet" to "s14", "sauge" to "q20", "ecrit_rare" to "r30", "plume" to "r60", "rose" to "p15", "marque" to "q45",
            "enluminure" to "r150", "nuit" to "s30",
            "roux" to "q3", "smoking" to "q30", "lavande" to "q8", "bleu_prusse" to "c25", "vert_eau" to "r12", "reliure_bordeaux" to "q2",
            "reliure_verte" to "m40", "monture_ecaille" to "c20", "ecrit_coucou" to "q3", "ecrit_ronron" to "s10", "grelot" to "q12", "beret" to "k15",
            "reveil_tasse" to "q21", "echarpe_sucre_orge" to "d:12-01..12-31", "bonnet_neige" to "d:12-01..02-28", "ecrit_flocon" to "d:12-01..12-31",
            "sapin_papier" to "d:12-01..12-31", "couronne_rois" to "d:01-02..01-20")
        assertEquals(table, Unlocks.items.associate { it.id to (it.window?.let { w -> "d:$w" } ?: "${it.on}${it.need}") })
        Unlocks.items.filter { it.need > 0 }.forEach {
            assertTrue(it.id, it.id in Unlocks.of(mapOf(it.on to it.need)))
            assertFalse(it.id, it.id in Unlocks.of(mapOf(it.on to it.need - 1)))
        }
        assertEquals(setOf("assis", "tigre", "bordeaux"), Unlocks.of(emptyMap()))
        assertEquals(Unlocks.items.filter { it.window == null }.map { it.id }.toSet(), Unlocks.of("qprsckm".associateWith { 150 }))
    }

    /** Every id is unique and resolves: poses to a drawable, other items (but the defaults) to an overlay that changes the picture; names never say "Glose". */
    @Test fun unlockTableResolves() {
        assertEquals(Unlocks.items.size, Unlocks.byId.size)
        val base = render(R.drawable.glose_assis_vd)
        Unlocks.items.forEach {
            assertFalse(it.nom, it.nom.contains("Glose"))
            if (it.kind.slot == Unlocks.Kind.POSE) assertTrue(it.id, painted(render(it.res)) > 2000)
            else if (it.need > 0 || it.window != null) assertTrue(it.id, diff(base, Glose.bitmap(Glose.preview(ctx, it), 256, 256)) > 50)
            else assertEquals(it.id, 0, it.style)
            assertEquals(it.id, it.kind.slot == Unlocks.Kind.POSE, it.res != 0)
        }
    }

    /** Windows any year, wrapping over the new year; seasonal hints say when they come back, never that they expired. */
    @Test fun windowsAndCounters() {
        fun d(s: String) = java.time.LocalDate.parse(s)
        assertTrue(Unlocks.inWindow("12-01..02-28", d("2027-01-15")))
        assertTrue(Unlocks.inWindow("12-01..02-28", d("2026-12-01")))
        assertFalse(Unlocks.inWindow("12-01..02-28", d("2027-03-01")))
        assertFalse(Unlocks.inWindow("01-02..01-20", d("2027-01-21")))
        assertEquals(setOf("echarpe_sucre_orge", "bonnet_neige", "ecrit_flocon", "sapin_papier"), Unlocks.of(emptyMap(), d("2031-12-24")) - Unlocks.of(emptyMap()))
        assertEquals(setOf("bonnet_neige", "couronne_rois"), Unlocks.of(emptyMap(), d("2027-01-06")) - Unlocks.of(emptyMap()))
        // the last quiz's date unlocks, once; outside its window an item says when it comes back
        val p = Prefs(ctx).apply { quizzes = 1; lastQuizDay = d("2026-12-24").toEpochDay() }
        assertTrue(Unlocks.update(p).map { it.id }.containsAll(listOf("sapin_papier", "ecrit_flocon")))
        assertEquals("Revient en janvier", Unlocks.hint(Unlocks.byId.getValue("couronne_rois"), p, d("2026-12-24").toEpochDay()))
        assertEquals("Revient en décembre", Unlocks.hint(Unlocks.byId.getValue("bonnet_neige"), p, d("2027-06-01").toEpochDay()))
        assertEquals("Un soir de quiz, en ce moment", Unlocks.hint(Unlocks.byId.getValue("couronne_rois"), p, d("2027-01-05").toEpochDay()))
        // c, m, k: correct answers, words and quotes seen
        History(ctx).run { mark("abaque", 1); mark("zinzolin", 2); mark("q:Le style est l’homme même.", 3) }
        p.correctAnswers = 7
        assertEquals(mapOf('c' to 7, 'm' to 2, 'k' to 1), Unlocks.counts(p).filterKeys { it in "cmk" })
        assertEquals("Encore 38 mots découverts", Unlocks.hint(Unlocks.byId.getValue("reliure_verte"), p))
        assertEquals("Encore 14 citations lues", Unlocks.hint(Unlocks.byId.getValue("beret"), p))
        assertEquals("Encore 13 bonnes réponses", Unlocks.hint(Unlocks.byId.getValue("monture_ecaille"), p))
    }

    /** One item per slot; the single accessory saved before the slots existed is still worn; a binding goes before the pelage. */
    @Test fun slotsKeepOldChoices() {
        val p = Prefs(ctx).apply { unlocked = setOf("noeud", "beret", "nuit", "reliure_verte", "ecrit_coucou"); gloseAcc = "noeud"; glosePose = "ecrit_coucou" }
        assertEquals("noeud", Glose.choice(p)[Unlocks.Kind.COU])
        assertEquals(R.drawable.glose_ecrit_coucou, Glose.pose(Glose.choice(p)))
        Glose.save(p, Glose.choice(p) + mapOf(Unlocks.Kind.TETE to "beret", Unlocks.Kind.LIVRE to "reliure_verte", Unlocks.Kind.PELAGE to "nuit"))
        assertEquals("beret,noeud,reliure_verte", p.gloseAcc)
        assertEquals(listOf(R.style.Glose_Livre_Verte, R.style.Glose_Pelage_Nuit, R.style.Glose_Acc_Beret, R.style.Glose_Acc_Noeud), Glose.styles(Glose.choice(p)))
    }

    @Test fun unlocksAreKeptAndAnnouncedOnce() {
        val p = Prefs(ctx).apply { quizzes = 1; streak = 3 }
        assertEquals(setOf("ecrit_mot", "ecrit_encore", "soir", "ecrit_dodo"), Unlocks.update(p).map { it.id }.toSet())
        p.announced = p.announced + Unlocks.update(p).map { it.id }
        p.streak = 1; p.lastQuizDay = java.time.LocalDate.now().toEpochDay() // broken streak restarted today: nothing is lost, nothing new
        assertTrue(Unlocks.update(p).isEmpty())
        assertTrue("soir" in p.unlocked)
        assertEquals("Encore 2 soirs de quiz d’affilée", Unlocks.hint(Unlocks.byId.getValue("soir"), p))
        // P8: broken since the last quiz (10 days ago), the hint counts from 0, not from the stale streak
        p.streak = 2; p.lastQuizDay -= 10
        assertEquals("Encore 7 soirs de quiz d’affilée", Unlocks.hint(Unlocks.byId.getValue("echarpe"), p))
    }

    @Test fun collectionTriesThenKeepsAChoice() {
        val p = Prefs(ctx).apply { quizzes = 5 }
        val a = Robolectric.buildActivity(CollectionActivity::class.java).setup().get()
        fun find(desc: String): android.view.View { val l = ArrayList<android.view.View>(); a.window.decorView.findViewsWithText(l, desc, android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION); return l.first() }
        assertFalse(find("Pelote sauge, à débloquer, Encore 15 quiz").isClickable)
        find("Pelote moutarde").performClick()
        assertEquals("bordeaux", p.glosePelote) // only tried
        val choose = a.window.decorView.let { val l = ArrayList<android.view.View>(); it.findViewsWithText(l, ctx.getString(R.string.collection_choose), android.view.View.FIND_VIEWS_WITH_TEXT); l.first() }
        choose.performClick()
        assertEquals("moutarde", p.glosePelote)
        assertEquals(listOf(R.style.Glose_Pelote_Moutarde), Glose.styles(Glose.choice(p)))
    }

    /** N11: a rotation keeps what is being tried (not yet chosen), and « Choisir » still keeps it. */
    @Test fun collectionTrialSurvivesRotation() {
        val p = Prefs(ctx).apply { quizzes = 5 }
        val c = Robolectric.buildActivity(CollectionActivity::class.java).setup()
        fun find(desc: String): android.view.View { val l = ArrayList<android.view.View>(); c.get().window.decorView.findViewsWithText(l, desc, android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION); return l.first() }
        fun choose() = c.get().window.decorView.let { val l = ArrayList<android.view.View>(); it.findViewsWithText(l, ctx.getString(R.string.collection_choose), android.view.View.FIND_VIEWS_WITH_TEXT); l.first() }
        find("Pelote moutarde").performClick()
        c.recreate()
        assertTrue(find("Pelote moutarde").isSelected)
        assertEquals(android.view.View.VISIBLE, choose().visibility)
        assertEquals("bordeaux", p.glosePelote) // still only tried
        choose().performClick()
        assertEquals("moutarde", p.glosePelote)
    }

    @Test fun eveningPoseAfterNinePmOnceUnlocked() {
        val p = Prefs(ctx).apply { glosePose = "assis" }
        assertEquals(R.drawable.glose_assis, Glose.mainPose(p, hour = 22)) // not unlocked yet
        p.unlocked = setOf("soir")
        assertEquals(R.drawable.glose_assis, Glose.mainPose(p, hour = 20))
        assertEquals(R.drawable.glose_soir, Glose.mainPose(p, hour = 21))
    }

    /** The idle life stops by itself after IDLE_MS (WCAG 2.2.2); a tap replays a full period. Robolectric cannot run an infinite AVD,
     *  so the drawable here only records start/stop. */
    @Test fun idleLifeStopsThenReplays() {
        var running = false
        val d = object : AnimatedVectorDrawable() {
            override fun start() { running = true }
            override fun stop() { running = false }
            override fun reset() {}
            override fun isRunning() = running
        }
        val v = ImageView(ctx).apply { setImageDrawable(d) }
        Robolectric.buildActivity(android.app.Activity::class.java).setup().get().setContentView(v)
        val idle = { ms: Long -> org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(ms)) }
        assertTrue(Glose.IDLE_MS in 20_000L..30_000L)
        Glose.play(v)
        idle(Glose.IDLE_MS - 1000)
        assertTrue(running)
        Glose.play(v) // a tap: the first period's stop is cancelled
        idle(Glose.IDLE_MS - 1000)
        assertTrue(running)
        idle(2000)
        assertFalse(running) // rests on its still frame
        Glose.play(v); Glose.stop(v) // onPause
        assertFalse(running)
    }

    /** The idle stop is scaled like the animators: ValueAnimator's scale from API 33, the global setting before (getDurationScale is hidden there). */
    @Test fun idleStopFollowsTheDurationScale() {
        val scale = android.animation.ValueAnimator::class.java.getMethod("setDurationScale", Float::class.javaPrimitiveType)
        try { scale.invoke(null, 2f); assertEquals(2f, Glose.durationScale(ctx)) } finally { scale.invoke(null, 1f) }
        val sdk = android.os.Build.VERSION.SDK_INT
        try {
            org.robolectric.util.ReflectionHelpers.setStaticField(android.os.Build.VERSION::class.java, "SDK_INT", 28)
            assertEquals(1f, Glose.durationScale(ctx)) // unset: 1
            android.provider.Settings.Global.putFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 0.5f)
            assertEquals(0.5f, Glose.durationScale(ctx))
        } finally { org.robolectric.util.ReflectionHelpers.setStaticField(android.os.Build.VERSION::class.java, "SDK_INT", sdk) }
    }

    /** A reaction plays once, then the idle drawable comes back; stop() (onPause) cancels that return. */
    @Test fun reactionsGoBackToIdle() {
        listOf(R.drawable.glose_icone_hoche, R.drawable.glose_icone_penche).forEach { res ->
            val v = ImageView(ctx)
            Robolectric.buildActivity(android.app.Activity::class.java).setup().get().setContentView(v)
            Glose.once(v, res, R.drawable.glose_icone_vd, accessory = false)
            val oneShot = v.drawable
            assertTrue(oneShot is AnimatedVectorDrawable)
            v.draw(Canvas(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)))
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(2000))
            assertFalse(v.drawable is AnimatedVectorDrawable) // back to the still icon
            Glose.once(v, res, R.drawable.glose_icone, accessory = false)
            v.draw(Canvas(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)))
            val again = v.drawable
            Glose.stop(v)
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(2000))
            assertTrue(v.drawable === again)
        }
    }

    /** Every pose has its tap and streak reactions (design/mascotte/android/reactions.js). On the home screen a tap plays the short one,
     *  taps meanwhile are ignored, the 4th in a row plays the special, each then back to the idle life; animations off: the still stays. */
    @Test fun tapsMakeGloseReact() {
        (Unlocks.items.filter { it.kind.slot == Unlocks.Kind.POSE }.map { it.res } + R.drawable.glose_soir).forEach { pose ->
            listOf("_tap", "_special").forEach { assertTrue(ctx.resources.getResourceEntryName(pose) + it in all) }
        }
        val v = Robolectric.buildActivity(MainActivity::class.java).setup().get().findViewById<ImageView>(R.id.glose)
        val idle = { ms: Long -> org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(ms)) }
        fun tap(): android.graphics.drawable.Drawable { v.performClick(); v.draw(Canvas(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888))); return v.drawable }
        val first = tap()
        assertTrue(tap() === first) // ignored: no restart mid-reaction
        idle(Glose.Taps.QUIET_MS)
        assertFalse(v.drawable === first) // the 600 ms reaction is over: idle life again
        repeat(2) { tap(); idle(Glose.Taps.QUIET_MS) }
        val special = tap() // the 4th (Robolectric runs it to its end at the next idle, so its length is not checked here)
        assertTrue(special is android.graphics.drawable.LayerDrawable && tap() === special) // a layered pose
        idle(Glose.Taps.SPECIAL_QUIET_MS)
        assertFalse(v.drawable === special)
        val scale = android.animation.ValueAnimator::class.java.getMethod("setDurationScale", Float::class.javaPrimitiveType)
        try {
            scale.invoke(null, 0f)
            val still = v.drawable
            assertTrue(tap() === still)
        } finally { scale.invoke(null, 1f) }
    }

    /** WCAG 2.2.2: a long press (TalkBack: « Mettre en pause » / « Reprendre ») pauses and resumes the idle life, here and on return;
     *  animations off: nothing to pause, no action. */
    @Test fun longPressPausesTheIdleLife() {
        val c = Robolectric.buildActivity(MainActivity::class.java).setup()
        val v = c.get().findViewById<ImageView>(R.id.glose)
        fun running(): Boolean { v.draw(Canvas(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)))
            return (v.drawable as android.graphics.drawable.LayerDrawable).let { l -> (0 until l.numberOfLayers).map(l::getDrawable) }.filterIsInstance<AnimatedVectorDrawable>().any { it.isRunning } }
        fun label() = android.view.accessibility.AccessibilityNodeInfo.obtain().also(v::onInitializeAccessibilityNodeInfo).actionList
            .firstOrNull { it.id == android.view.accessibility.AccessibilityNodeInfo.ACTION_LONG_CLICK }?.label?.toString()
        try {
            assertTrue(running())
            assertEquals("Mettre en pause", label())
            assertTrue(v.performLongClick())
            assertFalse(running())
            assertEquals("Reprendre", label())
            c.pause().resume()
            assertFalse(running()) // still paused when the screen comes back
            assertTrue(v.performLongClick())
            assertTrue(running())
            val scale = android.animation.ValueAnimator::class.java.getMethod("setDurationScale", Float::class.javaPrimitiveType)
            try {
                scale.invoke(null, 0f)
                assertEquals(null, label())
                assertFalse(v.performLongClick())
            } finally { scale.invoke(null, 1f) }
        } finally { Glose.paused = false }
    }

    @Test fun mainScreenAnimatesGloseOnlyWhileVisible() {
        val c = Robolectric.buildActivity(MainActivity::class.java).setup()
        val v = c.get().findViewById<ImageView>(R.id.glose)
        val d = (v.drawable as android.graphics.drawable.LayerDrawable).let { l -> (0 until l.numberOfLayers).map(l::getDrawable) }.filterIsInstance<AnimatedVectorDrawable>()
        v.draw(Canvas(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888))) // the AVD's animator really starts at its first draw
        assertTrue(d.isNotEmpty() && d.all { it.isRunning })
        c.pause()
        assertFalse(d.any { it.isRunning })
    }

    /** Not a check: control PNGs (every pose, every overlay on the seated pose) in app/build/demo/glose. */
    @Test fun renderControlImages() {
        val out = File("build/demo/glose").apply { mkdirs() }
        fun grid(cells: List<Bitmap>, name: String) {
            val n = 4; val s = cells.first().width
            val g = Bitmap.createBitmap(n * s, (cells.size + n - 1) / n * s, Bitmap.Config.ARGB_8888)
            Canvas(g).apply { drawColor(Color.parseColor("#F6EEDF")); cells.forEachIndexed { i, b -> drawBitmap(b, (i % n) * s.toFloat(), (i / n) * s.toFloat(), null) } }
            File(out, "$name.png").outputStream().use { g.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        grid(all.filterKeys { it.endsWith("_vd") }.toSortedMap().values.map { render(it, size = 384) }, "poses")
        grid(overlays.toSortedMap().values.map { render(R.drawable.glose_assis_vd, listOf(it), 384) }, "overlays_assis")
        val combos = listOf(listOf(R.style.Glose_Pelage_Nuit, R.style.Glose_Acc_Bonnet), listOf(R.style.Glose_Pelage_Enluminure, R.style.Glose_Acc_Noeud),
            listOf(R.style.Glose_Pelage_Creme, R.style.Glose_Pelote_Sauge, R.style.Glose_Acc_Echarpe), listOf(R.style.Glose_Acc_Marque, R.style.Glose_Acc_Plume))
        grid(listOf(R.drawable.glose_livres_vd, R.drawable.glose_soir_vd, R.drawable.glose_ecrit_bravo_vd, R.drawable.glose_icone_vd)
            .flatMap { res -> combos.map { render(res, it, 384) } }, "combos")
    }

    /** Animations off: play() leaves the still, so a written pose keeps its word (reset() used to rewind it to a blank thread). */
    @Test fun writtenPoseKeepsItsWordWithoutAnimations() {
        val scale = android.animation.ValueAnimator::class.java.getMethod("setDurationScale", Float::class.javaPrimitiveType)
        try {
            scale.invoke(null, 0f)
            listOf(R.drawable.glose_ecrit_mot to R.drawable.glose_ecrit_mot_vd, R.drawable.glose_ecrit_bravo_fin to R.drawable.glose_ecrit_bravo_vd).forEach { (avd, vd) ->
                val v = ImageView(ctx).apply { setImageDrawable(Glose.drawable(ctx, avd)) }
                Glose.play(v)
                assertEquals(0, diff(render(vd), Glose.bitmap(v.drawable, 256, 256)))
            }
        } finally { scale.invoke(null, 1f) }
    }

    /** Review 2 I1: an accessory's night outline is always clipped outside the body; unclipped it painted cream over the fur. */
    @Test fun accessoryNightOutlinesAreClipped() {
        val layers = File("src/main/res/drawable").listFiles { f -> f.name.matches(Regex("glose_\\w+_vd_[a-z_]+\\.xml")) }!!
        assertTrue(layers.size > 20)
        val f = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        for (file in layers) {
            val paths = f.newDocumentBuilder().parse(file).getElementsByTagName("path")
            for (i in 0 until paths.length) {
                val p = paths.item(i) as org.w3c.dom.Element
                val halo = (0 until p.attributes.length).any { p.attributes.item(it).nodeValue.matches(Regex("\\?attr/glose\\w*_f0e801")) }
                if (!halo) continue
                var g = p.parentNode
                var clipped = false
                while (g is org.w3c.dom.Element && !clipped) { clipped = g.getElementsByTagName("clip-path").length > 0 && g.tagName == "group"; g = g.parentNode }
                assertTrue("${file.name}: night outline without clip", clipped)
            }
        }
    }

    /** QA F1: the night outline follows the mode even when Glose was drawn in the other mode first (drawable cache). */
    @Test fun nightOutlineFollowsTheModeAfterASwitch() {
        fun cream() = Glose.bitmap(Glose.mine(ctx, R.drawable.glose_assis_vd, accessory = false), 256, 256).let { b ->
            (0 until b.width step 2).sumOf { x -> (0 until b.height step 2).count { y -> b.getPixel(x, y) == 0xFFF0E8D8.toInt() } } }
        assertEquals(0, cream()) // by day: no outline
        org.robolectric.RuntimeEnvironment.setQualifiers("+night")
        assertTrue(cream() > 100) // then at night, same process: the outline shows
        org.robolectric.RuntimeEnvironment.setQualifiers("+notnight")
        assertEquals(0, cream()) // and goes again by day
    }
}
