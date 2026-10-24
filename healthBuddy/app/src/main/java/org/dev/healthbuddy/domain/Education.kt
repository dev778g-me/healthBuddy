package org.dev.healthbuddy.domain

/**
 * Health Learning Corner. Plain language, ~60–80 words per card, no advice
 * that could replace a consultation. English + Hindi (Marathi falls back to
 * English — see PRD §11.3 translation policy).
 */
enum class EduIcon { HEART, COST, SIDE_EFFECT, CLOCK, WARNING, MIND }

data class EduText(val title: String, val body: String)

data class EduCard(
    val id: String,
    val icon: EduIcon,
    val en: EduText,
    val hi: EduText
) {
    fun text(lang: Lang): EduText = if (lang == Lang.HI) hi else en
}

object Education {

    val cards: List<EduCard> = listOf(
        EduCard(
            id = "why",
            icon = EduIcon.HEART,
            en = EduText(
                title = "Why missing doses matters",
                body = "Some medicines only work when their level in your body stays " +
                    "steady. Missing doses lets that level fall, so the condition can " +
                    "drift even while you feel fine. Damage from high blood pressure or " +
                    "high sugar usually builds quietly over years, not days — which is " +
                    "exactly why the boring, regular dose is the one that counts."
            ),
            hi = EduText(
                title = "खुराक छूटने से क्या फ़र्क़ पड़ता है",
                body = "कुछ दवाएँ तभी काम करती हैं जब शरीर में उनका स्तर स्थिर रहे। " +
                    "खुराक छूटने से यह स्तर गिर जाता है, और बीमारी बिगड़ सकती है — " +
                    "भले आप ठीक महसूस करें। उच्च रक्तचाप या शुगर से नुकसान अक्सर " +
                    "सालों में चुपचाप बढ़ता है, इसीलिए नियमित खुराक सबसे ज़रूरी है।"
            )
        ),
        EduCard(
            id = "adherence",
            icon = EduIcon.CLOCK,
            en = EduText(
                title = "What adherence actually means",
                body = "Adherence is not perfection. It means taking most doses, most " +
                    "days, and knowing what to do when you don't. If you miss one dose, " +
                    "do not double up unless your doctor has told you to. Note it, carry " +
                    "on with the next scheduled dose, and mention the pattern at your " +
                    "next visit."
            ),
            hi = EduText(
                title = "नियमितता का असली मतलब",
                body = "नियमितता का मतलब पूर्णता नहीं है। इसका मतलब है ज़्यादातर खुराक " +
                    "ज़्यादातर दिन लेना, और छूट जाने पर क्या करना है यह जानना। एक खुराक " +
                    "छूट जाए तो दोहरी खुराक न लें, जब तक डॉक्टर न कहें। उसे दर्ज करें, " +
                    "अगली खुराक समय पर लें, और अगली मुलाक़ात में बताएँ।"
            )
        ),
        EduCard(
            id = "cost",
            icon = EduIcon.COST,
            en = EduText(
                title = "Paying less for the same medicine",
                body = "Generic medicines contain the same active ingredient and usually " +
                    "cost less. Ask your doctor whether a generic or a cheaper equivalent " +
                    "is suitable for you. Jan Aushadhi Kendras stock low-cost essential " +
                    "medicines, and government schemes may cover part of the cost. Never " +
                    "switch brands on your own."
            ),
            hi = EduText(
                title = "वही दवा, कम कीमत में",
                body = "जेनेरिक दवाओं में वही सक्रिय तत्व होता है और आमतौर पर कीमत कम होती " +
                    "है। अपने डॉक्टर से पूछें कि क्या जेनेरिक या सस्ता विकल्प आपके लिए " +
                    "उपयुक्त है। जन औषधि केंद्रों पर कम कीमत की आवश्यक दवाएँ मिलती हैं। " +
                    "अपने आप ब्रांड न बदलें।"
            )
        ),
        EduCard(
            id = "side",
            icon = EduIcon.SIDE_EFFECT,
            en = EduText(
                title = "Side effects: what to report",
                body = "Write down what you felt, when it started, and how long it lasted. " +
                    "Common things worth reporting include nausea, dizziness, rash, sleep " +
                    "changes, and swelling. Get urgent help for difficulty breathing, " +
                    "swelling of the face or throat, chest pain, or fainting."
            ),
            hi = EduText(
                title = "साइड इफ़ेक्ट: क्या बताएँ",
                body = "क्या महसूस हुआ, कब शुरू हुआ और कितनी देर रहा — यह लिख लें। " +
                    "उल्टी जैसा लगना, चक्कर, दाने, नींद में बदलाव और सूजन जैसी बातें " +
                    "बताना उपयोगी है। साँस लेने में तकलीफ़, चेहरे या गले में सूजन, " +
                    "सीने में दर्द या बेहोशी हो तो तुरंत इलाज लें।"
            )
        ),
        EduCard(
            id = "when",
            icon = EduIcon.WARNING,
            en = EduText(
                title = "When to see a doctor sooner",
                body = "Do not wait for your next appointment if you have chest pain, " +
                    "sudden weakness on one side, trouble speaking, severe breathlessness, " +
                    "very high or very low sugar readings, or a fever that will not settle. " +
                    "These need professional assessment, not a change in your own medicines."
            ),
            hi = EduText(
                title = "डॉक्टर से जल्दी कब मिलें",
                body = "अगली मुलाक़ात का इंतज़ार न करें अगर सीने में दर्द, अचानक एक तरफ " +
                    "कमज़ोरी, बोलने में दिक्कत, तेज़ साँस की तकलीफ़, शुगर बहुत ज़्यादा " +
                    "या बहुत कम, या बुखार न उतर रहा हो। इनके लिए डॉक्टर की जाँच ज़रूरी है।"
            )
        ),
        EduCard(
            id = "mind",
            icon = EduIcon.MIND,
            en = EduText(
                title = "Stigma and mental health",
                body = "Taking medicine for your mind is no different from taking it for " +
                    "blood pressure. Your health information is yours, and privacy is a " +
                    "right, not a favour. If you feel low or anxious, talking to a " +
                    "professional is a sign of good judgement — Tele-MANAS on 14416 is " +
                    "free and confidential."
            ),
            hi = EduText(
                title = "कलंक और मानसिक स्वास्थ्य",
                body = "मन की दवा लेना, रक्तचाप की दवा लेने से अलग नहीं है। आपकी जानकारी " +
                    "आपकी है, और निजता आपका अधिकार है। उदासी या घबराहट हो तो किसी " +
                    "विशेषज्ञ से बात करना समझदारी है — टेली-मानस 14416 नि:शुल्क और " +
                    "गोपनीय है।"
            )
        )
    )

    fun byId(id: String?): EduCard? = cards.firstOrNull { it.id == id }
}
