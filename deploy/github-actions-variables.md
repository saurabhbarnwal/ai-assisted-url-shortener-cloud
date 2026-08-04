# GitHub Actions Configuration

Configure these GitHub repository variables:

| Name | Example | Secret? |
| --- | --- | --- |
| `GCP_PROJECT_ID` | `my-gcp-project` | No |
| `GCP_REGION` | `us-central1` | No |
| `CLOUD_RUN_SERVICE` | `url-shortener` | No |
| `CLOUD_RUN_RUNTIME_SERVICE_ACCOUNT` | `url-shortener-runtime@my-gcp-project.iam.gserviceaccount.com` | No |
| `ARTIFACT_REPOSITORY` | `url-shortener` | No |
| `APP_BASE_URL` | `https://url-shortener-abc-uc.a.run.app` | No |
| `CLOUD_SQL_CONNECTION_NAME` | `my-gcp-project:us-central1:url-shortener-postgres` | No |
| `CLOUD_SQL_DATABASE` | `urlshortener` | No |
| `SERVERLESS_VPC_CONNECTOR` | `url-shortener-vpc` | No |
| `REDIS_HOST` | `10.0.0.5` | No |
| `REDIS_PORT` | `6379` | No |
| `DB_USERNAME_SECRET` | `url-shortener-db-username` | No |
| `DB_PASSWORD_SECRET` | `url-shortener-db-password` | No |

Configure these GitHub repository secrets:

| Name | Purpose |
| --- | --- |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Full Workload Identity Provider resource name |
| `GCP_DEPLOY_SERVICE_ACCOUNT` | Deployment service account email |

Application database credentials belong in Google Secret Manager, not GitHub Secrets.

Use `deploy/workload-identity-setup-template.ps1` to create the Workload Identity Pool and Provider.
