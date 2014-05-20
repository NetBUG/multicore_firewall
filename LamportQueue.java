class FullException extends Exception {
}

class EmptyException extends Exception {
}

public final class LamportQueue<E> {
  private final E[] buffer;
  private volatile long tail = 0;
  private volatile long head = 0;
 
  public LamportQueue(final int capacity) {
    buffer = (E[]) new Object[capacity];
  }
 
  public boolean push(final E e) throws FullException, NullPointerException {
    if (null == e)
      throw new NullPointerException("Null is not a valid element");
    
    final long currentTail = tail;
    final long wrapPoint = currentTail - buffer.length;
    if (head <= wrapPoint)
	  throw new FullException();
      //return false;
    
    buffer[(int) (currentTail % buffer.length)] = e;
    tail = currentTail + 1;
    return true;
  }
 
  public E pop() throws EmptyException {
    final long currentHead = head;
    if (currentHead >= tail)
	  throw new EmptyException();
//      return null;
    
    final int index = (int) (currentHead % buffer.length);
    final E e = buffer[index];
    buffer[index] = null;
    head = currentHead + 1;
    return e;
  }
}
