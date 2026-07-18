package com.azkry.app.features.counter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.features.counter.services.CounterService
import com.azkry.app.features.counter.services.CounterState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CounterViewModel @Inject constructor(
    private val counterService: CounterService,
) : ViewModel() {
    val state: StateFlow<CounterState?> =
        counterService.state.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun onIncrement() {
        viewModelScope.launch { counterService.increment() }
    }

    fun onReset() {
        viewModelScope.launch { counterService.reset() }
    }

    fun onTargetSelected(target: Int) {
        viewModelScope.launch { counterService.setTarget(target) }
    }
}
