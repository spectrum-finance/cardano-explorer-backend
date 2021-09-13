package io.ergolabs.cardano.explorer.core.db

sealed trait SortOrder { val value: String }

object SortOrder {

  case object Asc extends SortOrder {
    val value: String = "asc"
  }

  case object Desc extends SortOrder {
    val value: String = "desc"
  }
}
