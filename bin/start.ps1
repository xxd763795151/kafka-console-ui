# PowerShell
# Set the script execution policy. If necessary, execute this command in PowerShell and then run the script.
# Set-ExecutionPolicy Bypass -Scope Process -Force

param()

$BIN_DIR = $PSScriptRoot
if (-not $BIN_DIR.EndsWith('\')) {
    $BIN_DIR += '\'
}

$BASE_DIR = (Get-Item (Join-Path $BIN_DIR "..")).FullName

if (-not $env:JAVA_HOME) {
    Write-Error "ERROR: JAVA_HOME is not defined"
    exit 1
}

$JAVA_OPTS = "-Xmx512m -Xms512m -Xmn256m -Xss256k -Dfile.encoding=utf-8"

# 检测JDK版本
$javaCmd = Join-Path $env:JAVA_HOME "bin\java.exe"
$javaVersionOutput = & $javaCmd -version 2>&1
$javaVersionString = $javaVersionOutput | Select-String -Pattern 'version "([^"]*)"' | ForEach-Object { $_.Matches.Groups[1].Value }

# 提取主版本号
$javaMajorVersion = $javaVersionString -replace '.*?(\d+)\..*', '$1'
if ($javaMajorVersion -eq "1") {
    $javaMajorVersion = $javaVersionString -replace '.*?1\.(\d+)\..*', '$1'
}

# 只在JDK 9及以上版本添加--add-opens参数
if ([int]$javaMajorVersion -ge 9) {
    Write-Host "JDK version $javaVersionString, adding --add-opens parameters..."
    $JAVA_OPTS += " --add-opens java.base/java.io=ALL-UNNAMED"
    $JAVA_OPTS += " --add-opens java.base/java.lang=ALL-UNNAMED"
    $JAVA_OPTS += " --add-opens java.base/java.util=ALL-UNNAMED"
    $JAVA_OPTS += " --add-opens java.base/java.net=ALL-UNNAMED"
} else {
    Write-Host "JDK version $javaVersionString, no need to add --add-opens parameters"
}

$CONFIG_FILE = Join-Path $BASE_DIR "config\application.yml"
$TARGET = Join-Path $BASE_DIR "lib\kafka-console-ui.jar"
$DATA_DIR = $BASE_DIR
$LOG_HOME = $BASE_DIR

if (-not (Test-Path $TARGET -PathType Leaf)) {
    Write-Error "ERROR: Jar file not found at [$TARGET]"
    exit 1
}

if (-not (Test-Path $CONFIG_FILE -PathType Leaf)) {
    Write-Warning "WARNING: Config file not found at [$CONFIG_FILE]"
}

& $javaCmd $JAVA_OPTS.Split() `
    -jar $TARGET `
    "--spring.config.location=$CONFIG_FILE" `
    "--data.dir=$DATA_DIR" `
    "--logging.home=$LOG_HOME"