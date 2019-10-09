package com.crestron.aurora.db

import com.crestron.aurora.showapi.Source

enum class NotWorkingShowSources (val url: String, val replacedBy: Source?) {
    ANIMEPLUS_TV("animeplus.tv", Source.ANIME)
}

enum class ShowSource(val sourceName: String) {
    PUTLOCKER("putlocker"),
    GOGOANIME("gogoanime"),
    ANIMETOON("animetoon")
}