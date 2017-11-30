package com.outlyer.tslib.compression

import scala.collection.mutable.ListBuffer

import com.outlyer.tslib.TimeSeriesValue
import scodec.bits.BitVector
import scodec.codecs.{double, list}
import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}

object CompressionCodec extends Codec[List[TimeSeriesValue]] {

  private lazy val floatCodec: Codec[TimeSeriesValue] = double.xmap(TimeSeriesValue.apply, _.v)
  private lazy val dummyCodec: Codec[List[TimeSeriesValue]] = list(floatCodec)

  override def sizeBound = SizeBound.unknown

  override def encode(values: List[TimeSeriesValue]): Attempt[BitVector] = {
    // TODO: fill buf with encoded bits
    val buf = BitVector.empty




    // TODO: return success or failure
    Attempt.successful(buf)
    Attempt.failure(Err("Failed to encode time series values"))

    // delete this line after implementation
    dummyCodec.encode(values)
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[List[TimeSeriesValue]]] = {
    // TODO: fill buf with encoded bits
    val buf = new ListBuffer[TimeSeriesValue]
    val remaining = BitVector.empty





    // TODO: return success or failure
    Attempt.successful(DecodeResult.apply(buf.toList, remaining))
    Attempt.failure(Err("Failed to encode time series values"))

    // delete this line after implementation
    dummyCodec.decode(bits)
  }
}
