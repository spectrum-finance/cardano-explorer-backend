package io.ergolabs.cardano.explorer.api.v1.models

final case class Items[A](items: List[A], total: Int)
