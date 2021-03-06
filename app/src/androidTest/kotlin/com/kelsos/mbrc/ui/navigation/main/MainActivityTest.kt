package com.kelsos.mbrc.ui.navigation.main

import android.app.Application
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.activestatus.MainDataModel
import com.kelsos.mbrc.content.activestatus.ModelCache
import com.kelsos.mbrc.content.library.tracks.TrackInfo
import com.kelsos.mbrc.events.bus.RxBus
import com.kelsos.mbrc.platform.ServiceChecker
import com.kelsos.mbrc.preferences.SettingsManager
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.testing.ToothPickRule

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

  private var toothPickRule = ToothPickRule(this)
  private val activityRule = IntentsTestRule(
    MainActivity::class.java,
    true,
    false
  )

  @Rule
  fun chain(): TestRule = RuleChain.outerRule(toothPickRule).around(activityRule)

  private lateinit var model: MainDataModel
  private lateinit var mockBus: RxBus
  private lateinit var mockSettingsManager: SettingsManager
  private lateinit var mockCache: ModelCache
  private lateinit var application: Application
  private lateinit var mockServiceChecker: ServiceChecker

  @Before
  fun setUp() {
    mockSettingsManager = mock(SettingsManager::class.java)
    mockCache = mock(ModelCache::class.java)
    mockServiceChecker = mock(ServiceChecker::class.java)

    val trackInfo = TrackInfo()
    given(mockCache.restoreCover()).willReturn(Single.just(""))
    given(mockCache.persistCover(anyString())).willReturn(Completable.complete())
    given(mockCache.restoreInfo()).willReturn(Single.just(trackInfo))
    given(mockCache.persistInfo(trackInfo)).willReturn(Completable.complete())
    given(mockSettingsManager.shouldShowChangeLog()).willReturn(Single.just(false))
    mockBus = mock(RxBus::class.java)

    model = MainDataModel(mockBus, mockCache)

    application = InstrumentationRegistry.getTargetContext().applicationContext as Application
    val scope = Toothpick.openScope(application)
    scope.installModules(TestModule())
  }

  @After
  fun tearDown() {
    Toothpick.reset()
  }

  @Test
  fun testShowOutdatedDialog() {
    activityRule.launchActivity(Intent())
    onView(withText(R.string.main__dialog_plugin_outdated_message)).check(doesNotExist())
    model.pluginProtocol = 3
    onView(withText(R.string.main__dialog_plugin_outdated_message)).check(matches(isDisplayed()))
  }

  @Test
  fun testShouldNotShowOutdatedPluginSnackBar() {
    activityRule.launchActivity(Intent())
    onView(withText(R.string.main__dialog_plugin_outdated_message)).check(doesNotExist())
    model.pluginProtocol = LATEST_PROTOCOL_VERSION
    onView(withText(R.string.main__dialog_plugin_outdated_message)).check(doesNotExist())
  }

  @Test
  fun testShouldShowChangeLog() {
    given(mockSettingsManager.shouldShowChangeLog()).willReturn(Single.just(true))
    activityRule.launchActivity(Intent())
    onView(withText(R.string.main__dialog_change_log)).check(matches(isDisplayed()))
    verify(mockSettingsManager, times(1)).shouldShowChangeLog()
  }

  @Test
  fun testShouldNoShowChangeLog() {
    activityRule.launchActivity(Intent())
    onView(withText(R.string.main__dialog_change_log)).check(doesNotExist())
    verify(mockSettingsManager, times(1)).shouldShowChangeLog()
  }

  inner class TestModule : Module() {
    init {
      bind(MainDataModel::class.java).toProviderInstance { model }.providesSingletonInScope()
      bind(RxBus::class.java).toProviderInstance { mockBus }.providesSingletonInScope()
      bind(SettingsManager::class.java)
        .toProviderInstance { mockSettingsManager }
        .providesSingletonInScope()
      bind(Scheduler::class.java).withName("main").toProviderInstance { TestScheduler() }
      bind(Application::class.java).toInstance(application)
      bind(ServiceChecker::class.java).toInstance(mockServiceChecker)
    }
  }

  companion object {
    const val LATEST_PROTOCOL_VERSION = 5
  }
}