// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.developer.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.core.notifications.NotificationChannel
import app.tivi.core.notifications.NotificationManager
import app.tivi.core.notifications.PendingNotification
import app.tivi.screens.DevNotificationsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class DevNotificationsPresenterFactory(
  private val presenterFactory: (Navigator) -> DevNotificationsPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is DevNotificationsScreen -> presenterFactory(navigator)
    else -> null
  }
}

@Inject
class DevNotificationsPresenter(
  @Assisted private val navigator: Navigator,
  private val notificationsManager: NotificationManager,
) : Presenter<DevNotificationsUiState> {

  @Composable
  override fun present(): DevNotificationsUiState {
    val coroutineScope = rememberCoroutineScope()
    var pending by remember { mutableStateOf(emptyList<PendingNotification>()) }

    LaunchedEffect(notificationsManager) {
      while (isActive) {
        pending = notificationsManager.getPendingNotifications()
        delay(1.seconds)
      }
    }

    fun eventSink(event: DevNotificationsUiEvent) {
      when (event) {
        DevNotificationsUiEvent.NavigateUp -> navigator.pop()
        DevNotificationsUiEvent.ScheduleNotification -> {
          coroutineScope.launch {
            notificationsManager.schedule(
              id = "scheduled_test",
              title = "Test Notification",
              message = "Scheduled from developer settings",
              channel = NotificationChannel.DEVELOPER,
              date = Clock.System.now() + 15.minutes,
            )
          }
        }

        DevNotificationsUiEvent.ShowNotification -> {
          coroutineScope.launch {
            notificationsManager.schedule(
              id = "immediate_test",
              title = "Test Notification",
              message = "Sent from developer settings",
              channel = NotificationChannel.DEVELOPER,
              date = Clock.System.now() + 5.seconds,
            )
          }
        }
      }
    }

    return DevNotificationsUiState(
      pendingNotifications = pending,
      eventSink = ::eventSink,
    )
  }
}
