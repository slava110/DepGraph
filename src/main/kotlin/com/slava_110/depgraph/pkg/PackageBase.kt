package com.slava_110.depgraph.pkg

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList

abstract class PackageBase<P: PackageBase<P, VR>, VR: Any>: IPackage<P, VR> {
    override val dependencies: Map<String, VR> = emptyMap()
    private var cachedDependencies: List<P>? = null

    override suspend fun fetchDependencies(): List<P> {
        if(cachedDependencies == null)
            cachedDependencies = dependencies.entries
                .asFlow()
                .mapNotNull { (name, versionConstraint) -> type.find(name, versionConstraint) }
                .toList()
        return cachedDependencies!!
    }
}