package com.soprasteria.seda.examples.insurance.architecture;

public interface SagaManager<S> {
    void check(S saga);
}
