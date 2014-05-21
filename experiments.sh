#!/bin/bash
WORKER=java
OUTFILE=ex3_result.txt
OUTCSV=ex3_result.csv
EXECTIME=2000
THREADS=8
COUNTER=6

rm $OUTFILE
rm $OUTCSV

echo "Experiment 1. Parallel overhead"
echo "Experiment 1. Parallel overhead" >> $OUTFILE
echo "Experiment 1. Parallel overhead" >> $OUTCSV

for W in 25 50 100 200 400 800
do
	for N in 1 2 4 8 12
	do
		echo "$WORKER SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER"
		echo "Iteration $COUNTER: " >> $OUTFILE
		echo -n "$W, $N, " >> $OUTCSV
		#$WORKER SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER >> $OUTFILE
		$WORKER SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER >> netbug-$COUNTER.stdout
		cat netbug-$COUNTER.stdout >> $OUTCSV
		COUNTER=$[$COUNTER +1]
		echo -n "$W, $N, " >> $OUTCSV
		#$WORKER SerialQueueFirewall $EXECTIME $THREADS $W True $N $COUNTER >> $OUTFILE
		$WORKER SerialQueueFirewall $EXECTIME $THREADS $W True $N $COUNTER >> netbug-$COUNTER.stdout
		cat netbug-$COUNTER.stdout >> $OUTCSV
		COUNTER=$[$COUNTER +1]
	done
done

echo "Experiment 2. Dispatcher rate"
echo "Experiment 2. Dispatcher rate" >> $OUTFILE
echo "Experiment 2. Dispatcher rate" >> $OUTCSV
W=1
for N in 1 2 4 8 12
do
	echo "$WORKER ParallelFirewall $EXECTIME $THREADS $W True $N $COUNTER"
	echo "Iteration $COUNTER: " >> $OUTFILE
	echo -n "$W, $N, " >> $OUTCSV
	#$WORKER ParallelFirewall $EXECTIME $THREADS $W True $N $COUNTER >> $OUTFILE
	$WORKER ParallelFirewall $EXECTIME $THREADS $W True $N $COUNTER >> netbug-$COUNTER.stdout
	cat netbug-$COUNTER.stdout >> $OUTCSV
	COUNTER=$[$COUNTER +1]
done

echo "Experiment 3. Speedup with Uniform Load"
echo "Experiment 3. Speedup with Uniform Load" >> $OUTFILE
echo "Experiment 3. Speedup with Uniform Load" >> $OUTCSV
for W in 1000 3000 6000
do
	for N in 1 2 4 8 12
	do
		echo "$WORKER SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER"
		echo "Iteration $COUNTER: " >> $OUTFILE
		echo -n "S, $W, $N, " >> $OUTCSV
		#$WORKER SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER >> $OUTFILE
		$WORKER SerialFirewall $EXECTIME $THREADS $W True $N $COUNTER >> netbug-$COUNTER.stdout
		cat netbug-$COUNTER.stdout >> $OUTCSV
		COUNTER=$[$COUNTER +1]
		echo "$WORKER ParallelFirewall $EXECTIME $THREADS $W True $N $COUNTER"
		echo "Iteration $COUNTER: " >> $OUTFILE
		echo -n "P, $W, $N, " >> $OUTCSV
		#$WORKER ParalleFirewall $EXECTIME $THREADS $W True $N $COUNTER >> $OUTFILE
		$WORKER ParalleFirewall $EXECTIME $THREADS $W True $N $COUNTER >> netbug-$COUNTER.stdout
		cat netbug-$COUNTER.stdout >> $OUTCSV
		COUNTER=$[$COUNTER +1]
	done
done

echo "Experiment 4. Speedup with Exponentially Distributed Load"
echo "Experiment 4. Speedup with Exponentially Distributed Load" >> $OUTFILE
echo "Experiment 4. Speedup with Exponentially Distributed Load" >> $OUTCSV
for W in 1000 3000 6000
do
	for N in 1 2 4 8 12
	do
		echo "$WORKER SerialFirewall $EXECTIME $THREADS $W False $N $COUNTER"
		echo "Iteration $COUNTER: " >> $OUTFILE
		echo -n "S, $W, $N, " >> $OUTCSV
		#$WORKER SerialFirewall $EXECTIME $THREADS $W False $N $COUNTER >> $OUTFILE
		$WORKER SerialFirewall $EXECTIME $THREADS $W False $N $COUNTER >> netbug-$COUNTER.stdout
		cat netbug-$COUNTER.stdout >> $OUTCSV
		COUNTER=$[$COUNTER +1]
		echo "$WORKER ParallelFirewall $EXECTIME $THREADS $W False $N $COUNTER"
		echo "Iteration $COUNTER: " >> $OUTFILE
		echo -n "P, $W, $N, " >> $OUTCSV
		#$WORKER ParalleFirewall $EXECTIME $THREADS $W False $N $COUNTER >> $OUTFILE
		$WORKER ParalleFirewall $EXECTIME $THREADS $W False $N $COUNTER >> netbug-$COUNTER.stdout
		cat netbug-$COUNTER.stdout >> $OUTCSV
		COUNTER=$[$COUNTER +1]
	done
done
