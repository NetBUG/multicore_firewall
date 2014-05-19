WORKER=java
OUTFILE=ex3_result.txt
EXECTIME=2000
THREADS=8
COUNTER=1

echo "Experiment 1. Parallel overhead"
echo "Experiment 1. Parallel overhead" >> $OUTFILE

for W in 25 50 100 200 400 800
do
	for N in 1 2 4 8 12
	do
		echo "java SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER"
		java SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER
		echo "Iteration $COUNTER: " >> $OUTFILE
		COUNTER=$[$COUNTER +1]
	done
done

echo "Experiment 2. Dispatcher rate"
echo "Experiment 2. Dispatcher rate" >> $OUTFILE
