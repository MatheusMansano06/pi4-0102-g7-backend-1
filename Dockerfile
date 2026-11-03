# Etapa 1: compilar o projeto.
# Usamos a imagem do Maven para ninguem do grupo precisar instalar Maven na propria maquina.
FROM maven:3.9-eclipse-temurin-17 AS compilacao
WORKDIR /app

# Copiamos so o pom.xml primeiro para o Docker guardar as bibliotecas baixadas em cache.
# Assim, mudar o codigo nao obriga a baixar tudo de novo.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Testes sem PostgreSQL nem frontend. Reaproveita a compilacao acima.
# Usado somente quando o Compose de testes pede o target "testes".
FROM compilacao AS testes
CMD ["mvn", "-B", "test"]

# Etapa 2: rodar.
# A imagem final leva so o Java e o programa compilado, sem o Maven. Fica bem menor.
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=compilacao /app/target/archflow-backend-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
