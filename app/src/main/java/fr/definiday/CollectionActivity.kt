package fr.definiday

import android.app.Activity
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

/** Glose's collection: a tap on an unlocked item tries it in the preview, "Choisir" keeps it. Locked items show what is missing:
 *  a pose as its silhouette, a colour (pelage, pelote, binding, frame) faded, an accessory as a dashed « ? » slot. */
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
        // fixed header (title, preview, "Choisir") above the scrolling grids: what is tried stays in sight
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; fitsSystemWindows = true } // the insets replace its padding
        val head = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, 0) }.also(root::addView)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, 0, pad, pad) }
        head.addView(label(getString(R.string.collection_title, de(p.gloseName))))
        preview = ImageView(this).apply {
            contentDescription = getString(R.string.glose, p.gloseName)
            layoutParams = LinearLayout.LayoutParams((160 * dp).toInt(), (160 * dp).toInt()).apply { gravity = Gravity.CENTER_HORIZONTAL }
            val taps = Glose.Taps()
            setOnClickListener { Glose.tap(this, taps, Glose.pose(trial)) { Glose.drawable(context, it, Glose.styles(trial)) } }
        }
        head.addView(preview)
        choose = Button(this).apply {
            text = getString(R.string.collection_choose)
            setOnClickListener { Glose.save(p, trial); refresh(); Widget.update(context) }
        }
        head.addView(choose)
        root.addView(ScrollView(this).apply { addView(box) }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        Unlocks.Kind.values().forEach { kind ->
            box.addView(label(kind.titre).apply { setPadding(0, (20 * dp).toInt(), 0, (8 * dp).toInt()) })
            box.addView(GridLayout(this).apply {
                columnCount = 3
                val some = Unlocks.items.filter { it.kind == kind }
                some.forEach { addView(cell(it)) }
                repeat(maxOf(0, 3 - some.size)) { addView(android.widget.Space(context).apply { layoutParams = column() }) } // 1 or 2 items keep the 3-column width
            })
        }
        refresh()
    }

    private companion object {
        val FADED = setOf(Unlocks.Kind.PELAGE, Unlocks.Kind.PELOTE, Unlocks.Kind.YEUX, Unlocks.Kind.LIVRE)
        val DASHED = setOf(Unlocks.Kind.TETE, Unlocks.Kind.COU, Unlocks.Kind.OBJET)
    }

    override fun onPause() {
        super.onPause()
        Glose.stop(preview)
    }

    private fun column() = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f)).apply { width = 0 }

    private fun label(s: String) = TextView(this, null, 0, R.style.Label).apply { text = s }

    private fun cell(item: Unlocks.Item): View {
        val open = item.id in p.unlocked
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding((4 * dp).toInt(), (6 * dp).toInt(), (4 * dp).toInt(), (6 * dp).toInt())
            layoutParams = column()
            val size = LinearLayout.LayoutParams((96 * dp).toInt(), (96 * dp).toInt())
            if (!open && item.kind in DASHED) addView(TextView(context).apply {
                text = "?"; textSize = 32f; gravity = Gravity.CENTER; setTextColor(getColor(R.color.ink2))
                background = GradientDrawable().apply { cornerRadius = 12 * dp; setStroke((2 * dp).toInt(), getColor(R.color.ink2), 6 * dp, 4 * dp) }
                layoutParams = LinearLayout.LayoutParams(size).apply { setMargins(0, 0, 0, (4 * dp).toInt()) }
            }) else addView(ImageView(context).apply {
                setImageBitmap(Glose.bitmap(Glose.preview(context, item), size.width, size.height)) // still: one bitmap, not one cache per layer; alpha fades it as a whole
                if (!open && item.kind in FADED) imageAlpha = 89 // 35 %: the colour to come, not yet worn
                else if (!open) setColorFilter(getColor(R.color.silhouette), PorterDuff.Mode.SRC_IN)
                layoutParams = size
            })
            addView(TextView(context).apply { text = item.nom; textSize = 13f; gravity = Gravity.CENTER; setTextColor(getColor(R.color.ink)) })
            if (!open) addView(TextView(context).apply { text = Unlocks.hint(item, p); textSize = 12f; gravity = Gravity.CENTER; setTextColor(getColor(R.color.ink2)) })
            contentDescription = item.nom + if (open) "" else ", " + getString(R.string.collection_locked) + ", " + Unlocks.hint(item, p)
            if (open) setOnClickListener {
                // a second tap on the worn accessory takes it off
                trial[item.kind.slot] = if (item.kind in Unlocks.ACC && trial[item.kind] == item.id) "" else item.id
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
}
