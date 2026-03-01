FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 安装 Maven
RUN apk add --no-cache maven

# 复制源码
COPY pom.xml .
COPY src ./src

# 构建
RUN mvn clean package -DskipTests

# 运行镜像
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 创建日志目录
RUN mkdir -p logs

# 复制 JAR
COPY --from=builder /app/target/*.jar app.jar

# 环境变量
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
