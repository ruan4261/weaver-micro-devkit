package com.weaver.test.kvcs;

public class Entity implements ShowNum {

    @Override
    public void print() {
        Dependence dependence = new Dependence();
        dependence.print();
    }

}
