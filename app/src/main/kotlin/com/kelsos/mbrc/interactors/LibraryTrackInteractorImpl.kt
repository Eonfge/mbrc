package com.kelsos.mbrc.interactors

import com.kelsos.mbrc.constants.Constants
import com.kelsos.mbrc.domain.Track
import com.kelsos.mbrc.extensions.io
import com.kelsos.mbrc.mappers.TrackMapper
import com.kelsos.mbrc.repository.library.TrackRepository
import rx.Observable
import rx.lang.kotlin.toSingletonObservable
import javax.inject.Inject

class LibraryTrackInteractorImpl
@Inject constructor(private val repository: TrackRepository) : LibraryTrackInteractor {

  override fun execute(page: Int, items: Int): Observable<List<Track>> {
    return repository.getTracks(page * Constants.PAGE_SIZE, Constants.PAGE_SIZE)
        .io()
        .flatMap { TrackMapper.map(it).toSingletonObservable() }
  }
}