# Bloomberg FX Deal Importer

This project is a Spring Boot microservice designed to accept and persist FX deal data for a data warehouse, as per the SDET challenge requirements.

The service provides a single API endpoint to import a batch of FX deals. It is designed for robustness, testability, and adherence to specific business logic:
* **Row-level Validation:** Each deal is validated individually.
* **Deduplication:** The system prevents importing deals with the same unique ID.
* **"No Rollback" Semantics:** Valid deals are saved even if other deals in the same batch are invalid.


## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3
* **Build:** Maven
* **Database:** PostgreSQL
* **Containerization:** Docker & Docker Compose
* **Testing:**
    * **Unit:** JUnit 5, Mockito
    * **API/E2E:** RESTAssured
    * **Integration:** Testcontainers
    * **Coverage:** JaCoCo
    * **Performance:** K6

## 🚀 Requirements

* Java 21
* Maven 3.9
* Docker & Docker Compose
* K6 (For performance testing)
* Postman (For manual testing)

## 📦 How to Run

The entire application stack (app + database) can be run using Docker Compose.

1.  **Build and Run with Makefile:**
    The simplest way is to use the provided `Makefile`.

    ```sh
    make up
    ```

2.  **Run Manually (without Makefile):**

    ```sh
    # 1. Build the app and run tests 
    mvn clean install
    
    # 2. Start the application and database
    docker-compose up --build -d
    ```

* The API will be available at `http://localhost:8080`.
* The PostgreSQL database will be available at `localhost:5432`.

### Other Makefile Commands

* `make down`: Stops and removes all containers and volumes.
* `make logs`: Follows the logs from the application container.
* `make test`: Running unit and integration tests.
* `make coverage`: Generates the JaCoCo coverage report.
* `make k6`: Runs the K6 performance tests.
* `make build`: Builds the application, runs all tests, and checks for 100% coverage.
* `make clean`: Cleans the Maven build artifacts.





### 1. Run All Tests (Unit, Integration, API, E2E)

This command runs all tests and enforces the 100% JaCoCo coverage check. The build will fail if coverage is not met.

```sh
# Using the Makefile
make build

# Or manually with Maven
mvn clean install