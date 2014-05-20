public interface PacketWorker extends Runnable {
  public void run();
}

class SerialPacketWorker implements PacketWorker {
  PaddedPrimitiveNonVolatile<Boolean> done;
  final PacketSource pkt;
  final Fingerprint residue = new Fingerprint();
  long fingerprint = 0;
  long totalPackets = 0;
  final int numSources;
  final boolean uniformBool;
  public SerialPacketWorker(
    PaddedPrimitiveNonVolatile<Boolean> done, 
    PacketSource pkt,
    boolean uniformBool,
    int numSources) {
    this.done = done;
    this.pkt = pkt;
    this.uniformBool = uniformBool;
    this.numSources = numSources;
  }
  
  public void run() {
    Packet tmp;
    while( !done.value ) {
      for( int i = 0; i < numSources; i++ ) {
        if( uniformBool )
          tmp = pkt.getUniformPacket(i);
        else
          tmp = pkt.getExponentialPacket(i);
        totalPackets++;
        fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);        
      }
    }
  }  
}

class SerialQueuePacketWorker implements PacketWorker {
  PaddedPrimitiveNonVolatile<Boolean> done;
  final PacketSource pkt;
  final Fingerprint residue = new Fingerprint();
  long fingerprint = 0;
  long totalPackets = 0;
  final int numSources;
  final boolean uniformBool;
  // Also all lamport queues.
  LamportQueue<Packet>[] queues;
  
  public SerialQueuePacketWorker(
    PaddedPrimitiveNonVolatile<Boolean> done, 
    PacketSource pkt,
    boolean uniformBool,
    int numSources,
    LamportQueue<Packet>[] queues) {
    this.done = done;
    this.pkt = pkt;
    this.uniformBool = uniformBool;
    this.numSources = numSources;
    this.queues = queues;
  }
  
  public void run() {
    Packet tmp;
    while( !done.value ) {
      for( int i = 0; i < numSources; i++ ) {
        if( uniformBool )
          tmp = pkt.getUniformPacket(i);
        else
          tmp = pkt.getExponentialPacket(i);
	    try {
           // enqueue tmp in the ith Lamport queue
           queues[i].push(tmp);
         } catch (FullException e) {;}
         try {
          // dequeue the next packet from the ith Lamport queue into tmp
          tmp = queues[i].pop();
		 } catch (EmptyException e) {;}
        totalPackets++;
        fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);        
      }
    }
  }  
}

class ParallelPacketWorker implements PacketWorker {
  PaddedPrimitiveNonVolatile<Boolean> done;
  final Fingerprint residue = new Fingerprint();
  long fingerprint = 0;
  long totalPackets = 0;
  // Also all lamport queues.
  LamportQueue<Packet> queue;
  
  public ParallelPacketWorker(
    PaddedPrimitiveNonVolatile<Boolean> done, 
    LamportQueue<Packet> queue) {
    this.done = done;
    this.queue = queue;
  }
  
  public void run() {
    Packet tmp = new Packet();
    while( !done.value ) {
        try {
          // dequeue the next packet from the ith Lamport queue into tmp
            tmp = queue.pop();
			totalPackets++;
			fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);        
	    } catch (EmptyException e) {;}
    }
  }  //run()
}	// class ParallelPacketWorker

class Dispatcher implements Runnable {
  PaddedPrimitiveNonVolatile<Boolean> done;
  final PacketSource pkt;
  final Fingerprint residue = new Fingerprint();
  long fingerprint = 0;
  long totalPackets = 0;
  final int numSources;
  final boolean uniformBool;
  // Also all lamport queues.
  LamportQueue<Packet>[] queues;
  
  public Dispatcher(
    PaddedPrimitiveNonVolatile<Boolean> done, 
    PacketSource pkt,
    boolean uniformBool,
    int numSources,
    LamportQueue<Packet>[] queue) {
	this.done = done;
    this.pkt = pkt;
    this.uniformBool = uniformBool;
    this.numSources = numSources;
    this.queues = queue;
  }

  public void run() {
	Packet tmp;
    while( !done.value ) {
		for( int i = 0; i < numSources; i++ ) {
			if( uniformBool )
			  tmp = pkt.getUniformPacket(i);
			else
			  tmp = pkt.getExponentialPacket(i);
			try {
			   // enqueue tmp in the ith Lamport queue
				queues[i].push(tmp);
			 } catch (FullException e) {;}
		}
	}
  }	// run()
}	// class DIspatcher
