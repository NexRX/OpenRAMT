ps -Ao pid,%mem,%cpu,stat,ucomm | awk 'NR>1'| awk '
BEGIN { ORS = ""; print " [ "}
{ printf "%s{\"IDProcess\": \"%s\", \"WorkingSetPrivate\": \"%s\", \"PercentProcessorTime\": \"%s\", \"Status\": \"%s\", \"Name\": \"%s %s %s %s\"}",
      separator, $1, $2, $3, $4, $5, $6, $7, $8
  separator = ", "
}
END { print " ] " }';