plugins {
  alias(libs.plugins.axion)
  alias(libs.plugins.kotlin.jvm) apply false
}

scmVersion {
  tag {
    prefix.set("")
  }
}

project.version = scmVersion.version

allprojects {
  project.version = rootProject.version
}
