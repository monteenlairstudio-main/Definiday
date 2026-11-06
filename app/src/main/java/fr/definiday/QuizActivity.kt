package fr.definiday

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
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
        box.addView(text(getString(R.string.quiz_title), 13f, bold = true))
        if (index == questions.size) return finishQuiz()
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
        if (questions.isEmpty()) {
            box.addView(text(getString(R.string.quiz_empty), 18f))
        } else {
            Quiz.record(Prefs(this), score, questions.size, LocalDate.now().toEpochDay())
            box.addView(text(getString(R.string.quiz_score, score, questions.size), 24f, bold = true).apply { gravity = Gravity.CENTER })
        }
        box.addView(Button(this).apply { text = getString(R.string.quiz_close); setOnClickListener { finish() } })
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

    /** Counters the mascot unlocks will read. A quiz counts once per day. */
    fun record(p: Prefs, score: Int, total: Int, day: Long) {
        if (p.lastQuizDay == day) return
        p.streak = if (p.lastQuizDay == day - 1) p.streak + 1 else 1
        p.lastQuizDay = day
        p.quizzes += 1
        if (score == total) p.perfectQuizzes += 1
        p.correctAnswers += score
    }
}
