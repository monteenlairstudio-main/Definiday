package fr.definiday

import android.app.Activity
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Glose's collection: a tap on an unlocked item tries it in the preview, "Choisir" keeps it. Locked items: grey silhouette + what is missing. */
class CollectionActivity : Activity() {
    private lateinit var p: Prefs
    private lateinit var preview: ImageView
    private lateinit var choose: Button
    private lateinit var trial: MutableMap<Unlocks.Kind, String>
    private val cells = mutableMapOf<String, View>()
    private val dp by lazy { resources.displayMetrics.density }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        p = Prefs(this)
        Unlocks.update(p)
        trial = Glose.choice(p).toMutableMap()
        val pad = (24 * dp).toInt()
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad) }
        setContentView(ScrollView(this).apply { fitsSystemWindows = true; addView(box) })
        box.addView(label(getString(R.string.collection_title)))
        preview = ImageView(this).apply {
            contentDescription = getString(R.string.glose)
            layoutParams = LinearLayout.LayoutParams((200 * dp).toInt(), (200 * dp).toInt()).apply { gravity = Gravity.CENTER_HORIZONTAL }
            setOnClickListener { Glose.play(this) }
        }
        box.addView(preview)
        choose = Button(this).apply {
            text = getString(R.string.collection_choose)
            setOnClickListener { Glose.save(p, trial); refresh() }
        }
        box.addView(choose)
        Unlocks.Kind.values().forEach { kind ->
            box.addView(label(kind.titre).apply { setPadding(0, (20 * dp).toInt(), 0, (8 * dp).toInt()) })
            box.addView(GridLayout(this).apply {
                columnCount = 3
                Unlocks.items.filter { it.kind == kind }.forEach { addView(cell(it)) }
            })
        }
        refresh()
    }

    override fun onPause() {
        super.onPause()
        Glose.stop(preview)
    }

    private fun label(s: String) = TextView(this, null, 0, R.style.Label).apply { text = s }

    private fun cell(item: Unlocks.Item): View {
        val open = item.id in p.unlocked
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding((4 * dp).toInt(), (6 * dp).toInt(), (4 * dp).toInt(), (6 * dp).toInt())
            layoutParams = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f)).apply { width = 0 }
            addView(ImageView(context).apply {
                setImageDrawable(Glose.preview(context, item)) // still: the AVD is never started here
                if (!open) setColorFilter(SILHOUETTE, PorterDuff.Mode.SRC_IN)
                layoutParams = LinearLayout.LayoutParams((96 * dp).toInt(), (96 * dp).toInt())
            })
            addView(TextView(context).apply { text = item.nom; textSize = 13f; gravity = Gravity.CENTER })
            if (!open) addView(TextView(context).apply { text = Unlocks.hint(item, p); textSize = 12f; gravity = Gravity.CENTER; setTextColor(getColor(R.color.ink2)) })
            contentDescription = item.nom + if (open) "" else ", " + getString(R.string.collection_locked) + ", " + Unlocks.hint(item, p)
            if (open) setOnClickListener {
                // a second tap on the worn accessory takes it off
                trial[item.kind] = if (item.kind == Unlocks.Kind.ACC && trial[item.kind] == item.id) "" else item.id
                refresh()
            }
            cells[item.id] = this
        }
    }

    private fun refresh() {
        preview.setImageDrawable(Glose.drawable(this, Glose.pose(trial), Glose.styles(trial)))
        Glose.play(preview)
        choose.isEnabled = trial != Glose.choice(p)
        cells.forEach { (id, v) ->
            v.isSelected = id in trial.values // worn: said to TalkBack, not only drawn
            v.background = if (id in trial.values) GradientDrawable().apply {
                cornerRadius = 12 * dp; setStroke((2 * dp).toInt(), getColor(R.color.accent))
            } else null
        }
    }

    companion object { private val SILHOUETTE = Color.parseColor("#B8AFA0") }
}
