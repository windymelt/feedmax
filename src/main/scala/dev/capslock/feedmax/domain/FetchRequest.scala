package dev.capslock.feedmax.domain

import java.time.OffsetDateTime

final case class FetchRequest(
    url: String,
    lastModified: Option[OffsetDateTime] = None,
)
// TODO: etag?
