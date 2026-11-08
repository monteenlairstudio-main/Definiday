package fr.definiday

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.time.LocalDate
import kotlin.random.Random

/** Evening quiz, a letter from Glose (« Correspondance »): header (Glose's head, « Question 2 sur 4 », title, stamps for the progress),
 *  then a Seyès sheet opening on « Bonsoir, » with the question and its answers as notebook check-box rows; the score screen closes the letter.
 *  For each word seen today, then up to 2 past words due for review, its definition (or its example with a blank) and 4 candidate words.
 *  Review questions count in the score like the others: « passed » stays 2/3 of all the questions, and so do the counters the unlocks read. */
class QuizActivity : Activity() {
    private lateinit var page: LinearLayout // the screen, under the ScrollView
    private lateinit var box: LinearLayout // the letter being written: button() adds to it
    private lateinit var shown: Map<String, Long> // History, words only
    /** Frozen when the quiz opens: finished after midnight it still counts for that day; the reminder opens its own day (the eve, after midnight). */
    private var day = 0L
    private var seed = 0
    private lateinit var questions: List<Quiz.Question>
    private var index = 0
    private var score = 0
    private var picked = -1 // option chosen for the current question, -1 = not answered yet
    private var icon: ImageView? = null // small Glose at the top of each question: nods or tilts its head on answers
    private var done = false // that day's quiz was already counted when this one opened: its score won't count
    private var missed = ArrayList<String>() // words answered wrong, listed « À revoir » at the end
    private var won: List<Unlocks.Item>? = null // the score screen's unlocks, null until it was played once: a rotation redraws it, silently
    private var rated = false // the rate card is on the score screen (until folded)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val all = Words.load(this)
        val byMot = all.associateBy { it.mot }
        shown = History(this).all().filterKeys(byMot::containsKey)
        if (savedInstanceState == null) start(all, byMot) else restore(savedInstanceState, all, byMot)
        val pad = (16 * resources.displayMetrics.density).toInt()
        page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad / 2, pad, pad * 3 / 2) }
        setContentView(ScrollView(this).apply { fitsSystemWindows = true; addView(page) })
        next()
    }

    /** A new quiz: the day asked (today or the eve, else today), its words, then the reviews due. */
    private fun start(all: List<Word>, byMot: Map<String, Word>) {
        val today = LocalDate.now().toEpochDay()
        day = intent.getLongExtra(EXTRA_DAY, today).takeIf { it in today - 1..today } ?: today
        seed = Random.nextInt()
        val seen = Words.seenToday(this, day)
        // reviews join an evening that has words of its own (review is in the quiz only: the daily display keeps its 9 months)
        val review = if (seen.isEmpty()) emptyList() else Quiz.due(Memory(this).entries(), shown, day, seen.map { it.mot }.toSet()).mapNotNull(byMot::get)
        questions = Quiz.build(seen, all, Random(seed), shown.keys, review)
        done = Prefs(this).lastQuizDay.let { it == day || it == day + 1 }
    }

    /** After a rotation: the same day, words and seed (same options), where the quiz was, and the score screen as it was played. */
    private fun restore(s: Bundle, all: List<Word>, byMot: Map<String, Word>) {
        day = s.getLong("day")
        seed = s.getInt("seed")
        val saved = s.getStringArrayList("mots").orEmpty().mapNotNull(byMot::get)
        val reviews = s.getInt("reviews")
        questions = Quiz.build(saved.dropLast(reviews), all, Random(seed), shown.keys, saved.takeLast(reviews))
        index = s.getInt("index")
        score = s.getInt("score")
        picked = s.getInt("picked")
        missed = s.getStringArrayList("missed") ?: missed
        won = s.getStringArrayList("won")?.mapNotNull(Unlocks.byId::get)
        rated = s.getBoolean("rated")
        done = s.getBoolean("done")
    }

    override fun onResume() {
        super.onResume()
        Sounds.hold(this) // sounds on: loaded before the first answer, kept while the quiz is open
    }

    override fun onPause() {
        super.onPause()
        icon?.let(Glose::stop) // a pending reaction never returns to idle on a hidden screen
        Sounds.letGo()
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putLong("day", day); out.putInt("seed", seed); out.putInt("index", index); out.putInt("score", score); out.putInt("picked", picked); out.putBoolean("done", done)
        out.putStringArrayList("missed", missed); out.putBoolean("rated", rated)
        won?.let { w -> out.putStringArrayList("won", ArrayList(w.map { it.id })) }
        out.putStringArrayList("mots", ArrayList(questions.map { it.answer.mot })); out.putInt("reviews", questions.count { it.review })
    }

    private fun text(s: String, size: Float, bold: Boolean = false) = TextView(this, null, 0, R.style.Body).apply {
        text = s; textSize = size; if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
    }

    private fun title() = TextView(this, null, 0, R.style.Title).apply { text = getString(R.string.quiz_title) }

    /** Starts the letter: a sheet (card_soft) added to the page, where text and buttons go from now on. */
    private fun letter() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        val d = resources.displayMetrics.density
        setBackgroundResource(R.drawable.card_soft)
        setPadding((20 * d).toInt(), (16 * d).toInt(), (20 * d).toInt(), (16 * d).toInt())
        page.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = (12 * d).toInt() })
        box = this
    }

    /** Full-width button, 8 dp apart, in the letter; [style] 0 = Btn. */
    private fun button(s: String, style: Int = 0) = (if (style == 0) Button(this) else Button(this, null, 0, style)).apply {
        text = s
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            .apply { topMargin = (8 * resources.displayMetrics.density).toInt() }
        box.addView(this)
    }

    private fun next() {
        page.removeAllViews()
        if (index == questions.size) return finishQuiz()
        val d = resources.displayMetrics.density
        val q = questions[index]
        // header: Glose's head (it nods or tilts on answers), the postal « Question 2 sur 4 » over the title, the stamps under it
        val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }.also(page::addView)
        head.addView(glose(Glose.mine(this, R.drawable.glose_icone_vd, accessory = false), 72).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO; icon = this })
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding((12 * d).toInt(), 0, 0, 0) }
        head.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val counter = TextView(this, null, 0, R.style.Postal).apply {
            contentDescription = getString(R.string.quiz_progress, index + 1, questions.size)
            text = contentDescription.toString().replace(" sur ", " sur ") // « 1 sur 4 » never broken at large font sizes
        }
        col.addView(counter)
        col.addView(title())
        col.addView(progress())
        letter()
        box.addView(TextView(this, null, 0, R.style.Note).apply { setText(R.string.quiz_hello) })
        if (day < LocalDate.now().toEpochDay()) box.addView(TextView(this, null, 0, R.style.Postal).apply { setText(R.string.quiz_eve); setPadding(0, (4 * d).toInt(), 0, 0) })
        if (q.review) box.addView(TextView(this, null, 0, R.style.Label).apply { setText(R.string.quiz_review); setPadding(0, (4 * d).toInt(), 0, 0) })
        box.addView(text(getString(if (q.cloze != null) R.string.quiz_prompt_cloze else R.string.quiz_prompt), 15f).apply {
            setTextColor(getColor(R.color.ink2)); setPadding(0, (8 * d).toInt(), 0, (8 * d).toInt())
        })
        // the definition in ink; a blanked example in plume (a quotation: the colour marks it, no italic)
        val asked = TextView(this, null, 0, if (q.cloze != null) R.style.Example else R.style.Body).apply {
            text = q.cloze ?: q.answer.definition; textSize = 19f; setPadding(0, 0, 0, (12 * d).toInt())
            q.cloze?.let { contentDescription = it.replace(Quiz.BLANK, getString(R.string.quiz_blank)) }
        }
        box.addView(asked)
        box.background = LayerDrawable(arrayOf(getDrawable(R.drawable.card_soft)!!, Postal.Seyes(this, asked)))
        val buttons = q.options.map { o -> button(o.mot).apply {
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.quiz_box, 0, 0, 0); compoundDrawablePadding = (14 * d).toInt()
        } }
        // said on screen and read by TalkBack when it appears (announceForAccessibility is deprecated)
        val result = text("", 16f).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE; visibility = View.GONE; setPadding(0, paddingBottom, 0, 0) }
        box.addView(result)
        fun reveal() {
            buttons.forEachIndexed { j, o ->
                o.isEnabled = false
                val good = q.options[j] == q.answer
                // right and wrong are said by an icon in the box's place (the word stays where it was) and to TalkBack, not by the colour alone;
                // the other boxes go ink2 with the disabled state (quiz_box_edge)
                if (good || j == picked) { // the chosen one keeps the selection outline
                    val box = o.compoundDrawablesRelative[0]!!.bounds
                    o.setCompoundDrawablesRelative(getDrawable(if (good) R.drawable.ic_check else R.drawable.ic_cross)!!.apply { bounds = box }, null, null, null)
                    o.compoundDrawableTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.quiz_on))
                    o.contentDescription = getString(if (good) R.string.quiz_answer_right else R.string.quiz_answer_picked, q.options[j].mot)
                    o.background = GradientDrawable().apply {
                        cornerRadius = resources.getDimension(R.dimen.radius_paper)
                        setColor(getColor(if (good) R.color.quiz_good else R.color.quiz_bad))
                        if (j == picked) setStroke((2 * d).toInt(), getColor(R.color.accent))
                    }
                    o.isSelected = j == picked
                    o.setTextColor(getColor(R.color.quiz_on))
                }
            }
            result.text = if (q.options[picked] == q.answer) getString(R.string.quiz_right) else getString(R.string.quiz_wrong, q.answer.mot)
            result.visibility = View.VISIBLE
            button(getString(R.string.quiz_next), R.style.Primary).setOnClickListener { index++; picked = -1; next() }
        }
        buttons.forEachIndexed { i, b ->
            b.setOnClickListener {
                picked = i
                val right = q.options[i] == q.answer
                if (right) score++ else missed += q.answer.mot
                // counted even in a quiz redone the same day: memory is not a reward
                Memory(this).answered(q.answer.mot, right, day, shown[q.answer.mot] ?: day, q.review)
                reveal()
                react(b, right)
            }
        }
        if (picked >= 0) reveal() // answered before a rotation: not twice
        focus(counter)
    }

    /** The progress as small stamps (24 x 30 dp, no caption), decorative: answered ones in plume, the current one open, the next ones kraft.
     *  The written « Question 2 sur 4 » above says it. */
    private fun progress() = LinearLayout(this).apply {
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        val d = resources.displayMetrics.density
        repeat(questions.size) { i ->
            val (bg, fg) = when {
                i < index -> R.color.accent to R.color.bg
                i == index -> R.color.sheet to R.color.accent
                else -> R.color.surface to R.color.kraft_dark
            }
            addView(ImageView(context).apply { setImageDrawable(Postal.Stamp(context, getColor(bg), getColor(fg), "", android.graphics.Typeface.SERIF, caption = null)) },
                LinearLayout.LayoutParams((24 * d).toInt(), (30 * d).toInt()).apply { marginEnd = (6 * d).toInt() })
        }
        setPadding(0, (8 * d).toInt(), 0, 0)
    }

    /** One short pulse on the chosen button: right, it pops (1 → 1.04 → 1); wrong, it shakes gently (±6 dp), never the screen.
     *  Haptics follow the system setting; motion is skipped when animations are off. */
    private fun react(b: View, right: Boolean) {
        if (Build.VERSION.SDK_INT >= 30) b.performHapticFeedback(if (right) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT)
        else if (right) b.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        Sounds.play(this, if (right) Sounds.RIGHT else Sounds.WRONG)
        icon?.let { Glose.once(it, if (right) R.drawable.glose_icone_hoche else R.drawable.glose_icone_penche, R.drawable.glose_icone_vd, accessory = false) }
        if (!ValueAnimator.areAnimatorsEnabled()) return
        b.stateListAnimator = null // its release (to scale 1) would fight the pop; the button stays disabled until next()
        if (right) b.animate().scaleX(1.04f).scaleY(1.04f).setDuration(90).withEndAction {
            b.animate().scaleX(1f).scaleY(1f).setInterpolator(OvershootInterpolator(3f)).setDuration(130)
        }
        else ObjectAnimator.ofFloat(b, View.TRANSLATION_X, *floatArrayOf(0f, 1f, -1f, 1f, -1f, 0f).map { it * 6 * resources.displayMetrics.density }.toFloatArray())
            .setDuration(250).start()
    }

    /** removeAllViews() drops TalkBack's focus: put it back on the new screen's first useful line. */
    private fun focus(v: View) = v.post { v.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null) }

    /** The end of the letter: Glose signs it by writing with his yarn (or wears what he won), the score, the stamps won. */
    private fun finishQuiz() {
        page.addView(title())
        letter()
        if (questions.isEmpty()) box.addView(text(getString(R.string.quiz_empty), 18f).also(::focus))
        else scoreScreen(Prefs(this))
        button(getString(R.string.quiz_close), R.style.Primary).setOnClickListener { finish() }
    }

    private fun scoreScreen(p: Prefs) {
        val passed = Quiz.passed(score, questions.size)
        val fresh = won ?: record(p, passed)
        box.addView(signature(p, fresh, passed))
        box.addView(text(getString(R.string.quiz_score, score, questions.size), 28f, bold = true).apply {
            gravity = Gravity.CENTER; contentDescription = getString(R.string.quiz_score_desc, score, questions.size); focus(this)
        })
        if (missed.isNotEmpty()) box.addView(text(getString(R.string.quiz_missed, missed.joinToString(", ")), 16f).apply { gravity = Gravity.CENTER })
        if (!passed) box.addView(text(getString(R.string.quiz_retry), 16f).apply { gravity = Gravity.CENTER })
        else if (fresh.isEmpty()) box.addView(text(getString(R.string.quiz_passed), 16f).apply { gravity = Gravity.CENTER }) // a bravo, no fanfare
        if (done) box.addView(text(getString(R.string.quiz_not_counted), 16f).apply { gravity = Gravity.CENTER })
        if (rated) rate()
        if (fresh.isNotEmpty()) {
            box.addView(text(getString(if (passed) R.string.unlock_new else R.string.unlock_new_anyway, p.gloseName), 16f, bold = true).apply { gravity = Gravity.CENTER; setPadding(0, paddingBottom, 0, 0) })
            stamps(fresh, Glose.choice(p))
            button(getString(R.string.collection_see)).setOnClickListener { startActivity(android.content.Intent(this, CollectionActivity::class.java)) }
        }
    }

    /** The first time the score screen shows: recorded, announced (once each) and heard once; returns what was unlocked. */
    private fun record(p: Prefs, passed: Boolean): List<Unlocks.Item> {
        Quiz.record(p, score, questions.size, day)
        val got = Unlocks.update(p)
        p.announced = p.announced + got.map { it.id }
        won = got
        rated = Quiz.fromPlay(this) && Quiz.askRate(p, passed, counted = !done)
        Sounds.play(this, if (got.isNotEmpty()) Sounds.STAMP else if (passed) Sounds.CHIME else 0) // failed: silence, never a mocking sound
        return got
    }

    /** Something unlocked: one Glose, wearing the last (rarest) one; else the thread writes the word once and holds it, never mocking when failed. */
    private fun signature(p: Prefs, fresh: List<Unlocks.Item>, passed: Boolean) =
        if (fresh.isNotEmpty()) glose(Glose.preview(this, fresh.last(), Glose.choice(p)), 200).apply { Glose.play(this) }
        else glose(Glose.mine(this, if (passed) R.drawable.glose_ecrit_bravo_fin else R.drawable.glose_ecrit_encore_fin,
            extra = if (score == questions.size) listOf(R.style.Glose_Eclats) else emptyList()), 200).apply {
            contentDescription = getString(if (passed) R.string.glose_bravo else R.string.glose_encore, p.gloseName)
            Glose.play(this)
        }

    /** Gentle rate card, never blocking: « Plus tard » folds it, « Noter » opens the store page (the web page without the Play Store). */
    private fun rate() {
        val pad = (16 * resources.displayMetrics.density).toInt()
        // Glose's note paper inside the letter; « Plus tard » in plume serif: 6.55:1 on warn (audit 011 A1 was 4.22:1)
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundResource(R.drawable.note_paper); setPadding(pad * 3 / 2, pad, pad, pad / 2) }
        card.addView(text(getString(R.string.rate_text), 16f))
        val row = LinearLayout(this).apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL }.also(card::addView)
        row.addView(Button(this, null, 0, R.style.Link).apply { setText(R.string.rate_later); setOnClickListener { card.visibility = View.GONE; rated = false } })
        row.addView(Button(this).apply {
            setText(R.string.rate_go)
            setOnClickListener {
                card.visibility = View.GONE; rated = false
                runCatching { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$packageName"))) }
                    .recoverCatching { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))) }
            }
        })
        box.addView(card, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = pad })
    }

    /** Unboxing: up to 3 album stamps (Glose wearing the item) with their names, cascading in, then « + N dans la collection ».
     *  No cancel: a new stamp is not worn yet. */
    private fun stamps(fresh: List<Unlocks.Item>, c: Map<Unlocks.Kind, String>) {
        val (shown, more) = Quiz.stamps(fresh)
        val d = resources.displayMetrics.density
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_HORIZONTAL; weightSum = 3f; setPadding(0, (8 * d).toInt(), 0, 0) }
        val move = ValueAnimator.areAnimatorsEnabled()
        shown.forEachIndexed { i, item ->
            val stamp = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                addView(CollectionActivity.stamp(context, Glose.preview(context, item, c), R.color.surface)) // kraft: a sheet stamp vanishes on the letter
                addView(text(item.nom, 14f).apply { gravity = Gravity.CENTER; setPadding(0, (6 * d).toInt(), 0, 0) })
            }
            row.addView(stamp, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = (2 * d).toInt(); marginEnd = (2 * d).toInt() })
            if (move) {
                stamp.alpha = 0f; stamp.translationY = 16 * d
                stamp.animate().alpha(1f).translationY(0f).setStartDelay(150L + 80L * i).setDuration(250).setInterpolator(OvershootInterpolator())
            }
        }
        box.addView(row)
        if (more > 0) box.addView(text(getString(R.string.unlock_more, more), 15f).apply { gravity = Gravity.CENTER; setPadding(0, (12 * d).toInt(), 0, (4 * d).toInt()) })
    }

    private fun glose(d: android.graphics.drawable.Drawable, dp: Int) = ImageView(this).apply {
        setImageDrawable(d)
        contentDescription = getString(R.string.glose, Prefs(context).gloseName)
        val px = (dp * resources.displayMetrics.density).toInt()
        layoutParams = LinearLayout.LayoutParams(px, px).apply { gravity = Gravity.CENTER_HORIZONTAL }
    }

    companion object {
        /** Epoch day of the quiz (the reminder's day); only that day or the eve is accepted. */
        const val EXTRA_DAY = "day"
    }
}

