$ErrorActionPreference = "SilentlyContinue"
$processes = Get-WmiObject Win32_PerfFormattedData_PerfProc_Process | Where-Object { $_.name -inotmatch '_total|idle' } | select IDProcess, Name, PercentProcessorTime, WorkingSetPrivate

foreach ($process in $processes) {

	$sus = 0
	$run = 0

	$processx = Get-Process -Id $process.IDProcess

	foreach($thread in $processx.Threads) {


		If($thread.WaitReason -eq 'Suspended') {$sus = 1}
		else {$run = 1}

		If($sus -eq 1 -And $run -eq 1) {break}
	}

	If ($sus -eq 0) {$status="Running"}
	ElseIf ($sus -eq 1 -And $run -eq 1) {$status="Running(*)"}
	Else {$status="Suspended"}



	$process | Add-Member -MemberType NoteProperty -Name Status -value $status
}

$processes | ConvertTo-Json