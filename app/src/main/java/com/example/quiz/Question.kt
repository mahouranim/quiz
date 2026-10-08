package com.example.quiz

import androidx.annotation.DrawableRes

/**
 * Une question du quiz.
 *
 * @property texte              énoncé de la question
 * @property imageRes           identifiant de l'image (ex. R.drawable.triangle)
 * @property choix              exactement trois réponses proposées
 * @property indiceBonneReponse indice (0, 1 ou 2) du choix correct
 * @property descriptionImage   texte lu par les lecteurs d'écran (accessibilité)
 */
data class Question(
    val texte: String,
    @DrawableRes val imageRes: Int,
    val choix: Array<String>,
    val indiceBonneReponse: Int,
    val descriptionImage: String
)