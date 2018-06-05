package com.sbonacho.seda.examples.insurance.architecture;

public interface Saga {
    Boolean isComplete();
    Boolean isFailed();
}
