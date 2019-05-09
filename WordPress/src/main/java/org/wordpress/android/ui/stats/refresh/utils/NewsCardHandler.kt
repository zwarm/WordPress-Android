package org.wordpress.android.ui.stats.refresh.utils

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.view.View
import kotlinx.coroutines.CoroutineDispatcher
import org.wordpress.android.fluxc.store.StatsStore
import org.wordpress.android.fluxc.store.StatsStore.InsightType
import org.wordpress.android.fluxc.store.StatsStore.StatsType
import org.wordpress.android.modules.BG_THREAD
import org.wordpress.android.modules.UI_THREAD
import org.wordpress.android.viewmodel.Event
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class NewsCardHandler
@Inject constructor(
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher,
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    private val statsStore: StatsStore,
    private val statsSiteProvider: StatsSiteProvider
) {
    private val mutableCardDismissed = MutableLiveData<Event<InsightType>>()
    val cardDismissed: LiveData<Event<InsightType>> = mutableCardDismissed

    fun onMenuClick(view: View, statsType: StatsType) {
    }

    enum class NewsCardAction {
        DISMISS, GO_TO_EDIT
    }
}
