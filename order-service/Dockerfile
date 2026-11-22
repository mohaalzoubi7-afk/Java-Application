FROM maven:3.9-eclipse-temurin-17
WORKDIR /src/code/order 
COPY ./pom.xml .
COPY ./src ./src 
RUN mvn clean package 
CMD ["java", "-jar", "target/order-service-1.0.0.jar"]
