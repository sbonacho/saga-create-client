package com.soprasteria.seda.examples.insurance.architecture;

public interface Saga {
    Boolean isComplete();
    Boolean isFailed();
}
