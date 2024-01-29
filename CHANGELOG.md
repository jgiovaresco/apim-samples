# [3.3.0](https://github.com/jgiovaresco/apim-samples/compare/3.2.1...3.3.0) (2024-01-29)


### Features

* improve graphql endpoint to accept limit param list queries ([1d7e81d](https://github.com/jgiovaresco/apim-samples/commit/1d7e81d760b9ac10cd42ac3c32d8694068b3bcac))

## [3.2.1](https://github.com/jgiovaresco/apim-samples/compare/3.2.0...3.2.1) (2024-01-29)


### Bug Fixes

* helm default values ([ff3a65f](https://github.com/jgiovaresco/apim-samples/commit/ff3a65fcfc589ede466747f8c1a9dea9fb6b7dba))

# [3.2.0](https://github.com/jgiovaresco/apim-samples/compare/3.1.0...3.2.0) (2024-01-28)


### Features

* add a GraphQL endpoint ([35ffbfd](https://github.com/jgiovaresco/apim-samples/commit/35ffbfdb845701653967c39c2ff70741729414a7))

# [3.1.0](https://github.com/jgiovaresco/apim-samples/compare/3.0.0...3.1.0) (2024-01-22)


### Features

* add a new gRPC service StreamService ([418a2f0](https://github.com/jgiovaresco/apim-samples/commit/418a2f0d12452c51d9b61232b4f2e867c8a07976))
* configure listening on all network interfaces by default ([fca17b0](https://github.com/jgiovaresco/apim-samples/commit/fca17b02cff5136b2e2e7a878477e01a55e01140))
* enable http access logs by default ([e02beb6](https://github.com/jgiovaresco/apim-samples/commit/e02beb6119da322d44df16e108fabd07cb04802c))

# [3.0.0](https://github.com/jgiovaresco/apim-samples/compare/2.1.0...3.0.0) (2024-01-21)


### Code Refactoring

* move to quarkus ([#165](https://github.com/jgiovaresco/apim-samples/issues/165)) ([4d9b917](https://github.com/jgiovaresco/apim-samples/commit/4d9b9171c3992eb4803cb1e727a623e7c6708016))


### BREAKING CHANGES

* WebSocket moved to HTTP server instead of a dedicated
server
gRPC server moved to HTTP server instead of a dedicated server

# [2.1.0](https://github.com/jgiovaresco/apim-samples/compare/2.0.1...2.1.0) (2024-01-17)


### Features

* add another gRPC service ([7bfe507](https://github.com/jgiovaresco/apim-samples/commit/7bfe50712e912fda134b526d66507558dfdd5292))

## [2.0.1](https://github.com/jgiovaresco/apim-samples/compare/2.0.0...2.0.1) (2023-04-21)


### Bug Fixes

* **deps:** update dependency io.confluent:kafka-avro-serializer to v7 ([1899570](https://github.com/jgiovaresco/apim-samples/commit/1899570dadfba6f9265c2f67f8c4b9849e2f1673))

# [2.0.0](https://github.com/jgiovaresco/apim-samples/compare/1.8.0...2.0.0) (2023-04-13)


### Features

* add an avro serde endpoint ([c1a6d30](https://github.com/jgiovaresco/apim-samples/commit/c1a6d30bc3819f44cccd3bcac4f50e9008ceb558))
* improve generate endpoint to generate a json matching a schema ([278d8e0](https://github.com/jgiovaresco/apim-samples/commit/278d8e0755a4231ba5000aed9a914ae4993981a4))
* move /avro route under /avro/generate ([5d57ef1](https://github.com/jgiovaresco/apim-samples/commit/5d57ef1f0508352b43f3c9b8c1b475dacd35d4b9))


### BREAKING CHANGES

* The route /avro has been moved to /avro/generate

# [1.8.0](https://github.com/jgiovaresco/apim-samples/compare/1.7.0...1.8.0) (2023-03-02)


### Features

* avro endpoint ([da8ac55](https://github.com/jgiovaresco/apim-samples/commit/da8ac55626a8112e1c2950e1760aacd2ca64fc41))

# [1.7.0](https://github.com/jgiovaresco/apim-samples/compare/1.6.0...1.7.0) (2023-01-22)


### Features

* rest echo can handle non json request ([d87a191](https://github.com/jgiovaresco/apim-samples/commit/d87a1918802607e66bc7259c470b8b7f9b8774a3))

# [1.6.0](https://github.com/jgiovaresco/apim-samples/compare/1.5.0...1.6.0) (2023-01-13)


### Features

* improve health checks ([b314156](https://github.com/jgiovaresco/apim-samples/commit/b314156d9946552bdb51e55d3c720d03799b71bb))

# [1.5.0](https://github.com/jgiovaresco/apim-samples/compare/1.4.0...1.5.0) (2023-01-12)


### Features

* route_guide grpc service ([743a83f](https://github.com/jgiovaresco/apim-samples/commit/743a83fceb53d21d6ad0b1fbd36ba972f86d3624))

# [1.4.0](https://github.com/jgiovaresco/apim-samples/compare/1.3.0...1.4.0) (2023-01-07)


### Features

* echo websocket endpoint ([a9ac83a](https://github.com/jgiovaresco/apim-samples/commit/a9ac83a31025010437a46aa3e215f5f912b4d48c))

# [1.3.0](https://github.com/jgiovaresco/apim-samples/compare/1.2.0...1.3.0) (2022-12-24)


### Features

* create an Helm chart ([a677095](https://github.com/jgiovaresco/apim-samples/commit/a677095413972d3d5cb343fe5fc3804fb85348f0))

# [1.2.0](https://github.com/jgiovaresco/apim-samples/compare/1.1.1...1.2.0) (2022-12-24)


### Features

* configure a healthcheck route ([3cef941](https://github.com/jgiovaresco/apim-samples/commit/3cef941c637f1b7b5c2ca57d5517397f431b2eda))

## [1.1.1](https://github.com/jgiovaresco/apim-samples/compare/1.1.0...1.1.1) (2022-12-23)


### Bug Fixes

* docker image ([6b658df](https://github.com/jgiovaresco/apim-samples/commit/6b658df7b90602b588089e35045f28715adcde27))

# [1.1.0](https://github.com/jgiovaresco/apim-samples/compare/1.0.0...1.1.0) (2022-12-23)


### Features

* make http server port configurable ([7ce7da5](https://github.com/jgiovaresco/apim-samples/commit/7ce7da544aadc7bfa3892790e8c4aad5ac1ce5c8))

# 1.0.0 (2022-12-23)


### Features

* echo http endpoint ([71b8fb9](https://github.com/jgiovaresco/apim-samples/commit/71b8fb941ed702f8306e84e9a98f5c6d9ca25c1b))
