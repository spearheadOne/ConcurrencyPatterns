package org.abondar.experimental.concurrency.pattern;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {

    private final AtomicInteger counter;

    public Counter(){
        counter = new AtomicInteger();
    }

    public int getCount(){
        return counter.get();
    }

    public int increment(){
        return counter.incrementAndGet();
    }

    public int decrement(){
        return counter.decrementAndGet();
    }

    public void reset(){
        counter.set(0);
    }

    public int add(int delta){
       return counter.addAndGet(delta);
    }

}
