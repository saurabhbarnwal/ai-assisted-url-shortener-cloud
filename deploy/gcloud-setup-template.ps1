# Replace values before running.
$ProjectId = "ai-assisted-url-shortener"
$Region = "us-central1"
$Repository = "url-shortener"
$CloudSqlInstance = "url-shortener-postgres"
$DatabaseName = "urlshortener"
$DbUsername = "barnwal"
$DbPasswordSecure = Read-Host "barnwal" -AsSecureString
$DbPasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DbPasswordSecure)
$DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($DbPasswordPointer)
$RuntimeServiceAccount = "url-shortener-runtime@$ProjectId.iam.gserviceaccount.com"
$DeployServiceAccount = "url-shortener-deploy@$ProjectId.iam.gserviceaccount.com"

gcloud config set project $ProjectId

gcloud services enable `
  run.googleapis.com `
  artifactregistry.googleapis.com `
  cloudbuild.googleapis.com `
  sqladmin.googleapis.com `
  redis.googleapis.com `
  vpcaccess.googleapis.com `
  secretmanager.googleapis.com `
  iamcredentials.googleapis.com

gcloud artifacts repositories create $Repository `
  --repository-format=docker `
  --location=$Region `
  --description="URL shortener Docker images"

gcloud iam service-accounts create url-shortener-runtime `
  --display-name="URL Shortener Cloud Run runtime"

gcloud iam service-accounts create url-shortener-deploy `
  --display-name="URL Shortener GitHub Actions deployer"

gcloud projects add-iam-policy-binding $ProjectId `
  --member="serviceAccount:$DeployServiceAccount" `
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding $ProjectId `
  --member="serviceAccount:$DeployServiceAccount" `
  --role="roles/artifactregistry.writer"

gcloud projects add-iam-policy-binding $ProjectId `
  --member="serviceAccount:$DeployServiceAccount" `
  --role="roles/iam.serviceAccountUser"

gcloud projects add-iam-policy-binding $ProjectId `
  --member="serviceAccount:$RuntimeServiceAccount" `
  --role="roles/cloudsql.client"

gcloud sql instances create $CloudSqlInstance `
  --database-version=POSTGRES_16 `
  --tier=db-f1-micro `
  --region=$Region

gcloud sql databases create $DatabaseName --instance=$CloudSqlInstance

gcloud sql users create $DbUsername `
  --instance=$CloudSqlInstance `
  --password=$DbPassword

$DbUsername | gcloud secrets create url-shortener-db-username --data-file=-
$DbPassword | gcloud secrets create url-shortener-db-password --data-file=-

gcloud secrets add-iam-policy-binding url-shortener-db-username `
  --member="serviceAccount:$RuntimeServiceAccount" `
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding url-shortener-db-username `
  --member="serviceAccount:$DeployServiceAccount" `
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding url-shortener-db-password `
  --member="serviceAccount:$RuntimeServiceAccount" `
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding url-shortener-db-password `
  --member="serviceAccount:$DeployServiceAccount" `
  --role="roles/secretmanager.secretAccessor"

gcloud compute networks vpc-access connectors create url-shortener-vpc `
  --region=$Region `
  --range=10.8.0.0/28

gcloud redis instances create url-shortener-redis `
  --region=$Region `
  --size=1 `
  --redis-version=redis_7_0

gcloud redis instances describe url-shortener-redis `
  --region=$Region `
  --format="value(host,port)"

[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($DbPasswordPointer)
Write-Host "Configure GitHub variables and secrets from deploy/github-actions-variables.md."
