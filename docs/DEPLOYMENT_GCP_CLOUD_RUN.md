# Google Cloud Run Deployment Guide

This guide deploys the Spring Boot URL shortener to Google Cloud Run with:

- Artifact Registry for Docker images
- Cloud Run for the application container
- Cloud SQL for PostgreSQL
- Memorystore for Redis
- Secret Manager for database credentials
- GitHub Actions for automated deployment on every push to `main`

## 1. Prerequisites

Install and authenticate the Google Cloud CLI:

```powershell
gcloud auth login
gcloud auth application-default login
```

Required local tools:

- Docker Desktop
- Google Cloud CLI
- Git

## 2. Create Google Cloud Resources

Copy and edit:

```powershell
deploy/gcloud-setup-template.ps1
```

Replace every `YOUR_*` value, then run it:

```powershell
.\deploy\gcloud-setup-template.ps1
```

The script enables APIs, creates Artifact Registry, Cloud SQL PostgreSQL, Secret Manager secrets,
service accounts, a Serverless VPC Access connector, and a Memorystore Redis instance.

Important: the database password appears only in your local setup script while creating the initial
secret. Do not commit real passwords.

## 3. Configure GitHub Actions Identity

Use Workload Identity Federation instead of a service account key.

Create a Workload Identity Pool and Provider for your GitHub repository, then grant the deploy
service account `roles/iam.workloadIdentityUser` for that repository principal.

You can use this template:

```powershell
.\deploy\workload-identity-setup-template.ps1
```

In GitHub repository settings, configure the variables and secrets listed in:

```text
deploy/github-actions-variables.md
```

Secrets required in GitHub:

- `GCP_WORKLOAD_IDENTITY_PROVIDER`
- `GCP_DEPLOY_SERVICE_ACCOUNT`

Application database credentials stay in Google Secret Manager:

- `url-shortener-db-username`
- `url-shortener-db-password`

## 4. Build and Deploy Manually

Set local variables:

```powershell
$ProjectId = "YOUR_PROJECT_ID"
$Region = "us-central1"
$Repository = "url-shortener"
$Image = "$Region-docker.pkg.dev/$ProjectId/$Repository/url-shortener:manual"
```

Build and push:

```powershell
gcloud auth configure-docker "$Region-docker.pkg.dev"
docker build -t $Image .
docker push $Image
```

Deploy:

```powershell
gcloud run deploy url-shortener `
  --project=$ProjectId `
  --region=$Region `
  --platform=managed `
  --image=$Image `
  --allow-unauthenticated `
  --service-account="url-shortener-runtime@$ProjectId.iam.gserviceaccount.com" `
  --add-cloudsql-instances="$ProjectId`:$Region`:url-shortener-postgres" `
  --vpc-connector="url-shortener-vpc" `
  --vpc-egress=private-ranges-only `
  --set-env-vars="APP_BASE_URL=https://YOUR_CLOUD_RUN_URL,APP_SHORT_CODE_LENGTH=7,APP_SHORT_CODE_MAX_ATTEMPTS=10,APP_CACHE_TTL=24h,SPRING_DATASOURCE_URL=jdbc:postgresql:///urlshortener?cloudSqlInstance=$ProjectId`:$Region`:url-shortener-postgres&socketFactory=com.google.cloud.sql.postgres.SocketFactory,SPRING_DATA_REDIS_HOST=REDIS_PRIVATE_IP,SPRING_DATA_REDIS_PORT=6379" `
  --set-secrets="SPRING_DATASOURCE_USERNAME=url-shortener-db-username:latest,SPRING_DATASOURCE_PASSWORD=url-shortener-db-password:latest"
```

After the first deploy, update `APP_BASE_URL` to the Cloud Run URL shown by `gcloud run deploy`.

## 5. Automatic Deployment

The workflow:

```text
.github/workflows/deploy-cloud-run.yml
```

runs on every push to `main`:

1. Runs non-container tests.
2. Authenticates to Google Cloud with Workload Identity Federation.
3. Builds a Docker image.
4. Pushes the image to Artifact Registry.
5. Deploys the image to Cloud Run.
6. Injects database credentials from Secret Manager.

## 6. Verify Deployment

Health:

```powershell
Invoke-RestMethod https://YOUR_CLOUD_RUN_URL/actuator/health
```

Create URL:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri https://YOUR_CLOUD_RUN_URL/api/v1/urls `
  -ContentType application/json `
  -Body '{"originalUrl":"https://www.google.com"}'
```

Redirect:

```powershell
curl.exe -i https://YOUR_CLOUD_RUN_URL/YOUR_SHORT_CODE
```

Analytics:

```powershell
Invoke-RestMethod https://YOUR_CLOUD_RUN_URL/api/v1/analytics/YOUR_SHORT_CODE
```

## 7. Production Notes

- Keep database credentials in Secret Manager only.
- Restrict unauthenticated access if the API should not be public.
- Use a custom domain before setting the final `APP_BASE_URL`.
- Replace Hibernate schema updates with Flyway or Liquibase for controlled migrations.
- Use a properly sized Cloud SQL tier and Redis size for expected traffic.
- Add Cloud Monitoring alerts for error rate, latency, instance count, and database saturation.
