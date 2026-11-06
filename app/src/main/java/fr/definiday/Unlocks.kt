package fr.definiday

import java.time.LocalDate
import java.time.MonthDay
import java.time.format.TextStyle
import java.util.Locale

/** Monsieur Glose's collection: what the quiz counters unlock. Names never agree with the mascot's gender nor contain its name (it will be renamable). */
object Unlocks {
    /** One item worn per kind. MOT items are poses ("le fil écrit…") listed apart; TETE..LIVRE are the accessory slots. */
    enum class Kind(val titre: String) {
        POSE("Poses"), MOT("Mots du fil"), PELAGE("Pelages"), PELOTE("Pelotes"), TETE("Sur la tête"), COU("Au cou"), YEUX("Montures"), OBJET("Objets"), LIVRE("Reliures");
        /** The choice slot: a written word is a pose. */
        val slot get() = if (this == MOT) POSE else this
    }
    /** Accessory slots ("" = nothing worn). */
    val ACC = listOf(Kind.TETE, Kind.COU, Kind.YEUX, Kind.OBJET, Kind.LIVRE)

    /** [res]: pose drawable, [style]: theme overlay (0 = default look). Unlocked when counter [on] reaches [need] (see [counts]),
     *  or, with a [window] "MM-dd..MM-dd" (any year, may wrap over new year), by one quiz inside it. [set]: its collection series. */
    class Item(val id: String, val kind: Kind, val nom: String, val res: Int = 0, val style: Int = 0, val on: Char = 'q', val need: Int = 0,
               val window: String? = null, val set: String? = null)

    private fun mot(w: String) = "Le fil écrit « $w »"

    val items = listOf(
        Item("assis", Kind.POSE, "Sur son volume", R.drawable.glose_assis),
        Item("soir", Kind.POSE, "Sieste dans la tasse", R.drawable.glose_soir, on = 's', need = 3),
        Item("livres", Kind.POSE, "Sur la pile de livres", R.drawable.glose_livres, need = 10),
        Item("reveil_tasse", Kind.POSE, "Réveil dans la tasse", R.drawable.glose_reveil, need = 21),
        Item("ecrit_mot", Kind.MOT, mot("mot"), R.drawable.glose_ecrit_mot, need = 1),
        Item("ecrit_encore", Kind.MOT, mot("encore"), R.drawable.glose_ecrit_encore, need = 1),
        Item("ecrit_bravo", Kind.MOT, mot("bravo"), R.drawable.glose_ecrit_bravo, on = 'r', need = 1),
        Item("ecrit_coucou", Kind.MOT, mot("coucou"), R.drawable.glose_ecrit_coucou, need = 3),
        Item("ecrit_dodo", Kind.MOT, mot("dodo"), R.drawable.glose_ecrit_dodo, on = 's', need = 3),
        Item("ecrit_ronron", Kind.MOT, mot("ronron"), R.drawable.glose_ecrit_ronron, on = 's', need = 10),
        Item("ecrit_rare", Kind.MOT, mot("rare"), R.drawable.glose_ecrit_rare, on = 'r', need = 30),
        Item("ecrit_flocon", Kind.MOT, mot("flocon"), R.drawable.glose_ecrit_flocon, window = "12-01..12-31", set = "saisons"),
        Item("tigre", Kind.PELAGE, "Gris tigré"),
        Item("roux", Kind.PELAGE, "Roux pain d’épices", style = R.style.Glose_Pelage_Roux, need = 3),
        Item("creme", Kind.PELAGE, "Crème caramel", style = R.style.Glose_Pelage_Creme, on = 'p', need = 5),
        Item("smoking", Kind.PELAGE, "Smoking", style = R.style.Glose_Pelage_Smoking, need = 30),
        Item("nuit", Kind.PELAGE, "Nuit d’encre", style = R.style.Glose_Pelage_Nuit, on = 's', need = 30),
        Item("enluminure", Kind.PELAGE, "Enluminure", style = R.style.Glose_Pelage_Enluminure, on = 'r', need = 150),
        Item("bordeaux", Kind.PELOTE, "Pelote bordeaux"),
        Item("moutarde", Kind.PELOTE, "Pelote moutarde", style = R.style.Glose_Pelote_Moutarde, need = 5),
        Item("lavande", Kind.PELOTE, "Pelote lavande", style = R.style.Glose_Pelote_Lavande, need = 8),
        Item("vert_eau", Kind.PELOTE, "Pelote vert d’eau", style = R.style.Glose_Pelote_VertEau, on = 'r', need = 12),
        Item("sauge", Kind.PELOTE, "Pelote sauge", style = R.style.Glose_Pelote_Sauge, need = 20),
        Item("bleu_prusse", Kind.PELOTE, "Pelote bleu de Prusse", style = R.style.Glose_Pelote_BleuPrusse, on = 'c', need = 25),
        Item("rose", Kind.PELOTE, "Pelote rose poudré", style = R.style.Glose_Pelote_Rose, on = 'p', need = 15),
        Item("bonnet", Kind.TETE, "Bonnet de nuit", style = R.style.Glose_Acc_Bonnet, on = 's', need = 14),
        Item("beret", Kind.TETE, "Béret de peintre", style = R.style.Glose_Acc_Beret, on = 'k', need = 15),
        Item("plume", Kind.TETE, "Plume d’oie", style = R.style.Glose_Acc_Plume, on = 'r', need = 60),
        Item("bonnet_neige", Kind.TETE, "Bonnet des neiges", style = R.style.Glose_Acc_BonnetNeige, window = "12-01..02-28", set = "saisons"),
        Item("couronne_rois", Kind.TETE, "Couronne des rois", style = R.style.Glose_Acc_Couronne, window = "01-02..01-20", set = "saisons"),
        Item("noeud", Kind.COU, "Nœud papillon", style = R.style.Glose_Acc_Noeud, on = 'p', need = 1),
        Item("echarpe", Kind.COU, "Écharpe tricotée", style = R.style.Glose_Acc_Echarpe, on = 's', need = 7),
        Item("grelot", Kind.COU, "Grelot doré", style = R.style.Glose_Acc_Grelot, need = 12),
        Item("echarpe_sucre_orge", Kind.COU, "Écharpe sucre d’orge", style = R.style.Glose_Acc_EcharpeSucreOrge, window = "12-01..12-31", set = "saisons"),
        Item("monture_ecaille", Kind.YEUX, "Monture écaille", style = R.style.Glose_Yeux_Ecaille, on = 'c', need = 20),
        Item("marque", Kind.OBJET, "Marque-page à gland d’or", style = R.style.Glose_Acc_Marque, need = 45),
        Item("sapin_papier", Kind.OBJET, "Petit sapin de papier", style = R.style.Glose_Acc_Sapin, window = "12-01..12-31", set = "saisons"),
        Item("reliure_bordeaux", Kind.LIVRE, "Reliure bordeaux", style = R.style.Glose_Livre_Bordeaux, need = 2),
        Item("reliure_verte", Kind.LIVRE, "Reliure vert bouteille", style = R.style.Glose_Livre_Verte, on = 'm', need = 40),
    )
    val byId = items.associateBy { it.id }

