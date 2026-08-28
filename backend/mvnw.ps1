param(
  [Parameter(ValueFromRemainingArguments = $true)]
  [string[]]$MavenArgs
)

$ErrorActionPreference = "Stop"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$propsFile = Join-Path $baseDir ".mvn\wrapper\maven-wrapper.properties"
$distributionUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"
if (Test-Path $propsFile) {
  Get-Content $propsFile | ForEach-Object {
    if ($_ -match "^distributionUrl=(.+)$") { $distributionUrl = $Matches[1].Trim() }
  }
}

$hash = [System.BitConverter]::ToString(
  [System.Security.Cryptography.SHA256]::Create().ComputeHash(
    [System.Text.Encoding]::UTF8.GetBytes($distributionUrl)
  )
).Replace("-", "").ToLowerInvariant().Substring(0, 12)

$wrapperHome = Join-Path $env:USERPROFILE ".m2\wrapper\dists\apache-maven-3.9.9\$hash"
$mvnCmd = Join-Path $wrapperHome "apache-maven-3.9.9\bin\mvn.cmd"
if (-not (Test-Path $mvnCmd)) {
  New-Item -ItemType Directory -Force -Path $wrapperHome | Out-Null
  $zip = Join-Path $wrapperHome "maven.zip"
  Write-Host "Downloading Maven from $distributionUrl"
  curl.exe -L --fail --retry 3 --retry-delay 2 -o $zip $distributionUrl
  if ($LASTEXITCODE -ne 0) {
    throw "Failed to download Maven distribution."
  }
  Expand-Archive -Path $zip -DestinationPath $wrapperHome -Force
  Remove-Item $zip -Force
}

& $mvnCmd -f (Join-Path $baseDir "pom.xml") @MavenArgs
exit $LASTEXITCODE
