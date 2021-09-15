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
  epochNo: Int,
  slotNo: Int,
  slotLeader: String,
  txCount: BigInt,
  time: LocalDateTime
)

object BlockInfo {

  implicit def schema: Schema[BlockInfo] = Schema.derived

  private val UNKNOWN_SLOT_LEADER = "Unknown slot leader"

  def inflate(blockHeader: BlockHeader, slotLeaderOpt: Option[SlotLeaderInfo]): BlockInfo =
    BlockInfo(
      blockHeader.blockId,
      blockHeader.blockHash,
      blockHeader.epochNo,
      blockHeader.slotNo,
      slotLeaderOpt.map(_.description).getOrElse(UNKNOWN_SLOT_LEADER),
      blockHeader.txCount,
      blockHeader.time
    )
}
