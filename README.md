# Biometric Matching Service (SourceAFIS)

Microserviço Java (Spring Boot) para cálculo de matching biométrico usando SourceAFIS.

## Requisitos
- Java 17+
- Maven 3.9+

## Build local
```bash
mvn clean package
```

## Run local
```bash
PORT=8080 BIOMETRIC_THRESHOLD=40.0 MAX_CANDIDATES=2000 REQUEST_MAX_BYTES=1048576 mvn spring-boot:run
```

## Docker
### Build
```bash
docker build -t biometric-matching-service .
```

### Run
```bash
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e BIOMETRIC_THRESHOLD=40.0 \
  -e MAX_CANDIDATES=2000 \
  -e REQUEST_MAX_BYTES=1048576 \
  biometric-matching-service
```

## Endpoints
- `GET /health`
- `POST /api/v1/biometric/identify`
- `POST /api/v1/biometric/verify`
