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
    private val questions by lazy { Quiz.build(Words.seenToday(this), Words.load(this)) }
    private var index = 0
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        setContentView(ScrollView(this).apply { fitsSystemWindows = true; addView(box) })
        next()
    }

    private fun text(s: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = s; textSize = size; if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 0, 0, (12 * resources.displayMetrics.density).toInt())
    }

    private fun next() {
        box.removeAllViews()
        if (index == questions.size) return finishQuiz()
        box.addView(glose(Glose.drawable(this, R.drawable.glose_icone_vd), 56))
        box.addView(text(getString(R.string.quiz_title), 13f, bold = true))
        val q = questions[index]
        box.addView(text("${index + 1} / ${questions.size}", 13f))
        box.addView(text(q.answer.definition, 20f))
        val buttons = q.options.map { w ->
            Button(this).apply { text = w.mot; isAllCaps = false; box.addView(this) }
        }
        buttons.forEachIndexed { i, b ->
            b.setOnClickListener {
                val right = q.options[i] == q.answer
                if (right) score++
                buttons.forEachIndexed { j, o ->
                    o.isEnabled = false
                    if (q.options[j] == q.answer) o.setBackgroundColor(GOOD) else if (j == i) o.setBackgroundColor(BAD)
                }
                box.addView(Button(this).apply {
                    text = getString(R.string.quiz_next); setOnClickListener { index++; next() }
                })
            }
        }
    }

    private fun finishQuiz() {
        box.addView(text(getString(R.string.quiz_title), 13f, bold = true))
        if (questions.isEmpty()) {
            box.addView(text(getString(R.string.quiz_empty), 18f))
        } else {
            Quiz.record(Prefs(this), score, questions.size, LocalDate.now().toEpochDay())
            val passed = Quiz.passed(score, questions.size)
            // the thread writes the word once and holds it; never mocking when failed
            box.addView(glose(Glose.drawable(this, if (passed) R.drawable.glose_ecrit_bravo_fin else R.drawable.glose_ecrit_encore_fin,
                if (score == questions.size) listOf(R.style.Glose_Eclats) else emptyList()), 200).apply {
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
        box.addView(glose(Glose.preview(this, fresh.last()), 160).apply { Glose.play(this) })
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
        private val GOOD = Color.parseColor("#7FB77E")
        private val BAD = Color.parseColor("#E08A7E")
    }
}

object Quiz {
    class Question(val answer: Word, val options: List<Word>)

    /** 3 distractors per word, same part of speech when possible, never another word of the quiz's answer. */
    fun build(seen: List<Word>, all: List<Word>, rnd: Random = Random.Default): List<Question> = seen.map { w ->
        val others = all.filter { it.mot != w.mot }
        val same = others.filter { it.nature == w.nature }.shuffled(rnd)
        val distractors = (same + others.shuffled(rnd)).distinct().take(3)
        Question(w, (distractors + w).shuffled(rnd))
    }

    /** Passed = at most one mistake per 3 questions (1/1, 2/2, 2/3). */
    fun passed(score: Int, total: Int) = score * 3 >= total * 2

    /** Counters the mascot unlocks read. A quiz counts once per day. */
    fun record(p: Prefs, score: Int, total: Int, day: Long) {
        if (p.lastQuizDay == day) return
        p.passedQuizzes += if (passed(score, total)) 1 else 0 // read before perfectQuizzes moves: its default is perfectQuizzes
        p.streak = if (p.lastQuizDay == day - 1) p.streak + 1 else 1
        p.lastQuizDay = day
        p.quizzes += 1
        if (score == total) p.perfectQuizzes += 1
        p.correctAnswers += score
    }
}
