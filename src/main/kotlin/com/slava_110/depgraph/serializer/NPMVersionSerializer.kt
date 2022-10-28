package com.slava_110.depgraph.serializer

import com.github.yuchi.semver.Version
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NPMVersionSerializer: KSerializer<Version> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("npm-semver-version", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Version =
        Version(decoder.decodeString(), false)

    override fun serialize(encoder: Encoder, value: Version) =
        encoder.encodeString(value.toString())
}