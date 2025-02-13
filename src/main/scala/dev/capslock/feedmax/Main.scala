package dev.capslock.feedmax

import zio.*
import zio.Console.*

object Main extends ZIOAppDefault:
  def run = appLogic

  val appLogic = feedMaxConfig.foldZIO(
    failure => printLine(s"Failed to load config: $failure"),
    config => printLine(s"Config loaded: $config"),
  )
