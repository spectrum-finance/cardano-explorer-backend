package io.ergolabs.cardano.explorer.core.db.repositories

import cats.FlatMap
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.logging.Logs
import tofu.syntax.monadic._

final case class TxRepoBundle[F[_]](
  assets: AssetsRepo[F],
  inputs: InputsRepo[F],
  outputs: OutputsRepo[F],
  transactions: TransactionsRepo[F]
)

object TxRepoBundle {

  def make[I[_]: FlatMap, D[_]: FlatMap: LiftConnectionIO](implicit
    elh: EmbeddableLogHandler[D],
    logs: Logs[I, D]
  ): I[TxRepoBundle[D]] =
    for {
      assetsR       <- AssetsRepo.make[I, D]
      inputsR       <- InputsRepo.make[I, D]
      outputsR      <- OutputsRepo.make[I, D]
      transactionsR <- TransactionsRepo.make[I, D]
    } yield TxRepoBundle(assetsR, inputsR, outputsR, transactionsR)
}
