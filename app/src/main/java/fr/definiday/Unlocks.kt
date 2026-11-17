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

    /** Rarity, named like book editions; shown as stars in the album. */
    enum class Rarete(val nom: String, val etoiles: String) { POCHE("Poche", "★"), BROCHE("Broché", "★★"), RELIE("Relié", "★★★"), ORIGINALE("Édition originale", "★★★★") }

    /** [res]: pose drawable, [style]: theme overlay (0 = default look). Unlocked when counter [on] reaches [need] (see [counts]),
     *  or, with a [window] "MM-dd..MM-dd" (any year, may wrap over new year), by one quiz inside it. [set]: its collection series.
     *  [desc]: one line for the album (no agreement with the mascot's gender, never its name). */
    class Item(val id: String, val kind: Kind, val nom: String, val res: Int = 0, val style: Int = 0, val on: Char = 'q', val need: Int = 0,
               val window: String? = null, val set: String? = null, val rarete: Rarete = Rarete.POCHE, val desc: String = "")

    private fun mot(w: String) = "Le fil écrit « $w »"

    val items = listOf(
        Item("assis", Kind.POSE, "Sur son volume", R.drawable.glose_assis, desc = "Sur le gros volume, la pelote dans les pattes."),
        Item("soir", Kind.POSE, "Sieste dans la tasse", R.drawable.glose_soir, on = 's', need = 3, desc = "Dans la tasse bleu nuit, à la lueur d’une bougie."),
        Item("livres", Kind.POSE, "Sur la pile de livres", R.drawable.glose_livres, need = 10, desc = "Sur trois livres anciens, une patte taquine la pelote."),
        Item("reveil_tasse", Kind.POSE, "Réveil dans la tasse", R.drawable.glose_reveil, need = 21, rarete = Rarete.BROCHE, desc = "La pose du soir, yeux ouverts : le thé du matin."),
        Item("ecrit_mot", Kind.MOT, mot("mot"), R.drawable.glose_ecrit_mot, need = 1, desc = "Le fil de la pelote écrit d’un seul trait."),
        Item("ecrit_encore", Kind.MOT, mot("encore"), R.drawable.glose_ecrit_encore, need = 1, desc = "Pour recommencer, sans drame."),
        Item("ecrit_bravo", Kind.MOT, mot("bravo"), R.drawable.glose_ecrit_bravo, on = 'r', need = 1, desc = "Après un quiz réussi."),
        Item("ecrit_coucou", Kind.MOT, mot("coucou"), R.drawable.glose_ecrit_coucou, need = 3, desc = "Un bonjour en laine."),
        Item("ecrit_dodo", Kind.MOT, mot("dodo"), R.drawable.glose_ecrit_dodo, on = 's', need = 3, desc = "L’heure de dormir."),
        Item("ecrit_ronron", Kind.MOT, mot("ronron"), R.drawable.glose_ecrit_ronron, on = 's', need = 10, rarete = Rarete.BROCHE, desc = "Le mot du contentement."),
        Item("ecrit_rare", Kind.MOT, mot("rare"), R.drawable.glose_ecrit_rare, on = 'r', need = 30, rarete = Rarete.BROCHE, desc = "Pour les mots qu’on croise peu."),
        Item("ecrit_flocon", Kind.MOT, mot("flocon"), R.drawable.glose_ecrit_flocon, window = "12-01..12-31", set = "saisons", rarete = Rarete.BROCHE, desc = "Le mot de l’hiver."),
        Item("tigre", Kind.PELAGE, "Gris tigré", desc = "Gris rayé, la robe de toujours."),
        Item("roux", Kind.PELAGE, "Roux pain d’épices", style = R.style.Glose_Pelage_Roux, need = 3, desc = "Orangé chaud, rayures cannelle, plastron crème."),
        Item("creme", Kind.PELAGE, "Crème caramel", style = R.style.Glose_Pelage_Creme, on = 'p', need = 5, desc = "Crème doux, rayures caramel."),
        Item("smoking", Kind.PELAGE, "Smoking", style = R.style.Glose_Pelage_Smoking, need = 30, rarete = Rarete.BROCHE, desc = "Noir et blanc, plastron et museau blancs."),
        Item("nuit", Kind.PELAGE, "Nuit d’encre", style = R.style.Glose_Pelage_Nuit, on = 's', need = 30, rarete = Rarete.RELIE, desc = "Bleu d’encre semé d’étoiles pâles."),
        Item("enluminure", Kind.PELAGE, "Enluminure", style = R.style.Glose_Pelage_Enluminure, on = 'r', need = 150, rarete = Rarete.RELIE, desc = "Doré comme une page enluminée, paillettes d’or."),
        Item("bordeaux", Kind.PELOTE, "Pelote bordeaux", desc = "Le bordeaux des reliures anciennes."),
        Item("moutarde", Kind.PELOTE, "Pelote moutarde", style = R.style.Glose_Pelote_Moutarde, need = 5, desc = "Jaune moutarde, laine chaude."),
        Item("lavande", Kind.PELOTE, "Pelote lavande", style = R.style.Glose_Pelote_Lavande, need = 8, desc = "Mauve gris, fil doux."),
        Item("vert_eau", Kind.PELOTE, "Pelote vert d’eau", style = R.style.Glose_Pelote_VertEau, on = 'r', need = 12, rarete = Rarete.BROCHE, desc = "Vert pâle et frais."),
        Item("sauge", Kind.PELOTE, "Pelote sauge", style = R.style.Glose_Pelote_Sauge, need = 20, desc = "Vert sauge, tout en douceur."),
        Item("prune", Kind.PELOTE, "Pelote prune", style = R.style.Glose_Pelote_Prune, need = 40, desc = "Violet sombre, comme une encre de prune."),
        Item("corail", Kind.PELOTE, "Pelote corail", style = R.style.Glose_Pelote_Corail, on = 'm', need = 60, desc = "Orange rosé, laine vive."),
        Item("bleu_prusse", Kind.PELOTE, "Pelote bleu de Prusse", style = R.style.Glose_Pelote_BleuPrusse, on = 'c', need = 25, desc = "Le bleu profond des encres anciennes."),
        Item("rose", Kind.PELOTE, "Pelote rose poudré", style = R.style.Glose_Pelote_Rose, on = 'p', need = 15, rarete = Rarete.BROCHE, desc = "Rose poudré, comme un vieux billet doux."),
        Item("bonnet", Kind.TETE, "Bonnet de nuit", style = R.style.Glose_Acc_Bonnet, on = 's', need = 14, rarete = Rarete.BROCHE, desc = "Rouge à revers crème, pompon au bout."),
        Item("beret", Kind.TETE, "Béret de peintre", style = R.style.Glose_Acc_Beret, on = 'k', need = 15, rarete = Rarete.BROCHE, desc = "Béret penché, queue de feutre au sommet."),
        Item("plume", Kind.TETE, "Plume d’oie", style = R.style.Glose_Acc_Plume, on = 'r', need = 60, rarete = Rarete.BROCHE, desc = "Glissée derrière l’oreille, prête à écrire."),
        Item("bonnet_neige", Kind.TETE, "Bonnet des neiges", style = R.style.Glose_Acc_BonnetNeige, window = "12-01..02-28", set = "saisons", desc = "Le bonnet de nuit en bleu glacier, pompon blanc."),
        Item("couronne_rois", Kind.TETE, "Couronne des rois", style = R.style.Glose_Acc_Couronne, window = "01-02..01-20", set = "saisons", desc = "La couronne de papier doré de la galette."),
        Item("bonnet_etoile", Kind.TETE, "Bonnet bleu nuit", style = R.style.Glose_Acc_BonnetEtoile, on = 's', need = 21, desc = "Le bonnet de nuit en bleu nuit, pompon d’or."),
        Item("bonnet_imprimeur", Kind.TETE, "Bonnet d’imprimeur", style = R.style.Glose_Acc_Papier, on = 'c', need = 250, rarete = Rarete.BROCHE, desc = "Un chapeau plié dans une feuille imprimée."),
        Item("noeud", Kind.COU, "Nœud papillon", style = R.style.Glose_Acc_Noeud, on = 'p', need = 1, desc = "Bleu nuit à pois d’or."),
        Item("echarpe", Kind.COU, "Écharpe tricotée", style = R.style.Glose_Acc_Echarpe, on = 's', need = 7, desc = "Tricotée main, rayures d’or."),
        Item("grelot", Kind.COU, "Grelot doré", style = R.style.Glose_Acc_Grelot, need = 12, desc = "Petit grelot doré sur un ruban."),
        Item("echarpe_sucre_orge", Kind.COU, "Écharpe sucre d’orge", style = R.style.Glose_Acc_EcharpeSucreOrge, window = "12-01..12-31", set = "saisons", desc = "L’écharpe tricotée en rouge et blanc."),
        Item("lavalliere", Kind.COU, "Lavallière", style = R.style.Glose_Acc_Lavalliere, on = 'c', need = 160, rarete = Rarete.BROCHE, desc = "Grand nœud souple de poète."),
        Item("monture_ecaille", Kind.YEUX, "Monture écaille", style = R.style.Glose_Yeux_Ecaille, on = 'c', need = 20, rarete = Rarete.BROCHE, desc = "Lunettes rondes couleur écaille de tortue."),
        Item("monture_rose", Kind.YEUX, "Monture rose poudré", style = R.style.Glose_Yeux_Rose, on = 'k', need = 25, desc = "Assortie à la pelote rose."),
        Item("monture_argent", Kind.YEUX, "Monture argent", style = R.style.Glose_Yeux_Argent, need = 60, desc = "Un fil d’argent fin."),
        Item("marque", Kind.OBJET, "Marque-page à gland d’or", style = R.style.Glose_Acc_Marque, need = 45, rarete = Rarete.BROCHE, desc = "Cuir bordeaux et gland d’or, entre les pages."),
        Item("sapin_papier", Kind.OBJET, "Petit sapin de papier", style = R.style.Glose_Acc_Sapin, window = "12-01..12-31", set = "saisons", rarete = Rarete.BROCHE, desc = "Sapin plié, étoile dorée."),
        Item("croissant", Kind.OBJET, "Croissant du matin", style = R.style.Glose_Acc_Croissant, need = 30, desc = "Doré, sur une petite assiette."),
        Item("reliure_bordeaux", Kind.LIVRE, "Reliure bordeaux", style = R.style.Glose_Livre_Bordeaux, need = 2, desc = "Le volume habillé de bordeaux, plaque d’or."),
        Item("reliure_verte", Kind.LIVRE, "Reliure vert bouteille", style = R.style.Glose_Livre_Verte, on = 'm', need = 40, rarete = Rarete.BROCHE, desc = "Toile verte, nerfs dorés."),
        Item("cuir_fauve", Kind.LIVRE, "Cuir fauve", style = R.style.Glose_Livre_Fauve, need = 16, desc = "Le volume relié de cuir clair."),
        Item("bandeau_prix", Kind.LIVRE, "Bandeau de prix", style = R.style.Glose_Livre_Bandeau, window = "11-01..11-20", set = "saisons", rarete = Rarete.BROCHE, desc = "Le bandeau rouge des livres primés, autour du volume."),
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
