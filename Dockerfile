FROM graviteeio/java:17

ARG BUILD_VERSION

COPY apim-samples-${BUILD_VERSION}-fat.jar apim-samples-${BUILD_VERSION}-fat.jar

CMD ["java", "-jar", "apim-samples-fat.jar"]
