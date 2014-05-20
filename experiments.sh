WORKER=java
OUTFILE=ex3_result.txt
EXECTIME=2000
THREADS=8
COUNTER=1

rm $OUTFILE

echo "Experiment 1. Parallel overhead"
echo "Experiment 1. Parallel overhead" >> $OUTFILE

for W in 25 50 100 200 400 800
do
	for N in 1 2 4 8 12
	do
		echo "java SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER"
		echo "Iteration $COUNTER: " >> $OUTFILE
		java SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER >> $OUTFILE
		java SerialQueueFirewall $EXECTIME $THREADS $W True $N $COUNTER >> $OUTFILE
		COUNTER=$[$COUNTER +1]
	done
done

echo "Experiment 2. Dispatcher rate"
echo "Experiment 2. Dispatcher rate" >> $OUTFILE
W=1
for N in 1 2 4 8 12
do
	echo "java SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER"
	echo "Iteration $COUNTER: " >> $OUTFILE
	java ParallelFirewall $EXECTIME $THREADS $W True $N $COUNTER >> $OUTFILE
	COUNTER=$[$COUNTER +1]
done

echo "Experiment 3. Speedup with Uniform Load"
echo "Experiment 3. Speedup with Uniform Load" >> $OUTFILE
for W in 1000 3000 6000
do
	for N in 1 2 4 8 12
	do
		echo "java SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER"
		echo "Iteration $COUNTER: " >> $OUTFILE
		java SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER >> $OUTFILE
		COUNTER=$[$COUNTER +1]
	done
done

echo "Experiment 4. Speedup with Exponentially Distributed Load"
echo "Experiment 4. Speedup with Exponentially Distributed Load" >> $OUTFILE
for W in 1000 3000 6000
do
	for N in 1 2 4 8 12
	do
		echo "java SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER"
		echo "Iteration $COUNTER: " >> $OUTFILE
		java SerialFirewall $EXECTIME $THREADS $W False $N $COUNTER >> $OUTFILE
		COUNTER=$[$COUNTER +1]
	done
done