    private fun range(w: String) = w.split("..").map { MonthDay.parse("--$it") }
    /** [day] falls in window "MM-dd..MM-dd", whatever the year (a window may wrap over the new year). */
    fun inWindow(w: String, day: LocalDate): Boolean {
        val (a, b) = range(w); val d = MonthDay.from(day)
        return if (a <= b) d in a..b else d >= a || d <= b
    }

    /** Counters by code: q quizzes, p perfect, r passed, s streak, c correct answers, m words seen, k quotes read (history keys "q:…"). */
    fun counts(p: Prefs, history: Collection<String> = History(p.ctx).all().keys): Map<Char, Int> {
        val k = history.count { it.startsWith("q:") }
        return mapOf('q' to p.quizzes, 'p' to p.perfectQuizzes, 'r' to p.passedQuizzes, 's' to p.streak, 'c' to p.correctAnswers, 'm' to history.size - k, 'k' to k)
    }

    /** Ids unlocked by these counter values right now, and by a quiz taken on [quizDay] for the window items. */
    fun of(n: Map<Char, Int>, quizDay: LocalDate? = null): Set<String> = items.filter {
        if (it.window != null) quizDay != null && inWindow(it.window, quizDay) else (n[it.on] ?: 0) >= it.need
    }.map { it.id }.toSet()

    /** Adds what the counters (and the last quiz's date) unlock to the remembered set (a broken streak takes nothing back);
     *  returns what was never announced. Called right after each quiz, so every quiz day is seen once as lastQuizDay. */
    fun update(p: Prefs): List<Item> {
        p.unlocked = p.unlocked + of(counts(p), runCatching { LocalDate.ofEpochDay(p.lastQuizDay) }.getOrNull())
        return items.filter { (it.need > 0 || it.window != null) && it.id in p.unlocked && it.id !in p.announced }
    }

    /** What is still missing, in plain words ("Encore 3 soirs de quiz d’affilée"); a seasonal item says when it comes back, never that it expired. */
    fun hint(it: Item, p: Prefs, today: Long = LocalDate.now().toEpochDay()): String {
        it.window?.let { w ->
            return if (inWindow(w, LocalDate.ofEpochDay(today))) "Un soir de quiz, en ce moment"
            else "Revient en " + range(w)[0].month.getDisplayName(TextStyle.FULL, Locale.FRENCH)
        }
        val streak = if (Quiz.unbroken(p, today)) p.streak else 0 // broken since the last quiz: p.streak only restarts at the next one
        val n = it.need - if (it.on == 's') streak else counts(p).getValue(it.on)
        val s = if (n > 1) "s" else ""
        return "Encore $n " + when (it.on) {
            'p' -> "quiz sans faute"; 'r' -> "quiz réussi$s"; 's' -> "soir$s de quiz d’affilée"
            'c' -> "bonne$s réponse$s"; 'm' -> "mot$s découvert$s"; 'k' -> "citation$s lue$s"; else -> "quiz"
        }
    }
}
