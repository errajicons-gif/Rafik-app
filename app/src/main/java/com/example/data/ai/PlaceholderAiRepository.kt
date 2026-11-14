package com.example.data.ai

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.local.AppLanguage
import com.example.data.local.LearningDifficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Placeholder implementation used when Firebase AI Logic is not provisioned.
 * Computes genuine basic pixel brightness/dominant color clues locally for camera images
 * while clearly labeling AI generative output as sample preview responses requiring Firebase.
 */
class PlaceholderAiRepository : AiRepository {

    override val requiresFirebaseSetup: Boolean = true

    override suspend fun analyzeImage(
        bitmap: Bitmap,
        mode: VisionAnalysisMode,
        language: AppLanguage
    ): AiResult<VisionAnalysis> = withContext(Dispatchers.Default) {
        delay(450)
        val colorSummary = inspectDominantTone(bitmap, language)
        val result = when (language) {
            AppLanguage.ARABIC -> when (mode) {
                VisionAnalysisMode.OBJECTS -> VisionAnalysis(
                    summary = "[معاينة تجريبية] تم التقاط الصورة (${bitmap.width}×${bitmap.height}). في الوضع الكامل عبر Gemini، يتم تحديد كل غرض وموقعه بدقة.",
                    details = listOf(
                        "الإضاءة السائدة في الصورة: $colorSummary",
                        "مثال نتيجة: كوب موضوع في المنتصف مع سطح مستوٍ أمامك.",
                        "فعّل Firebase AI Logic لربط الكاميرا بوصف Gemini المباشر."
                    )
                )
                VisionAnalysisMode.SCENE -> VisionAnalysis(
                    summary = "[معاينة تجريبية] وصف المحيط: يظهر مشهد بإضاءة $colorSummary دون عوائق واضحة في المقدمة.",
                    details = listOf(
                        "تحليل محلي مبدئي لحالة الإضاءة: $colorSummary",
                        "يتطلب الوصف التفصيلي الكامل للمشهد تفعيل Firebase AI Logic."
                    )
                )
                VisionAnalysisMode.OCR_TEXT -> VisionAnalysis(
                    summary = "[معاينة تجريبية] قراءة النصوص المطبوعة: جاهز لاستخراج النصوص العربية والفرنسية والإنجليزية عند تفعيل Firebase AI Logic.",
                    details = listOf(
                        "نصيحة: ثبّت الورقة في إضاءة جيدة على بعد 25 سم للحصول على أوضح قراءة."
                    )
                )
                VisionAnalysisMode.COLORS -> VisionAnalysis(
                    summary = "تحليل الألوان المحلي من الصورة: النغمة السائدة هي $colorSummary.",
                    details = listOf(
                        "تم فحص بكسلات الصورة مباشرة على جهازك.",
                        "لتسمية تفاصيل الملابس والأغراض بدقة، فعّل Firebase AI Logic."
                    )
                )
            }

            AppLanguage.FRENCH -> when (mode) {
                VisionAnalysisMode.OBJECTS -> VisionAnalysis(
                    summary = "[Aperçu exemple] Image capturée (${bitmap.width}×${bitmap.height}). Avec Gemini via Firebase, chaque objet et sa position sont identifiés.",
                    details = listOf(
                        "Tonalité dominante mesurée : $colorSummary",
                        "Exemple : objet du quotidien posé sur une surface claire devant vous.",
                        "Activez Firebase AI Logic pour l'analyse Gemini en direct."
                    )
                )
                VisionAnalysisMode.SCENE -> VisionAnalysis(
                    summary = "[Aperçu exemple] Scène observée avec une luminosité $colorSummary.",
                    details = listOf(
                        "Mesure locale de luminosité : $colorSummary",
                        "Activez Firebase AI Logic pour une description complète de l'environnement."
                    )
                )
                VisionAnalysisMode.OCR_TEXT -> VisionAnalysis(
                    summary = "[Aperçu exemple] Extraction de texte (OCR) prête dès l'activation de Firebase AI Logic.",
                    details = listOf(
                        "Astuce : tenez le document à environ 25 cm dans un endroit bien éclairé."
                    )
                )
                VisionAnalysisMode.COLORS -> VisionAnalysis(
                    summary = "Analyse locale des couleurs : teinte dominante $colorSummary.",
                    details = listOf(
                        "Calcul effectué localement sur votre image.",
                        "Activez Firebase AI Logic pour décrire les motifs et vêtements."
                    )
                )
            }

            AppLanguage.ENGLISH -> when (mode) {
                VisionAnalysisMode.OBJECTS -> VisionAnalysis(
                    summary = "[Sample Preview] Photo captured (${bitmap.width}×${bitmap.height}). With Gemini via Firebase, every object and its relative position is identified.",
                    details = listOf(
                        "Measured dominant tone: $colorSummary",
                        "Sample output: Everyday item resting on a flat surface ahead.",
                        "Enable Firebase AI Logic for live Gemini multimodal vision."
                    )
                )
                VisionAnalysisMode.SCENE -> VisionAnalysis(
                    summary = "[Sample Preview] Scene check: $colorSummary lighting detected in frame.",
                    details = listOf(
                        "Local brightness & tone estimate: $colorSummary",
                        "Enable Firebase AI Logic for full environmental descriptions."
                    )
                )
                VisionAnalysisMode.OCR_TEXT -> VisionAnalysis(
                    summary = "[Sample Preview] Printed text OCR is ready to connect once Firebase AI Logic is enabled.",
                    details = listOf(
                        "Tip: Hold documents steady about 25 cm away in bright light."
                    )
                )
                VisionAnalysisMode.COLORS -> VisionAnalysis(
                    summary = "On-device color reading: dominant tone is $colorSummary.",
                    details = listOf(
                        "Computed directly from your image pixels on-device.",
                        "Enable Firebase AI Logic for detailed garment and object color naming."
                    )
                )
            }
        }
        AiResult.Success(result, isPlaceholder = true)
    }

