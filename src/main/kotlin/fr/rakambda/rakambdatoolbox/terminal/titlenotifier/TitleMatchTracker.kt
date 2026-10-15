package fr.rakambda.rakambdatoolbox.terminal.titlenotifier

import java.util.concurrent.atomic.AtomicReference

/**
 * Remembers which patterns matched the previous titles of one terminal, so that a notification is sent only when a
 * pattern appears, not on every title change while it stays present.
 *
 * Thread-safe: title listeners can be called from any thread.
 */
class TitleMatchTracker {
    private val matched = AtomicReference<Set<String>>(emptySet())

    /**
     * Records the patterns found in [titles] and returns those that were not found by the previous call.
     */
    fun update(titles: Collection<String>, patterns: Collection<String>): Set<String> {
        val current = patterns.filterTo(LinkedHashSet()) { pattern -> titles.any { it.contains(pattern) } }
        val previous = matched.getAndSet(current)
        return current - previous
    }
}
