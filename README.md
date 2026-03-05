# Weather Metrics API

A REST API service for receiving and querying weather data from various sensors. 
The application allows sensors to report metrics such as temperature, humidity, and wind speed,
and provides aggregated statistics (avg, min, max, sum) over configurable date ranges.

## Features
- Receive weather metric values from sensors via REST API
- Query sensor data using flexible filters (sensor IDs, metrics, date ranges)
- Statistical aggregations: avg, min, max, sum
- Date range validation (1 day to 1 month)
- Input validation
- Exception handling
- H2 database with file persistence in dev, MySQL in prod
- HTTP Basic Authentication with role-based access control in prod
- Unit and integration tests
- Swagger API documentation 

## Technology Stack
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- Maven
- JUnit 5 & Mockito

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/TaritaK/weather-metrics-api.git
cd weather-metrics-api
```

### 2. Run the application

**Option A: Using Maven (Requires cloning the repository)**

Build the project
```bash
./mvnw clean install
```
Run the application
```bash
./mvnw spring-boot:run
```

**Option B: Using Docker**
```bash
docker pull tarita29/weather-metrics-api:0505
docker run -d -p 8080:8080 --name weather-api tarita29/weather-metrics-api:0505
```

The application will start on `http://localhost:8080`

**Note**: On first startup with insufficient data, sample data will be automatically created:
- 2 sensors 
- 7 days of historical metrics (temperature and humidity readings every 6 hours)
- Weather data from OpenWeather API is fetched every 12 hours (temperature, humidity, pressure, wind-speed)

### H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/sensors`
- Username: `sa`
- Password: (empty)

### 3. API Endpoints

Full API documentation available at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### Examples
### 1. Create a Sensor
**POST** `/api/sensors`
```bash
curl -X POST http://localhost:8080/api/sensors \
  -H "Content-Type: application/json" \
  -d '{"name": "Temperature Sensor"}'
```

### 2. Store a Metric
**POST** `/api/metrics`
```bash
curl -X POST http://localhost:8080/api/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "sensorId": 128,
    "metricType": "temperature",
    "metricValue": 20.5
  }'
```
**Response:**
```json
{
  "id": 133,
  "sensor": {
    "id": 120,
    "name": "OpenWeather API Sensor"
  },
  "metricType": "temperature",
  "metricValue": 20.5,
  "timestamp": "2026-03-05T02:05:44.170686"
}
```
### 3. Get All Metrics (paginated)

**GET** `/api/metrics`

**Note**: Default page size: 50. Use `?page=0&size=10` to customize.

```bash
curl http://localhost:8080/api/metrics
curl "http://localhost:8080/api/metrics?size=10"
curl "http://localhost:8080/api/metrics?page=0&size=10"
```
## Query Examples
### Example 1: Average temperature and humidity for a single sensor in the last week
**POST** `/api/metrics/query`

```bash
curl -X POST http://localhost:8080/api/metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "sensorIds": [128],
    "metricTypes": ["temperature", "humidity"],
    "statistic": "avg",
    "startDate": "2026-02-21T00:00:00",
    "endDate": "2026-02-28T23:59:59"
  }'
```

### Example 2: Average temperature and humidity for multiple sensors in the last week

```bash
curl -X POST http://localhost:8080/api/metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "sensorIds": [128, 129],
    "metricTypes": ["temperature", "humidity"],
    "statistic": "avg",
    "startDate": "2026-02-21T00:00:00",
    "endDate": "2026-02-28T23:59:59"
  }'
```

**Response:**
```json
[
  {"sensorId":120,
    "results":
    {"humidity_avg":0.0,"temperature_avg":20.5}},
  {"sensorId":121,
    "results":{"humidity_avg":0.0,"temperature_avg":0.0}}]

```

### Example 3: Query without date range (defaults to last 1 day)

```bash
curl -X POST http://localhost:8080/api/metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "sensorIds": [128],
    "metricTypes": ["temperature"],
    "statistic": "avg"
  }'
```


## Validation Rules

### Metric Request
- `sensorId`: Required, must exist in database
- `metricType`: Required
- `metricValue`: Required

### Query Request
- `sensorIds`: Required, at least one sensor ID
- `metricTypes`: Required, at least one metric type
- `statistic`: Required, must be one of: `avg`, `average`, `min`, `max`, `sum`
- `startDate`: Optional (defaults to 1 day ago)
- `endDate`: Optional (defaults to now)
- `Date range` : Must be between 1 day and 31 days

## Error Handling

- Invalid Date Range (Too Short)
- Invalid Date Range (Too Long)
- Invalid Statistic for anything other than avg, average, min, max, sum 
- Sensor Not Found
- Validation Errors


## Running Tests

### Run all tests
```bash
./mvnw test
```

### Run specific test class
```bash
./mvnw test -Dtest=MetricServiceTest
```

## Project Structure

```
src/main/java/com/project/weathermetrics/
├───config               # database seeding
├── controller/          # REST controllers
├── dto/                 # Data transfer objects
├── entity/              # JPA entities
├── exception/           # Custom exceptions and handlers
├── repository/          # JPA repositories
├── scheduler/           # Scheduled tasks for fetching weather metric from OpenWeather API
├── securityconfig/      # Security configurations
└── service/             # Business logic
```

## Database Schema

The application uses 2 tables to store sensor and metric data:

**Sensors** 
- `id` - Unique identifier (auto-generated) (PK)
- `name` - Sensor name

**Metrics** 
- `id` - Unique identifier (auto-generated) (PK)
- `sensor_id` - References to the sensor that reported this metric (FK)
- `metric_type` - Type of metric (e.g., temperature, humidity, pressure)
- `metric_value` - The metric value
- `timestamp` - When the metric was recorded

## Design Decisions

1. **H2 Database**: Chosen for simplicity and file persistence without external dependencies
2. **Map-based Response**: QueryResponse uses a Map to efficiently group multiple metrics per sensor
3. **Default Date Range**: When not specified, queries default to the last 1 day of data
4. **Null Handling**: Repository aggregations return 0.0 when no data exists for the range
