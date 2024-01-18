FROM graviteeio/java:21

ARG BUILD_VERSION
ENV JAR_FILE=apim-samples-${BUILD_VERSION}-fat.jar

WORKDIR /app
COPY app-${BUILD_VERSION}-fat.jar $JAR_FILE

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $JAR_FILE"]
