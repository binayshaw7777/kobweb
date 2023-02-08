package com.varabyte.kobweb.compose.css

import org.jetbrains.compose.web.css.*

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Transitions/Using_CSS_transitions

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/transition-property
class TransitionProperty private constructor(val value: String) {
    companion object {
        // Custom
        fun of(customValue: String) = TransitionProperty(customValue)

        // Keywords
        val None get() = TransitionProperty("none")
        val All get() = TransitionProperty("all")

        // Global values
        val Inherit get() = TransitionProperty("inherit")
        val Initial get() = TransitionProperty("initial")
        val Revert get() = TransitionProperty("revert")
        val RevertLayer get() = TransitionProperty("revert-layer")
        val Unset get() = TransitionProperty("unset")
    }
}

fun StyleScope.transitionProperty(property: TransitionProperty) {
    transitionProperty(property.value)
}

fun StyleScope.transitionProperty(vararg properties: String) {
    property("transition-property", properties.joinToString())
}

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/transition-duration
/**
 * Special values for Transition Duration Property.
 */
class TransitionDuration private constructor(val value: String) {
    companion object {
        // Global values
        val Inherit get() = TransitionDuration("inherit")
        val Initial get() = TransitionDuration("initial")
        val Revert get() = TransitionDuration("revert")
        val Unset get() = TransitionDuration("unset")
    }
}

fun StyleScope.transitionDuration(duration: TransitionDuration) {
    property("transition-duration", duration.value)
}

fun StyleScope.transitionDuration(vararg duration: CSSSizeValue<out CSSUnitTime>) {
    property("transition-duration", duration.joinToString())
}

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/transition-delay
/**
 * Special values for Transition Delay Property.
 */
class TransitionDelay private constructor(val value: String) {
    companion object {
        // Global values
        val Inherit get() = TransitionDelay("inherit")
        val Initial get() = TransitionDelay("initial")
        val Revert get() = TransitionDelay("revert")
        val RevertLayer get() = TransitionDelay("revert-layer")
        val Unset get() = TransitionDelay("unset")
    }
}

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/transition-delay
fun StyleScope.transitionDelay(vararg delay: CSSSizeValue<out CSSUnitTime>) {
    property("transition-delay", delay.joinToString())
}

fun StyleScope.transitionDelay(delay: TransitionDelay) {
    property("transition-delay", delay.value)
}

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/transition-timing-function
typealias TransitionTimingFunction = AnimationTimingFunction

fun StyleScope.transitionTimingFunction(vararg value: TransitionTimingFunction) {
    property("transition-timing-function", value.joinToString { it.value })
}

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/transition
data class CSSTransition(
    val property: TransitionProperty,
    val duration: CSSSizeValue<out CSSUnitTime>? = null,
    val timingFunction: TransitionTimingFunction? = null,
    val delay: CSSSizeValue<out CSSUnitTime>? = null,
) : CSSStyleValue {
    constructor(
        property: String, duration: CSSSizeValue<out CSSUnitTime>? = null,
        timingFunction: TransitionTimingFunction? = null,
        delay: CSSSizeValue<out CSSUnitTime>? = null
    ) : this(TransitionProperty.of(property), duration, timingFunction, delay)

    override fun toString() = buildList {
        add(property.value)
        // https://developer.mozilla.org/en-US/docs/Web/CSS/animation#syntax
        duration?.let { add(it.toString()) }
        timingFunction?.let { add(it.toString()) }
        if (delay != null) {
            if (duration == null) {
                add("0s") // Needed so parser knows that the next time string is for "delay"
            }
            add(delay.toString())
        }
    }.joinToString(" ")
}

fun StyleScope.transition(vararg transitions: CSSTransition) {
    if (transitions.isNotEmpty()) {
        property("transition", transitions.joinToString())
    }
}
