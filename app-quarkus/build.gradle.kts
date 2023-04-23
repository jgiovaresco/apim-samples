plugins {
  alias(libs.plugins.axion)
  alias(libs.plugins.kotlin.allopen)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.quarkus)
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

dependencies {
  implementation(enforcedPlatform(libs.quarkus.bom))
  implementation(enforcedPlatform(libs.mutiny.clients.bom))

  implementation(libs.slf4j.api)
  implementation("io.quarkus:quarkus-arc")
  implementation("io.quarkus:quarkus-grpc")
  implementation("io.quarkus:quarkus-kotlin")
  implementation("io.quarkus:quarkus-reactive-routes")
  implementation("io.quarkus:quarkus-resteasy-reactive")
  implementation("io.quarkus:quarkus-vertx")
  implementation("io.quarkus:quarkus-websockets")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.bundles.strikt)
  testImplementation("io.quarkus:quarkus-junit5")
  testImplementation("io.smallrye.reactive:smallrye-mutiny-vertx-web-client")

  testRuntimeOnly(libs.junit.jupiter.engine)
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

allOpen {
  annotation("javax.ws.rs.Path")
  annotation("javax.enterprise.context.ApplicationScoped")
  annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<Test> {
  systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
  kotlinOptions.javaParameters = true
}

tasks.register("copyProto", Copy::class.java) {
  from("src/main/proto")
  into(layout.buildDirectory.dir("resources/main/META-INF/resources/proto"))
}

tasks.withType<ProcessResources> {
  dependsOn("copyProto")
}
