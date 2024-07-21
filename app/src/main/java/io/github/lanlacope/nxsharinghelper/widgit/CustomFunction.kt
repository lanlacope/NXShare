package io.github.lanlacope.nxsharinghelper.widgit

import org.json.JSONArray

@Suppress("unused")
fun <T> Iterable<T>.toArrayList(): ArrayList<T> {
    if (this is Collection<T>)
        return this.toArrayList()
    return toCollection(ArrayList<T>())
}

@Suppress("unused")
fun <T> Collection<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}

@Suppress("unused")
inline fun JSONArray.forEachIndexOnly(action: (Int) -> Unit) {
    for (index in 0 until length()) action(index)
}

@Suppress("unused")
inline fun <R> JSONArray.mapIndexOnly(action: (Int) -> R): List<R> {
    return (0 until length()).map { index -> action(index) }
}
