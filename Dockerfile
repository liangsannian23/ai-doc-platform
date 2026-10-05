# 运行镜像：项目 pom 目标 Java 17，故用 17-jre（Ubuntu 基础，含 bash）
# 说明：jar 先在宿主机用 mvn clean package -DskipTests 打好，这里只做复制运行
FROM eclipse-temurin:17-jre

WORKDIR /app

# 时区设为东八区，与 application.yml 的 Asia/Shanghai 对齐（容器默认 UTC 会差 8 小时）
ENV TZ=Asia/Shanghai

# 创建非 root 系统用户，遵循最小权限原则（容器被攻破时降低影响面）
RUN groupadd --system spring && useradd --system --gid spring --home /app --shell /usr/sbin/nologin spring

# 复制宿主机已打好的 Spring Boot 可执行 jar，并归属给非 root 用户
COPY --chown=spring:spring target/*.jar app.jar

USER spring

# 应用端口
EXPOSE 8080

# JVM 容器感知参数：按容器内存限额百分比给堆，避免 OOM 或浪费
# 可用 docker run -e JAVA_OPTS="..." 覆盖
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# 健康检查：用 bash 的 /dev/tcp 直连本容器 8080 发一次 HTTP 请求，命中 200 视为健康
# 不额外安装 curl，保持镜像精简；start-period 给足 Spring Boot 启动时间
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080 && printf "GET /hello HTTP/1.0\r\n\r\n" >&3 && grep -q "200" <&3' || exit 1

# exec 形式启动，java 作为 PID 1 能正确接收 SIGTERM 实现优雅停机
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
