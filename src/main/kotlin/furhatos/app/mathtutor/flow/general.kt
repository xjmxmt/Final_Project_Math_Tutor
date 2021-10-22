package furhatos.app.mathtutor.flow

import furhatos.app.mathtutor.nlu.Swearing
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.util.*

val Idle: State = state {

    init {

        // Make Furhat interruptable during all furhat.ask(...)
        furhat.param.interruptableOnAsk = true
        // Make Furhat interruptable during all furhat.say(...)
        furhat.param.interruptableOnSay = true
        furhat.param.interruptableDelay = 1000

        furhat.setVoice(Language.ENGLISH_US, Gender.MALE)
        if (users.count > 0) {
            furhat.attend(users.random)
            goto(Start)
        }
}

    onEntry {
        furhat.attendNobody()
    }

    onUserEnter {
        furhat.attend(it)
        goto(Start)
    }
}

val Interaction: State = state {

    onUserLeave(instant = true) {
        if (users.count > 0) {
            if (it == users.current) {
                furhat.attend(users.other)
                goto(Start)
            } else {
                furhat.glance(it)
            }
        } else {
            goto(Idle)
        }
    }

    onUserEnter(instant = true) {
        furhat.glance(it)
    }
}


val FallbackState: State = state(Interaction) {

     onResponse(cond={it.interrupted}) {

        furhat.glance(it.userId, 1000)

        furhat.ask({random{
            + "Sorry?"
            + "What did you say?"
            + "um.."
            + "Can you repeat?"
        }})
    }

    onResponse<Swearing> {
        furhat.attendAll()
        furhat.say("That is not kind to say.")
        reentry()
    }

    onResponse {
        furhat.gesture(
                Gestures.BigSmile(0.4, 2.0)
        )

        furhat.say(
                """
                    I’m sorry, could you rephrase what you just said?
                """.trimIndent()
        )

        reentry()
    }
}