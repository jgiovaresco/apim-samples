import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.protobuf.gradle.id
import com.palantir.gradle.docker.DockerExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  application
  alias(libs.plugins.shadow)
  alias(libs.plugins.docker)
  alias(libs.plugins.axion)
  alias(libs.plugins.protobuf)
}

repositories {
  mavenCentral()
  maven {
    url = uri("https://packages.confluent.io/maven/")
    name = "Confluent"
    content {
      includeGroup("io.confluent")
      includeGroup("org.apache.kafka")
    }
  }
}

scmVersion {
  tag {
    prefix.set("")
  }
}
project.version = scmVersion.version

val jarClassifier = "fat"
val mainVerticleName = "io.apim.samples.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "17"
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions.jvmTarget = "17"

dependencies {
  implementation(kotlin("stdlib-jdk8"))

  implementation(platform("io.vertx:vertx-stack-depchain:${libs.versions.vertx.get()}"))
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-grpc") // required for generated stubs
  implementation("io.vertx:vertx-grpc-server")
  implementation("io.vertx:vertx-health-check")
  implementation("io.vertx:vertx-junit5")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("io.vertx:vertx-rx-java3")
  implementation("io.vertx:vertx-web")

  implementation(libs.bundles.grpc)
  implementation(libs.bundles.logback)
  implementation(libs.bundles.rx)

  implementation(libs.avro)
  implementation(libs.kafka.serializer.avro)
  implementation(libs.kotlin.faker)
  implementation(libs.slf4j.api)

  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.bundles.strikt)
  testImplementation("io.vertx:vertx-grpc-client")
  testImplementation("io.vertx:vertx-web-client")
  testRuntimeOnly(libs.junit.jupiter.engine)
}

application {
  mainClass.set(launcherClassName)
}

sourceSets {
  main {
    proto {
      srcDir("src/main/resources/grpc")
    }
  }
}

tasks.withType<ShadowJar> {
  archiveClassifier.set(jarClassifier)
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<JavaExec> {
  val watchForChange = "src/**/*"
  val doOnChange = "${projectDir}/gradlew classes"
  args = listOf(
    "run",
    mainVerticleName,
    "--redeploy=$watchForChange",
    "--launcher-class=$launcherClassName",
    "--on-redeploy=$doOnChange"
  )
}

tasks.test {
  useJUnitPlatform()
}

configure<DockerExtension> {
  name = "${rootProject.name}:${project.version}"
  buildArgs(mapOf("BUILD_VERSION" to "${project.version}"))
  files(tasks.findByName("shadowJar")?.outputs?.files)

  tag("DockerHub", "jgiovaresco/${name}")
}

if (hasProperty("buildScan")) {
  extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
  }
}

protobuf {
  protoc {
    artifact = libs.protobuf.compiler.get().toString()
  }

  plugins {
    id("grpc") {
      artifact = libs.protoc.gen.java.get().toString()
    }
    id("vertx") {
      artifact = "io.vertx:vertx-grpc-protoc-plugin:${libs.versions.vertx.get()}"

    }
  }

  generateProtoTasks {
    all().forEach {
      it.builtins {
        id("kotlin")
      }
      it.plugins {
        id("grpc") {}
        id("vertx") {}
      }
    }
  }
}
