package io.github.lanlacope.nxsharinghelper.clazz.propaty

fun removeStringsForFile(value: String): String {
    return value.replace(Regex("""[\x21-\x2f\x3a-\x3f\x5b-\x5e\x60\x7b-\x7e\\]"""), "")
}

fun getGameId(fileNames: List<String>): List<String> {
    val regex = Regex(""".*-(.*?)\..*?$""")
    val ids = fileNames.map { rawId ->
        val matchResult = regex.find(rawId)
        matchResult?.groupValues?.get(1) ?: ""
    }.distinct()
    return ids
}

