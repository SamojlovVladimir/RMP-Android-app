package ru.samoylov.matule.uikit

/**
 * AppIcons — коды символов шрифта иконок uikit_icons.ttf.
 * Файл создан скриптом из описания иконок, вручную не правится.
 * Используется в AppIcon: icon.setIcon(AppIcons.SEARCH)
 */
object AppIcons {
    const val BACK = "\uE000"
    const val CHEVRON_DOWN = "\uE001"
    const val SEARCH = "\uE002"
    const val PLUS = "\uE003"
    const val MINUS = "\uE004"
    const val CLOSE = "\uE005"
    const val CLOSE_CIRCLE = "\uE006"
    const val COMMENT = "\uE007"
    const val FILTER = "\uE008"
    const val DOWNLOAD = "\uE009"
    const val MAP = "\uE00A"
    const val MORE = "\uE00B"
    const val TRASH = "\uE00C"
    const val CART = "\uE00D"
    const val CHECK = "\uE00E"
    const val DOC = "\uE00F"
    const val SEND = "\uE010"
    const val MIC = "\uE011"
    const val ATTACH = "\uE012"
    const val EYE = "\uE013"
    const val EYE_OFF = "\uE014"
    const val BACKSPACE = "\uE015"
    const val FINGERPRINT = "\uE016"
    const val TAB_HOME = "\uE017"
    const val TAB_CATALOG = "\uE018"
    const val TAB_PROJECTS = "\uE019"
    const val TAB_PROFILE = "\uE01A"

    /** Все иконки по имени — нужно для атрибута app:iconName в XML. */
    val byName: Map<String, String> = mapOf(
        "back" to BACK,
        "chevron_down" to CHEVRON_DOWN,
        "search" to SEARCH,
        "plus" to PLUS,
        "minus" to MINUS,
        "close" to CLOSE,
        "close_circle" to CLOSE_CIRCLE,
        "comment" to COMMENT,
        "filter" to FILTER,
        "download" to DOWNLOAD,
        "map" to MAP,
        "more" to MORE,
        "trash" to TRASH,
        "cart" to CART,
        "check" to CHECK,
        "doc" to DOC,
        "send" to SEND,
        "mic" to MIC,
        "attach" to ATTACH,
        "eye" to EYE,
        "eye_off" to EYE_OFF,
        "backspace" to BACKSPACE,
        "fingerprint" to FINGERPRINT,
        "tab_home" to TAB_HOME,
        "tab_catalog" to TAB_CATALOG,
        "tab_projects" to TAB_PROJECTS,
        "tab_profile" to TAB_PROFILE
    )
}
