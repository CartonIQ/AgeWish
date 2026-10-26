package com.agewish.calculator.util

data class OccasionConfig(
    val name: String,
    val emoji: String,
    val defaultGreeting: String,
    val defaultThemeIndex: Int
)

data class ThemePreset(
    val id: String,
    val name: String,
    val startColor: Int,
    val endColor: Int,
    val textColor: Int = 0xFFFFFFFF.toInt(),
    val accentColor: Int = 0xFFFFD54F.toInt()
)

object WishTemplates {

    val THEME_PRESETS = listOf(
        ThemePreset(
            id = "sunset_red",
            name = "Sunset Red 🎂",
            startColor = 0xFFFF5252.toInt(),
            endColor = 0xFFFF7A00.toInt()
        ),
        ThemePreset(
            id = "royal_violet",
            name = "Royal Violet 💍",
            startColor = 0xFF9C27B0.toInt(),
            endColor = 0xFFE91E63.toInt()
        ),
        ThemePreset(
            id = "golden_grand",
            name = "Golden Grand 🎊",
            startColor = 0xFFD4AF37.toInt(),
            endColor = 0xFFE5C158.toInt(),
            textColor = 0xFF2C2400.toInt(),
            accentColor = 0xFF4E3800.toInt()
        ),
        ThemePreset(
            id = "midnight_spark",
            name = "Midnight Spark 🎆",
            startColor = 0xFF1A237E.toInt(),
            endColor = 0xFF4A148C.toInt()
        ),
        ThemePreset(
            id = "emerald_glow",
            name = "Emerald Glow 🌙",
            startColor = 0xFF004D40.toInt(),
            endColor = 0xFF00897B.toInt()
        ),
        ThemePreset(
            id = "festive_pine",
            name = "Festive Pine 🎄",
            startColor = 0xFFB71C1C.toInt(),
            endColor = 0xFF1B5E20.toInt()
        )
    )

    val OCCASIONS = listOf(
        OccasionConfig(
            name = "Birthday",
            emoji = "🎂",
            defaultGreeting = "Wishing you a wonderful birthday filled with boundless love, laughter, and sweet moments! May this year bring you closer to all your cherished dreams.",
            defaultThemeIndex = 0
        ),
        OccasionConfig(
            name = "Anniversary",
            emoji = "💍",
            defaultGreeting = "Happy Anniversary! Celebrating the enduring love and wonderful memories you have built together. Wishing you many more decades of togetherness and bliss.",
            defaultThemeIndex = 1
        ),
        OccasionConfig(
            name = "Wedding",
            emoji = "🎊",
            defaultGreeting = "Warmest congratulations on your wedding day! May the love and joy you share today grow exponentially with each passing year. Wishing you a lifetime of happiness.",
            defaultThemeIndex = 2
        ),
        OccasionConfig(
            name = "New Year",
            emoji = "🎆",
            defaultGreeting = "Happy New Year! May the coming year bring new happiness, new goals, breakthrough achievements, and a wealth of inspiration to your journey.",
            defaultThemeIndex = 3
        ),
        OccasionConfig(
            name = "Diwali",
            emoji = "🪔",
            defaultGreeting = "Wishing you and your loved ones a very Happy and Prosperous Diwali! May the festival of lights illuminate your path with health, peace, and abundance.",
            defaultThemeIndex = 2
        ),
        OccasionConfig(
            name = "Eid",
            emoji = "🌙",
            defaultGreeting = "Eid Mubarak! May the divine blessings of Allah bring peace, faith, and endless joy to your heart, family, and home today and always.",
            defaultThemeIndex = 4
        ),
        OccasionConfig(
            name = "Christmas",
            emoji = "🎄",
            defaultGreeting = "Merry Christmas! May the peace, wonder, and warmth of the festive season fill your home with laughter and goodwill all year long.",
            defaultThemeIndex = 5
        ),
        OccasionConfig(
            name = "Custom",
            emoji = "✨",
            defaultGreeting = "Sending you warmest wishes and heartfelt regards! May your days ahead be radiant, peaceful, and filled with great happiness.",
            defaultThemeIndex = 4
        )
    )

    fun getOccasionByName(name: String): OccasionConfig {
        return OCCASIONS.find { it.name.equals(name, ignoreCase = true) } ?: OCCASIONS[0]
    }
}
