module.exports = {
  extends: ["@commitlint/config-conventional"],
  rules: {
    "body-max-line-length": [2, "always", 120],
  },
  ignores:[
    (message) => message.includes('chore(deps)') || message.includes('fix(deps)')|| message.includes('ci(deps)')
  ]
};
