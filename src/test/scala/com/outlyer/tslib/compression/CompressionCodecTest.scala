package com.outlyer.tslib.compression

import com.outlyer.tslib.TimeSeriesValue
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Matchers


final class CompressionCodecTest extends CodecSpec with Matchers {
    import ValuesCodecTestData._

  "the CompressionCodec class" should {
    "use single bit 0 for duplicated value" in {
      val three = TimeSeriesValue(3.0)
      val encoded = CompressionCodec.encode(List(three, three)).require
      encoded.last should be (false)
      encoded.length should be (65)
    }

    "use 11 prefix with 5 bits for size and new maningfull value" in {
      val first = TimeSeriesValue(9.0)
      val second = TimeSeriesValue(12.0)
      val encoded = CompressionCodec.encode(List(first, second)).require
      encoded.slice(64, 80).toBin should be ("11" + "01100" + "000011" + "101")
      encoded.length should be (80)
    }

    "handle compression and decompression for timeseries values" in {
      forAll { vs: List[TimeSeriesValue] => roundTrip(CompressionCodec, vs) }
    }
  }
}

object ValuesCodecTestData {

  lazy val genValues = for {
    size <- Gen.chooseNum(0, 10000)
    vs   <- Gen.listOfN(size, Gen.chooseNum(Double.MinValue, Double.MaxValue))
  } yield vs.map(TimeSeriesValue.apply)

  implicit lazy val arbitraryValues: Arbitrary[List[TimeSeriesValue]] = Arbitrary(genValues)
}
