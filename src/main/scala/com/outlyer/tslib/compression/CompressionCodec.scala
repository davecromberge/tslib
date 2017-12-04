package com.outlyer.tslib.compression

import com.outlyer.tslib.TimeSeriesValue
import scodec.bits.{BitVector, BinStringSyntax}
import scodec.codecs.{double, int8}
import scodec.{Attempt, Codec, DecodeResult, SizeBound}


object CompressionCodec extends Codec[List[TimeSeriesValue]] {

  override def sizeBound = SizeBound.unknown

  override def encode(values: List[TimeSeriesValue]): Attempt[BitVector] = {
    if (values.isEmpty) {
      return Attempt.successful(BitVector.empty)
    }

    val first = values.head.v
    var buf:BitVector = double.encode(first).require
    var prev = buf
    var prev_left_zeros: Option[Long] = Option.empty
    var prev_right_zeros: Option[Long] = Option.empty

    for (x <- values.tail) {
      val curr = double.encode(x.v).require

      if (curr == prev) {
        buf = buf ++ bin"0"

      } else {
        buf = buf ++ bin"1"
        val xored = prev ^ curr
        val left_zeros = xored.indexOfSlice(bin"1")
        val right_zeros = xored.reverse.indexOfSlice(bin"1")

        val (offset, size, prefix) = (prev_left_zeros, prev_right_zeros) match {

          case(Some(lz), Some(rz)) if (left_zeros >= lz) && (right_zeros >= rz) =>
            val offset = lz
            val size = 64 - lz - rz
            val prefix = bin"0"
            (offset, size, prefix)

          case other =>
            prev_left_zeros = Some(left_zeros)
            prev_right_zeros = Some(right_zeros)
            val offset = math.min(left_zeros, 31)
            val offset_field = int8.encode(offset.toInt).require.slice(3, 8)
            val size = 64 - left_zeros - right_zeros
            val size_field = int8.encode(size.toInt).require.slice(2, 8)
            val prefix = bin"1" ++ offset_field ++ size_field
            (offset, size, prefix)
        }

        buf = buf ++ prefix ++ xored.slice(offset, offset + size)
      }
      prev = curr
    }

    Attempt.successful(buf)
  }

  override def decode(bits: BitVector): Attempt[DecodeResult[List[TimeSeriesValue]]] = {

    def decode_rest(v: BitVector, acc:List[Double],
                    prevOffset: Option[Long],
                    prevSize: Option[Long]): List[Double] = {
      if (v.isEmpty) {
        return acc.reverse
      }

      val prev = acc.head
      v.splitAt(1) match {

        case (BitVector.zero, v1) =>
          decode_rest(v1, prev :: acc, prevOffset, prevSize)

        case (_, v1) =>
          val (offset, size, v4) = v1.splitAt(1) match {

            case (BitVector.zero, v2) =>
              (prevOffset.get, prevSize.get, v2)

            case (_, v2) =>
              val (offset_field, v3) = v2.splitAt(5)
              val offset = int8.decode(offset_field.padLeft(8)).require.value.toLong
              val (size_field, v4) = v3.splitAt(6)
              val size = int8.decode(size_field.padLeft(8)).require.value.toLong match {
                case 0 => 64
                case x => x
              }
              (offset, size, v4)
          }
          val (xored_part, v5) = v4.splitAt(size)
          val xored = (BitVector.fill(offset)(false) ++ xored_part).padRight(64)
          val value_bin = double.encode(prev).require ^ xored
          val value = double.decode(value_bin).require.value
          decode_rest(v5, value :: acc, Some(offset), Some(size))
      }
    }

    if (bits.isEmpty) {
      return Attempt.successful(DecodeResult(List(), BitVector.empty))
    }

    val (head, tail) = bits.splitAt(64)
    val first_value = double.decode(head).require.value
    val values = decode_rest(tail, List(first_value), Option.empty, Option.empty)
    val result = DecodeResult(values.map(TimeSeriesValue), BitVector.empty)
    Attempt.successful(result)
  }
}
