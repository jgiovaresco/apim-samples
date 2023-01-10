rootProject.name = "apim-samples"
include("app")
include("helm")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      version("kotlin", "1.7.21")
      version("logback", "1.4.5")
      version("vertx", "4.3.6")
      version("junit", "5.9.2")
      version("rxjava", "3.1.5")
      version("rxkotlin", "3.0.1")
      version("slf4j", "2.0.6")
      version("strikt", "0.34.1")

      library("logback-classic", "ch.qos.logback", "logback-classic").versionRef("logback")
      library("logback-core", "ch.qos.logback", "logback-core").versionRef("logback")
      library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
      library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
      library("rxjava3", "io.reactivex.rxjava3", "rxjava").versionRef("rxjava")
      library("rxkotlin", "io.reactivex.rxjava3", "rxkotlin").versionRef("rxkotlin")
      library("strikt-core", "io.strikt", "strikt-core").versionRef("strikt")
      library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")

      bundle("logback", listOf("logback-classic", "logback-core"))
      bundle("rx", listOf("rxjava3", "rxkotlin"))
      bundle("strikt", listOf("strikt-core"))

      plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
      plugin("shadow", "com.github.johnrengelman.shadow").version("7.1.2")
      plugin("docker", "com.palantir.docker").version("0.34.0")
      plugin("axion", "pl.allegro.tech.build.axion-release").version("1.14.0")
    }
  }
}
