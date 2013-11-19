set term eps
set key invert box top right reverse Left
set xtics nomirror
set ytics nomirror
set border 3
set output "sprint3.eps"
days=14
max_time=235
set yrange [0:max_time] 
set xrange [0:days]

set xlabel "Iteration timeline [days]"
set ylabel "Effort remaining [hours]"
set grid
set xtics 1
set ytics 50

plot - (x*max_time/days) + max_time title 'Ideal progress' with lines linecolor rgb "green" lw 4, \
         "sprint3.data" notitle lt 7 linecolor rgb "red" ps 0.5, \
         "sprint3.data" with lines linecolor rgb "red" lw 4 title 'Actual progress'
