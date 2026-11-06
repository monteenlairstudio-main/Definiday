package fr.definiday

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.time.LocalDate
import kotlin.random.Random

/** Evening quiz: for each word seen today, its definition and 4 candidate words. */
class QuizActivity : Activity() {
    private lateinit var box: LinearLayout
    /** Frozen when the quiz opens: finished after midnight it still counts for that day; the reminder opens its own day (the eve, after midnight). */
    private var day = 0L
    private var seed = 0
    private lateinit var questions: List<Quiz.Question>
    private var index = 0
    private var score = 0
    private var picked = -1 // option chosen for the current question, -1 = not answered yet
    private var done = false // that day's quiz was already counted when this one opened: its score won't count

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val s = savedInstanceState
        val today = LocalDate.now().toEpochDay()
        day = s?.getLong("day") ?: intent.getLongExtra(EXTRA_DAY, today).takeIf { it in today - 1..today } ?: today
        seed = s?.getInt("seed") ?: Random.nextInt()
        val all = Words.load(this)
        val byMot = all.associateBy { it.mot }
        val seen = s?.getStringArrayList("mots")?.mapNotNull(byMot::get) ?: Words.seenToday(this, day)
        questions = Quiz.build(seen, all, Random(seed)) // same seed and words after a rotation: same options
        index = s?.getInt("index") ?: 0
        score = s?.getInt("score") ?: 0
        picked = s?.getInt("picked") ?: -1
        done = s?.getBoolean("done") ?: Prefs(this).lastQuizDay.let { it == day || it == day + 1 }
        val pad = (24 * resources.displayMetrics.density).toInt()
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        setContentView(ScrollView(this).apply { fitsSystemWindows = true; addView(box) })
        next()
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putLong("day", day); out.putInt("seed", seed); out.putInt("index", index); out.putInt("score", score); out.putInt("picked", picked); out.putBoolean("done", done)
        out.putStringArrayList("mots", ArrayList(questions.map { it.answer.mot }))
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
        box.addView(glose(Glose.mine(this, R.drawable.glose_icone_vd, accessory = false), 56).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO })
        box.addView(title())
        if (day < LocalDate.now().toEpochDay()) box.addView(text(getString(R.string.quiz_eve), 13f))
        val q = questions[index]
        val counter = text("${index + 1} / ${questions.size}", 13f).apply { contentDescription = getString(R.string.quiz_progress, index + 1, questions.size) }
        box.addView(counter)
        box.addView(text(getString(R.string.quiz_prompt), 15f))
        box.addView(text(q.answer.definition, 20f))
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
                if (q.options[i] == q.answer) score++
                reveal()
            }
        }
        if (picked >= 0) reveal() // answered before a rotation: not twice
        focus(counter)
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
            if (done) box.addView(text(getString(R.string.quiz_not_counted), 16f).apply { gravity = Gravity.CENTER })
            if (fresh.isNotEmpty()) {
                box.addView(text(getString(R.string.unlock_new), 16f, bold = true).apply { gravity = Gravity.CENTER; setPadding(0, paddingBottom, 0, 0) })
                box.addView(text(fresh.joinToString("\n") { it.nom }, 16f).apply { gravity = Gravity.CENTER })
                button(getString(R.string.collection_see)).setOnClickListener { startActivity(android.content.Intent(this, CollectionActivity::class.java)) }
            }
        }
        button(getString(R.string.quiz_close)).setOnClickListener { finish() }
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
    class Question(val answer: Word, val options: List<Word>)

    /** 3 distractors per word, same part of speech when possible, never one with the answer's definition (aruspice/haruspice). */
    fun build(seen: List<Word>, all: List<Word>, rnd: Random = Random.Default): List<Question> = seen.map { w ->
        val others = all.filter { it.mot != w.mot && it.definition != w.definition }
        val same = others.filter { it.nature == w.nature }.shuffled(rnd)
        val distractors = (same + others.shuffled(rnd)).distinct().take(3)
        Question(w, (distractors + w).shuffled(rnd))
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
