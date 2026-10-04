package com.example.topmoviesfeature.watchlist

import android.content.SharedPreferences
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class WatchlistManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val WATCHLIST_KEY = "watchlist_films"
    }

    fun addToWatchlist(filmId: Long) {
        val watchlist = getWatchlist().toMutableSet()
        watchlist.add(filmId)
        saveWatchlist(watchlist)
    }

    fun removeFromWatchlist(filmId: Long) {
        val watchlist = getWatchlist().toMutableSet()
        watchlist.remove(filmId)
        saveWatchlist(watchlist)
    }

    fun isInWatchlist(filmId: Long): Boolean {
        return getWatchlist().contains(filmId)
    }

    fun getWatchlist(): Set<Long> {
        return sharedPreferences
            .getStringSet(WATCHLIST_KEY, emptySet())
            ?.mapNotNull { it.toLongOrNull() }
            ?.toSet() ?: emptySet()
    }

    private fun saveWatchlist(watchlist: Set<Long>) {
        sharedPreferences.edit()
            .putStringSet(WATCHLIST_KEY, watchlist.map { it.toString() }.toSet())
            .apply()
    }
}
