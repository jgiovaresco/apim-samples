rootProject.name = "apim-samples"

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      version("kotlin", "1.7.21")
      version("kotlin-coroutines", "1.6.4")
      version("vertx", "4.3.6")

      library("kotlin-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("kotlin-coroutines")

      bundle("kotlin-coroutines", listOf("kotlin-coroutines-core"))

      plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
      plugin("shadow", "com.github.johnrengelman.shadow").version("7.1.2")
    }
  }
}
