param(
  [Parameter(Mandatory = $true)]
  [string] $JarPath,

  [Parameter(Mandatory = $true)]
  [string] $ServiceName,

  [string] $OtlpEndpoint = "http://localhost:4317",

  [string] $AgentJar = (Join-Path $PSScriptRoot "opentelemetry-javaagent.jar")
)

if (-not (Test-Path $AgentJar)) {
  throw "Missing OpenTelemetry agent: $AgentJar"
}
if (-not (Test-Path $JarPath)) {
  throw "Missing service jar: $JarPath"
}

$agent = (Resolve-Path $AgentJar).Path
$jar = (Resolve-Path $JarPath).Path

Write-Host "Starting $ServiceName with OTel agent -> $OtlpEndpoint"

java `
  "-javaagent:$agent" `
  "-Dotel.service.name=$ServiceName" `
  "-Dotel.exporter.otlp.endpoint=$OtlpEndpoint" `
  "-Dotel.exporter.otlp.protocol=grpc" `
  "-Dotel.traces.exporter=otlp" `
  "-Dotel.metrics.exporter=none" `
  "-Dotel.logs.exporter=none" `
  "-Dotel.propagators=tracecontext,baggage" `
  -jar $jar
