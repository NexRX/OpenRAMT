# load assembly System.Windows.Forms which will be used
Add-Type -AssemblyName System.Windows.Forms

# set powerstate to suspend (sleep mode)
$PowerState = [System.Windows.Forms.PowerState]::Suspend;

# do not force putting Windows to sleep
$Force = $false;

# so you can wake up your computer from sleep
$DisableWake = $false;

# do it! Set computer to sleep
[System.Windows.Forms.Application]::SetSuspendState($PowerState, $Force, $DisableWake);