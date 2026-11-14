param(
    [switch]$ResetDatabaseLogin
)

$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
$appData = Join-Path $env:LOCALAPPDATA 'TrafficConePlayer'
$loginFile = Join-Path $appData 'mysql-login.xml'

function Get-Java25 {
    $java = Get-Command java.exe -ErrorAction SilentlyContinue
    $pathHome = if ($java) { Split-Path -Parent (Split-Path -Parent $java.Source) }
    $jdkVersion = '25.0.5'
    $jdkBuild = '8'
    $jdkHome = Join-Path $appData "jdk-$jdkVersion+$jdkBuild"

    foreach ($candidate in @($jdkHome, $env:JAVA_HOME, $pathHome)) {
        if ($candidate -and (Test-Path (Join-Path $candidate 'bin\javac.exe'))) {
            $release = Join-Path $candidate 'release'
            if ((Test-Path $release) -and (Select-String -LiteralPath $release -Pattern '^JAVA_VERSION="25(?:[.+-][^"]*)?"$' -Quiet)) {
                return $candidate
            }
        }
    }

    $architecture = if ($env:PROCESSOR_ARCHITEW6432) {
        $env:PROCESSOR_ARCHITEW6432
    } else {
        $env:PROCESSOR_ARCHITECTURE
    }
    if ($architecture -ne 'AMD64') {
        throw 'Automatic JDK setup requires 64-bit x86 Windows. Use a compatible JDK 25 on JAVA_HOME or PATH.'
    }

    $asset = "OpenJDK25U-jdk_x64_windows_hotspot_${jdkVersion}_${jdkBuild}.zip"
    $url = "https://github.com/adoptium/temurin25-binaries/releases/download/jdk-$jdkVersion%2B$jdkBuild/$asset"
    $staging = Join-Path $appData ("jdk-download-" + [guid]::NewGuid())
    $archive = Join-Path $staging $asset
    $extracted = Join-Path $staging 'extracted'
    New-Item -ItemType Directory -Path $staging | Out-Null
    try {
        Write-Host "Downloading Temurin JDK $jdkVersion. This can take a few minutes..."
        $ProgressPreference = 'SilentlyContinue'
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $checksumFile = Join-Path $staging 'jdk.sha256'
        Invoke-WebRequest -UseBasicParsing -Uri "$url.sha256.txt" -OutFile $checksumFile
        $expectedHash = (Get-Content -LiteralPath $checksumFile -Raw).Trim() -split '\s+' | Select-Object -First 1
        Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $archive
        if ((Get-FileHash -Path $archive -Algorithm SHA256).Hash -ne $expectedHash) {
            throw 'The downloaded JDK failed its checksum verification.'
        }
        Expand-Archive -LiteralPath $archive -DestinationPath $extracted
        $unpackedJdk = Join-Path $extracted "jdk-$jdkVersion+$jdkBuild"
        if (!(Test-Path (Join-Path $unpackedJdk 'bin\javac.exe'))) {
            throw 'The JDK archive is missing the Java compiler.'
        }
        try {
            [IO.Directory]::Move($unpackedJdk, $jdkHome)
        } catch {
            if (!(Test-Path (Join-Path $jdkHome 'bin\javac.exe'))) { throw }
        }
        return $jdkHome
    } finally {
        $resolvedStaging = (Resolve-Path -LiteralPath $staging).Path
        $resolvedAppData = (Resolve-Path -LiteralPath $appData).Path
        if ($resolvedStaging.StartsWith($resolvedAppData + '\', [StringComparison]::OrdinalIgnoreCase)) {
            Remove-Item -LiteralPath $resolvedStaging -Recurse -Force
        }
    }
}

try {
    New-Item -ItemType Directory -Path $appData -Force | Out-Null
    $env:JAVA_HOME = Get-Java25
    $env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path

    if ($ResetDatabaseLogin -or !$env:DB_USER -or !$env:DB_PASSWORD) {
        if ($ResetDatabaseLogin -or !(Test-Path -LiteralPath $loginFile)) {
            Write-Host 'Enter the local MySQL account for Traffic Cone Player.'
            $user = (Read-Host 'MySQL username [tcp_java_app]').Trim()
            if (!$user) { $user = 'tcp_java_app' }
            $password = Read-Host 'MySQL password' -AsSecureString
            if ($password.Length -eq 0) { throw 'Enter a nonempty MySQL password.' }
            $login = [PSCredential]::new($user, $password)
            $login | Export-Clixml -LiteralPath $loginFile
        } else {
            $login = Import-Clixml -LiteralPath $loginFile
        }
        $env:DB_USER = $login.UserName
        $env:DB_PASSWORD = $login.GetNetworkCredential().Password
    }

    Set-Location -LiteralPath $project
    & '.\mvnw.cmd' javafx:run
    exit $LASTEXITCODE
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}
