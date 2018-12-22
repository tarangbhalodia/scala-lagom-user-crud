package com.lagom.user.infrastructure.util

import com.lagom.user.api.User
import com.sksamuel.elastic4s.http.search.SearchHits
import com.sksamuel.elastic4s.playjson._

object Converters {
  implicit class RichSearchHits(val searchHits: SearchHits) extends AnyVal {
    def toUsers: Seq[User] = {
      searchHits.hits.toSeq.map(_.to[User])
    }
  }
}
