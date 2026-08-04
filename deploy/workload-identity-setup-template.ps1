# Replace values before running.
$ProjectId = "ai-assisted-url-shortener"
$ProjectNumber = "204743655262"
$GitHubRepository = "https://github.com/saurabhbarnwal/ai-assisted-url-shortener-cloud"
$PoolId = "github"
$ProviderId = "github-main"
$DeployServiceAccount = "url-shortener-deploy@$ProjectId.iam.gserviceaccount.com"

gcloud config set project $ProjectId

gcloud iam workload-identity-pools create $PoolId `
  --project=$ProjectId `
  --location=global `
  --display-name="GitHub Actions"

gcloud iam workload-identity-pools providers create-oidc $ProviderId `
  --project=$ProjectId `
  --location=global `
  --workload-identity-pool=$PoolId `
  --display-name="GitHub main branch" `
  --issuer-uri="https://token.actions.githubusercontent.com" `
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.ref=assertion.ref" `
  --attribute-condition="assertion.repository == '$GitHubRepository' && assertion.ref == 'refs/heads/main'"

gcloud iam service-accounts add-iam-policy-binding $DeployServiceAccount `
  --project=$ProjectId `
  --role="roles/iam.workloadIdentityUser" `
  --member="principalSet://iam.googleapis.com/projects/$ProjectNumber/locations/global/workloadIdentityPools/$PoolId/attribute.repository/$GitHubRepository"

gcloud iam workload-identity-pools providers describe $ProviderId `
  --project=$ProjectId `
  --location=global `
  --workload-identity-pool=$PoolId `
  --format="value(name)"

Write-Host "Use the printed provider name as GitHub secret GCP_WORKLOAD_IDENTITY_PROVIDER."
Write-Host "Use $DeployServiceAccount as GitHub secret GCP_DEPLOY_SERVICE_ACCOUNT."
