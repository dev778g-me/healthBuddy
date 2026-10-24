package org.dev.healthbuddy.domain

/**
 * THE CORE CONTENT OF THE PRODUCT.
 *
 * Each barrier maps to one reviewed response. Rules (PRD §9) that every entry
 * below obeys:
 *   1. never advise starting / stopping / switching / substituting a medicine
 *   2. never suggest a dose or dose change
 *   3. never diagnose
 *   4. never imply the app replaces a clinician
 *   5. always offer a route to a human where clinically relevant
 *   6. COST and SIDE_EFFECTS always carry a safety note
 *
 * Copy is fixed and reviewed. It is never generated at runtime.
 */

enum class ActionType {
    ADJUST_REMINDER,   // shift today's reminder later
    FIND_FACILITY,     // curated care options + health helpline
    CALL,              // dial a verified helpline
    READ,              // show guidance bullets inline
    NONE
}

enum class Escalation { NONE, CLINICIAN, CRISIS }

data class BarrierText(
    val label: String,
    val title: String,
    val body: String,
    val actionLabel: String? = null,
    val safety: String? = null,
    val bullets: List<String> = emptyList()
)

data class BarrierSupport(
    val barrier: Barrier,
    val action: ActionType,
    val escalation: Escalation,
    val callNumber: String? = null,
    val en: BarrierText,
    val hi: BarrierText
) {
    fun text(lang: Lang): BarrierText = when (lang) {
        Lang.HI -> hi
        else -> en
    }
}

object BarrierCatalog {

