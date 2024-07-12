package com.example.mokoli;

public class CustomItem {
    private String spinnerItemName;
    private int spinnerItemImage;

    public CustomItem(String spinnerItemName, int darker_gray){
        this.spinnerItemImage = spinnerItemImage;
        this.spinnerItemName = spinnerItemName;
    }

    public String getSpinnerItemName(){
        return spinnerItemName;
    }

    public  int getSpinnerItemImage(){
        return spinnerItemImage;
    }
}

