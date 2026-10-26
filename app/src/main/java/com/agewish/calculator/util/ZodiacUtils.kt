package com.agewish.calculator.util

object ZodiacUtils {

    fun getWesternZodiac(month: Int, day: Int): String {
        return when (month) {
            1 -> if (day < 20) "Capricorn ♑" else "Aquarius ♒"
            2 -> if (day < 19) "Aquarius ♒" else "Pisces ♓"
            3 -> if (day < 21) "Pisces ♓" else "Aries ♈"
            4 -> if (day < 20) "Aries ♈" else "Taurus ♉"
            5 -> if (day < 21) "Taurus ♉" else "Gemini ♊"
            6 -> if (day < 21) "Gemini ♊" else "Cancer ♋"
            7 -> if (day < 23) "Cancer ♋" else "Leo ♌"
            8 -> if (day < 23) "Leo ♌" else "Virgo ♍"
            9 -> if (day < 23) "Virgo ♍" else "Libra ♎"
            10 -> if (day < 23) "Libra ♎" else "Scorpio ♏"
            11 -> if (day < 22) "Scorpio ♏" else "Sagittarius ♐"
            12 -> if (day < 22) "Sagittarius ♐" else "Capricorn ♑"
            else -> "Unknown"
        }
    }

    fun getChineseZodiac(year: Int): String {
        val animals = arrayOf(
            "Rat 🐀", "Ox 🐂", "Tiger 🐅", "Rabbit 🐇",
            "Dragon 🐉", "Snake 🐍", "Horse 🐎", "Goat 🐐",
            "Monkey 🐒", "Rooster 🐓", "Dog 🐕", "Pig 🐖"
        )
        val remainder = ((year - 4) % 12 + 12) % 12
        return animals[remainder]
    }
}
