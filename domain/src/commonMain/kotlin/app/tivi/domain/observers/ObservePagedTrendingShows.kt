// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.data.compoundmodels.TrendingEntryWithShow
import app.tivi.data.daos.TrendingDao
import app.tivi.domain.PaginatedEntryRemoteMediator
import app.tivi.domain.PagingInteractor
import app.tivi.domain.interactors.UpdateTrendingShows
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObservePagedTrendingShows(
  private val trendingShowsDao: TrendingDao,
  private val updateTrendingShows: UpdateTrendingShows,
) : PagingInteractor<ObservePagedTrendingShows.Params, TrendingEntryWithShow>() {
  @OptIn(androidx.paging.ExperimentalPagingApi::class)
  override fun createObservable(
    params: Params,
  ): Flow<PagingData<TrendingEntryWithShow>> {
    return Pager(
      config = params.pagingConfig,
      remoteMediator = PaginatedEntryRemoteMediator { page ->
        try {
          updateTrendingShows(
            UpdateTrendingShows.Params(page = page, isUserInitiated = true),
          )
        } catch (ce: CancellationException) {
          throw ce
        } catch (t: Throwable) {
          Logger.e(t) { "Error while fetching from RemoteMediator" }
          throw t
        }
      },
      pagingSourceFactory = trendingShowsDao::entriesPagingSource,
    ).flow
  }

  data class Params(
    override val pagingConfig: PagingConfig,
  ) : Parameters<TrendingEntryWithShow>
}
