package fr.definiday

/** Monsieur Glose's collection: what the quiz counters unlock. Thresholds count quizzes, not answers (1 or 3 words a day progress alike). */
object Unlocks {
    enum class Kind(val titre: String) { POSE("Poses"), PELAGE("Pelages"), PELOTE("Pelotes"), ACC("Accessoires") }

    /** [res]: pose drawable, [style]: theme overlay (0 = default look). Unlocked when counter [on] reaches [need]: q quizzes, p perfect, r passed, s streak. */
    class Item(val id: String, val kind: Kind, val nom: String, val res: Int = 0, val style: Int = 0, val on: Char = 'q', val need: Int = 0)

    val items = listOf(
        Item("assis", Kind.POSE, "Assis sur son volume", R.drawable.glose_assis),
        Item("ecrit_mot", Kind.POSE, "Le fil écrit « mot »", R.drawable.glose_ecrit_mot, need = 1),
        Item("ecrit_encore", Kind.POSE, "Le fil écrit « encore »", R.drawable.glose_ecrit_encore, need = 1),
        Item("ecrit_bravo", Kind.POSE, "Le fil écrit « bravo »", R.drawable.glose_ecrit_bravo, on = 'r', need = 1),
        Item("soir", Kind.POSE, "Endormi dans sa tasse", R.drawable.glose_soir, on = 's', need = 3),
        Item("ecrit_dodo", Kind.POSE, "Le fil écrit « dodo »", R.drawable.glose_ecrit_dodo, on = 's', need = 3),
        Item("livres", Kind.POSE, "Sur la pile de livres", R.drawable.glose_livres, need = 10),
        Item("ecrit_rare", Kind.POSE, "Le fil écrit « rare »", R.drawable.glose_ecrit_rare, on = 'r', need = 30),
        Item("tigre", Kind.PELAGE, "Gris tigré"),
        Item("creme", Kind.PELAGE, "Crème caramel", style = R.style.Glose_Pelage_Creme, on = 'p', need = 5),
        Item("nuit", Kind.PELAGE, "Nuit d’encre", style = R.style.Glose_Pelage_Nuit, on = 's', need = 30),
        Item("enluminure", Kind.PELAGE, "Enluminure", style = R.style.Glose_Pelage_Enluminure, on = 'r', need = 150),
        Item("bordeaux", Kind.PELOTE, "Pelote bordeaux"),
        Item("moutarde", Kind.PELOTE, "Pelote moutarde", style = R.style.Glose_Pelote_Moutarde, need = 5),
        Item("sauge", Kind.PELOTE, "Pelote sauge", style = R.style.Glose_Pelote_Sauge, need = 20),
        Item("rose", Kind.PELOTE, "Pelote rose poudré", style = R.style.Glose_Pelote_Rose, on = 'p', need = 15),
        Item("noeud", Kind.ACC, "Nœud papillon", style = R.style.Glose_Acc_Noeud, on = 'p', need = 1),
        Item("echarpe", Kind.ACC, "Écharpe tricotée", style = R.style.Glose_Acc_Echarpe, on = 's', need = 7),
        Item("bonnet", Kind.ACC, "Bonnet de nuit", style = R.style.Glose_Acc_Bonnet, on = 's', need = 14),
        Item("plume", Kind.ACC, "Plume d’oie", style = R.style.Glose_Acc_Plume, on = 'r', need = 60),
        Item("marque", Kind.ACC, "Marque-page à gland d’or", style = R.style.Glose_Acc_Marque, need = 45),
    )
    val byId = items.associateBy { it.id }

    private fun value(on: Char, quizzes: Int, perfect: Int, passed: Int, streak: Int) =
        when (on) { 'p' -> perfect; 'r' -> passed; 's' -> streak; else -> quizzes }

    /** Ids unlocked by these counter values right now. */
    fun of(quizzes: Int, perfect: Int, passed: Int, streak: Int): Set<String> =
        items.filter { value(it.on, quizzes, perfect, passed, streak) >= it.need }.map { it.id }.toSet()

    /** Adds what the counters unlock to the remembered set (a broken streak takes nothing back); returns what was never announced. */
    fun update(p: Prefs): List<Item> {
        p.unlocked = p.unlocked + of(p.quizzes, p.perfectQuizzes, p.passedQuizzes, p.streak)
        return items.filter { it.need > 0 && it.id in p.unlocked && it.id !in p.announced }
    }

    /** What is still missing, in plain words ("Encore 3 soirs d’affilée"). */
    fun hint(it: Item, p: Prefs): String {
        val n = it.need - value(it.on, p.quizzes, p.perfectQuizzes, p.passedQuizzes, p.streak)
        val s = if (n > 1) "s" else ""
        return "Encore $n " + when (it.on) { 'p' -> "quiz sans faute"; 'r' -> "quiz réussi$s"; 's' -> "soir$s de quiz d’affilée"; else -> "quiz" }
    }
}