object Quiz {
    /** [cloze]: the word's example with the word blanked, asked instead of its definition; [review]: a past word due on the ladder. */
    class Question(val answer: Word, val options: List<Word>, val cloze: String? = null, val review: Boolean = false)

    /** Review ladder: days after the start (first shown, or last miss) at which a word comes back in the quiz. */
    val LADDER = longArrayOf(2, 7, 30, 90)
    const val MAX_REVIEWS = 2
    const val BLANK = "_____"

    /**
     * Tonight's [words], then the [review] ones. 3 distractors each: same part of speech first, and within it words already [seen]
     * (but not tonight's answers, or every question would offer the same few words); never one with the answer's definition (aruspice/haruspice).
     * One question in 3 asks for the blank of the word's example instead of its definition, when the example holds the word.
     */
    fun build(words: List<Word>, all: List<Word>, rnd: Random = Random.Default, seen: Set<String> = emptySet(), review: List<Word> = emptyList()): List<Question> {
        val known = seen - (words + review).map { it.mot }.toSet()
        return (words + review).mapIndexed { i, w ->
            val (old, fresh) = all.filter { it.mot != w.mot && it.definition != w.definition }.shuffled(rnd).partition { it.mot in known }
            val distractors = (old + fresh).sortedBy { it.nature != w.nature }.take(3) // stable: seen before unseen within each group
            Question(w, (distractors + w).shuffled(rnd), cloze(w)?.takeIf { rnd.nextInt(3) == 0 }, review = i >= words.size)
        }
    }

