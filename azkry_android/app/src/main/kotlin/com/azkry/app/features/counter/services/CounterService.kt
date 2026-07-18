package com.azkry.app.features.counter.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Selectable tasbih targets; 0 means a free (unbounded) counter. */
val COUNTER_TARGETS = listOf(33, 99, 100, 0)

data class CounterState(
    val count: Int = 0,
    val target: Int = COUNTER_TARGETS.first(),
) {
    val isFree: Boolean get() = target == 0

    /** Laps completed for bounded targets (a tasbih "round"). */
    val laps: Int get() = if (isFree) 0 else count / target

    val countInLap: Int get() = if (isFree) count else count % target
}

interface CounterService {
    val state: Flow<CounterState>

    suspend fun increment()
    suspend fun reset()
    suspend fun setTarget(target: Int)
}

@Singleton
class DataStoreCounterService @Inject constructor(
    @ApplicationContext context: Context,
) : CounterService {
    private val dataStore = context.counterDataStore

    override val state: Flow<CounterState> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences ->
                CounterState(
                    count = preferences[Keys.Count] ?: 0,
                    target = preferences[Keys.Target] ?: COUNTER_TARGETS.first(),
                )
            }

    override suspend fun increment() {
        dataStore.edit { preferences ->
            preferences[Keys.Count] = (preferences[Keys.Count] ?: 0) + 1
        }
    }

    override suspend fun reset() {
        dataStore.edit { preferences ->
            preferences[Keys.Count] = 0
        }
    }

    override suspend fun setTarget(target: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.Target] = target
            preferences[Keys.Count] = 0
        }
    }

    private object Keys {
        val Count = intPreferencesKey("count")
        val Target = intPreferencesKey("target")
    }
}

private val Context.counterDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "counter_prefs",
)
