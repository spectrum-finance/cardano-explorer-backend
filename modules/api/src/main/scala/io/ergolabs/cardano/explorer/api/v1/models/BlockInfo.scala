package io.ergolabs.cardano.explorer.api.v1.models

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.ergolabs.cardano.explorer.core.db.models.{BlockHeader, SlotLeaderInfo}
import io.ergolabs.cardano.explorer.core.types.BlockHash
import io.ergolabs.cardano.explorer.api.v1.instances._
import sttp.tapir.Schema

import java.time.LocalDateTime

@derive(encoder, decoder)
final case class BlockInfo(
  blockId: BigInt,
  blockHash: BlockHash,
  blockNo: Int,
  epochNo: Int,
  slotNo: Int,
  slotLeader: String,
  txCount: BigInt,
  time: LocalDateTime
)

object BlockInfo {

  implicit def schema: Schema[BlockInfo] =
    Schema
      .derived[BlockInfo]
      .modify(_.blockId)(_.description("The block identifier."))
      .modify(_.blockHash)(_.description("The hash identifier of the block."))
      .modify(_.blockNo)(_.description("The block number."))
      .modify(_.epochNo)(_.description("The epoch number."))
      .modify(_.slotNo)(_.description("The slot number."))
      .modify(_.slotLeader)(_.description("The SlotLeader index number of the creator of this block."))
      .modify(_.txCount)(_.description("The number of transactions in this block."))
      .modify(_.time)(_.description("The block time (UTCTime)."))

  private val UNKNOWN_SLOT_LEADER = "Unknown slot leader"

  def inflate(blockHeader: BlockHeader, slotLeaderOpt: Option[SlotLeaderInfo]): BlockInfo =
    BlockInfo(
      blockHeader.blockId,
      blockHeader.blockHash,
      blockHeader.blockNo,
      blockHeader.epochNo,
      blockHeader.slotNo,
      slotLeaderOpt.map(_.description).getOrElse(UNKNOWN_SLOT_LEADER),
      blockHeader.txCount,
      blockHeader.time
    )
}
