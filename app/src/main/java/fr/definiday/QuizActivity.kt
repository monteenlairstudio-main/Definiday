package fr.definiday

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
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
        val pad = (24 * resources.displayMetrics.density).toInt()
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        setContentView(ScrollView(this).apply { fitsSystemWindows = true; addView(box) })
        next()
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putLong("day", day); out.putInt("seed", seed); out.putInt("index", index); out.putInt("score", score); out.putInt("picked", picked)
        out.putStringArrayList("mots", ArrayList(questions.map { it.answer.mot }))
    }

    private fun text(s: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = s; textSize = size; if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
    }

    private fun next() {
        box.removeAllViews()
        if (index == questions.size) return finishQuiz()
        box.addView(glose(Glose.mine(this, R.drawable.glose_icone_vd, accessory = false), 56))
        box.addView(text(getString(R.string.quiz_title), 13f, bold = true))
        if (day < LocalDate.now().toEpochDay()) box.addView(text(getString(R.string.quiz_eve), 13f))
        val q = questions[index]
        box.addView(text("${index + 1} / ${questions.size}", 13f))
        box.addView(text(q.answer.definition, 20f))
        val buttons = q.options.map { w ->
            Button(this).apply { text = w.mot; isAllCaps = false; box.addView(this) }
        }
        fun reveal() {
            buttons.forEachIndexed { j, o ->
                o.isEnabled = false
                if (q.options[j] == q.answer) o.setBackgroundColor(GOOD) else if (j == picked) o.setBackgroundColor(BAD)
            }
            box.addView(Button(this).apply {
                text = getString(R.string.quiz_next); setOnClickListener { index++; picked = -1; next() }
            })
        }
        buttons.forEachIndexed { i, b -> b.setOnClickListener { picked = i; if (q.options[i] == q.answer) score++; reveal() } }
        if (picked >= 0) reveal() // answered before a rotation: not twice
    }

    private fun finishQuiz() {
        box.addView(text(getString(R.string.quiz_title), 13f, bold = true))
        if (questions.isEmpty()) {
            box.addView(text(getString(R.string.quiz_empty), 18f))
        } else {
            Quiz.record(Prefs(this), score, questions.size, day)
            val passed = Quiz.passed(score, questions.size)
            // the thread writes the word once and holds it; never mocking when failed
            box.addView(glose(Glose.mine(this, if (passed) R.drawable.glose_ecrit_bravo_fin else R.drawable.glose_ecrit_encore_fin,
                extra = if (score == questions.size) listOf(R.style.Glose_Eclats) else emptyList()), 200).apply {
                contentDescription = getString(if (passed) R.string.glose_bravo else R.string.glose_encore)
                Glose.play(this)
            })
            box.addView(text(getString(R.string.quiz_score, score, questions.size), 24f, bold = true).apply { gravity = Gravity.CENTER })
            if (!passed) box.addView(text(getString(R.string.quiz_retry), 16f).apply { gravity = Gravity.CENTER })
            announce()
        }
        box.addView(Button(this).apply { text = getString(R.string.quiz_close); setOnClickListener { finish() } })
    }

    /** "Nouveau : …" once per unlocked item, with Glose showing the last (rarest) one. */
    private fun announce() {
        val p = Prefs(this)
        val fresh = Unlocks.update(p).ifEmpty { return }
        p.announced = p.announced + fresh.map { it.id }
        box.addView(text(getString(R.string.unlock_new, fresh.joinToString(", ") { it.nom }), 18f, bold = true).apply { gravity = Gravity.CENTER })
        box.addView(glose(Glose.preview(this, fresh.last(), Glose.choice(p)), 160).apply { Glose.play(this) })
        box.addView(Button(this).apply {
            text = getString(R.string.collection_see)
            setOnClickListener { startActivity(android.content.Intent(context, CollectionActivity::class.java)) }
        })
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
        private val GOOD = Color.parseColor("#7FB77E")
        private val BAD = Color.parseColor("#E08A7E")
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

    /** Counters the mascot unlocks read. A quiz counts once per day (the eve's, finished after today's, not at all). */
    fun record(p: Prefs, score: Int, total: Int, day: Long) {
        val last = p.lastQuizDay
        if (last == day || last == day + 1) return
        p.passedQuizzes += if (passed(score, total)) 1 else 0 // read before perfectQuizzes moves: its default is perfectQuizzes
        // days without a word (quote-only slots, current settings) don't break the streak; gap bounded (and MIN_VALUE overflows out of it)
        val unbroken = day - last in 1..7 && (last + 1 until day).all { Words.quotesOnly(it, p.wordsPerDay, p.moreQuotes) }
        p.streak = if (unbroken) p.streak + 1 else 1
        p.lastQuizDay = day
        p.quizzes += 1
        if (score == total) p.perfectQuizzes += 1
        p.correctAnswers += score
    }
}
