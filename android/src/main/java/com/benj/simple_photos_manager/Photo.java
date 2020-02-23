package com.benj.simple_photos_manager;

class Photo {
    private String id;
    private int inx;
    private int width;
    private int height;
    private String data;

    Photo(String id, int inx, int width, int height, String data){
        this.id = id;
        this.inx = inx;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getInx() {
        return inx;
    }

    public void setInx(int inx) {
        this.inx = inx;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
