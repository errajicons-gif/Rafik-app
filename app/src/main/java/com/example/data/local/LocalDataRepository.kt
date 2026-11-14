package com.example.data.local

import kotlinx.coroutines.flow.Flow

class LocalDataRepository(private val dao: RafiqDao) {

    val pictograms: Flow<List<AacPictogramEntity>> = dao.getAllPictograms()
    val savedExpressions: Flow<List<SavedExpressionEntity>> = dao.getSavedExpressions()
    val learningActivities: Flow<List<LearningActivityEntity>> = dao.getAllLearningActivities()

    suspend fun ensureSeeded() {
        if (dao.countPictograms() == 0) {
            dao.insertPictograms(defaultPictograms())
        }
        if (dao.countLearningActivities() == 0) {
            dao.insertLearningActivities(defaultLearningActivities())
        }
    }

    suspend fun addCustomPictogram(label: String, language: AppLanguage) {
        val entity = AacPictogramEntity(
            category = AacCategory.CUSTOM.name,
            labelAr = label,
            labelFr = label,
            labelEn = label,
            iconKey = "star",
            isCustom = true
        )
        dao.insertPictogram(entity)
    }

    suspend fun deleteCustomPictogram(id: Int) = dao.deleteCustomPictogram(id)

    suspend fun saveExpression(textAr: String, textFr: String, textEn: String) {
        dao.insertExpression(
            SavedExpressionEntity(
                textAr = textAr,
                textFr = textFr,
                textEn = textEn
            )
        )
    }

    suspend fun deleteExpression(id: Int) = dao.deleteExpression(id)

    suspend fun markActivityCompleted(id: String, completed: Boolean) =
        dao.markActivityCompleted(id, completed)

    suspend fun deleteAllUserData() {
        dao.clearAllExpressions()
        dao.clearCustomPictograms()
        dao.resetLearningProgress()
    }

    private fun defaultPictograms(): List<AacPictogramEntity> = listOf(
        // Daily Needs
        AacPictogramEntity(category = AacCategory.DAILY_NEEDS.name, labelAr = "أحتاج مساعدة", labelFr = "J'ai besoin d'aide", labelEn = "I need help", iconKey = "help"),
        AacPictogramEntity(category = AacCategory.DAILY_NEEDS.name, labelAr = "نعم", labelFr = "Oui", labelEn = "Yes", iconKey = "yes"),
        AacPictogramEntity(category = AacCategory.DAILY_NEEDS.name, labelAr = "لا", labelFr = "Non", labelEn = "No", iconKey = "no"),
        AacPictogramEntity(category = AacCategory.DAILY_NEEDS.name, labelAr = "شكراً لك", labelFr = "Merci", labelEn = "Thank you", iconKey = "thanks"),
        AacPictogramEntity(category = AacCategory.DAILY_NEEDS.name, labelAr = "أريد الراحة", labelFr = "Je veux me reposer", labelEn = "I want to rest", iconKey = "rest"),
        AacPictogramEntity(category = AacCategory.DAILY_NEEDS.name, labelAr = "دورة المياه", labelFr = "Toilettes", labelEn = "Bathroom", iconKey = "bathroom"),
        // Food
        AacPictogramEntity(category = AacCategory.FOOD.name, labelAr = "أريد ماء", labelFr = "Je veux de l'eau", labelEn = "I want water", iconKey = "water"),
        AacPictogramEntity(category = AacCategory.FOOD.name, labelAr = "أنا جائع", labelFr = "J'ai faim", labelEn = "I am hungry", iconKey = "food"),
        AacPictogramEntity(category = AacCategory.FOOD.name, labelAr = "فاكهة", labelFr = "Fruit", labelEn = "Fruit", iconKey = "fruit"),
        AacPictogramEntity(category = AacCategory.FOOD.name, labelAr = "شاي / قهوة", labelFr = "Thé / Café", labelEn = "Tea / Coffee", iconKey = "drink"),
        // Emotions
        AacPictogramEntity(category = AacCategory.EMOTIONS.name, labelAr = "أنا سعيد", labelFr = "Je suis heureux", labelEn = "I am happy", iconKey = "happy"),
        AacPictogramEntity(category = AacCategory.EMOTIONS.name, labelAr = "أنا متعب", labelFr = "Je suis fatigué", labelEn = "I am tired", iconKey = "tired"),
        AacPictogramEntity(category = AacCategory.EMOTIONS.name, labelAr = "أنا قلق", labelFr = "Je suis inquiet", labelEn = "I feel worried", iconKey = "worried"),
        AacPictogramEntity(category = AacCategory.EMOTIONS.name, labelAr = "أحب عائلتي", labelFr = "J'aime ma famille", labelEn = "I love my family", iconKey = "love"),
        // Health
        AacPictogramEntity(category = AacCategory.HEALTH.name, labelAr = "أشعر بألم", labelFr = "J'ai mal", labelEn = "I feel pain", iconKey = "pain"),
        AacPictogramEntity(category = AacCategory.HEALTH.name, labelAr = "أحتاج دوائي", labelFr = "Mon médicament", labelEn = "I need medicine", iconKey = "medicine"),
        AacPictogramEntity(category = AacCategory.HEALTH.name, labelAr = "اتصل بالطبيب", labelFr = "Appeler le médecin", labelEn = "Call the doctor", iconKey = "doctor"),
        AacPictogramEntity(category = AacCategory.HEALTH.name, labelAr = "أشعر بتحسن", labelFr = "Je me sens mieux", labelEn = "I feel better", iconKey = "better"),
        // Movement
        AacPictogramEntity(category = AacCategory.MOVEMENT.name, labelAr = "أريد الخروج", labelFr = "Je veux sortir", labelEn = "I want to go out", iconKey = "go_out"),
        AacPictogramEntity(category = AacCategory.MOVEMENT.name, labelAr = "إلى المنزل", labelFr = "À la maison", labelEn = "Go home", iconKey = "home"),
        AacPictogramEntity(category = AacCategory.MOVEMENT.name, labelAr = "توقف من فضلك", labelFr = "Arrêtez s'il vous plaît", labelEn = "Please stop", iconKey = "stop"),
        AacPictogramEntity(category = AacCategory.MOVEMENT.name, labelAr = "ببطء من فضلك", labelFr = "Doucement s'il vous plaît", labelEn = "Slowly please", iconKey = "slow")
    )

