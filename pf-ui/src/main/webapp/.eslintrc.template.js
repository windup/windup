module.exports = {
  extends: ["react-app", "react-app/jest"],
  plugins: ["import"],
  ignorePatterns: ["node_modules/**", "build/**", "target/**", "server.js"],
  rules: {
    "import/order": [
      "warn",
      {
        "newlines-between": "always",
        alphabetize: {
          caseInsensitive: true,
          order: "asc",
        },
        pathGroups: [
          {
            pattern: "react*",
            group: "builtin",
            position: "before",
          },
          {
            pattern: "@app/**",
            group: "external",
            position: "after",
          },
        ],
        pathGroupsExcludedImportTypes: [
          "react",
          "react-router",
          "react-dom",
          "react-router-dom",
        ],
        groups: [
          "builtin",
          "external",
          "internal",
          "parent",
          "sibling",
          "index",
          "object",
          "type",
        ],
      },
    ],
  },
};
