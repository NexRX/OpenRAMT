# OpenRAMT (Remote Access & Monitoring Tool)
<p align="center">
  <img width="300" height="300" src="https://github.com/Nex-Coder/OpenRAMT/blob/master/nex-circle-cropped-halfrez.png?raw=true">
</p>

<a href="https://github.com/oshi/oshi"><img width="200" height="41.5625" src="https://camo.githubusercontent.com/998d1fd744377c8585dcfebd59e27449960a13b7b909bc2df99ca460625369d0/68747470733a2f2f646c2e64726f70626f7875736572636f6e74656e742e636f6d2f732f63383271626f7976767564707664702f6f7368696c6f676f2e706e67"></a>     <a href="https://mina.apache.org/ftpserver-project/"><img width="116.853932584" height="41.5730337079" src="https://images2.imgbox.com/53/27/05FP6dqm_o.png"></a>     **[jPowerShell](https://github.com/profesorfalken/jPowerShell)**

## Introduction

OpenRAMT is a Open-source Remote Access and Monitoring tool that depends on open source projects to give a cross platform applications that can allow you to setup a server on one end and a client on the other, without the need to use the same supporting environment.

## Features

- A user management system that is heavily customizable with groups and extra users.
- Basic disk cleaning utilities and recycling.
- Embedded FTP server (powered by Apache FTPServer) with some lite customisable settings.
- WiFi & Bluetooth toggling
- A task manager with killing and restarting. (Restarts attempt to kill the process and rebuild it, slightly experimental)
- Monitoring (powered by Oshi) that tracks, CPU & RAM usage, disk space, I/O activity, CPU & system average temperature.
- 4 option power menu. Shutdown, restart, sleep & Wake (On-LAN. Only after a successful login)
- Remote server settings menu to adjust to your liking.

## Problems? Take note.
Here are some limitations or problems you may run into.

### Temperature Data
You may find that temperature data is reading as zero. This most likely isn't a bug (especially if other readings are showing) as firstly, like everything else this needs admin/sudo privileges but more importantly, Oshi (the dependacy for the readings) can't always find these values when it needs them. So in these cases it needs some helping tools. If you're experiencing these problems I recommend installing **[Open Hardware Monitor](https://openhardwaremonitor.org)** as does Oshi recommend.

### Not accepting my SSL/TLS Certificate!
If your cert isn't being accepted that is probably because you haven't assigned a correct password when creating/exporting it (read the import prompt on the server) or its not in the right format. Java programs typically need .jks files to handle certs. Java comes bundled with a tool to create these certs, the search term you need is Java Keytool. If you know how to make these it is recommended for improved security. However, you'll need to give these to your clients.

### Admin or Sudo warning for server.
If you attempt to run the server without admin/sudo privileges you'll get a warning. This warning can be progressed passed but it is not recommended. Certain tasks will fail and this mode is not supported. The client may not know it has failed or it won't know why. Please, do not report bugs when running without these requirements. The client however will run just fine with or without these privileges

## Early Days / Autobuild (bots)

At the moment. The repo isn't finely tuned so no CI/working garuenteed. This will be the next challenge to fix any code that will causes these problems to arise. The main reason is Disable Bluetooth, Clean Disk, Enable Bluetooth, Disable WiFi, Enable WiFi all have platform specific code which is not failing successfully (i.e. failing because no bluetooth, etc) in the right way. Untill this is address. It can be CI'd.

## Misc

Please see SECURITY.MD for more information about bugs and security issues.