    val all: List<BarrierSupport> = listOf(
        BarrierSupport(
            barrier = Barrier.FORGOT,
            action = ActionType.ADJUST_REMINDER,
            escalation = Escalation.NONE,
            en = BarrierText(
                label = "I simply forgot",
                title = "Forgetting is normal — let's make it easier",
                body = "Most doses are missed when the timing doesn't fit the day. " +
                    "We can move this reminder to a moment that works better — after " +
                    "breakfast, before bed, whenever you're actually home.",
                actionLabel = "Move this reminder"
            ),
            hi = BarrierText(
                label = "मैं भूल गया / भूल गई",
                title = "भूल जाना सामान्य है — इसे आसान बनाते हैं",
                body = "ज़्यादातर खुराक तब छूटती है जब समय दिनचर्या में फिट नहीं बैठता। " +
                    "हम इस रिमाइंडर को ऐसे समय पर ले जा सकते हैं जो आपके लिए बेहतर हो — " +
                    "नाश्ते के बाद, सोने से पहले, जब आप घर पर हों।",
                actionLabel = "रिमाइंडर का समय बदलें"
            )
        ),
        BarrierSupport(
            barrier = Barrier.COST,
            action = ActionType.FIND_FACILITY,
            escalation = Escalation.CLINICIAN,
            callNumber = "104",
            en = BarrierText(
                label = "It costs too much",
                title = "Cost is a real barrier — and there is help",
                body = "Medicines costing more than expected is one of the most common " +
                    "reasons doses get missed. This is not your fault. Your prescriber " +
                    "or pharmacist can tell you about cheaper options, generic " +
                    "equivalents, or government schemes you may qualify for.",
                actionLabel = "Find care options",
                safety = "Never stop or change a prescribed medicine on your own. " +
                    "Speak to your doctor or pharmacist first.",
                bullets = listOf(
                    "Primary Health Centre (PHC) — free consultation and essential medicines",
                    "Jan Aushadhi Kendra — low-cost generic medicines",
                    "Health Helpline 104 — free guidance, 24×7"
                )
            ),
            hi = BarrierText(
                label = "दवा बहुत महँगी है",
                title = "खर्च एक वास्तविक बाधा है — और मदद उपलब्ध है",
                body = "दवा अपेक्षा से महँगी होना खुराक छूटने के सबसे आम कारणों में से एक है। " +
                    "यह आपकी गलती नहीं है। आपके डॉक्टर या फार्मासिस्ट सस्ते विकल्प, " +
                    "जेनेरिक दवाएँ या सरकारी योजनाओं के बारे में बता सकते हैं।",
                actionLabel = "देखभाल के विकल्प देखें",
                safety = "कृपया अपनी दवा स्वयं बंद या बदलें नहीं। पहले अपने डॉक्टर या " +
                    "फार्मासिस्ट से बात करें।",
                bullets = listOf(
                    "प्राथमिक स्वास्थ्य केंद्र (PHC) — नि:शुल्क परामर्श और आवश्यक दवाइयाँ",
                    "जन औषधि केंद्र — कम कीमत की जेनेरिक दवाइयाँ",
                    "स्वास्थ्य हेल्पलाइन 104 — नि:शुल्क सलाह, 24×7"
                )
            )
        ),
        BarrierSupport(
            barrier = Barrier.SIDE_EFFECTS,
            action = ActionType.READ,
            escalation = Escalation.CLINICIAN,
            en = BarrierText(
                label = "I felt unwell, or was worried about side effects",
                title = "Side effects should be reported, not endured silently",
                body = "If a medicine is making you feel unwell — or you're worried it " +
                    "might — that is important information for your doctor, not " +
                    "something to work out alone. Note what you felt and when, and take " +
                    "it to your next visit.",
                actionLabel = "What to note down",
                safety = "Do not stop the medicine on your own. If you have trouble " +
                    "breathing, swelling, chest pain, or severe dizziness, seek urgent care now.",
                bullets = listOf(
                    "What you felt — e.g. nausea, dizziness, rash, sleep changes",
                    "When it started, and how long it lasted",
                    "Whether it happens after every dose or only sometimes",
                    "Anything else you changed at the same time"
                )
            ),
            hi = BarrierText(
                label = "तबीयत ठीक नहीं लगी, या साइड इफ़ेक्ट की चिंता थी",
                title = "साइड इफ़ेक्ट बताने चाहिए, चुपचाप सहने नहीं",
                body = "अगर कोई दवा आपको अस्वस्थ कर रही है — या आपको चिंता है कि ऐसा हो " +
                    "सकता है — तो यह आपके डॉक्टर के लिए ज़रूरी जानकारी है, अकेले सुलझाने " +
                    "वाली बात नहीं। क्या और कब महसूस हुआ, यह लिख लें और अगली मुलाक़ात में बताएँ।",
                actionLabel = "क्या लिखना है",
                safety = "दवा अपने आप बंद न करें। साँस लेने में तकलीफ़, सूजन, सीने में " +
                    "दर्द या तेज़ चक्कर हो तो तुरंत इलाज लें।",
                bullets = listOf(
                    "क्या महसूस हुआ — जैसे उल्टी जैसा लगना, चक्कर, दाने, नींद में बदलाव",
                    "कब शुरू हुआ और कितनी देर रहा",
                    "हर खुराक के बाद होता है या कभी-कभी",
                    "उसी समय आपने और क्या बदला था"
                )
            )
        ),
        BarrierSupport(
            barrier = Barrier.CONFUSED,
            action = ActionType.READ,
            escalation = Escalation.NONE,
            en = BarrierText(
                label = "I didn't understand how to take it",
                title = "Let's make the instructions clear",
                body = "Instructions get confusing — different timings, before or after " +
                    "food, tablets that look alike. Your pharmacist can explain this in " +
                    "your own language in under a minute, and it's a completely normal " +
                    "thing to ask.",
                actionLabel = "What to ask your pharmacist",
                bullets = listOf(
                    "Should I take this before food, after food, or with food?",
                    "What should I do if I miss a dose?",
                    "Can I take it at the same time as my other medicines?",
                    "Can you write the timing down for me?"
                )
            ),
            hi = BarrierText(
                label = "दवा कैसे लेनी है, यह समझ नहीं आया",
                title = "निर्देश साफ़ कर लेते हैं",
                body = "निर्देश उलझा देते हैं — अलग-अलग समय, खाने से पहले या बाद, एक जैसी " +
                    "दिखने वाली गोलियाँ। आपका फार्मासिस्ट एक मिनट में आपकी भाषा में " +
                    "समझा सकता है, और यह पूछना बिलकुल सामान्य बात है।",
                actionLabel = "फार्मासिस्ट से क्या पूछें",
                bullets = listOf(
                    "यह खाने से पहले, बाद में या खाने के साथ लेनी है?",
                    "खुराक छूट जाए तो क्या करना चाहिए?",
                    "क्या इसे बाकी दवाओं के साथ ले सकता/सकती हूँ?",
                    "क्या समय लिखकर दे सकते हैं?"
                )
            )
        ),
        BarrierSupport(
            barrier = Barrier.STIGMA,
            action = ActionType.ADJUST_REMINDER,
            escalation = Escalation.NONE,
            en = BarrierText(
                label = "I felt embarrassed, or worried what people would think",
                title = "Your treatment is private — and you're not alone",
                body = "Many people delay medicines because of what others might think. " +
                    "Your health information stays on this device, and you can hide " +
                    "medicine names from this screen whenever you need to.",
                actionLabel = "Turn on private mode"
            ),
            hi = BarrierText(
                label = "शर्म आई, या लोग क्या सोचेंगे इसकी चिंता हुई",
                title = "आपका इलाज निजी है — और आप अकेले नहीं हैं",
                body = "बहुत लोग दूसरों की सोच के कारण दवा टाल देते हैं। आपकी जानकारी " +
                    "इसी फ़ोन में रहती है, और आप चाहें तो इस स्क्रीन पर दवा के नाम छिपा सकते हैं।",
                actionLabel = "निजी मोड चालू करें"
            )
        ),
        BarrierSupport(
            barrier = Barrier.ACCESS,
            action = ActionType.FIND_FACILITY,
            escalation = Escalation.NONE,
            callNumber = "104",
            en = BarrierText(
                label = "I couldn't get to a clinic, pharmacy, or my medicine",
                title = "Distance shouldn't decide your treatment",
                body = "If travel, transport, or a medicine being out of stock is getting " +
                    "in the way, there may be something closer — a primary health centre, " +
                    "a Jan Aushadhi store, or a phone consultation.",
                actionLabel = "Find something closer",
                bullets = listOf(
                    "Your nearest Primary Health Centre (PHC)",
                    "Jan Aushadhi Kendra for essential medicines",
                    "Health Helpline 104 for phone guidance"
                )
            ),
            hi = BarrierText(
                label = "क्लिनिक, फार्मेसी या दवा तक नहीं पहुँच सका / सकी",
                title = "दूरी को आपका इलाज तय नहीं करना चाहिए",
                body = "अगर सफ़र, वाहन या दवा का स्टॉक ख़त्म होना रुकावट है, तो कुछ " +
                    "करीब हो सकता है — प्राथमिक स्वास्थ्य केंद्र, जन औषधि केंद्र, " +
                    "या फ़ोन पर परामर्श।",
                actionLabel = "करीब का विकल्प खोजें",
                bullets = listOf(
                    "आपका निकटतम प्राथमिक स्वास्थ्य केंद्र (PHC)",
                    "आवश्यक दवाओं के लिए जन औषधि केंद्र",
                    "फ़ोन पर सलाह के लिए स्वास्थ्य हेल्पलाइन 104"
                )
            )
        ),
        BarrierSupport(
            barrier = Barrier.DISTRESS,
            action = ActionType.CALL,
            escalation = Escalation.CRISIS,
            callNumber = "14416",
            en = BarrierText(
                label = "I was feeling low, anxious, or alone",
                title = "Looking after your mind is part of looking after your health",
                body = "When you're exhausted or overwhelmed, taking medicine can feel " +
                    "impossible. That is worth telling someone — a clinician, or a " +
                    "helpline that is free and confidential.",
                actionLabel = "Call a helpline",
                safety = "If you are having thoughts of harming yourself, please call " +
                    "14416 (Tele-MANAS, free, 24×7) or your local emergency number now."
            ),
            hi = BarrierText(
                label = "मन उदास था, घबराहट थी, या अकेलापन लगा",
                title = "मन का ख़याल रखना भी सेहत का हिस्सा है",
                body = "जब आप थके हुए या बोझ से दबे हों, तो दवा लेना नामुमकिन लग सकता है। " +
                    "यह किसी से कहने लायक बात है — डॉक्टर से, या किसी नि:शुल्क और " +
                    "गोपनीय हेल्पलाइन से।",
                actionLabel = "हेल्पलाइन पर कॉल करें",
                safety = "अगर आपको खुद को नुकसान पहुँचाने के विचार आ रहे हैं, तो कृपया " +
                    "अभी 14416 (टेली-मानस, नि:शुल्क, 24×7) या अपने स्थानीय आपातकालीन " +
                    "नंबर पर कॉल करें।"
            )
        ),
        BarrierSupport(
            barrier = Barrier.OTHER,
            action = ActionType.READ,
            escalation = Escalation.NONE,
            en = BarrierText(
                label = "Something else",
                title = "Thanks for telling us",
                body = "Whatever it was, it's worth mentioning at your next visit. Even a " +
                    "small difficulty is useful information for your doctor.",
                actionLabel = "What to mention",
                bullets = listOf(
                    "What got in the way that day",
                    "Whether it happens often or was a one-off",
                    "Anything that would make it easier next time"
                )
            ),
            hi = BarrierText(
                label = "कुछ और",
                title = "बताने के लिए धन्यवाद",
                body = "जो भी कारण रहा हो, अगली मुलाक़ात में बताना उपयोगी है। छोटी " +
                    "कठिनाई भी आपके डॉक्टर के लिए महत्वपूर्ण जानकारी है।",
                actionLabel = "क्या बताना है",
                bullets = listOf(
                    "उस दिन क्या रुकावट आई",
                    "यह अक्सर होता है या एक बार",
                    "अगली बार क्या आसान बना सकता है"
                )
            )
        ),
        BarrierSupport(
            barrier = Barrier.PREFER_NOT_TO_SAY,
            action = ActionType.NONE,
            escalation = Escalation.NONE,
            en = BarrierText(
                label = "I'd rather not say",
                title = "That's completely fine",
                body = "You don't have to explain. Your dose has been recorded and you can " +
                    "carry on. We'll be here if you ever want to share."
            ),
            hi = BarrierText(
                label = "मैं नहीं बताना चाहता / चाहती",
                title = "यह बिलकुल ठीक है",
                body = "आपको कुछ समझाना ज़रूरी नहीं। आपकी खुराक दर्ज हो गई है, आप आगे " +
                    "बढ़ सकते हैं। जब भी बताना चाहें, हम यहाँ हैं।"
            )
        )
    )

    private val byBarrier: Map<Barrier, BarrierSupport> = all.associateBy { it.barrier }

    fun of(barrier: Barrier): BarrierSupport = byBarrier.getValue(barrier)
}
