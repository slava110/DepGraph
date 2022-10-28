package com.slava_110.depgraph

import guru.nidi.graphviz.graph
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.serialization.json.Json

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
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

    val type by argParser.option(ArgType.String, shortName = "t", description = "Package type (npm/pip)").required()
    val packageName by argParser.option(ArgType.String, shortName = "p", description = "Package name").required()
    val packageVersion by argParser.option(ArgType.String, shortName = "v", description = "Package version")

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

    println("Fetching dependencies...")

    for(dep in packageObj.fetchDependencies()) {
        println("- ${dep.name} (${dep.version})")
    }

    /*graph("${packageObj.name} dependencies") {

    }*/
}