    override suspend fun sendVoiceQuery(
        query: String,
        history: List<ChatTurn>,
        language: AppLanguage
    ): AiResult<String> {
        delay(350)
        val reply = when (language) {
            AppLanguage.ARABIC ->
                "[رد تجريبي] سؤالك: «$query». يمكنني مساعدتك في تنظيم مهامك أو قراءة النصوص أو فتح الوحدات. لتفعيل الذكاء الاصطناعي الحقيقي من Gemini، يرجى تفعيل Firebase AI Logic."
            AppLanguage.FRENCH ->
                "[Réponse exemple] Vous avez demandé : « $query ». Activez Firebase AI Logic pour obtenir des réponses Gemini en temps réel en français et en arabe."
            AppLanguage.ENGLISH ->
                "[Sample Response] You asked: \"$query\". Enable Firebase AI Logic in AI Studio to connect this voice assistant to live Gemini models."
        }
        return AiResult.Success(reply, isPlaceholder = true)
    }

    override suspend fun simplifyText(
        text: String,
        language: AppLanguage
    ): AiResult<String> {
        delay(350)
        val previewSnippet = text.trim().take(140)
        val simplified = when (language) {
            AppLanguage.ARABIC ->
                "[ملخص تجريبي مبسط]\n• الفكرة الأساسية: هذا النص يشرح معلومة مهمة بخطوات واضحة.\n• مقتطف: «$previewSnippet»\n• فعّل Firebase AI Logic للحصول على تبسيط ذكي فوري لأي مستند."
            AppLanguage.FRENCH ->
                "[Résumé simplifié exemple]\n• Idée principale : ce texte présente des informations clés de façon simple.\n• Extrait : « $previewSnippet »\n• Activez Firebase AI Logic pour simplifier automatiquement tout document."
            AppLanguage.ENGLISH ->
                "[Sample Simplified Summary]\n• Main idea: This passage shares key information in clear steps.\n• Excerpt: \"$previewSnippet\"\n• Enable Firebase AI Logic for instant Gemini text simplification."
        }
        return AiResult.Success(simplified, isPlaceholder = true)
    }

