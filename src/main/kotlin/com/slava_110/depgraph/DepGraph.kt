package com.slava_110.depgraph

import com.slava_110.depgraph.pkg.IPackage
import com.slava_110.depgraph.pkg.types.PackageNPM
import com.slava_110.depgraph.pkg.types.PackagePIP
import guru.nidi.graphviz.KraphvizContext
import guru.nidi.graphviz.graph
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
    }
}

val packageTypes = sequenceOf(
    PackageNPM,
    PackagePIP
).associateBy { it.name }

suspend fun main(args: Array<String>) {
    val argParser = ArgParser(
        programName = "DepGraph"
    )

    val type by argParser.option(ArgType.String, shortName = "t", description = "Package type (${ packageTypes.keys.joinToString("/") })").required()
    val packageName by argParser.option(ArgType.String, shortName = "p", description = "Package name").required()
    val packageVersion by argParser.option(ArgType.String, shortName = "v", description = "Package version")
    val outputFile by argParser.option(ArgType.String, shortName = "o", description = "Output graphviz file path").default("depgraph.dot")

    argParser.parse(args)

    val packageType = packageTypes[type.lowercase()] ?: throw IllegalArgumentException("Package type $type isn't supported yet! Supported types: ${packageTypes.keys}")
    val packageObj = packageType.get(packageName, packageVersion)

    if(packageObj == null) {
        println("Package $packageName of type $type not found!")
        return
    }

    println("<=== Package ===>")
    println("Name: ${packageObj.name}")
    println("Version: ${packageObj.version}")
    println("Dependencies:")

    for((name, version) in packageObj.dependencies) {
        println("- $name ($version)")
    }

    val g = graph("${packageObj.name} dependencies") {
        runBlocking {
            graphDependencies(packageObj)
        }
    }

    println("Writing output to file...")

    File(outputFile).writeText(g.toString())

    println("Done")
}

suspend fun <P: IPackage<*, *>> KraphvizContext.graphDependencies(pkg: P) {
    println("Fetching ${pkg.name} dependencies...")
    for (dependency in pkg.fetchDependencies()) {
        pkg.name - dependency.name
        if(dependency.dependencies.isNotEmpty()) {
            graphDependencies(dependency)
        }
    }
}