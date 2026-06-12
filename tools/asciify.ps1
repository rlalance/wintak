# One-shot: make all Java sources pure ASCII.
# Inside string/char literals -> \uXXXX escapes (javac decodes them identically under any
# file encoding). In comments/code -> plain ASCII equivalents.
$map = @{
    0x2014 = '-'; 0x2013 = '-'; 0x2026 = '...'; 0x2192 = '->'; 0x2194 = '<->';
    0x2265 = '>='; 0x00B7 = '|'; 0x25BA = '(select)'; 0x25A0 = '(square)';
    0x25C6 = '(diamond)'; 0x25CF = '(dot)'
}

Get-ChildItem -Recurse src -Filter *.java | ForEach-Object {
    $text = [IO.File]::ReadAllText($_.FullName, [Text.Encoding]::UTF8)
    if (-not ($text.ToCharArray() | Where-Object { [int]$_ -gt 127 } | Select-Object -First 1)) { return }

    $sb = [Text.StringBuilder]::new()
    $state = 'code'   # code | str | chr | line | block
    $i = 0
    while ($i -lt $text.Length) {
        $c = $text[$i]
        $n = if ($i + 1 -lt $text.Length) { $text[$i + 1] } else { [char]0 }
        $code = [int]$c

        if ($code -gt 127) {
            if ($state -eq 'str' -or $state -eq 'chr') {
                [void]$sb.AppendFormat('\u{0:x4}', $code)
            } elseif ($map.ContainsKey($code)) {
                [void]$sb.Append($map[$code])
            } else {
                [void]$sb.Append('?')
            }
            $i++
            continue
        }

        [void]$sb.Append($c)
        switch ($state) {
            'code' {
                if ($c -eq '"') { $state = 'str' }
                elseif ($c -eq "'") { $state = 'chr' }
                elseif ($c -eq '/' -and $n -eq '/') { $state = 'line' }
                elseif ($c -eq '/' -and $n -eq '*') { $state = 'block' }
            }
            'str' {
                if ($c -eq '\') { [void]$sb.Append($n); $i++ }
                elseif ($c -eq '"') { $state = 'code' }
            }
            'chr' {
                if ($c -eq '\') { [void]$sb.Append($n); $i++ }
                elseif ($c -eq "'") { $state = 'code' }
            }
            'line' {
                if ($c -eq "`n") { $state = 'code' }
            }
            'block' {
                if ($c -eq '*' -and $n -eq '/') { [void]$sb.Append($n); $i++; $state = 'code' }
            }
        }
        $i++
    }
    [IO.File]::WriteAllText($_.FullName, $sb.ToString(), [Text.UTF8Encoding]::new($false))
    Write-Output "asciified: $($_.Name)"
}
