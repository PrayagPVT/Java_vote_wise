FROM eclipse-temurin:11-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the source code and web files
COPY src /app/src
COPY web /app/web

# Compile the Java application
RUN javac -d out -sourcepath src src/Main.java

# Expose port 8080 since our server runs on it
EXPOSE 8080

# Command to run the application
CMD ["java", "-cp", "out", "Main"]
