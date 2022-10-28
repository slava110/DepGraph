package com.slava_110.depgraph.pkg

/**
 * Represents abstract package from package manager
 */
interface IPackage<P: IPackage<P, VR>, VR: Any> {
    val name: String
    val type: PackageType<P, VR>
    val version: String
    val dependencies: Map<String, VR>

    /**
     * Get dependencies as packages
     */
    suspend fun fetchDependencies(): List<P>

    interface PackageType<P: IPackage<P, VR>, VR: Any> {
        val name: String

        suspend fun get(name: String, version: String? = null): P?

        suspend fun find(name: String, versionRange: VR): P?
    }
}