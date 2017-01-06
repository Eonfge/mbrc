package com.kelsos.mbrc.services

import com.kelsos.mbrc.annotations.Queue.QueueType
import com.kelsos.mbrc.data.QueueResponse
import rx.Single

interface QueueService {
  fun queue(@QueueType type: String, tracks: List<String>): Single<QueueResponse>
}