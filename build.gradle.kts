import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.palantir.gradle.docker.DockerExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  application
  alias(libs.plugins.shadow)
  alias(libs.plugins.axion)
  alias(libs.plugins.docker)
}

repositories {
  mavenCentral()
}

project.version = scmVersion.version

val jarClassifier = "fat"
val mainVerticleName = "io.apim.samples.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "11"

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:${libs.versions.vertx.get()}"))
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-lang-kotlin")

  implementation(libs.bundles.kotlin.coroutines)
  implementation(kotlin("stdlib-jdk8"))

  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.bundles.strikt)
  testImplementation("io.vertx:vertx-web-client")
  testRuntimeOnly(libs.junit.jupiter.engine)
}

application {
  mainClass.set(launcherClassName)
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
  name = "${project.name}:${project.version}"
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