    /** [w]'s example with the word blanked, as written (« abaques », « s’acagnardait »): the headword, else its first letters
     *  (all but the last 2: verbs in -er/-ir/-re, -eux/-euse); null when the example does not hold it (« bayer », « ouïr »). */
    fun cloze(w: Word): String? {
        val starts = listOfNotNull(w.mot, w.mot.dropLast(2).takeIf { w.mot.length >= 6 })
        val m = starts.firstNotNullOfOrNull { Regex("(?<!\\p{L})" + Regex.escape(it) + "\\p{L}*", RegexOption.IGNORE_CASE).find(w.exemple) } ?: return null
        return w.exemple.replaceRange(m.range, BLANK)
    }

    /** Day [e] is next due, null once learnt. */
    fun dueDay(e: Memory.Entry) = LADDER.getOrNull(e.step)?.let { e.start + it }

    /** Up to [max] words of [history] (mot -> day shown) due on [day], most overdue first, outside [exclude] (tonight's own words).
     *  A word never answered yet starts on its History day: words seen before this version join the ladder too. */
    fun due(entries: Map<String, Memory.Entry>, history: Map<String, Long>, day: Long, exclude: Set<String>, max: Int = MAX_REVIEWS): List<String> =
        history.mapNotNull { (mot, shown) -> dueDay(entries[mot] ?: Memory.Entry(shown))?.takeIf { it <= day && mot !in exclude }?.let { mot to it } }
            .sortedWith(compareBy({ it.second }, { it.first })).take(max).map { it.first }