    private fun defaultLearningActivities(): List<LearningActivityEntity> = listOf(
        LearningActivityEntity(
            id = "act_01_beg",
            difficulty = LearningDifficulty.BEGINNER.name,
            categoryIcon = "language",
            titleAr = "التحية اليومية اللطيفة",
            titleFr = "Les salutations quotidiennes",
            titleEn = "Everyday Polite Greetings",
            lessonAr = "نستخدم «السلام عليكم» أو «مرحباً» عند لقاء الناس، ونقول «شكراً» عندما يساعدنا شخص ما.",
            lessonFr = "On dit « Bonjour » en rencontrant quelqu'un et « Merci » lorsqu'une personne nous aide.",
            lessonEn = "We say 'Hello' when greeting someone, and 'Thank you' when someone helps us.",
            questionAr = "ماذا تقول عندما يقدم لك صديقك كوب ماء؟",
            questionFr = "Que dites-vous quand un ami vous donne de l'eau ?",
            questionEn = "What do you say when a friend hands you water?",
            optionsAr = "شكراً لك|لا أريد|توقف الآن",
            optionsFr = "Merci beaucoup|Pas maintenant|Au revoir",
            optionsEn = "Thank you|Not now|Goodbye",
            correctIndex = 0
        ),
        LearningActivityEntity(
            id = "act_02_beg",
            difficulty = LearningDifficulty.BEGINNER.name,
            categoryIcon = "safety",
            titleAr = "التعرف على ألوان الإشارة",
            titleFr = "Comprendre les feux de signalisation",
            titleEn = "Understanding Traffic Light Colors",
            lessonAr = "الضوء الأحمر يعني التوقف تماماً، والضوء الأخضر يعني أنه يمكنك العبور بأمان بعد التأكد من الطريق.",
            lessonFr = "Le feu rouge signifie s'arrêter. Le feu vert indique qu'on peut traverser prudemment.",
            lessonEn = "Red light means stop safely. Green light means it is safe to cross after checking the street.",
            questionAr = "أي لون يخبرنا بالتوقف عند معبر المشاة؟",
            questionFr = "Quelle couleur indique de s'arrêter au passage piéton ?",
            questionEn = "Which color tells pedestrians to stop?",
            optionsAr = "الأخضر|الأحمر|الأزرق",
            optionsFr = "Vert|Rouge|Bleu",
            optionsEn = "Green|Red|Blue",
            correctIndex = 1
        ),
        LearningActivityEntity(
            id = "act_03_int",
            difficulty = LearningDifficulty.INTERMEDIATE.name,
            categoryIcon = "money",
            titleAr = "إدارة الميزانية اليومية",
            titleFr = "Gérer les achats simples",
            titleEn = "Everyday Shopping Math",
            lessonAr = "عند الشراء نتحقق من السعر ونحسب الباقي ببساطة: إذا كان معك 20 درهماً واشتريت خبزاً بـ 5 دراهم، سيبقى لك 15 درهماً.",
            lessonFr = "Si vous avez 20 euros et achetez un article à 5 euros, il vous reste 15 euros.",
            lessonEn = "If you have 20 units and spend 5 units on bread, you have 15 units remaining.",
            questionAr = "معك 30 واشتريت فاكهة بـ 10، كم يتبقى معك؟",
            questionFr = "Vous avez 30 et dépensez 10 pour des fruits. Combien reste-t-il ?",
            questionEn = "You have 30 and spend 10 on fruit. How much is left?",
            optionsAr = "15|20|25",
            optionsFr = "15|20|25",
            optionsEn = "15|20|25",
            correctIndex = 1
        ),
        LearningActivityEntity(
            id = "act_04_int",
            difficulty = LearningDifficulty.INTERMEDIATE.name,
            categoryIcon = "health",
            titleAr = "تنظيم مواعيد الدواء",
            titleFr = "Organiser ses médicaments",
            titleEn = "Organizing Daily Medication",
            lessonAr = "قراءة التعليمات بوضوح تساعدنا على الاستقلالية: حبة واحدة صباحاً بعد الإفطار تعني تناولها بعد الأكل وليس على معدة فارغة.",
            lessonFr = "« 1 comprimé le matin après le petit-déjeuner » signifie prendre le médicament après avoir mangé.",
            lessonEn = "'One tablet in the morning after breakfast' means taking it after eating food, not on an empty stomach.",
            questionAr = "متى يجب تناول الدواء المكتوب عليه: «بعد وجبة الإفطار»؟",
            questionFr = "Quand prendre un médicament indiqué « après le petit-déjeuner » ?",
            questionEn = "When should you take medicine labeled 'after breakfast'?",
            optionsAr = "قبل النوم ليلاً|في الصباح بعد الأكل|قبل الأكل مباشرة",
            optionsFr = "Le soir avant de dormir|Le matin après avoir mangé|Avant de manger",
            optionsEn = "Before sleep at night|In the morning after eating|Before eating",
            correctIndex = 1
        ),
        LearningActivityEntity(
            id = "act_05_adv",
            difficulty = LearningDifficulty.ADVANCED.name,
            categoryIcon = "work",
            titleAr = "التعبير الواضح عن احتياجاتك",
            titleFr = "Exprimer ses besoins avec confiance",
            titleEn = "Self-Advocacy & Clear Requests",
            lessonAr = "الاستقلالية تبدأ بطلب المساعدة المحددة بلطف ووضوح، مثل: «هل يمكنك قراءة الرقم لي بصوت أوضح من فضلك؟».",
            lessonFr = "L'autonomie consiste aussi à formuler une demande précise : « Pouvez-vous parler un peu plus lentement, s'il vous plaît ? »",
            lessonEn = "Independence includes making clear, specific requests, like: 'Could you please speak a little more slowly?'",
            questionAr = "ما هي أفضل عبارة لطلب قراءة لافتة بعيدة؟",
            questionFr = "Quelle phrase est la plus claire pour demander de lire un panneau ?",
            questionEn = "Which request is clearest when asking someone to read a sign?",
            optionsAr = "لا أستطيع|من فضلك، هل يمكنك قراءة اسم الشارع على اللافتة؟|انظر هناك",
            optionsFr = "Je ne sais pas|Pouvez-vous me lire le nom sur le panneau, s'il vous plaît ?|Regardez là",
            optionsEn = "I cannot|Could you please read the street name on that sign for me?|Look over there",
            correctIndex = 1
        )
    )
}
