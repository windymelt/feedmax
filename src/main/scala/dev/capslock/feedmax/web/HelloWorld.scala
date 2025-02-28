package dev.capslock.feedmax.web

import zio.*
import zio.http.*

object HelloWorld:
  val routes = Routes(
    Method.GET / Root ->
      handler(Response.text("hello world")),
  )
