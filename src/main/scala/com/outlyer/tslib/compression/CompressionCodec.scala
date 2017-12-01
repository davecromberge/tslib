package com.outlyer.tslib.compression

import com.outlyer.tslib.TimeSeriesValue
import scodec.bits.{BitVector, BinStringSyntax}
import scodec.codecs.{double, int8}
import scodec.{Attempt, Codec, DecodeResult, SizeBound}


object CompressionCodec extends Codec[List[TimeSeriesValue]] {

  override def sizeBound = SizeBound.unknown

  override def encode(values: List[TimeSeriesValue]): Attempt[BitVector] = {
    val first = values.head.v
    var buf:BitVector = double.encode(first).require
    var prev = buf

    for (x <- values.tail) {
      val curr = double.encode(x.v).require

      if (curr == prev) {
        buf = buf :+ false

      } else {
        buf = buf :+ true
        val xored = prev ^ curr
        val left_zeros = xored.indexOfSlice(bin"1")
        val right_zeros = xored.reverse.indexOfSlice(bin"1")

        // TODO
        // get number of trailing zeros
        //if (prev_left_zeros >= left_zeros and prev_right_zeros >= right_zeros ) {
        //  buf = buf :+ false ++ value_bits
        //} else {

        val offset_field = int8.encode(left_zeros.toInt).require.slice(3, 8)
        val size = 64 - left_zeros - right_zeros
        val size_field = int8.encode(size.toInt).require.slice(2, 8)

        buf = (buf :+ true) ++ offset_field ++ size_field
        buf = buf ++ xored.slice(left_zeros, left_zeros + size)
      }
      prev = curr
    }

    Attempt.successful(buf)
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[List[TimeSeriesValue]]] = {

    def decode_rest(v: BitVector, acc:List[Double]): List[Double] = {
      if (v.isEmpty) {
        acc.reverse

      } else {
        val (hd1, v1) = v.splitAt(1)
        val prev = acc.head

        if (hd1 == bin"0") {
          decode_rest(v1, prev :: acc)

        } else {
          val (hd2, v2) = v1.splitAt(1)
          // TODO: handle case when (hd2 == bin"0")

          val (offset_field, v3) = v2.splitAt(5)
          val offset = int8.decode(offset_field.padLeft(8)).require.value.toLong
          val (size_field, v4) = v3.splitAt(6)
          val size = int8.decode(size_field.padLeft(8)).require.value.toLong
          val (xored_part, v5) = v4.splitAt(size)
          val xored = (BitVector.fill(offset)(false) ++ xored_part).padRight(64)
          val value_bin = double.encode(prev).require ^ xored
          val value = double.decode(value_bin).require.value
          decode_rest(v5, value :: acc)
        }
      }
    }

    if (bits.isEmpty) {
      return Attempt.successful(DecodeResult(List(), BitVector.empty))
    }

    val (head, tail) = bits.splitAt(64)
    val first_value = double.decode(head).require.value
    val values = decode_rest(tail, List(first_value))
    val result = DecodeResult(values.map(TimeSeriesValue), BitVector.empty)
    Attempt.successful(result)
  }
}
