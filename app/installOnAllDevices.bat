echo off       
setlocal EnableDelayedExpansion           
for /f "tokens=*" %%f in ('adb.exe devices') do (
set devicestr=%%f
	if "!devicestr!"=="!devicestr:List=!" (
		for /f "tokens=1" %%d in ("!devicestr!") do (
			set deviceid=%%d
			echo !deviceid!
			echo !apk!
			adb.exe -s !deviceid! install Needle.apk
		)
	)
)