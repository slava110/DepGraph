package com.slava_110.depgraph

import io.ktor.client.request.*

//TODO parse XML?
/*
data class PackageMaven(
    override val name: String,
    override val version: PackageVersion
): IPackage<PackageMaven> {

    override suspend fun fetchDependencies(): List<PackageMaven> {
        TODO("Not yet implemented")
    }

    companion object {

        suspend fun get(name: String, version: String? = null): PackageMaven? {
            val (aGroup, aName) = name.split(":")
            val path = name.replace(':', '/').replace('.', '/')
            val versionToGet = version ?: getLatestVersion(path)

            val pom = httpClient.get("https://repo1.maven.org/maven2/$path/$aGroup-$aName-$versionToGet-pom.xml")
        }

        private suspend fun getLatestVersion(path: String): String =
            httpClient.get("https://repo1.maven.org/maven2/$path/maven-metadata.xml")

    }
}*/
