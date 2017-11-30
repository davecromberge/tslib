package com.outlyer.tslib.compression

import com.outlyer.tslib.TimeSeriesValue
import org.scalacheck.{Arbitrary, Gen}

final class CompressionCodecTest extends CodecSpec {
    import ValuesCodecTestData._

    "the CompressionCodec class" should {
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