# Build stage
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cài tzdata và đặt múi giờ Việt Nam cho cả OS (logs, shell, ...)
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime \
    && echo "Asia/Ho_Chi_Minh" > /etc/timezone
ENV TZ=Asia/Ho_Chi_Minh

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# -Duser.timezone ép JVM dùng giờ VN NGAY TỪ LÚC KHỞI ĐỘNG,
# trước khi HikariCP mở connection -> tránh lệch ngày check-in/check-out trên Render
ENTRYPOINT ["java", "-Duser.timezone=Asia/Ho_Chi_Minh", "-Xmx300m", "-jar", "app.jar"]
