class SerialFirewall {
  public static void main(String[] args) {
    final int numMilliseconds = Integer.parseInt(args[0]);   
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final int queueDepth = Integer.parseInt(args[4]);	// not used
    final short experimentNumber = Short.parseShort(args[5]);
    
    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
        
    SerialPacketWorker workerData = new SerialPacketWorker(done, pkt, uniformFlag, numSources);
    Thread workerThread = new Thread(workerData);
    
    workerThread.start();
    timer.startTimer();
    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    done.value = true;
    memFence.value = true;  // memFence is a 'volatile' forcing a memory fence
    try {                   // which means that done.value is visible to the workers
      workerThread.join();
    } catch (InterruptedException ignore) {;}      
    timer.stopTimer();
    final long totalCount = workerData.totalPackets;
    //System.out.println("count: " + totalCount);
    //System.out.println("time: " + timer.getElapsedTime());
    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
  }
}

class SerialQueueFirewall {
  public static void main(String[] args) {
    final int numMilliseconds = Integer.parseInt(args[0]);   
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final int queueDepth = Integer.parseInt(args[4]);
    final short experimentNumber = Short.parseShort(args[5]);
   
    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

    // allocate and initialize bank of numSources Lamport queues
    // each with depth queueDepth
    // they should throw FullException and EmptyException upon those conditions
    LamportQueue<Packet>[] queues = new LamportQueue[numSources];
    for (int i = 0; i < numSources; i++)
		queues[i] = new LamportQueue<Packet>(queueDepth);

    // Create a SerialQueuePackerWorker workerData 
    // as SerialPackerWorker, but be sure to Pass the lamport queues
    SerialQueuePacketWorker workerData = new SerialQueuePacketWorker(done, pkt, uniformFlag, numSources, queues);
    
    // The rest of the code looks as in Serial Firewall
    Thread workerThread = new Thread(workerData);
    workerThread.start();
    timer.startTimer();
    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    done.value = true;
    memFence.value = true;  // memFence is a 'volatile' forcing a memory fence
    try {                   // which means that done.value is visible to the workers
      workerThread.join();
    } catch (InterruptedException ignore) {;}      
    timer.stopTimer();
    final long totalCount = workerData.totalPackets;
    //System.out.println("count: " + totalCount);
    //System.out.println("time: " + timer.getElapsedTime());
    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
    
  }
}

class ParallelFirewall {
  public static void main(String[] args) {
    final int numMilliseconds = Integer.parseInt(args[0]);     
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final int queueDepth = Integer.parseInt(args[4]);
    final short experimentNumber = Short.parseShort(args[5]);

    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    
    // Allocate and initialize bank of Lamport queues, as in SerialQueueFirewall
    LamportQueue<Packet>[] queues = new LamportQueue[numSources];
    // Allocate and initialize any signals used to marshal threads (eg. done signals)
    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

    for (int i = 0; i < numSources; i++)
		queues[i] = new LamportQueue<Packet>(queueDepth);

    // Allocate and initialize a Dispatcher class implementing Runnable
    // and a corresponding Dispatcher Thread
	Dispatcher dispatcherData = new Dispatcher(done, pkt, uniformFlag, numSources, queues);
    Thread dispatcherThread = new Thread(dispatcherData);

    // Allocate and initialize an array of Worker classes (ParallelPacketWorker), 
    // implementing Runnable and the corresponding Worker Threads
    ParallelPacketWorker[] workerData = new ParallelPacketWorker[numSources];
    Thread[] workerThread = new Thread[numSources];
    for (int i = 0; i < numSources; i++)
    {
		workerData[i] = new ParallelPacketWorker(done, queues[i]);
		workerThread[i] = new Thread(workerData[i]);
		// Call start() for each worker
		workerThread[i].start();
	}
    timer.startTimer();

    // Call start() for the Dispatcher thread
	dispatcherThread.start();
	
    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    // 
    // assert signals to stop Dispatcher - remember, Dispatcher needs to deliver an 
    // equal number of packets from each source
    //
    // call .join() on Dispatcher
	done.value = true;
    try {                   // which means that done.value is visible to the workers
		dispatcherThread.join();
		memFence.value = true;  // memFence is a 'volatile' forcing a memory fence
		for (int i = 0; i < numSources; i++)
			workerThread[i].join();
    } catch (InterruptedException ignore) {;}      
    //
    // assert signals to stop Workers - they are responsible for leaving the queues
    // empty - use whatever protocol you like, but one easy one is to have each
    // worker verify that it's corresponding queue is empty after it observes the
    // done signal set to true
    //
    // call .join() for each Worker
    timer.stopTimer();
    // Output the statistics
    timer.stopTimer();
    long totalCount = 0;
    for (int i = 0; i < numSources; i++)
		totalCount += workerData[i].totalPackets;
    //System.out.println("count: " + totalCount);
    //System.out.println("time: " + timer.getElapsedTime());
    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
  }
}
