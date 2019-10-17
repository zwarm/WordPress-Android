package org.wordpress.android.ui.sitecreation.usecases

import kotlinx.coroutines.suspendCancellableCoroutine
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.generated.SiteActionBuilder
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.OnDomainAvailabilityChecked
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Transforms newCheckDomainAvailabilityAction EventBus event to a coroutine.
 *
 * The client may dispatch multiple requests, but we want to accept only the latest one and ignore all others.
 * We can't rely just on job.cancel() as the OnDomainAvailabilityChecked may have already been dispatched and FluxC will
 * return a result.
 */
class CheckDomainAvailabilityUseCase @Inject constructor(
    val dispatcher: Dispatcher,
    @Suppress("unused") val siteStore: SiteStore
) {
    /**
     * Query - Continuation pair
     */
    private var pair: Pair<String, Continuation<OnDomainAvailabilityChecked>>? = null

    suspend fun checkDomainAvailability(domainName: String): OnDomainAvailabilityChecked {
        return suspendCancellableCoroutine { cont ->
            pair = Pair(domainName, cont)
            dispatcher.dispatch(SiteActionBuilder.newCheckDomainAvailabilityAction(domainName))
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    @Suppress("unused")
    fun onDomainAvailabilityChecked(event: OnDomainAvailabilityChecked) {
        pair?.let {
            if (event.domainName == it.first) {
                it.second.resume(event)
                pair = null
            }
        }
    }
}
