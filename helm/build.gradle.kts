plugins {
  alias(libs.plugins.helm)
}

helm {
  charts {
    create(rootProject.name) {
      sourceDir.set(file("src/main/helm"))
    }
  }
}
