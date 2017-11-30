package com.outlyer.tslib

import scodec.bits.BitVector

case class TimeSeriesValue(v: Double) extends AnyVal

case class Error(s: String) extends AnyVal

package object compression {
  // These materialize the values for use outside of other codecs
  def compress(vs: Seq[Double]): Either[Error, Seq[Byte]] =
    CompressionCodec.encode(vs.map(TimeSeriesValue.apply).toList).toEither
      .left.map(err => Error(err.toString))
      .right.map(_.toByteArray)

  def decompress(bytes: Seq[Byte]): Either[Error, Seq[Double]] =
    CompressionCodec.decode(BitVector(bytes)).toEither
      .left.map(err => Error(err.toString))
      .right.map(_.value.map(_.v))
}
