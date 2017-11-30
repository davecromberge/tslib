package com.outlyer.tslib.compression

import org.scalatest.{Matchers, WordSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult}

abstract class CodecSpec
  extends WordSpec
    with Matchers
    with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 1000)

  protected def roundTrip[A](codec: Codec[A], value: A) = {
    val encoded = codec.encode(value)
    encoded shouldBe 'successful
    val Attempt.Successful(DecodeResult(decoded, remainder)) = codec.decode(encoded.require)
    remainder shouldEqual BitVector.empty
    decoded shouldEqual value
  }
}

