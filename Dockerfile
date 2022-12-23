FROM graviteeio/java:17

ARG BUILD_VERSION
ENV JAR_FILE=apim-samples-${BUILD_VERSION}-fat.jar

WORKDIR /app
COPY apim-samples-${BUILD_VERSION}-fat.jar $JAR_FILE

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $JAR_FILE"]
