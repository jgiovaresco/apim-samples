rootProject.name = "apim-samples"
include("app")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      version("kotlin", "1.7.21")
      version("kotlin-coroutines", "1.6.4")
      version("vertx", "4.3.6")
      version("junit", "5.9.1")
      version("strikt", "0.34.0")

      library("kotlin-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("kotlin-coroutines")
      library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
      library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
      library("strikt-core", "io.strikt", "strikt-core").versionRef("strikt")

      bundle("kotlin-coroutines", listOf("kotlin-coroutines-core"))
      bundle("strikt", listOf("strikt-core"))

      plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
      plugin("shadow", "com.github.johnrengelman.shadow").version("7.1.2")
      plugin("docker", "com.palantir.docker").version("0.34.0")
      plugin("axion", "pl.allegro.tech.build.axion-release").version("1.14.0")
    }
  }
}
