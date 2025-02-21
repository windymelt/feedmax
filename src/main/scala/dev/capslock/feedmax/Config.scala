package dev.capslock.feedmax

import java.net.URI

/** Configuration model for FeedMax
  *
  * @param feeds
  *   List of feed URLs to monitor
  */
case class FeedMaxConfig(feeds: Vector[URI])