    override suspend fun generateLearningHint(
        difficulty: LearningDifficulty,
        language: AppLanguage
    ): AiResult<LearningSuggestion> {
        delay(300)
        val suggestion = when (language) {
            AppLanguage.ARABIC -> LearningSuggestion(
                title = "نشاط تجريبي: ترتيب خطوات الصباح",
                explanation = "ابدأ دائماً بغسل الوجه وتناول الإفطار ثم التحقق من جدولك اليومي بصوت مسموع.",
                practiceQuestion = "ما الخطوة الأولى التي تمنحك طاقة في الصباح؟",
                options = listOf("تناول فطور صحي", "ترك الحقيبة مفتوحة", "النوم متأخراً"),
                correctIndex = 0
            )
            AppLanguage.FRENCH -> LearningSuggestion(
                title = "Exercice exemple : routine du matin",
                explanation = "Commencez par vous rafraîchir, prendre un petit-déjeuner équilibré, puis écouter votre programme du jour.",
                practiceQuestion = "Quelle habitude donne de l'énergie le matin ?",
                options = listOf("Prendre un bon petit-déjeuner", "Oublier ses clés", "Se dépêcher sans pause"),
                correctIndex = 0
            )
            AppLanguage.ENGLISH -> LearningSuggestion(
                title = "Sample Activity: Calm Morning Routine",
                explanation = "Start by washing up, eating a balanced breakfast, and checking your daily plan with audio reminders.",
                practiceQuestion = "Which habit gives you energy to start the day?",
                options = listOf("Eating a healthy breakfast", "Skipping water", "Rushing without a plan"),
                correctIndex = 0
            )
        }
        return AiResult.Success(suggestion, isPlaceholder = true)
    }

    private fun inspectDominantTone(bitmap: Bitmap, language: AppLanguage): String {
        val scaled = Bitmap.createScaledBitmap(bitmap, 16, 16, true)
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        val total = scaled.width * scaled.height
        for (x in 0 until scaled.width) {
            for (y in 0 until scaled.height) {
                val p = scaled.getPixel(x, y)
                rSum += Color.red(p)
                gSum += Color.green(p)
                bSum += Color.blue(p)
            }
        }
        val r = (rSum / total).toInt()
        val g = (gSum / total).toInt()
        val b = (bSum / total).toInt()
        val brightness = (0.299 * r + 0.587 * g + 0.114 * b)
        val isBright = brightness > 135

        val colorKey = when {
            r > g + 35 && r > b + 35 -> "warm_red"
            g > r + 25 && g > b + 25 -> "green"
            b > r + 25 && b > g + 25 -> "blue"
            r > 180 && g > 160 && b < 110 -> "yellow"
            brightness > 190 -> "white"
            brightness < 70 -> "dark"
            else -> "neutral"
        }

        return when (language) {
            AppLanguage.ARABIC -> when (colorKey) {
                "warm_red" -> "دافئة مائلة للأحمر"
                "green" -> "خضراء هادئة"
                "blue" -> "زرقاء"
                "yellow" -> "صفراء أو ذهبية"
                "white" -> "فاتحة جداً / بيضاء"
                "dark" -> "داكنة"
                else -> if (isBright) "فاتحة متوازنة" else "هادئة متوسطة"
            }
            AppLanguage.FRENCH -> when (colorKey) {
                "warm_red" -> "chaude (rouge / orangé)"
                "green" -> "verte"
                "blue" -> "bleue"
                "yellow" -> "jaune / dorée"
                "white" -> "très claire / blanche"
                "dark" -> "sombre"
                else -> if (isBright) "claire et neutre" else "douce et neutre"
            }
            AppLanguage.ENGLISH -> when (colorKey) {
                "warm_red" -> "warm red / amber"
                "green" -> "green"
                "blue" -> "blue"
                "yellow" -> "yellow / golden"
                "white" -> "very bright / white"
                "dark" -> "dark"
                else -> if (isBright) "bright neutral" else "balanced neutral"
            }
        }
    }
}
