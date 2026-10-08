package com.example.quiz

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // ----- Widgets (récupérés avec findViewById) -----
    private lateinit var txtScore: TextView
    private lateinit var progressQuiz: ProgressBar
    private lateinit var txtProgression: TextView
    private lateinit var layoutQuiz: View
    private lateinit var txtNumero: TextView
    private lateinit var imgQuestion: ImageView
    private lateinit var txtQuestion: TextView
    private lateinit var txtFeedback: TextView
    private lateinit var btnSuivant: Button
    private lateinit var zoneResultat: View
    private lateinit var txtResultat: TextView
    private lateinit var btnRejouer: Button
    private lateinit var boutonsChoix: List<Button>

    // Les cinq questions statiques (thème : formes géométriques).
    // La bonne réponse n'est pas toujours dans le même bouton (indices 1, 0, 1, 2, 2).
    private val questions = arrayOf(
        Question(
            texte = "Quelle est cette forme ?",
            imageRes = R.drawable.triangle,
            choix = arrayOf("Carré", "Triangle", "Cercle"),
            indiceBonneReponse = 1,
            descriptionImage = "Un triangle bleu"
        ),
        Question(
            texte = "Combien de côtés égaux possède cette forme ?",
            imageRes = R.drawable.carre,
            choix = arrayOf("Quatre", "Trois", "Aucun"),
            indiceBonneReponse = 0,
            descriptionImage = "Un carré bleu"
        ),
        Question(
            texte = "Cette forme possède-t-elle des angles ?",
            imageRes = R.drawable.cercle,
            choix = arrayOf("Oui, quatre", "Non, aucun", "Oui, trois"),
            indiceBonneReponse = 1,
            descriptionImage = "Un cercle bleu"
        ),
        Question(
            texte = "Combien de branches possède cette étoile ?",
            imageRes = R.drawable.etoile,
            choix = arrayOf("Six", "Quatre", "Cinq"),
            indiceBonneReponse = 2,
            descriptionImage = "Une étoile bleue à cinq branches"
        ),
        Question(
            texte = "Quelle forme a deux côtés plus longs que les deux autres ?",
            imageRes = R.drawable.rectangle,
            choix = arrayOf("Cercle", "Carré", "Rectangle"),
            indiceBonneReponse = 2,
            descriptionImage = "Un rectangle bleu"
        )
    )

    // ----- État de la partie -----
    private var indiceQuestion = 0        // question courante (0 à 4)
    private var score = 0                 // nombre de bonnes réponses
    private var reponseDonnee = false     // la question courante a-t-elle reçu une réponse ?
    private var partieTerminee = false    // le résultat final est-il affiché ?
    private var choixSelectionne = -1     // indice du bouton cliqué (-1 = aucun)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtScore = findViewById(R.id.txt_score)
        progressQuiz = findViewById(R.id.progress_quiz)
        txtProgression = findViewById(R.id.txt_progression)
        layoutQuiz = findViewById(R.id.layout_quiz)
        txtNumero = findViewById(R.id.txt_numero)
        imgQuestion = findViewById(R.id.img_question)
        txtQuestion = findViewById(R.id.txt_question)
        txtFeedback = findViewById(R.id.txt_feedback)
        btnSuivant = findViewById(R.id.btn_suivant)
        zoneResultat = findViewById(R.id.zone_resultat)
        txtResultat = findViewById(R.id.txt_resultat)
        btnRejouer = findViewById(R.id.btn_rejouer)
        boutonsChoix = listOf(
            findViewById(R.id.btn_choix1),
            findViewById(R.id.btn_choix2),
            findViewById(R.id.btn_choix3)
        )

        // Chaque bouton transmet son propre indice : c'est ainsi qu'on sait quel choix a été cliqué.
        boutonsChoix.forEachIndexed { indice, bouton ->
            bouton.setOnClickListener { verifierReponse(indice) }
        }
        btnSuivant.setOnClickListener { questionSuivante() }
        btnRejouer.setOnClickListener { rejouer() }

        // Amélioration facultative : restauration après rotation
        if (savedInstanceState != null) {
            indiceQuestion = savedInstanceState.getInt(KEY_INDICE)
            score = savedInstanceState.getInt(KEY_SCORE)
            reponseDonnee = savedInstanceState.getBoolean(KEY_REPONSE_DONNEE)
            partieTerminee = savedInstanceState.getBoolean(KEY_TERMINEE)
            choixSelectionne = savedInstanceState.getInt(KEY_CHOIX)
        }

        if (partieTerminee) afficherResultat() else afficherQuestion()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INDICE, indiceQuestion)
        outState.putInt(KEY_SCORE, score)
        outState.putBoolean(KEY_REPONSE_DONNEE, reponseDonnee)
        outState.putBoolean(KEY_TERMINEE, partieTerminee)
        outState.putInt(KEY_CHOIX, choixSelectionne)
    }

    /** Affiche l'image, la question et les trois choix de la question courante. */
    private fun afficherQuestion() {
        val question = questions[indiceQuestion]

        zoneResultat.visibility = View.GONE
        layoutQuiz.visibility = View.VISIBLE

        txtNumero.text = "Question ${indiceQuestion + 1} sur ${questions.size}"
        imgQuestion.setImageResource(question.imageRes)
        imgQuestion.contentDescription = question.descriptionImage
        txtQuestion.text = question.texte

        boutonsChoix.forEachIndexed { i, bouton ->
            bouton.text = question.choix[i]
            bouton.isEnabled = !reponseDonnee
        }

        if (reponseDonnee) {
            afficherCorrection()           // cas d'une restauration après rotation
        } else {
            txtFeedback.visibility = View.GONE
            btnSuivant.visibility = View.GONE
        }
        actualiserBandeau()
    }

    /** Valide la réponse cliquée. Ne fait rien si la question a déjà reçu une réponse. */
    private fun verifierReponse(indiceChoix: Int) {
        if (reponseDonnee || partieTerminee) return   // un seul point possible par question

        reponseDonnee = true
        choixSelectionne = indiceChoix

        if (indiceChoix == questions[indiceQuestion].indiceBonneReponse) {
            score++
        }
        afficherCorrection()
        actualiserBandeau()
    }

    /** Affiche le message juste/faux (texte + symbole + couleur) et le bouton de navigation. */
    private fun afficherCorrection() {
        val question = questions[indiceQuestion]
        val estJuste = choixSelectionne == question.indiceBonneReponse

        if (estJuste) {
            txtFeedback.text = "✔ Bonne réponse !"
            txtFeedback.setTextColor(ContextCompat.getColor(this, R.color.bonne_reponse))
        } else {
            txtFeedback.text =
                "✘ Mauvaise réponse. La bonne réponse était : ${question.choix[question.indiceBonneReponse]}"
            txtFeedback.setTextColor(ContextCompat.getColor(this, R.color.mauvaise_reponse))
        }
        txtFeedback.visibility = View.VISIBLE

        boutonsChoix.forEach { it.isEnabled = false }

        btnSuivant.setText(
            if (estDerniereQuestion()) R.string.btn_voir_resultat else R.string.btn_suivant
        )
        btnSuivant.visibility = View.VISIBLE
    }

    /** Met à jour le score (haut à droite) et la barre de progression (réponses données). */
    private fun actualiserBandeau() {
        val reponsesDonnees = if (reponseDonnee) indiceQuestion + 1 else indiceQuestion
        txtScore.text = "Score : $score / ${questions.size}"
        progressQuiz.max = questions.size
        progressQuiz.progress = reponsesDonnees
        txtProgression.text = "Réponses données : $reponsesDonnees / ${questions.size}"
    }

    private fun estDerniereQuestion() = indiceQuestion == questions.size - 1

    /** Passe à la question suivante, ou au résultat si c'était la dernière. */
    private fun questionSuivante() {
        if (!reponseDonnee) return                    // pas de saut sans réponse
        if (estDerniereQuestion()) {
            afficherResultat()
        } else {
            indiceQuestion++                          // jamais au-delà de questions.size - 1
            reponseDonnee = false
            choixSelectionne = -1
            afficherQuestion()
        }
    }

    /** Masque le jeu (donc les choix) et affiche le score final. */
    private fun afficherResultat() {
        partieTerminee = true
        reponseDonnee = true                          // la progression vaut alors 5 / 5
        indiceQuestion = questions.size - 1

        layoutQuiz.visibility = View.GONE
        zoneResultat.visibility = View.VISIBLE
        txtResultat.text = "Votre score final : $score / ${questions.size}"
        actualiserBandeau()
    }

    /** Réinitialise le quiz, le score et la progression. */
    private fun rejouer() {
        indiceQuestion = 0
        score = 0
        reponseDonnee = false
        partieTerminee = false
        choixSelectionne = -1
        afficherQuestion()
    }

    private companion object {
        const val KEY_INDICE = "indice"
        const val KEY_SCORE = "score"
        const val KEY_REPONSE_DONNEE = "reponse_donnee"
        const val KEY_TERMINEE = "terminee"
        const val KEY_CHOIX = "choix"
    }
}