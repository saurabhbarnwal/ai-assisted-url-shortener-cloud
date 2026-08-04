# GitHub Actions Configuration

Configure these GitHub repository variables:

| Name | Example | Secret? |
| --- | --- | --- |
| `GCP_PROJECT_ID` | `ai-assisted-url-shortener` | No |
| `GCP_REGION` | `us-central1` | No |
| `CLOUD_RUN_SERVICE` | `url-shortener` | No |
| `CLOUD_RUN_RUNTIME_SERVICE_ACCOUNT` | `url-shortener-runtime@ai-assisted-url-shortener.iam.gserviceaccount.com` | No |
| `ARTIFACT_REPOSITORY` | `url-shortener` | No |
| `APP_BASE_URL` | A real URL, for example `https://url-shortener-xxxxx-uc.a.run.app` | No |
| `CLOUD_SQL_CONNECTION_NAME` | `ai-assisted-url-shortener:us-central1:url-shortener-postgres` | No |
| `CLOUD_SQL_DATABASE` | `urlshortener` | No |
| `SERVERLESS_VPC_CONNECTOR` | `url-shortener-vpc` | No |
| `REDIS_HOST` | Memorystore Redis private IP, for example `10.0.0.5` | No |
| `REDIS_PORT` | `6379` | No |
| `DB_USERNAME_SECRET` | `url-shortener-db-username` | No |
| `DB_PASSWORD_SECRET` | `url-shortener-db-password` | No |

Configure these GitHub repository secrets:

| Name | Purpose |
| --- | --- |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | `projects/204743655262/locations/global/workloadIdentityPools/github/providers/github-main` |
| `GCP_DEPLOY_SERVICE_ACCOUNT` | `url-shortener-deploy@ai-assisted-url-shortener.iam.gserviceaccount.com` |

Application database credentials belong in Google Secret Manager, not GitHub Secrets.

Use `deploy/workload-identity-setup-template.ps1` to create the Workload Identity Pool and Provider.

Important: `GCP_WORKLOAD_IDENTITY_PROVIDER` must not include the `//iam.googleapis.com/` prefix.
Use the `projects/.../providers/...` value exactly.

Important: do not put explanatory placeholder text into GitHub variables. `APP_BASE_URL` must start
with `http://` or `https://`, and `REDIS_HOST` must be the actual Memorystore Redis host value from:

```powershell
gcloud redis instances describe url-shortener-redis `
  --region=us-central1 `
  --format="value(host)"
```

If Cloud Run says the container failed to start, inspect the latest revision logs:

```powershell
gcloud run services logs read url-shortener `
  --project=ai-assisted-url-shortener `
  --region=us-central1 `
  --limit=100
```

The GitHub deploy service account needs `roles/logging.viewer` to read these logs from GitHub
Actions.

If startup logs contain `The Cloud SQL instance does not exist`, create or rename the Cloud SQL
instance so this command succeeds:

```powershell
gcloud sql instances describe url-shortener-postgres `
  --project=ai-assisted-url-shortener `
  --format="value(connectionName)"
```
