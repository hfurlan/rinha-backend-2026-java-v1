FROM maven:4.0.0-rc-5-amazoncorretto-25
COPY load/mcc_risk.json tmp/
COPY load/normalization.json tmp/
COPY load/references.json.gz tmp/
WORKDIR /app
COPY . .
RUN mvn clean install -Dmaven.test.skip=true
CMD ["java", "-jar", "target/rinha-backend-2026-0.0.1-SNAPSHOT.jar"]