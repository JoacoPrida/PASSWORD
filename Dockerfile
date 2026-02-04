# Etapa 1: Construir el código (Usamos Maven)
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Preparar el servidor (Usamos Tomcat 9 con Java 8)
FROM tomcat:9.0-jre8
# Borramos la app por defecto de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*
# Copiamos tu proyecto y lo renombramos a ROOT.war para que salga en la página principal
COPY --from=build /app/target/gestion-estudiantes.war /usr/local/tomcat/webapps/ROOT.war

# Abrimos el puerto
EXPOSE 8080
CMD ["catalina.sh", "run"]
