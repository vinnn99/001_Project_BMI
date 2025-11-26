package com.example.projectbmi.utils

object TextUtils {
    // Simple sanitizer to remove common markdown artifacts while preserving readable text
    fun sanitizeAssistantText(input: String?): String {
        if (input.isNullOrBlank()) return ""

        var text = input

        // Remove fenced code blocks ```...```
        text = text.replace(Regex("```[\\s\\S]*?```"), "")

        // Remove inline code ticks
        text = text.replace("`", "")

        // Remove heading markers at the start of lines (e.g., ###, ##, #)
        text = text.replace(Regex("(?m)^\\s*#+\\s*"), "")

        // Remove bold/italic asterisks or underscores
        text = text.replace(Regex("[*_]{1,3}"), "")

        // Replace list markers (e.g., - , * , numbered lists) with a simple dash + space
        text = text.replace(Regex("(?m)^\\s*[-*+]\\s+"), "- ")
        text = text.replace(Regex("(?m)^\\s*\\d+\\.\\s+"), "- ")

        // Collapse multiple blank lines into a single blank line
        text = text.replace(Regex("(?m)^[ \t]*\r?\n{2,}"), "\n\n")

        // Trim whitespace at start and end
        text = text.trim()

        return text
    }
}
