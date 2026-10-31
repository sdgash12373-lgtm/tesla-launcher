package com.sdgash.carlauncher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sdgash.carlauncher.data.AppEntry
import com.sdgash.carlauncher.data.AppRepository
import com.sdgash.carlauncher.data.DockStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    val repo = AppRepository(app)
    private val dockStore = DockStore(app)

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    private val _dockKeys = MutableStateFlow<List<String>?>(dockStore.load())

    /** Dock apps in the user's order; falls back to defaults until edited. */
    val dock: StateFlow<List<AppEntry>> = combine(_apps, _dockKeys) { apps, keys ->
        if (keys != null) {
            val byKey = apps.associateBy { it.key }
            keys.mapNotNull { byKey[it] }
        } else {
            DockStore.DEFAULT_PACKAGES
                .mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
                .distinctBy { it.packageName }
                .take(5)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var loadJob: Job? = null
    private val unregister = repo.registerChanges { reload() }

    init {
        reload()
    }

    fun reload() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { _apps.value = repo.loadApps() }
    }

    fun isPinned(app: AppEntry) = dock.value.any { it.key == app.key }

    fun togglePin(app: AppEntry) {
        val current = dock.value.map { it.key }.toMutableList()
        if (app.key in current) current.remove(app.key)
        else if (current.size < DockStore.MAX_DOCK) current.add(app.key)
        _dockKeys.value = current
        dockStore.save(current)
    }

    override fun onCleared() {
        unregister()
    }
}
