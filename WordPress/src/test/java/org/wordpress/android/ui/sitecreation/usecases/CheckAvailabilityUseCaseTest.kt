package org.wordpress.android.ui.sitecreation.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.annotations.action.Action
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.SiteStore.DomainAvailabilityStatus.INVALID_DOMAIN
import org.wordpress.android.fluxc.store.SiteStore.DomainMappabilityStatus
import org.wordpress.android.fluxc.store.SiteStore.OnDomainAvailabilityChecked
import org.wordpress.android.test

private const val DOMAIN_NAME = "example"

@RunWith(MockitoJUnitRunner::class)
class CheckAvailabilityUseCaseTest {
    @Rule
    @JvmField val rule = InstantTaskExecutorRule()

    @Mock lateinit var dispatcher: Dispatcher
    @Mock lateinit var store: SiteStore
    private lateinit var useCase: CheckDomainAvailabilityUseCase
    private lateinit var dispatchCaptor: KArgumentCaptor<Action<String>>
    private val event = OnDomainAvailabilityChecked(
            DOMAIN_NAME,
            INVALID_DOMAIN,
            DomainMappabilityStatus.INVALID_DOMAIN,
            true,
            null
    )

    @Before
    fun setUp() {
        useCase = CheckDomainAvailabilityUseCase(dispatcher, store)
        dispatchCaptor = argumentCaptor()
    }

    @Test
    fun coroutineResumedWhenResultEventDispatched() = test {
        whenever(dispatcher.dispatch(any())).then { useCase.onDomainAvailabilityChecked(event) }

        val resultEvent = useCase.checkDomainAvailability(DOMAIN_NAME)

        verify(dispatcher).dispatch(dispatchCaptor.capture())
        assertEquals(dispatchCaptor.lastValue.payload, DOMAIN_NAME)
        assertEquals(event, resultEvent)
    }
}
