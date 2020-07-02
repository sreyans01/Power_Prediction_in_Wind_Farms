
package com.ibm.energyoptimizer.PojoClasses;


public class Rain {

    private Double _1h;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Rain() {
    }

    /**
     * 
     * @param _1h
     */
    public Rain(Double _1h) {
        super();
        this._1h = _1h;
    }

    public Double get1h() {
        return _1h;
    }

    public void set1h(Double _1h) {
        this._1h = _1h;
    }

}
