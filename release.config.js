module.exports = {
  branches: [
    "main",
    { name: "alpha", prerelease: true },
  ],
  tagFormat: "${version}",
  plugins: [
    "@semantic-release/commit-analyzer",
    "@semantic-release/release-notes-generator",
    "@semantic-release/changelog",
    [
      "@semantic-release/exec",
      {
        prepareCmd: 'yq e -i " .version = \\"${nextRelease.version}\\" | .appVersion = \\"${nextRelease.version}\\"" ./helm/src/main/helm/Chart.yaml',
      },
    ],
    "@semantic-release/github",
    [
      "@semantic-release/git",
      {
        assets: ["CHANGELOG.md"],
        message: "chore: release ${nextRelease.version} [skip ci]",
      },
    ],
  ],
};
