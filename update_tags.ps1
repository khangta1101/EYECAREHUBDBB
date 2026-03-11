$directory = "d:\SWP391_BE\EYECAREHUBDBB\src\main\java\com\example\EyeCareHubDB\controller"
$files = Get-ChildItem -Path $directory -Filter "*Controller.java"
$utf8NoBom = New-Object System.Text.UTF8Encoding $False

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName)
    
    if (-not $content.Contains("@Tag")) {
        $match = [regex]::Match($content, 'public class (\w+)Controller')
        if ($match.Success) {
            $namePart = $match.Groups[1].Value
            $properName = [regex]::Replace($namePart, '(?<!^)(?=[A-Z])', ' ')
            
            $importStr = "import io.swagger.v3.oas.annotations.tags.Tag;"
            $content = [regex]::Replace($content, '(package .+;)', "`$1`r`n`r`n$importStr", 1)
            
            $tagStr = "@Tag(name = `"$properName`")`r`n@RestController"
            
            # Use regex to replace first occurrence of @RestController
            $regex = [regex] "(?m)^@RestController"
            $content = $regex.Replace($content, $tagStr, 1)
            
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            Write-Host "Updated $($file.Name) with Tag: $properName"
        }
    }
}
