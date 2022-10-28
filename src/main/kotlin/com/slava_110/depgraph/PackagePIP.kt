package com.slava_110.depgraph

import com.slava_110.depgraph.`package`.PackageBase
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.bufferedReader

@Serializable
data class PIPPackageData(
    val info: PIPPackageInfo,
    val urls: List<PIPPackageURL>
) {

    suspend fun downloadPackage(): File? {
        val packageUrl = urls.firstOrNull { info -> info.packagetype == "bdist_wheel" }?.url ?: return null

        val downloadedFile = withContext(Dispatchers.IO) {
            File.createTempFile("depgraph", "pippackage")
        }

        httpClient.prepareGet(packageUrl).execute { response ->
            val channel: ByteReadChannel = response.body()
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    downloadedFile.appendBytes(bytes)
                    //TODO progress bar?
                    //println("Received ${downloadedFile.length()} bytes from ${response.contentLength()}")
                }
            }
            println("A file saved to ${downloadedFile.path}")
        }

        return downloadedFile
    }

    @Serializable
    data class PIPPackageInfo(
        val name: String,
        val version: String
    )

    @Serializable
    data class PIPPackageURL(
        val filename: String,
        val packagetype: String,
        val size: Int,
        val url: String
    )

    companion object {

        suspend fun get(name: String, version: String? = null): PIPPackageData =
            httpClient.get("https://pypi.org/pypi/$name/${ version?.let { "$it/" }.orEmpty() }json").body()
    }
}

@Serializable
data class PackagePIP(
    override val name: String,
    override val version: String,
    override val dependencies: Map<String, String> = emptyMap()
): PackageBase<PackagePIP, String>() {
    override val type: IPackage.PackageType<PackagePIP, String>
        get() = PackagePIP

    companion object: IPackage.PackageType<PackagePIP, String> {
        override val name: String = "pip"

        override suspend fun get(name: String, version: String?): PackagePIP? {
            val packageData: PIPPackageData = PIPPackageData.get(name, version)

            val downloaded = packageData.downloadPackage()

            if(downloaded != null) {
                val packageObj = try {
                    withContext(Dispatchers.IO) {
                        FileSystems.newFileSystem(downloaded.toPath())
                    }.use { fs ->
                        val metadataPath = fs.getPath("${ packageData.info.name }-${ packageData.info.version }.dist-info", "METADATA")

                        parseMetadata(metadataPath)
                    }
                } catch (e: Throwable) {
                    throw e
                } finally {
                    downloaded.delete()
                }

                return packageObj
            }
            return null
        }

        private val metadataDepRegex = Regex("(.*?) \\((.*?)\\)")

        private fun parseMetadata(metadataPath: Path): PackagePIP {
            lateinit var packageName: String
            lateinit var packageVersion: String
            val depNames = mutableMapOf<String, String>()

            metadataPath.bufferedReader().lineSequence()
                .takeWhile { it.isNotBlank() }
                .map { line -> line.split(": ", limit = 2) }
                .forEach { (k, v) ->
                    when(k) {
                        "Name" -> packageName = v
                        "Version" -> packageVersion = v
                        "Requires-Dist" -> {
                            val (depName, depVersion) = (metadataDepRegex.find(v)?.destructured ?: return@forEach)
                            depNames[depName] = depVersion
                        }
                    }
                }

            return PackagePIP(packageName, packageVersion, depNames)
        }

        override suspend fun find(name: String, versionRange: String): PackagePIP? =
            get(name, versionRange)
    }
}