package fr.definiday

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
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

/** Evening quiz: for each word seen today, then up to 2 past words due for review, its definition (or its example with a blank) and 4 candidate words.
 *  Review questions count in the score like the others: « passed » stays 2/3 of all the questions, and so do the counters the unlocks read. */
class QuizActivity : Activity() {
    private lateinit var box: LinearLayout
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val s = savedInstanceState
        val today = LocalDate.now().toEpochDay()
        day = s?.getLong("day") ?: intent.getLongExtra(EXTRA_DAY, today).takeIf { it in today - 1..today } ?: today
        seed = s?.getInt("seed") ?: Random.nextInt()
        val all = Words.load(this)
        val byMot = all.associateBy { it.mot }
        shown = History(this).all().filterKeys(byMot::containsKey)
        val saved = s?.getStringArrayList("mots")?.mapNotNull(byMot::get)
        val reviews = s?.getInt("reviews") ?: 0
        val seen = saved?.dropLast(reviews) ?: Words.seenToday(this, day)
        // reviews join an evening that has words of its own (review is in the quiz only: the daily display keeps its 9 months)
        val review = saved?.takeLast(reviews)
            ?: if (seen.isEmpty()) emptyList() else Quiz.due(Memory(this).entries(), shown, day, seen.map { it.mot }.toSet()).mapNotNull(byMot::get)
        questions = Quiz.build(seen, all, Random(seed), shown.keys, review) // same seed and words after a rotation: same options
        index = s?.getInt("index") ?: 0
        score = s?.getInt("score") ?: 0
        picked = s?.getInt("picked") ?: -1
        done = s?.getBoolean("done") ?: Prefs(this).lastQuizDay.let { it == day || it == day + 1 }
        val pad = (24 * resources.displayMetrics.density).toInt()
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        setContentView(ScrollView(this).apply { fitsSystemWindows = true; addView(box) })
        next()
    }

    override fun onPause() {
        super.onPause()
        icon?.let(Glose::stop) // a pending reaction never returns to idle on a hidden screen
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putLong("day", day); out.putInt("seed", seed); out.putInt("index", index); out.putInt("score", score); out.putInt("picked", picked); out.putBoolean("done", done)
        out.putStringArrayList("mots", ArrayList(questions.map { it.answer.mot })); out.putInt("reviews", questions.count { it.review })
    }

    private fun text(s: String, size: Float, bold: Boolean = false) = TextView(this, null, 0, R.style.Body).apply {
        text = s; textSize = size; if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
    }

    private fun title() = TextView(this, null, 0, R.style.Label).apply {
        text = getString(R.string.quiz_title); setPadding(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
    }

    /** Full-width button, 8 dp apart. */
    private fun button(s: String) = Button(this).apply {
        text = s
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            .apply { topMargin = (8 * resources.displayMetrics.density).toInt() }
        box.addView(this)
    }

    private fun next() {
        box.removeAllViews()
        if (index == questions.size) return finishQuiz()
        box.addView(glose(Glose.mine(this, R.drawable.glose_icone_vd, accessory = false), 56).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO; icon = this })
        box.addView(title())
        if (day < LocalDate.now().toEpochDay()) box.addView(text(getString(R.string.quiz_eve), 13f))
        val q = questions[index]
        val counter = progress()
        box.addView(counter)
        if (q.review) box.addView(text(getString(R.string.quiz_review), 13f))
        box.addView(text(getString(if (q.cloze != null) R.string.quiz_prompt_cloze else R.string.quiz_prompt), 15f))
        box.addView(text(q.cloze ?: q.answer.definition, 20f).apply { q.cloze?.let { contentDescription = it.replace(Quiz.BLANK, getString(R.string.quiz_blank)) } })
        val buttons = q.options.map { button(it.mot) }
        // said on screen and read by TalkBack when it appears (announceForAccessibility is deprecated)
        val result = text("", 16f).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE; visibility = View.GONE; setPadding(0, paddingBottom, 0, paddingBottom) }
        box.addView(result)
        fun reveal() {
            buttons.forEachIndexed { j, o ->
                o.isEnabled = false
                val good = q.options[j] == q.answer
                if (good || j == picked) { // said in the text, not by the color alone; tint keeps the button's shape
                    o.text = (if (good) "✓ " else "✗ ") + q.options[j].mot
                    o.background.mutate().setTint(getColor(if (good) R.color.quiz_good else R.color.quiz_bad))
                    o.setTextColor(getColor(R.color.quiz_on))
                }
            }
            result.text = if (q.options[picked] == q.answer) getString(R.string.quiz_right) else getString(R.string.quiz_wrong, q.answer.mot)
            result.visibility = View.VISIBLE
            button(getString(R.string.quiz_next)).setOnClickListener { index++; picked = -1; next() }
        }
        buttons.forEachIndexed { i, b ->
            b.setOnClickListener {
                picked = i
                val right = q.options[i] == q.answer
                if (right) score++
                // counted even in a quiz redone the same day: memory is not a reward
                Memory(this).answered(q.answer.mot, right, day, shown[q.answer.mot] ?: day, q.review)
                reveal()
                react(b, right)
            }
        }
        if (picked >= 0) reveal() // answered before a rotation: not twice
        focus(counter)
    }

    /** « 2 / 4 » as yarn balls, one per question, filled up to the current one; TalkBack reads « Question 2 sur 4 ». */
    private fun progress() = LinearLayout(this).apply {
        contentDescription = getString(R.string.quiz_progress, index + 1, questions.size)
        if (Build.VERSION.SDK_INT >= 28) isScreenReaderFocusable = true
        val px = (18 * resources.displayMetrics.density).toInt()
        repeat(questions.size) { i ->
            addView(ImageView(context).apply {
                setImageDrawable(if (i <= index) Glose.mine(context, R.drawable.pelote) else getDrawable(R.drawable.pelote_vide))
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }, LinearLayout.LayoutParams(px, px).apply { marginEnd = px / 3 })
        }
        setPadding(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
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

    private fun finishQuiz() {
        box.addView(title())
        if (questions.isEmpty()) {
            box.addView(text(getString(R.string.quiz_empty), 18f).also(::focus))
        } else {
            val p = Prefs(this)
            Quiz.record(p, score, questions.size, day)
            val passed = Quiz.passed(score, questions.size)
            val fresh = Unlocks.update(p) // announced once each
            p.announced = p.announced + fresh.map { it.id }
            Sounds.play(this, if (fresh.isNotEmpty()) Sounds.STAMP else if (passed) Sounds.CHIME else 0) // failed: silence, never a mocking sound
            // something unlocked: one Glose, wearing the last (rarest) one; else the thread writes the word once and holds it, never mocking when failed
            box.addView(if (fresh.isNotEmpty()) glose(Glose.preview(this, fresh.last(), Glose.choice(p)), 200).apply { Glose.play(this) }
            else glose(Glose.mine(this, if (passed) R.drawable.glose_ecrit_bravo_fin else R.drawable.glose_ecrit_encore_fin,
                extra = if (score == questions.size) listOf(R.style.Glose_Eclats) else emptyList()), 200).apply {
                contentDescription = getString(if (passed) R.string.glose_bravo else R.string.glose_encore)
                Glose.play(this)
            })
            box.addView(text(getString(R.string.quiz_score, score, questions.size), 24f, bold = true).apply {
                gravity = Gravity.CENTER; contentDescription = getString(R.string.quiz_score_desc, score, questions.size); focus(this)
            })
            if (!passed) box.addView(text(getString(R.string.quiz_retry), 16f).apply { gravity = Gravity.CENTER })
            else if (fresh.isEmpty()) box.addView(text(getString(R.string.quiz_passed), 16f).apply { gravity = Gravity.CENTER }) // a bravo, no fanfare
            if (done) box.addView(text(getString(R.string.quiz_not_counted), 16f).apply { gravity = Gravity.CENTER })
            if (fresh.isNotEmpty()) {
                box.addView(text(getString(R.string.unlock_new), 16f, bold = true).apply { gravity = Gravity.CENTER; setPadding(0, paddingBottom, 0, 0) })
                stamps(fresh, Glose.choice(p))
                button(getString(R.string.collection_see)).setOnClickListener { startActivity(android.content.Intent(this, CollectionActivity::class.java)) }
            }
        }
        button(getString(R.string.quiz_close)).setOnClickListener { finish() }
    }

    /** Unboxing: up to 3 stamps (Glose wearing the item, its name), cascading in, then « + N dans la collection ». */
    private fun stamps(fresh: List<Unlocks.Item>, c: Map<Unlocks.Kind, String>) {
        val (shown, more) = Quiz.stamps(fresh)
        val d = resources.displayMetrics.density
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_HORIZONTAL; weightSum = 3f; setPadding(0, (8 * d).toInt(), 0, 0) }
        val move = ValueAnimator.areAnimatorsEnabled()
        shown.forEachIndexed { i, item ->
            val stamp = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setBackgroundResource(R.drawable.stamp)
                val pad = (8 * d).toInt()
                setPadding(pad, pad, pad, pad)
                addView(glose(Glose.preview(context, item, c), 72).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO })
                addView(text(item.nom, 14f).apply { gravity = Gravity.CENTER; setPadding(0, pad / 2, 0, 0) })
            }
            row.addView(stamp, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = (4 * d).toInt(); marginEnd = (4 * d).toInt() })
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
        contentDescription = getString(R.string.glose)
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
