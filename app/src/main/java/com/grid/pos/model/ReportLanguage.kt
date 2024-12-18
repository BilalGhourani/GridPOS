package com.grid.pos.model

import com.grid.pos.data.EntityModel

data class ReportLanguage(
        val language: Language
) : EntityModel() {

    override fun getId(): String {
        return language.code
    }

    override fun getName(): String {
        return language.value
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}

enum class Language(
        val code: String,
        val value: String
) {
    DEFAULT(
        "Default",
        "Default"
    ),
    ENGLISH(
        "en",
        "English"
    ),
    SPANISH(
        "es",
        "Spanish"
    ),
    FRENCH(
        "fr",
        "French"
    ),
    GERMAN(
        "de",
        "German"
    ),
    CHINESE(
        "zh",
        "Chinese"
    ),
    HINDI(
        "hi",
        "Hindi"
    ),
    ARABIC(
        "ar",
        "Arabic"
    ),
    RUSSIAN(
        "ru",
        "Russian"
    ),
    PORTUGUESE(
        "pt",
        "Portuguese"
    ),
    JAPANESE(
        "ja",
        "Japanese"
    ),
    KOREAN(
        "ko",
        "Korean"
    ),
    ITALIAN(
        "it",
        "Italian"
    ),
    DUTCH(
        "nl",
        "Dutch"
    ),
    SWEDISH(
        "sv",
        "Swedish"
    ),
    NORWEGIAN(
        "no",
        "Norwegian"
    ),
    DANISH(
        "da",
        "Danish"
    ),
    FINNISH(
        "fi",
        "Finnish"
    ),
    GREEK(
        "el",
        "Greek"
    ),
    HEBREW(
        "he",
        "Hebrew"
    ),
    INDONESIAN(
        "id",
        "Indonesian"
    ),
    MALAY(
        "ms",
        "Malay"
    ),
    THAI(
        "th",
        "Thai"
    ),
    VIETNAMESE(
        "vi",
        "Vietnamese"
    ),
    TURKISH(
        "tr",
        "Turkish"
    ),
    POLISH(
        "pl",
        "Polish"
    ),
    UKRAINIAN(
        "uk",
        "Ukrainian"
    ),
    ROMANIAN(
        "ro",
        "Romanian"
    ),
    CZECH(
        "cs",
        "Czech"
    ),
    HUNGARIAN(
        "hu",
        "Hungarian"
    ),
    SLOVAK(
        "sk",
        "Slovak"
    ),
    BULGARIAN(
        "bg",
        "Bulgarian"
    ),
    SERBIAN(
        "sr",
        "Serbian"
    ),
    CROATIAN(
        "hr",
        "Croatian"
    ),
    LITHUANIAN(
        "lt",
        "Lithuanian"
    ),
    LATVIAN(
        "lv",
        "Latvian"
    ),
    ESTONIAN(
        "et",
        "Estonian"
    ),
    PERSIAN(
        "fa",
        "Persian"
    ),
    URDU(
        "ur",
        "Urdu"
    );


}