    /** After an answer on [day]: missed, the ladder starts again from [day] (J+2); right while due, it climbs to the first rung still ahead
     *  (a long-overdue word does not come back every evening); right before it is due (tonight's word), nothing moves. */
    fun after(e: Memory.Entry, right: Boolean, day: Long): Memory.Entry = when {
        !right -> Memory.Entry(day, 0, e.misses + 1)
        (dueDay(e) ?: Long.MAX_VALUE) > day -> e
        else -> e.copy(step = LADDER.indexOfFirst { e.start + it > day }.takeIf { it >= 0 } ?: LADDER.size)
    }

    /** Unboxing: at most 3 stamps, the last (rarest, the one Glose wears) ones; the rest is only counted (« + N dans la collection »). */
    fun <T> stamps(fresh: List<T>) = fresh.takeLast(3) to maxOf(0, fresh.size - 3)

    /** Installed by the Play Store: the rate card has a listing to open (the GitHub releases' APK has no installer, or another one). */
    fun fromPlay(c: android.content.Context) = runCatching {
        if (Build.VERSION.SDK_INT >= 30) c.packageManager.getInstallSourceInfo(c.packageName).installingPackageName
        else @Suppress("DEPRECATION") c.packageManager.getInstallerPackageName(c.packageName)
    }.getOrNull() == "com.android.vending"

