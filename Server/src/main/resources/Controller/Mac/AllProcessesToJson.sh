ps -Ao pid,ucomm,%cpu,%mem,stat | awk '
BEGIN { ORS = ""; print " [ "}
{ printf "%s{\"user\": \"%s\", \"pid\": \"%s\", \"cpu\": \"%s\"}",
      separator, $1, $2, $3
  separator = ", "
}
END { print " ] " }';