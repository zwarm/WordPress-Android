package org.wordpress.android.ui.stats.refresh.lists

import android.arch.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.ui.stats.refresh.NavigationTarget
import org.wordpress.android.ui.stats.refresh.lists.sections.BaseStatsUseCase
import org.wordpress.android.util.distinct

class BaseViewAllUseCase
(
    private val bgDispatcher: CoroutineDispatcher,
    private val useCase: BaseStatsUseCase<*, *>
) {
    val data: LiveData<StatsBlock> = useCase.liveData.distinct()

    val navigationTarget: LiveData<NavigationTarget> = useCase.navigationTarget

    suspend fun loadData(site: SiteModel) {
        loadData(site, false, false)
    }

    suspend fun refreshData(site: SiteModel, forced: Boolean = false) {
        loadData(site, true, forced)
    }

    private suspend fun loadData(site: SiteModel, refresh: Boolean, forced: Boolean) {
        withContext(bgDispatcher) {
            useCase.fetch(site, refresh, forced)
        }
    }

    fun onCleared() {
        useCase.clear()
    }
}
