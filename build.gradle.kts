import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  application
  alias(libs.plugins.shadow)
  alias(libs.plugins.axion)
}

repositories {
  mavenCentral()
}

project.version = scmVersion.version

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
  archiveClassifier.set("fat")
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
