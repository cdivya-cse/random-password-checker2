
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN mkdir -p out && javac -d out src/*.java

EXPOSE 8080

CMD ["sh", "-c", "java -cp out PasswordServer"]