    /** The rate card, once: on the score screen of a passed quiz that counted, from the 5th passed one; never after a failed quiz. */
    fun askRate(p: Prefs, passed: Boolean, counted: Boolean): Boolean {
        if (!passed || !counted || p.passedQuizzes < 5 || p.rateAsked) return false
        p.rateAsked = true
        return true
    }

    /** Passed = at most one mistake per 3 questions (1/1, 2/2, 2/3). */
    fun passed(score: Int, total: Int) = score * 3 >= total * 2

    /** A quiz on [day] would continue the streak: last quiz that day, the eve, or before days without a word
     *  (quote-only slots, current settings); gap bounded (and MIN_VALUE overflows out of it). */
    fun unbroken(p: Prefs, day: Long): Boolean {
        val last = p.lastQuizDay
        return day - last in 0..7 && (last + 1 until day).all { Words.quotesOnly(it, p.wordsPerDay, p.moreQuotes) }
    }

    /** Counters the mascot unlocks read. A quiz counts once per day (the eve's, finished after today's, not at all). */
    fun record(p: Prefs, score: Int, total: Int, day: Long) {
        val last = p.lastQuizDay
        if (last == day || last == day + 1) return
        p.passedQuizzes += if (passed(score, total)) 1 else 0 // read before perfectQuizzes moves: its default is perfectQuizzes
        // days without a word (quote-only slots, current settings) don't break the streak; gap bounded (and MIN_VALUE overflows out of it)
        p.streak = if (unbroken(p, day)) p.streak + 1 else 1
        p.lastQuizDay = day
        p.quizzes += 1
        if (score == total) p.perfectQuizzes += 1
        p.correctAnswers += score
    }
}
