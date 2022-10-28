package com.slava_110.depgraph.serializer

import com.github.yuchi.semver.Range
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NPMRangeSerializer: KSerializer<Range> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("npm-semver-range", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Range =
        Range(decoder.decodeString().takeIf { it != "workspace:^" && it != "latest" } ?: "*", false)

    override fun serialize(encoder: Encoder, value: Range) =
        encoder.encodeString(value.toString())
}