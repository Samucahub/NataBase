package com.belasoft.natabase_alpha.utils

import java.util.regex.Pattern

object InputSanitizer {
    private val SQL_INJECTION_PATTERNS = listOf(
        Pattern.compile("(\\%27)|(\\')|(\\-\\-)|(\\%23)|(#)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("((\\%3D)|(=))[^\\n]*((\\%27)|(\\')|(\\-\\-)|(\\%3B)|(;))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("w*((\\%27)|(\\'))((\\%6F)|o|(\\%4F))((\\%72)|r|(\\%52))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\%27)|(\\')|(\\%22)|(\")", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(\\b)(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|EXEC|ALTER|CREATE|TRUNCATE)(\\b)")
    )

    private val XSS_PATTERNS = listOf(
        Pattern.compile("<script.*?>.*?</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onclick|onload|onerror|onmouseover", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe.*?>.*?</iframe>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<object.*?>.*?</object>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<embed.*?>.*?</embed>", Pattern.CASE_INSENSITIVE)
    )

    private val DANGEROUS_CHARS = charArrayOf('<', '>', '"', '\'', '&', ';', '(', ')', '[', ']', '{', '}')

    fun sanitizeProductInput(input: String): String {
        if (input.isBlank()) return input

        var sanitized = input.trim()

        SQL_INJECTION_PATTERNS.forEach { pattern ->
            sanitized = pattern.matcher(sanitized).replaceAll("")
        }

        XSS_PATTERNS.forEach { pattern ->
            sanitized = pattern.matcher(sanitized).replaceAll("")
        }

        DANGEROUS_CHARS.forEach { char ->
            sanitized = sanitized.replace(char.toString(), "\\$char")
        }

        return sanitized.take(100).replace("\\s+".toRegex(), " ")
    }

    fun sanitizeNumericInput(input: String): String {
        return input.replace("[^0-9]".toRegex(), "")
    }

    fun isValidQuantity(quantidade: Int): Boolean {
        return quantidade in 0..1000
    }

    fun isValidProductName(name: String): Boolean {
        if (name.isBlank() || name.length > 100) return false

        val safePattern = Pattern.compile("^[a-zA-Z0-9\\s\\-\\.,À-ÿ]+$")
        return safePattern.matcher(name).matches()
    }

    fun sanitizeFileName(input: String): String {
        var sanitized = input.trim()

        val dangerousFileNameChars = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        dangerousFileNameChars.forEach { char ->
            sanitized = sanitized.replace(char.toString(), "_")
        }

        return sanitized.take(50)
    }

    fun validateEmail(email: String): Boolean {
        if (email.isBlank()) return false

        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        )
        return emailPattern.matcher(email).matches()
    }
}