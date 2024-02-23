package com.mv.api

object Utils {
  def sanitize(input: String): String =
    input.trim.replaceAll("[<>\"';]", "")
}
