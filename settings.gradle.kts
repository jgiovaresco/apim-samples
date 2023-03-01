rootProject.name = "apim-samples"
include("app")
include("helm")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      version("annotation-api", "1.3.2")
      version("avro", "1.11.0")
      version("confluent", "5.3.0")
      version("kotlin-faker", "1.13.0")
      version("grpc", "1.53.0")
      version("junit", "5.9.2")
      version("kotlin", "1.7.21")
      version("logback", "1.4.5")
      version("protobuf", "3.22.0")
      version("rxjava", "3.1.6")
      version("rxkotlin", "3.0.1")
      version("slf4j", "2.0.6")
      version("strikt", "0.34.1")
      version("vertx", "4.3.6")

      library("avro", "org.apache.avro", "avro").versionRef("avro")
      library("grpc-protobuf", "io.grpc", "grpc-protobuf").versionRef("grpc")
      library("grpc-services", "io.grpc", "grpc-services").versionRef("grpc")
      library("kafka-serializer-avro", "io.confluent", "kafka-avro-serializer").versionRef("confluent")
      library("kotlin-faker", "io.github.serpro69", "kotlin-faker").versionRef("kotlin-faker")
      library("logback-classic", "ch.qos.logback", "logback-classic").versionRef("logback")
      library("logback-core", "ch.qos.logback", "logback-core").versionRef("logback")
      library("javax-annotation-api", "javax.annotation", "javax.annotation-api").versionRef("annotation-api")
      library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
      library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
      library("protobuf-java", "com.google.protobuf", "protobuf-java").versionRef("protobuf")
      library("protobuf-kotlin", "com.google.protobuf", "protobuf-kotlin").versionRef("protobuf")
      library("protobuf-compiler", "com.google.protobuf", "protoc").versionRef("protobuf")
      library("protoc-gen-java", "io.grpc", "protoc-gen-grpc-java").versionRef("grpc")
      library("rxjava3", "io.reactivex.rxjava3", "rxjava").versionRef("rxjava")
      library("rxkotlin", "io.reactivex.rxjava3", "rxkotlin").versionRef("rxkotlin")
      library("strikt-core", "io.strikt", "strikt-core").versionRef("strikt")
      library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")

      bundle("grpc", listOf("javax-annotation-api", "grpc-protobuf", "grpc-services", "protobuf-java", "protobuf-kotlin"))
      bundle("logback", listOf("logback-classic", "logback-core"))
      bundle("rx", listOf("rxjava3", "rxkotlin"))
      bundle("strikt", listOf("strikt-core"))

      plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
      plugin("shadow", "com.github.johnrengelman.shadow").version("7.1.2")
      plugin("docker", "com.palantir.docker").version("0.34.0")
      plugin("axion", "pl.allegro.tech.build.axion-release").version("1.14.0")
      plugin("protobuf", "com.google.protobuf").version("0.9.1")
    }
  }
}
