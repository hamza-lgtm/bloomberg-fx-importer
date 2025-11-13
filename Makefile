.PHONY: all build up down logs test coverage k6 clean

# Default command
all: build up

# Build the Maven project and run all tests + JaCoCo check
build:
	@echo "Building the application and running all tests (JaCoCo check)..."
	@mvn clean install

# Start the application and database using Docker Compose
up: 
	@echo "Building and starting tests"
	@mvn clean install
	@echo "Starting services with Docker Compose..."
	@docker-compose up --build -d

# Stop and remove the Docker Compose services
down:
	@echo "Stopping and removing services..."
	@docker-compose down -v

# Follow the application logs
logs:
	@docker-compose logs -f app

# Run unit and integration tests
test:
	@echo "Running unit and integration tests..."
	@mvn clean test

# Generate the JaCoCo coverage report 
coverage-report:
	@echo "Generating JaCoCo site report..."
	@mvn jacoco:report
	@echo "Report available at target/site/jacoco/index.html"

# Run K6 performance tests
k6:
	@echo "Running K6 performance test..."
	@k6 run k6-script.js

# Clean the Maven build artifacts
clean:
	@mvn clean