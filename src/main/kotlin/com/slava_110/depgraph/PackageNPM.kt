package com.slava_110.depgraph

import com.github.yuchi.semver.Range
import com.github.yuchi.semver.Version
import com.slava_110.depgraph.`package`.PackageBase
import com.slava_110.depgraph.serializer.NPMRangeSerializer
import com.slava_110.depgraph.serializer.NPMVersionSerializer
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PackageNPM(
    override val name: String,
    override val version: String,
    override val dependencies: Map<String, @Serializable(with = NPMRangeSerializer::class) Range> = emptyMap()
): PackageBase<PackageNPM, Range>() {
    override val type: IPackage.PackageType<PackageNPM, Range>
        get() = PackageNPM

    @Serializable
    data class NPMSearchResult(
        val versions: Map<@Serializable(with = NPMVersionSerializer::class) Version, PackageNPM>
    )

    companion object: IPackage.PackageType<PackageNPM, Range> {
        override val name: String = "npm"

        override suspend fun get(name: String, version: String?): PackageNPM? =
            httpClient.get("https://registry.npmjs.org/$name/${ version ?: "latest" }").body()

        override suspend fun find(name: String, versionRange: Range): PackageNPM? =
            httpClient.get("https://registry.npmjs.org/$name")
                .body<NPMSearchResult>()
                .versions
                .entries
                .findLast { (v, _) ->
                    versionRange.test(v)
                }?.value
    }
}