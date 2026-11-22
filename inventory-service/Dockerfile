FROM  maven:3.9-eclipse-temurin-17
WORKDIR /src/code/inventory 
COPY ./pom.xml .
COPY ./src ./src
RUN mvn clean package 
CMD ["java", "-jar", "target/inventory-service-1.0.0.jar"]
