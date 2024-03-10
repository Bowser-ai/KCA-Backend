package com.mv.domain.errors

case class ApplicationError(msg: String) extends RuntimeException(msg)
