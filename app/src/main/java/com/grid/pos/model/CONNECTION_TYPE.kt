package com.grid.pos.model

import com.grid.pos.data.DataModel

enum class CONNECTION_TYPE(val key: String) {
    LOCAL("LOCAL"), FIRESTORE("FIRESTORE"), SQL_SERVER("SQL_SERVER")
}

data class ConnectionModel(
        val connectionName: String
) : DataModel() {
    override fun getName(): String {
        return connectionName
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}

enum class ORIENTATION_TYPE(val key: String) {
    PORTRAIT("Portrait"), LANDSCAPE("Landscape"), DEVICE_SENSOR("Device Sensor")
}

data class OrientationModel(
        val orientatioName: String
) : DataModel() {
    override fun getName(): String {
        return orientatioName
    }

    override fun search(key: String): Boolean {
        return getName().contains(
            key,
            ignoreCase = true
        )
    }
}

enum class Country(
        val code: String,
        val value: String
) {
    DEFAULT(
        "default",
        "Default"
    )
}

data class ReportCountry(
        val country: Country
) : DataModel() {

    override fun getId(): String {
        return country.code
    }

    override fun getName(): String {
        return country.value
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
        "default",
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

data class ReportLanguage(
        val language: Language
) : DataModel() {

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
