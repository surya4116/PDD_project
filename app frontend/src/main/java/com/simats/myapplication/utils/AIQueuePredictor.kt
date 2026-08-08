package com.simats.myapplication.utils

import com.simats.myapplication.data.local.entity.SlotEntity

/**
 * AI Smart Queue Prediction Engine
 * 
 * Provides predictive waiting times, congestion levels, peak hour analysis, 
 * token prioritization weights, counter allocation, and alternative slot suggestions.
 */
object AIQueuePredictor {

    /**
     * Waiting Time Prediction:
     * Predicts waiting time (in minutes) based on queue position, service speed (e.g. 15 mins),
     * and a congestion multiplier.
     */
    fun predictWaitingTime(queuePosition: Int, currentTokens: Int, maxTokens: Int): Int {
        if (queuePosition <= 0) return 0
        
        val baseWaitTime = 15 // 15 mins base per client
        val congestionMultiplier = when {
            maxTokens <= 0 -> 1.0
            (currentTokens.toDouble() / maxTokens) >= 0.8 -> 1.4 // High congestion slows down counter
            (currentTokens.toDouble() / maxTokens) >= 0.5 -> 1.15 // Moderate congestion
            else -> 0.9 // Normal/low queue is handled faster
        }
        
        return (queuePosition * baseWaitTime * congestionMultiplier).toInt()
    }

    /**
     * Queue Congestion Detection:
     * Returns a visual string representation of queue density.
     */
    fun detectCongestion(currentTokens: Int, maxTokens: Int): String {
        if (maxTokens <= 0) return "Normal"
        val density = currentTokens.toDouble() / maxTokens
        return when {
            density >= 0.8 -> "High Congestion"
            density >= 0.5 -> "Moderate Congestion"
            else -> "Normal"
        }
    }

    /**
     * Peak Hour Analysis:
     * Analyzes if a given time slot falls into peak traffic hours (e.g. 10 AM - 12 PM or 2 PM - 4 PM).
     */
    fun isPeakHour(startTime: String): Boolean {
        // Simple mock parser: e.g. "10:30 AM", "14:00"
        val normalized = startTime.uppercase()
        return normalized.contains("10:") || normalized.contains("11:") || 
               normalized.contains("02:") || normalized.contains("03:") ||
               normalized.contains("14:") || normalized.contains("15:")
    }

    /**
     * Automatic Counter Allocation:
     * Dynamically allocates tokens to counters (Counter 1, 2, or 3) using token prefixes/IDs.
     */
    fun allocateCounter(tokenNumber: String): String {
        // Hashing the token number to allocate counter
        val sum = tokenNumber.fold(0) { acc, char -> acc + char.code }
        val counterNum = (sum % 3) + 1
        return "Counter $counterNum"
    }

    /**
     * Token Prioritization:
     * Calculates priority weight (higher means more priority).
     * e.g. Senior citizens (age >= 60) get higher priority (weight > 1).
     */
    fun getPriorityWeight(age: Int): Double {
        return if (age >= 60) 1.5 else 1.0
    }

    /**
     * Alternative Slot Recommendation:
     * Recommends a slot from the alternatives list that has the lowest congestion level.
     */
    fun recommendAlternativeSlot(slots: List<SlotEntity>): SlotEntity? {
        return slots.filter { it.currentTokens < it.maxTokens }
            .minByOrNull { it.currentTokens.toDouble() / it.maxTokens }
    }
}
