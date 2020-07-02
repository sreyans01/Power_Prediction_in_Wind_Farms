
package com.ibm.energyoptimizer.PojoClasses;


public class Weather_ {

    private Integer id;
    private String main;
    private String description;
    private String icon;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Weather_() {
    }

    /**
     * 
     * @param icon
     * @param description
     * @param main
     * @param id
     */
    public Weather_(Integer id, String main, String description, String icon) {
        super();
        this.id = id;
        this.main = main;
        this.description = description;
        this.icon = icon;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}
