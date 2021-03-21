ps -Ao ucomm,pid,stat,%cpu,%mem | awk 'NR>1'| awk '
BEGIN { ORS = ""; print " [ "}
{ printf "%s{\"Name\": \"%s\", \"IDProcess\": \"%s\", \"Status\": \"%s\", \"PercentProcessorTime\": \"%s\", \"WorkingSetPrivate\": \"%s\"}",
      separator, $1, $2, $3, $4, $5
  separator = ", "
}
END { print " ] " }';